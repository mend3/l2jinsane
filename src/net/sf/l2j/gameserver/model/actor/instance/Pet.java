/**/
package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.manager.CursedWeaponManager;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.LootRule;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.enums.items.ArmorType;
import net.sf.l2j.gameserver.enums.items.EtcItemType;
import net.sf.l2j.gameserver.enums.items.WeaponType;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.handler.ItemHandler;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.PetDataEntry;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.actor.stat.PetStat;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.actor.template.PetTemplate;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.holder.Timestamp;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.model.item.kind.Weapon;
import net.sf.l2j.gameserver.model.itemcontainer.Inventory;
import net.sf.l2j.gameserver.model.itemcontainer.PetInventory;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.*;
import net.sf.l2j.gameserver.taskmanager.DecayTaskManager;
import net.sf.l2j.gameserver.taskmanager.ItemsOnGroundTaskManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

public class Pet extends Summon {
    private static final String LOAD_PET = "SELECT item_obj_id, name, level, curHp, curMp, exp, sp, fed FROM pets WHERE item_obj_id=?";
    private static final String STORE_PET = "INSERT INTO pets (name,level,curHp,curMp,exp,sp,fed,item_obj_id) VALUES (?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE name=VALUES(name),level=VALUES(level),curHp=VALUES(curHp),curMp=VALUES(curMp),exp=VALUES(exp),sp=VALUES(sp),fed=VALUES(fed)";
    private static final String DELETE_PET = "DELETE FROM pets WHERE item_obj_id=?";
    private final Map<Integer, Timestamp> _reuseTimeStamps = new ConcurrentHashMap();
    private final PetInventory _inventory;
    private final int _controlItemId;
    private final boolean _isMountable;
    private int _curFed;
    private int _curWeightPenalty = 0;
    private long _expBeforeDeath = 0L;
    private Future<?> _feedTask;
    private PetDataEntry _petData;

    public Pet(int objectId, NpcTemplate template, Player owner, ItemInstance control) {
        super(objectId, template, owner);
        this.getPosition().set(owner.getX() + 50, owner.getY() + 100, owner.getZ());
        this._inventory = new PetInventory(this);
        this._controlItemId = control.getObjectId();
        this._isMountable = template.getNpcId() == 12526 || template.getNpcId() == 12527 || template.getNpcId() == 12528 || template.getNpcId() == 12621;
    }

