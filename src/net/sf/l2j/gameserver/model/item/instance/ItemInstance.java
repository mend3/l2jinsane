/**/
package net.sf.l2j.gameserver.model.item.instance;

import net.sf.l2j.Config;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.items.EtcItemType;
import net.sf.l2j.gameserver.enums.items.ItemType;
import net.sf.l2j.gameserver.enums.items.ShotType;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2Augmentation;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.item.MercenaryTicket;
import net.sf.l2j.gameserver.model.item.kind.Armor;
import net.sf.l2j.gameserver.model.item.kind.EtcItem;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.model.item.kind.Weapon;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.*;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;
import net.sf.l2j.gameserver.skills.basefuncs.Func;
import net.sf.l2j.gameserver.taskmanager.ItemsOnGroundTaskManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public final class ItemInstance extends WorldObject implements Runnable, Comparable<ItemInstance> {
    private static final Logger ITEM_LOG = Logger.getLogger("item");
    private static final String DELETE_AUGMENTATION = "DELETE FROM augmentations WHERE item_id = ?";
    private static final String RESTORE_AUGMENTATION = "SELECT attributes,skill_id,skill_level FROM augmentations WHERE item_id=?";
    private static final String UPDATE_AUGMENTATION = "REPLACE INTO augmentations VALUES(?,?,?,?)";
    private static final String UPDATE_ITEM = "UPDATE items SET owner_id=?,count=?,loc=?,loc_data=?,enchant_level=?,custom_type1=?,custom_type2=?,mana_left=?,time=? WHERE object_id = ?";
    private static final String INSERT_ITEM = "INSERT INTO items (owner_id,item_id,count,loc,loc_data,enchant_level,object_id,custom_type1,custom_type2,mana_left,time) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
    private static final String DELETE_ITEM = "DELETE FROM items WHERE object_id=?";
    private static final String DELETE_PET_ITEM = "DELETE FROM pets WHERE item_obj_id=?";
    private static final long REGULAR_LOOT_PROTECTION_TIME = 15000L;
    private static final long RAID_LOOT_PROTECTION_TIME = 300000L;
    private final int _itemId;
    private final Item _item;
    private final ReentrantLock _dbLock;
    private int _ownerId;
    private int _dropperObjectId = 0;
    private int _count;
    private int _initCount;
    private long _time;
    private boolean _decrease = false;
    private ItemInstance.ItemLocation _loc;
    private int _locData;
    private int _enchantLevel;
    private L2Augmentation _augmentation = null;
    private int _mana = -1;
    private int _type1;
    private int _type2;
    private boolean _destroyProtected;
    private ItemInstance.ItemState _lastChange;
    private boolean _existsInDb;
    private boolean _storedInDb;
    private ScheduledFuture<?> _dropProtection;
    private int _shotsMask;
    private boolean _isAgathionItem;

    public ItemInstance(int objectId, int itemId) {
        super(objectId);
        this._lastChange = ItemInstance.ItemState.MODIFIED;
        this._dbLock = new ReentrantLock();
        this._shotsMask = 0;
        this._itemId = itemId;
        this._item = ItemTable.getInstance().getTemplate(itemId);
        if (this._itemId != 0 && this._item != null) {
            super.setName(this._item.getName());
            this.setCount(1);
            this._loc = ItemInstance.ItemLocation.VOID;
            this._type1 = 0;
            this._type2 = 0;
            this._mana = this._item.getDuration() * 60;
            this._isAgathionItem = false;
        } else {
            throw new IllegalArgumentException();
        }
    }

    public ItemInstance(int objectId, Item item) {
        super(objectId);
        this._lastChange = ItemInstance.ItemState.MODIFIED;
        this._dbLock = new ReentrantLock();
        this._shotsMask = 0;
        this._itemId = item.getItemId();
        this._item = item;
        this.setName(this._item.getName());
        this.setCount(1);
        this._loc = ItemInstance.ItemLocation.VOID;
        this._mana = this._item.getDuration() * 60;
        this._isAgathionItem = false;
    }

    public static ItemInstance restoreFromDb(int ownerId, ResultSet rs) {
        ItemInstance inst = null;

        int objectId;
        int itemId;
        int slot;
        int enchant;
        int type1;
        int type2;
        int manaLeft;
        int count;
        long time;
        ItemInstance.ItemLocation loc;
        try {
            objectId = rs.getInt(1);
            itemId = rs.getInt("item_id");
            count = rs.getInt("count");
            loc = ItemInstance.ItemLocation.valueOf(rs.getString("loc"));
            slot = rs.getInt("loc_data");
            enchant = rs.getInt("enchant_level");
            type1 = rs.getInt("custom_type1");
            type2 = rs.getInt("custom_type2");
            manaLeft = rs.getInt("mana_left");
            time = rs.getLong("time");
        } catch (Exception var15) {
            LOGGER.error("Couldn't restore an item owned by {}.", var15, ownerId);
            return null;
        }

        Item item = ItemTable.getInstance().getTemplate(itemId);
        if (item == null) {
            return null;
        } else {
            inst = new ItemInstance(objectId, item);
            inst._ownerId = ownerId;
            inst.setCount(count);
            inst._enchantLevel = enchant;
            inst._type1 = type1;
            inst._type2 = type2;
            inst._loc = loc;
            inst._locData = slot;
            inst._existsInDb = true;
            inst._storedInDb = true;
            inst._mana = manaLeft;
            inst._time = time;
            if (inst.isEquipable()) {
                inst.restoreAttributes();
            }

            return inst;
        }
    }

    public static ItemInstance create(int itemId, int count, Player actor, WorldObject reference) {
        ItemInstance item = new ItemInstance(IdFactory.getInstance().getNextId(), itemId);
        World.getInstance().addObject(item);
        if (item.isStackable() && count > 1) {
            item.setCount(count);
        }

        if (Config.LOG_ITEMS) {
            LogRecord record = new LogRecord(Level.INFO, "CREATE");
            record.setLoggerName("item");
            record.setParameters(new Object[]{actor, item, reference});
            ITEM_LOG.log(record);
        }

        return item;
    }

    public boolean isAgathionItem() {
        return this._isAgathionItem;
    }

    public void setAgathionItem(boolean value) {
        this._isAgathionItem = value;
    }

    public synchronized void run() {
        this._ownerId = 0;
        this._dropProtection = null;
    }

    public void setOwnerId(String process, int owner_id, Player creator, WorldObject reference) {
        this.setOwnerId(owner_id);
        if (Config.LOG_ITEMS) {
            LogRecord record = new LogRecord(Level.INFO, "CHANGE:" + process);
            record.setLoggerName("item");
            record.setParameters(new Object[]{creator, this, reference});
            ITEM_LOG.log(record);
        }

    }

    public int getOwnerId() {
        return this._ownerId;
    }

    public void setOwnerId(int owner_id) {
        if (owner_id != this._ownerId) {
            this._ownerId = owner_id;
            this._storedInDb = false;
        }
    }

    public void setLocation(ItemInstance.ItemLocation loc, int loc_data) {
        if (loc != this._loc || loc_data != this._locData) {
            this._loc = loc;
            this._locData = loc_data;
            this._storedInDb = false;
        }
    }

    public ItemInstance.ItemLocation getLocation() {
        return this._loc;
    }

    public void setLocation(ItemInstance.ItemLocation loc) {
        this.setLocation(loc, 0);
    }

    public int getCount() {
        return this._count;
    }

    public void setCount(int count) {
        if (this.getCount() != count) {
            this._count = count >= -1 ? count : 0;
            this._storedInDb = false;
        }
    }

    public void changeCount(String process, int count, Player creator, WorldObject reference) {
        if (count != 0) {
            if (count > 0 && this.getCount() > Integer.MAX_VALUE - count) {
                this.setCount(Integer.MAX_VALUE);
            } else {
                this.setCount(this.getCount() + count);
            }

            if (this.getCount() < 0) {
                this.setCount(0);
            }

            this._storedInDb = false;
            if (Config.LOG_ITEMS && process != null) {
                LogRecord record = new LogRecord(Level.INFO, "CHANGE:" + process);
                record.setLoggerName("item");
                record.setParameters(new Object[]{creator, this, reference});
                ITEM_LOG.log(record);
            }

        }
    }

    public boolean isEquipable() {
        return this._item.getBodyPart() != 0 && this._item.getItemType() != EtcItemType.ARROW && this._item.getItemType() != EtcItemType.LURE;
    }

    public boolean isEquipped() {
        return this._loc == ItemInstance.ItemLocation.PAPERDOLL || this._loc == ItemInstance.ItemLocation.PET_EQUIP;
    }

    public int getLocationSlot() {
        assert this._loc == ItemInstance.ItemLocation.PAPERDOLL || this._loc == ItemInstance.ItemLocation.PET_EQUIP || this._loc == ItemInstance.ItemLocation.FREIGHT;

        return this._locData;
    }

    public Item getItem() {
        return this._item;
    }

    public int getCustomType1() {
        return this._type1;
    }

    public void setCustomType1(int newtype) {
        this._type1 = newtype;
    }

    public int getCustomType2() {
        return this._type2;
    }

    public void setCustomType2(int newtype) {
        this._type2 = newtype;
    }

    public boolean isOlyRestrictedItem() {
        return this.getItem().isOlyRestrictedItem();
    }

    public ItemType getItemType() {
        return this._item.getItemType();
    }

    public int getItemId() {
        return this._itemId;
    }

    public boolean isEtcItem() {
        return this._item instanceof EtcItem;
    }

    public boolean isWeapon() {
        return this._item instanceof Weapon;
    }

    public boolean isArmor() {
        return this._item instanceof Armor;
    }

    public EtcItem getEtcItem() {
        return this._item instanceof EtcItem ? (EtcItem) this._item : null;
    }

    public Weapon getWeaponItem() {
        return this._item instanceof Weapon ? (Weapon) this._item : null;
    }

    public Armor getArmorItem() {
        return this._item instanceof Armor ? (Armor) this._item : null;
    }

    public int getCrystalCount() {
        return this._item.getCrystalCount(this._enchantLevel);
    }

    public int getReferencePrice() {
        return this._item.getReferencePrice();
    }

    public String getItemName() {
        return this._item.getName();
    }

    public ItemInstance.ItemState getLastChange() {
        return this._lastChange;
    }

    public void setLastChange(ItemInstance.ItemState lastChange) {
        this._lastChange = lastChange;
    }

    public boolean isStackable() {
        return this._item.isStackable();
    }

    public boolean isDropable() {
        return !this.isAugmented() && this._item.isDropable();
    }

    public boolean isDestroyable() {
        return !this.isQuestItem() && this._item.isDestroyable();
    }

    public boolean isTradable() {
        return !this.isAugmented() && this._item.isTradable();
    }

    public boolean isSellable() {
        return !this.isAugmented() && this._item.isSellable();
    }

    public boolean isDepositable(boolean isPrivateWareHouse) {
        if (!this.isEquipped() && this._item.isDepositable()) {
            return isPrivateWareHouse || this.isTradable() && !this.isShadowItem();
        } else {
            return false;
        }
    }

    public boolean isConsumable() {
        return this._item.isConsumable();
    }

    public boolean isAvailable(Player player, boolean allowAdena, boolean allowNonTradable) {
        return !this.isEquipped() && this.getItem().getType2() != 3 && (this.getItem().getType2() != 4 || this.getItem().getType1() != 1) && (player.getSummon() == null || this.getObjectId() != player.getSummon().getControlItemId()) && player.getActiveEnchantItem() != this && (allowAdena || this.getItemId() != 57) && (player.getCurrentSkill().getSkill() == null || player.getCurrentSkill().getSkill().getItemConsumeId() != this.getItemId()) && (!player.isCastingSimultaneouslyNow() || player.getLastSimultaneousSkillCast() == null || player.getLastSimultaneousSkillCast().getItemConsumeId() != this.getItemId()) && (allowNonTradable || this.isTradable());
    }

    public void onAction(Player player) {
        if (player.isFlying()) {
            player.sendPacket(ActionFailed.STATIC_PACKET);
        } else {
            if (this._item.getItemType() == EtcItemType.CASTLE_GUARD) {
                if (player.isInParty()) {
                    player.sendPacket(ActionFailed.STATIC_PACKET);
                    return;
                }

                Castle castle = CastleManager.getInstance().getCastle(player);
                if (castle == null) {
                    player.sendPacket(ActionFailed.STATIC_PACKET);
                    return;
                }

                MercenaryTicket ticket = castle.getTicket(this._itemId);
                if (ticket == null) {
                    player.sendPacket(ActionFailed.STATIC_PACKET);
                    return;
                }

                if (!player.isCastleLord(castle.getCastleId())) {
                    player.sendPacket(SystemMessageId.THIS_IS_NOT_A_MERCENARY_OF_A_CASTLE_THAT_YOU_OWN_AND_SO_CANNOT_CANCEL_POSITIONING);
                    player.sendPacket(ActionFailed.STATIC_PACKET);
                    return;
                }
            }

            player.getAI().setIntention(IntentionType.PICK_UP, this);
        }
    }

    public void onActionShift(Player player) {
        if (player.isGM()) {
            NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
            html.setFile("data/html/admin/iteminfo.htm");
            html.replace("%objid%", this.getObjectId());
            html.replace("%itemid%", this.getItemId());
            html.replace("%ownerid%", this.getOwnerId());
            html.replace("%loc%", this.getLocation().toString());
            html.replace("%class%", this.getClass().getSimpleName());
            player.sendPacket(html);
        }

        super.onActionShift(player);
    }

    public int getEnchantLevel() {
        return this._enchantLevel;
    }

    public void setEnchantLevel(int enchantLevel) {
        if (this._enchantLevel != enchantLevel) {
            this._enchantLevel = enchantLevel;
            this._storedInDb = false;
        }
    }

    public boolean isAugmented() {
        return this._augmentation != null;
    }

    public L2Augmentation getAugmentation() {
        return this._augmentation;
    }

    public boolean setAugmentation(L2Augmentation augmentation) {
        if (this._augmentation != null) {
            return false;
        } else {
            this._augmentation = augmentation;
            this.updateItemAttributes();
            return true;
        }
    }

    public void removeAugmentation() {
        if (this._augmentation != null) {
            this._augmentation = null;

            try {
                Connection con = ConnectionPool.getConnection();

                try {
                    PreparedStatement ps = con.prepareStatement("DELETE FROM augmentations WHERE item_id = ?");

                    try {
                        ps.setInt(1, this.getObjectId());
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
                LOGGER.error("Couldn't remove augmentation for {}.", var9, this.toString());
            }

        }
    }

    private void restoreAttributes() {
        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("SELECT attributes,skill_id,skill_level FROM augmentations WHERE item_id=?");

                try {
                    ps.setInt(1, this.getObjectId());
                    ResultSet rs = ps.executeQuery();

                    try {
                        if (rs.next()) {
                            this._augmentation = new L2Augmentation(rs.getInt("attributes"), rs.getInt("skill_id"), rs.getInt("skill_level"));
                        }
                    } catch (Throwable var9) {
                        if (rs != null) {
                            try {
                                rs.close();
                            } catch (Throwable var8) {
                                var9.addSuppressed(var8);
                            }
                        }

                        throw var9;
                    }

                    if (rs != null) {
                        rs.close();
                    }
                } catch (Throwable var10) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var7) {
                            var10.addSuppressed(var7);
                        }
                    }

                    throw var10;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (Throwable var11) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var6) {
                        var11.addSuppressed(var6);
                    }
                }

                throw var11;
            }

            if (con != null) {
                con.close();
            }
        } catch (Exception var12) {
            LOGGER.error("Couldn't restore augmentation for {}.", var12, this.toString());
        }

    }

    private void updateItemAttributes() {
        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("REPLACE INTO augmentations VALUES(?,?,?,?)");

                try {
                    ps.setInt(1, this.getObjectId());
                    if (this._augmentation == null) {
                        ps.setInt(2, -1);
                        ps.setInt(3, -1);
                        ps.setInt(4, -1);
                    } else {
                        ps.setInt(2, this._augmentation.getAttributes());
                        if (this._augmentation.getSkill() == null) {
                            ps.setInt(3, 0);
                            ps.setInt(4, 0);
                        } else {
                            ps.setInt(3, this._augmentation.getSkill().getId());
                            ps.setInt(4, this._augmentation.getSkill().getLevel());
                        }
                    }

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
            LOGGER.error("Couldn't update attributes for {}.", var9, this.toString());
        }

    }

    public boolean isShadowItem() {
        return this._mana >= 0;
    }

    public int decreaseMana(int period) {
        this._storedInDb = false;
        return this._mana -= period;
    }

    public int getMana() {
        return this._mana / 60;
    }

    public boolean isAutoAttackable(Creature attacker) {
        return false;
    }

    public List<Func> getStatFuncs(Creature player) {
        return this.getItem().getStatFuncs(this, player);
    }

    public void updateDatabase() {
        this._dbLock.lock();

        try {
            if (this._existsInDb) {
                if (this._ownerId != 0 && this._loc != ItemInstance.ItemLocation.VOID && (this.getCount() != 0 || this._loc == ItemInstance.ItemLocation.LEASE)) {
                    this.updateInDb();
                } else {
                    this.removeFromDb();
                }
            } else {
                if (this._ownerId == 0 || this._loc == ItemInstance.ItemLocation.VOID || this.getCount() == 0 && this._loc != ItemInstance.ItemLocation.LEASE) {
                    return;
                }

                this.insertIntoDb();
            }
        } finally {
            this._dbLock.unlock();
        }

    }

    public void dropMe(Creature dropper, int x, int y, int z) {
        ThreadPool.execute(new ItemDropTask(this, this, dropper, x, y, z));
    }

    public void pickupMe(Creature player) {
        player.broadcastPacket(new GetItem(this, player.getObjectId()));
        Castle castle = CastleManager.getInstance().getCastle(player);
        if (castle != null && castle.getTicket(this._itemId) != null) {
            castle.removeDroppedTicket(this);
        }

        if (!Config.DISABLE_TUTORIAL && (this._itemId == 57 || this._itemId == 6353)) {
            Player actor = player.getActingPlayer();
            if (actor != null) {
                QuestState qs = actor.getQuestState("Tutorial");
                if (qs != null) {
                    qs.getQuest().notifyEvent("CE" + this._itemId, null, actor);
                }
            }
        }

        this.setIsVisible(false);
    }

    private void updateInDb() {
        assert this._existsInDb;

        if (!this._storedInDb) {
            try {
                Connection con = ConnectionPool.getConnection();

                try {
                    PreparedStatement ps = con.prepareStatement("UPDATE items SET owner_id=?,count=?,loc=?,loc_data=?,enchant_level=?,custom_type1=?,custom_type2=?,mana_left=?,time=? WHERE object_id = ?");

                    try {
                        ps.setInt(1, this._ownerId);
                        ps.setInt(2, this.getCount());
                        ps.setString(3, this._loc.name());
                        ps.setInt(4, this._locData);
                        ps.setInt(5, this.getEnchantLevel());
                        ps.setInt(6, this.getCustomType1());
                        ps.setInt(7, this.getCustomType2());
                        ps.setInt(8, this._mana);
                        ps.setLong(9, this.getTime());
                        ps.setInt(10, this.getObjectId());
                        ps.executeUpdate();
                        this._existsInDb = true;
                        this._storedInDb = true;
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
                LOGGER.error("Couldn't update {}. ", var9, this.toString());
            }

        }
    }

    private void insertIntoDb() {
        assert !this._existsInDb && this.getObjectId() != 0;

        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("INSERT INTO items (owner_id,item_id,count,loc,loc_data,enchant_level,object_id,custom_type1,custom_type2,mana_left,time) VALUES (?,?,?,?,?,?,?,?,?,?,?)");

                try {
                    ps.setInt(1, this._ownerId);
                    ps.setInt(2, this._itemId);
                    ps.setInt(3, this.getCount());
                    ps.setString(4, this._loc.name());
                    ps.setInt(5, this._locData);
                    ps.setInt(6, this.getEnchantLevel());
                    ps.setInt(7, this.getObjectId());
                    ps.setInt(8, this._type1);
                    ps.setInt(9, this._type2);
                    ps.setInt(10, this._mana);
                    ps.setLong(11, this.getTime());
                    ps.executeUpdate();
                    this._existsInDb = true;
                    this._storedInDb = true;
                    if (this._augmentation != null) {
                        this.updateItemAttributes();
                    }
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
            LOGGER.error("Couldn't insert {}.", var9, this.toString());
        }

    }

    private void removeFromDb() {
        assert this._existsInDb;

        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("DELETE FROM items WHERE object_id=?");

                try {
                    ps.setInt(1, this.getObjectId());
                    ps.executeUpdate();
                } catch (Throwable var9) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var7) {
                            var9.addSuppressed(var7);
                        }
                    }

                    throw var9;
                }

                if (ps != null) {
                    ps.close();
                }

                ps = con.prepareStatement("DELETE FROM augmentations WHERE item_id = ?");

                try {
                    ps.setInt(1, this.getObjectId());
                    ps.executeUpdate();
                } catch (Throwable var8) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var6) {
                            var8.addSuppressed(var6);
                        }
                    }

                    throw var8;
                }

                if (ps != null) {
                    ps.close();
                }

                this._existsInDb = false;
                this._storedInDb = false;
            } catch (Throwable var10) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var5) {
                        var10.addSuppressed(var5);
                    }
                }

                throw var10;
            }

            if (con != null) {
                con.close();
            }
        } catch (Exception var11) {
            LOGGER.error("Couldn't delete {}.", var11, this.toString());
        }

    }

    public String toString() {
        int var10000 = this.getObjectId();
        return "(" + var10000 + ") " + this.getName();
    }

    public synchronized boolean hasDropProtection() {
        return this._dropProtection != null;
    }

    public synchronized void setDropProtection(int ownerId, boolean isRaidParty) {
        this._ownerId = ownerId;
        this._dropProtection = ThreadPool.schedule(this, isRaidParty ? 300000L : 15000L);
    }

    public synchronized void removeDropProtection() {
        if (this._dropProtection != null) {
            this._dropProtection.cancel(true);
            this._dropProtection = null;
        }

        this._ownerId = 0;
    }

    public boolean isDestroyProtected() {
        return this._destroyProtected;
    }

    public void setDestroyProtected(boolean destroyProtected) {
        this._destroyProtected = destroyProtected;
    }

    public boolean isNightLure() {
        return this._itemId >= 8505 && this._itemId <= 8513 || this._itemId == 8485;
    }

    public boolean getCountDecrease() {
        return this._decrease;
    }

    public void setCountDecrease(boolean decrease) {
        this._decrease = decrease;
    }

    public int getInitCount() {
        return this._initCount;
    }

    public void setInitCount(int InitCount) {
        this._initCount = InitCount;
    }

    public void restoreInitCount() {
        if (this._decrease) {
            this._count = this._initCount;
        }

    }

    public long getTime() {
        return this._time;
    }

    public void actualizeTime() {
        this._time = System.currentTimeMillis();
    }

    public boolean isPetItem() {
        return this.getItem().isPetItem();
    }

    public boolean isPotion() {
        return this.getItem().isPotion();
    }

    public boolean isElixir() {
        return this.getItem().isElixir();
    }

    public boolean isHerb() {
        return this.getItem().getItemType() == EtcItemType.HERB;
    }

    public boolean isHeroItem() {
        return this.getItem().isHeroItem();
    }

    public boolean isQuestItem() {
        return this.getItem().isQuestItem();
    }

    public void decayMe() {
        ItemsOnGroundTaskManager.getInstance().remove(this);
        super.decayMe();
    }

    public void destroyMe(String process, Player actor, WorldObject reference) {
        this.setCount(0);
        this.setOwnerId(0);
        this.setLocation(ItemInstance.ItemLocation.VOID);
        this.setLastChange(ItemInstance.ItemState.REMOVED);
        World.getInstance().removeObject(this);
        IdFactory.getInstance().releaseId(this.getObjectId());
        if (Config.LOG_ITEMS) {
            LogRecord record = new LogRecord(Level.INFO, "DELETE:" + process);
            record.setLoggerName("item");
            record.setParameters(new Object[]{actor, this, reference});
            ITEM_LOG.log(record);
        }

        if (this.getItemType() == EtcItemType.PET_COLLAR) {
            try {
                Connection con = ConnectionPool.getConnection();

                try {
                    PreparedStatement ps = con.prepareStatement("DELETE FROM pets WHERE item_obj_id=?");

                    try {
                        ps.setInt(1, this.getObjectId());
                        ps.execute();
                    } catch (Throwable var10) {
                        if (ps != null) {
                            try {
                                ps.close();
                            } catch (Throwable var9) {
                                var10.addSuppressed(var9);
                            }
                        }

                        throw var10;
                    }

                    if (ps != null) {
                        ps.close();
                    }
                } catch (Throwable var11) {
                    if (con != null) {
                        try {
                            con.close();
                        } catch (Throwable var8) {
                            var11.addSuppressed(var8);
                        }
                    }

                    throw var11;
                }

                if (con != null) {
                    con.close();
                }
            } catch (Exception var12) {
                LOGGER.error("Couldn't delete {}.", var12, this.toString());
            }
        }

    }

    public void setDropperObjectId(int id) {
        this._dropperObjectId = id;
    }

    public void sendInfo(Player activeChar) {
        if (this._dropperObjectId != 0) {
            activeChar.sendPacket(new DropItem(this, this._dropperObjectId));
        } else {
            activeChar.sendPacket(new SpawnItem(this));
        }

    }

    public List<Quest> getQuestEvents() {
        return this._item.getQuestEvents();
    }

    public boolean isChargedShot(ShotType type) {
        return (this._shotsMask & type.getMask()) == type.getMask();
    }

    public void setChargedShot(ShotType type, boolean charged) {
        if (charged) {
            this._shotsMask |= type.getMask();
        } else {
            this._shotsMask &= ~type.getMask();
        }

    }

    public void unChargeAllShots() {
        this._shotsMask = 0;
    }

    public int compareTo(ItemInstance item) {
        int time = Long.compare(item.getTime(), this._time);
        return time != 0 ? time : Integer.compare(item.getObjectId(), this.getObjectId());
    }

    public enum ItemState {
        UNCHANGED,
        ADDED,
        MODIFIED,
        REMOVED;

        // $FF: synthetic method
        private static ItemInstance.ItemState[] $values() {
            return new ItemInstance.ItemState[]{UNCHANGED, ADDED, MODIFIED, REMOVED};
        }
    }

    public enum ItemLocation {
        VOID,
        INVENTORY,
        PAPERDOLL,
        WAREHOUSE,
        CLANWH,
        PET,
        PET_EQUIP,
        LEASE,
        FREIGHT;

        // $FF: synthetic method
        private static ItemInstance.ItemLocation[] $values() {
            return new ItemInstance.ItemLocation[]{VOID, INVENTORY, PAPERDOLL, WAREHOUSE, CLANWH, PET, PET_EQUIP, LEASE, FREIGHT};
        }
    }

    public static class ItemDropTask implements Runnable {
        private final Creature _dropper;
        private final ItemInstance _itm;
        private int _x;
        private int _y;
        private int _z;

        public ItemDropTask(final ItemInstance item, ItemInstance param2, Creature dropper, int x, int y, int z) {
            this._x = x;
            this._y = y;
            this._z = z;
            this._dropper = dropper;
            this._itm = item;
        }

        public final void run() {
            assert this._itm.getRegion() == null;

            if (this._dropper != null) {
                Location dropDest = GeoEngine.getInstance().canMoveToTargetLoc(this._dropper.getX(), this._dropper.getY(), this._dropper.getZ(), this._x, this._y, this._z);
                this._x = dropDest.getX();
                this._y = dropDest.getY();
                this._z = dropDest.getZ();
            }

            this._itm.setDropperObjectId(this._dropper != null ? this._dropper.getObjectId() : 0);
            this._itm.spawnMe(this._x, this._y, this._z);
            ItemsOnGroundTaskManager.getInstance().add(this._itm, this._dropper);
            this._itm.setDropperObjectId(0);
        }
    }
}