    public static Pet restore(ItemInstance control, NpcTemplate template, Player owner) {
        Object pet;
        if (template.isType("BabyPet")) {
            pet = new BabyPet(IdFactory.getInstance().getNextId(), template, owner, control);
        } else {
            pet = new Pet(IdFactory.getInstance().getNextId(), template, owner, control);
        }

        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("SELECT item_obj_id, name, level, curHp, curMp, exp, sp, fed FROM pets WHERE item_obj_id=?");

                try {
                    ps.setInt(1, control.getObjectId());
                    ResultSet rs = ps.executeQuery();

                    try {
                        if (rs.next()) {
                            ((Pet) pet).setName(rs.getString("name"));
                            ((Pet) pet).getStat().setLevel(rs.getByte("level"));
                            ((Pet) pet).getStat().setExp(rs.getLong("exp"));
                            ((Pet) pet).getStat().setSp(rs.getInt("sp"));
                            ((Pet) pet).getStatus().setCurrentHp(rs.getDouble("curHp"));
                            ((Pet) pet).getStatus().setCurrentMp(rs.getDouble("curMp"));
                            if (rs.getDouble("curHp") < 0.5D) {
                                ((Pet) pet).setIsDead(true);
                                ((Pet) pet).stopHpMpRegeneration();
                            }

                            ((Pet) pet).setCurrentFed(rs.getInt("fed"));
                        } else {
                            ((Pet) pet).getStat().setLevel(template.getNpcId() == 12564 ? (byte) ((Pet) pet).getOwner().getLevel() : template.getLevel());
                            ((Pet) pet).getStat().setExp(((Pet) pet).getExpForThisLevel());
                            ((Pet) pet).getStatus().setCurrentHp(((Pet) pet).getMaxHp());
                            ((Pet) pet).getStatus().setCurrentMp(((Pet) pet).getMaxMp());
                            ((Pet) pet).setCurrentFed(((Pet) pet).getPetData().getMaxMeal());
                            ((Pet) pet).store();
                        }
                    } catch (Throwable var12) {
                        if (rs != null) {
                            try {
                                rs.close();
                            } catch (Throwable var11) {
                                var12.addSuppressed(var11);
                            }
                        }

                        throw var12;
                    }

                    if (rs != null) {
                        rs.close();
                    }
                } catch (Throwable var13) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var10) {
                            var13.addSuppressed(var10);
                        }
                    }

                    throw var13;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (Throwable var14) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var9) {
                        var14.addSuppressed(var9);
                    }
                }

                throw var14;
            }

            if (con != null) {
                con.close();
            }

            return (Pet) pet;
        } catch (Exception var15) {
            LOGGER.error("Couldn't restore pet data for {}.", var15, owner.getName());
            return null;
        }
    }

    public void initCharStat() {
        this.setStat(new PetStat(this));
    }

    public PetStat getStat() {
        return (PetStat) super.getStat();
    }

    public PetTemplate getTemplate() {
        return (PetTemplate) super.getTemplate();
    }

    public PetInventory getInventory() {
        return this._inventory;
    }

    public int getControlItemId() {
        return this._controlItemId;
    }

    public boolean isMountable() {
        return this._isMountable;
    }

    public int getSummonType() {
        return 2;
    }

    public void onAction(Player player) {
        if (player.getObjectId() == this.getOwner().getObjectId() && player != this.getOwner()) {
            this.setOwner(player);
        }

        super.onAction(player);
    }

    public ItemInstance getActiveWeaponInstance() {
        return this._inventory.getPaperdollItem(7);
    }

    public Weapon getActiveWeaponItem() {
        ItemInstance weapon = this.getActiveWeaponInstance();
        return weapon == null ? null : (Weapon) weapon.getItem();
    }

    public boolean destroyItem(String process, int objectId, int count, WorldObject reference, boolean sendMessage) {
        ItemInstance item = this._inventory.destroyItem(process, objectId, count, this.getOwner(), reference);
        if (item == null) {
            if (sendMessage) {
                this.getOwner().sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
            }

            return false;
        } else {
            PetInventoryUpdate petIU = new PetInventoryUpdate();
            petIU.addItem(item);
            this.getOwner().sendPacket(petIU);
            if (sendMessage) {
                if (count > 1) {
                    this.getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(item.getItemId()).addItemNumber(count));
                } else {
                    this.getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(item.getItemId()));
                }
            }

            return true;
        }
    }

    public boolean destroyItemByItemId(String process, int itemId, int count, WorldObject reference, boolean sendMessage) {
        ItemInstance item = this._inventory.destroyItemByItemId(process, itemId, count, this.getOwner(), reference);
        if (item == null) {
            if (sendMessage) {
                this.getOwner().sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
            }

            return false;
        } else {
            PetInventoryUpdate petIU = new PetInventoryUpdate();
            petIU.addItem(item);
            this.getOwner().sendPacket(petIU);
            if (sendMessage) {
                if (count > 1) {
                    this.getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(item.getItemId()).addItemNumber(count));
                } else {
                    this.getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(item.getItemId()));
                }
            }

            return true;
        }
    }

    public void doPickupItem(WorldObject object) {
        if (!this.isDead()) {
            this.getAI().setIntention(IntentionType.IDLE);
            if (object instanceof ItemInstance target) {
                this.broadcastPacket(new StopMove(this.getObjectId(), this.getX(), this.getY(), this.getZ(), this.getHeading()));
                if (CursedWeaponManager.getInstance().isCursed(target.getItemId())) {
                    this.getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1).addItemName(target.getItemId()));
                } else if (target.getItem().getItemType() != EtcItemType.ARROW && target.getItem().getItemType() != EtcItemType.SHOT) {
                    synchronized (target) {
                        if (!target.isVisible()) {
                            return;
                        }

                        if (!this._inventory.validateCapacity(target)) {
                            this.getOwner().sendPacket(SystemMessageId.YOUR_PET_CANNOT_CARRY_ANY_MORE_ITEMS);
                            return;
                        }

                        if (!this._inventory.validateWeight(target, target.getCount())) {
                            this.getOwner().sendPacket(SystemMessageId.UNABLE_TO_PLACE_ITEM_YOUR_PET_IS_TOO_ENCUMBERED);
                            return;
                        }

                        if (target.getOwnerId() != 0 && !this.getOwner().isLooterOrInLooterParty(target.getOwnerId())) {
                            if (target.getItemId() == 57) {
                                this.getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1_ADENA).addNumber(target.getCount()));
                            } else if (target.getCount() > 1) {
                                this.getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S2_S1_S).addItemName(target.getItemId()).addNumber(target.getCount()));
                            } else {
                                this.getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1).addItemName(target.getItemId()));
                            }

                            return;
                        }

                        if (target.hasDropProtection()) {
                            target.removeDropProtection();
                        }

                        Party party = this.getOwner().getParty();
                        if (party != null && party.getLootRule() != LootRule.ITEM_LOOTER) {
                            party.distributeItem(this.getOwner(), target);
                        } else {
                            target.pickupMe(this);
                        }

                        ItemsOnGroundTaskManager.getInstance().remove(target);
                    }

                    if (target.getItemType() == EtcItemType.HERB) {
                        IItemHandler handler = ItemHandler.getInstance().getHandler(target.getEtcItem());
                        if (handler != null) {
                            handler.useItem(this, target, false);
                        }

                        target.destroyMe("Consume", this.getOwner(), null);
                        this.broadcastStatusUpdate();
                    } else {
                        SystemMessage sm2;
                        if (target.getItemType() instanceof ArmorType || target.getItemType() instanceof WeaponType) {
                            if (target.getEnchantLevel() > 0) {
                                sm2 = SystemMessage.getSystemMessage(SystemMessageId.ATTENTION_S1_PET_PICKED_UP_S2_S3).addCharName(this.getOwner()).addNumber(target.getEnchantLevel()).addItemName(target.getItemId());
                            } else {
                                sm2 = SystemMessage.getSystemMessage(SystemMessageId.ATTENTION_S1_PET_PICKED_UP_S2).addCharName(this.getOwner()).addItemName(target.getItemId());
                            }

                            this.getOwner().broadcastPacketInRadius(sm2, 1400);
                        }

                        if (target.getItemId() == 57) {
                            sm2 = SystemMessage.getSystemMessage(SystemMessageId.PET_PICKED_S1_ADENA).addItemNumber(target.getCount());
                        } else if (target.getEnchantLevel() > 0) {
                            sm2 = SystemMessage.getSystemMessage(SystemMessageId.PET_PICKED_S1_S2).addNumber(target.getEnchantLevel()).addItemName(target.getItemId());
                        } else if (target.getCount() > 1) {
                            sm2 = SystemMessage.getSystemMessage(SystemMessageId.PET_PICKED_S2_S1_S).addItemName(target.getItemId()).addItemNumber(target.getCount());
                        } else {
                            sm2 = SystemMessage.getSystemMessage(SystemMessageId.PET_PICKED_S1).addItemName(target.getItemId());
                        }

                        this.getOwner().sendPacket(sm2);
                        this.getInventory().addItem("Pickup", target, this.getOwner(), this);
                        this.getOwner().sendPacket(new PetItemList(this));
                    }

                    if (this.getFollowStatus()) {
                        this.followOwner();
                    }

                } else {
                    this.getOwner().sendPacket(SystemMessageId.ITEM_NOT_FOR_PETS);
                }
            }
        }
    }

    public void deleteMe(Player owner) {
        this.getInventory().deleteMe();
        super.deleteMe(owner);
        this.destroyControlItem(owner);
    }

    public boolean doDie(Creature killer) {
        if (!super.doDie(killer)) {
            return false;
        } else {
            this.stopFeed();
            this.getOwner().sendPacket(SystemMessageId.MAKE_SURE_YOU_RESSURECT_YOUR_PET_WITHIN_20_MINUTES);
            DecayTaskManager.getInstance().add(this, 1200);
            Player owner = this.getOwner();
            if (owner != null && !owner.isInDuel() && (!this.isInsideZone(ZoneId.PVP) || this.isInsideZone(ZoneId.SIEGE))) {
                this.deathPenalty();
            }

            return true;
        }
    }

    public void doRevive() {
        this.getOwner().removeReviving();
        super.doRevive();
        DecayTaskManager.getInstance().cancel(this);
        this.startFeed();
        if (!this.checkHungryState()) {
            this.setRunning();
        }

        this.getAI().setIntention(IntentionType.ACTIVE, null);
    }

    public void doRevive(double revivePower) {
        this.restoreExp(revivePower);
        this.doRevive();
    }

    public final int getWeapon() {
        ItemInstance weapon = this.getInventory().getPaperdollItem(7);
        return weapon != null ? weapon.getItemId() : 0;
    }

    public final int getArmor() {
        ItemInstance weapon = this.getInventory().getPaperdollItem(10);
        return weapon != null ? weapon.getItemId() : 0;
    }

    public void setName(String name) {
        ItemInstance controlItem = this.getControlItem();
        if (controlItem.getCustomType2() == (name == null ? 1 : 0)) {
            controlItem.setCustomType2(name != null ? 1 : 0);
            controlItem.updateDatabase();
            InventoryUpdate iu = new InventoryUpdate();
            iu.addModifiedItem(controlItem);
            this.getOwner().sendPacket(iu);
        }

        super.setName(name);
    }

    public void store() {
        if (this._controlItemId != 0) {
            try {
                Connection con = ConnectionPool.getConnection();

                try {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO pets (name,level,curHp,curMp,exp,sp,fed,item_obj_id) VALUES (?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE name=VALUES(name),level=VALUES(level),curHp=VALUES(curHp),curMp=VALUES(curMp),exp=VALUES(exp),sp=VALUES(sp),fed=VALUES(fed)");

                    try {
                        ps.setString(1, this.getName());
                        ps.setInt(2, this.getStat().getLevel());
                        ps.setDouble(3, this.getStatus().getCurrentHp());
                        ps.setDouble(4, this.getStatus().getCurrentMp());
                        ps.setLong(5, this.getStat().getExp());
                        ps.setInt(6, this.getStat().getSp());
                        ps.setInt(7, this.getCurrentFed());
                        ps.setInt(8, this._controlItemId);
                        ps.executeUpdate();
                    } catch (Throwable var7) {
                        if (ps != null) {
                            try {
                                ps.close();
                            } catch (Throwable var6) {
                                var7.addSuppressed(var6);
                            }
                        }

                        throw var7;
                    }

                    if (ps != null) {
                        ps.close();
                    }
                } catch (Throwable var8) {
                    if (con != null) {
                        try {
                            con.close();
                        } catch (Throwable var5) {
                            var8.addSuppressed(var5);
                        }
                    }

                    throw var8;
                }

                if (con != null) {
                    con.close();
                }
            } catch (Exception var9) {
                LOGGER.error("Couldn't store pet data for {}.", var9, this.getObjectId());
            }

            ItemInstance itemInst = this.getControlItem();
            if (itemInst != null && itemInst.getEnchantLevel() != this.getStat().getLevel()) {
                itemInst.setEnchantLevel(this.getStat().getLevel());
                itemInst.updateDatabase();
            }

        }
    }

    public synchronized void unSummon(Player owner) {
        this.stopFeed();
        if (!this.isDead() && this.getInventory() != null) {
            this.getInventory().deleteMe();
        }

        super.unSummon(owner);
        if (!this.isDead()) {
            World.getInstance().removePet(owner.getObjectId());
        }

    }

    public void addExpAndSp(long addToExp, int addToSp) {
        this.getStat().addExpAndSp(Math.round((double) addToExp * (this.getNpcId() == 12564 ? Config.SINEATER_XP_RATE : Config.PET_XP_RATE)), addToSp);
    }

    public long getExpForThisLevel() {
        return this.getStat().getExpForLevel(this.getLevel());
    }

    public long getExpForNextLevel() {
        return this.getStat().getExpForLevel(this.getLevel() + 1);
    }

    public final int getLevel() {
        return this.getStat().getLevel();
    }

    public final int getSkillLevel(int skillId) {
        return this.getSkill(skillId) == null ? 0 : Math.max(1, Math.min((this.getLevel() - 8) / 6, SkillTable.getInstance().getMaxLevel(skillId)));
    }

    public final int getMaxLoad() {
        return 54510;
    }

    public int getSoulShotsPerHit() {
        return this.getPetData().getSsCount();
    }

    public int getSpiritShotsPerHit() {
        return this.getPetData().getSpsCount();
    }

    public void updateAndBroadcastStatus(int val) {
        this.refreshOverloaded();
        super.updateAndBroadcastStatus(val);
    }

    public void addTimeStamp(L2Skill skill, long reuse) {
        this._reuseTimeStamps.put(skill.getReuseHashCode(), new Timestamp(skill, reuse));
    }

    public Collection<Timestamp> getReuseTimeStamps() {
        return this._reuseTimeStamps.values();
    }

    public Map<Integer, Timestamp> getReuseTimeStamp() {
        return this._reuseTimeStamps;
    }

    public PetDataEntry getPetData() {
        return this._petData;
    }

    public void setPetData(int level) {
        this._petData = this.getTemplate().getPetDataEntry(level);
    }

    public ItemInstance getControlItem() {
        return this.getOwner().getInventory().getItemByObjectId(this._controlItemId);
    }

    public int getCurrentFed() {
        return this._curFed;
    }

    public void setCurrentFed(int num) {
        this._curFed = Math.min(num, this.getPetData().getMaxMeal());
    }

    public ItemInstance transferItem(String process, int objectId, int count, Inventory target, Player actor, WorldObject reference) {
        ItemInstance oldItem = this.checkItemManipulation(objectId, count);
        if (oldItem == null) {
            return null;
        } else {
            boolean wasWorn = oldItem.isPetItem() && oldItem.isEquipped();
            ItemInstance newItem = this.getInventory().transferItem(process, objectId, count, target, actor, reference);
            if (newItem == null) {
                return null;
            } else {
                PetInventoryUpdate petIU = new PetInventoryUpdate();
                if (oldItem.getCount() > 0 && oldItem != newItem) {
                    petIU.addModifiedItem(oldItem);
                } else {
                    petIU.addRemovedItem(oldItem);
                }

                this.sendPacket(petIU);
                InventoryUpdate playerIU = new InventoryUpdate();
                if (newItem.getCount() > count) {
                    playerIU.addModifiedItem(newItem);
                } else {
                    playerIU.addNewItem(newItem);
                }

                this.sendPacket(playerIU);
                StatusUpdate playerSU = new StatusUpdate(this.getOwner());
                playerSU.addAttribute(14, this.getOwner().getCurrentLoad());
                this.sendPacket(playerSU);
                if (wasWorn) {
                    this.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_TOOK_OFF_S1).addItemName(newItem));
                }

                return newItem;
            }
        }
    }

    public ItemInstance checkItemManipulation(int objectId, int count) {
        ItemInstance item = this.getInventory().getItemByObjectId(objectId);
        if (item == null) {
            return null;
        } else if (count >= 1 && (count <= 1 || item.isStackable())) {
            return count > item.getCount() ? null : item;
        } else {
            return null;
        }
    }

    public void destroyControlItem(Player owner) {
        World.getInstance().removePet(owner.getObjectId());
        owner.destroyItem("PetDestroy", this._controlItemId, 1, this.getOwner(), false);

        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");

                try {
                    ps.setInt(1, this._controlItemId);
                    ps.executeUpdate();
                } catch (Throwable var8) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var7) {
                            var8.addSuppressed(var7);
                        }
                    }

                    throw var8;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (Throwable var9) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var6) {
                        var9.addSuppressed(var6);
                    }
                }

                throw var9;
            }

            if (con != null) {
                con.close();
            }
        } catch (Exception var10) {
            LOGGER.error("Couldn't delete pet data for {}.", var10, this.getObjectId());
        }

    }

    public synchronized void stopFeed() {
        if (this._feedTask != null) {
            this._feedTask.cancel(false);
            this._feedTask = null;
        }

    }

    public synchronized void startFeed() {
        this.stopFeed();
        if (!this.isDead() && this.getOwner().getSummon() == this) {
            this._feedTask = ThreadPool.scheduleAtFixedRate(new Pet.FeedTask(), 10000L, 10000L);
        }

    }

    public void restoreExp(double restorePercent) {
        if (this._expBeforeDeath > 0L) {
            this.getStat().addExp(Math.round((double) (this._expBeforeDeath - this.getStat().getExp()) * restorePercent / 100.0D));
            this._expBeforeDeath = 0L;
        }

    }

    private void deathPenalty() {
        int lvl = this.getStat().getLevel();
        double percentLost = -0.07D * (double) lvl + 6.5D;
        long lostExp = Math.round((double) (this.getStat().getExpForLevel(lvl + 1) - this.getStat().getExpForLevel(lvl)) * percentLost / 100.0D);
        this._expBeforeDeath = this.getStat().getExp();
        this.getStat().addExp(-lostExp);
    }

    public int getCurrentLoad() {
        return this._inventory.getTotalWeight();
    }

    public int getInventoryLimit() {
        return Config.INVENTORY_MAXIMUM_PET;
    }

    public void refreshOverloaded() {
        int maxLoad = this.getMaxLoad();
        if (maxLoad > 0) {
            int weightproc = this.getCurrentLoad() * 1000 / maxLoad;
            byte newWeightPenalty;
            if (weightproc < 500) {
                newWeightPenalty = 0;
            } else if (weightproc < 666) {
                newWeightPenalty = 1;
            } else if (weightproc < 800) {
                newWeightPenalty = 2;
            } else if (weightproc < 1000) {
                newWeightPenalty = 3;
            } else {
                newWeightPenalty = 4;
            }

            if (this._curWeightPenalty != newWeightPenalty) {
                this._curWeightPenalty = newWeightPenalty;
                if (newWeightPenalty > 0) {
                    this.setIsOverloaded(this.getCurrentLoad() >= maxLoad);
                } else {
                    this.setIsOverloaded(false);
                }
            }
        }

    }

    public boolean checkAutoFeedState() {
        return (double) this.getCurrentFed() < (double) this._petData.getMaxMeal() * this.getTemplate().getAutoFeedLimit();
    }

    public boolean checkHungryState() {
        return (double) this.getCurrentFed() < (double) this._petData.getMaxMeal() * this.getTemplate().getHungryLimit();
    }

    public boolean checkUnsummonState() {
        return (double) this.getCurrentFed() < (double) this._petData.getMaxMeal() * this.getTemplate().getUnsummonLimit();
    }

    public boolean canWear(Item item) {
        int npcId = this.getTemplate().getNpcId();
        if (npcId > 12310 && npcId < 12314 && item.getBodyPart() == -101) {
            return true;
        } else if (npcId == 12077 && item.getBodyPart() == -100) {
            return true;
        } else if (npcId > 12525 && npcId < 12529 && item.getBodyPart() == -102) {
            return true;
        } else {
            return npcId > 12779 && npcId < 12783 && item.getBodyPart() == -103;
        }
    }

    protected class FeedTask implements Runnable {
        public void run() {
            if (Pet.this.getOwner() != null && Pet.this.getOwner().getSummon() != null && Pet.this.getOwner().getSummon().getObjectId() == Pet.this.getObjectId()) {
                Pet.this.setCurrentFed(Pet.this.getCurrentFed() > this.getFeedConsume() ? Pet.this.getCurrentFed() - this.getFeedConsume() : 0);
                ItemInstance food = Pet.this.getInventory().getItemByItemId(Pet.this.getTemplate().getFood1());
                if (food == null) {
                    food = Pet.this.getInventory().getItemByItemId(Pet.this.getTemplate().getFood2());
                }

                if (food != null && Pet.this.checkAutoFeedState()) {
                    IItemHandler handler = ItemHandler.getInstance().getHandler(food.getEtcItem());
                    if (handler != null) {
                        Pet.this.getOwner().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PET_TOOK_S1_BECAUSE_HE_WAS_HUNGRY).addItemName(food));
                        handler.useItem(Pet.this, food, false);
                    }
                } else if (Pet.this.getCurrentFed() == 0) {
                    Pet.this.getOwner().sendPacket(SystemMessageId.YOUR_PET_IS_VERY_HUNGRY);
                    if (Rnd.get(100) < 30) {
                        Pet.this.stopFeed();
                        Pet.this.getOwner().sendPacket(SystemMessageId.STARVING_GRUMPY_AND_FED_UP_YOUR_PET_HAS_LEFT);
                        Pet.this.deleteMe(Pet.this.getOwner());
                        return;
                    }
                } else if ((double) Pet.this.getCurrentFed() < 0.1D * (double) Pet.this.getPetData().getMaxMeal()) {
                    Pet.this.getOwner().sendPacket(SystemMessageId.YOUR_PET_IS_VERY_HUNGRY_PLEASE_BE_CAREFUL);
                    if (Rnd.get(100) < 3) {
                        Pet.this.stopFeed();
                        Pet.this.getOwner().sendPacket(SystemMessageId.STARVING_GRUMPY_AND_FED_UP_YOUR_PET_HAS_LEFT);
                        Pet.this.deleteMe(Pet.this.getOwner());
                        return;
                    }
                }

                if (Pet.this.checkHungryState()) {
                    Pet.this.setWalking();
                } else {
                    Pet.this.setRunning();
                }

                Pet.this.broadcastStatusUpdate();
            } else {
                Pet.this.stopFeed();
            }
        }

        private int getFeedConsume() {
            return Pet.this.isAttackingNow() ? Pet.this.getPetData().getMealInBattle() : Pet.this.getPetData().getMealInNormal();
        }
    }
}