package net.sf.l2j.gameserver.scripting;

import net.sf.l2j.Config;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.itemcontainer.PcInventory;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import java.util.Map;

public final class QuestState {
    public static final String SOUND_ACCEPT = "ItemSound.quest_accept";
    public static final String SOUND_ITEMGET = "ItemSound.quest_itemget";
    public static final String SOUND_MIDDLE = "ItemSound.quest_middle";
    public static final String SOUND_FINISH = "ItemSound.quest_finish";
    public static final String SOUND_GIVEUP = "ItemSound.quest_giveup";
    public static final String SOUND_JACKPOT = "ItemSound.quest_jackpot";
    public static final String SOUND_FANFARE = "ItemSound.quest_fanfare_2";
    public static final String SOUND_BEFORE_BATTLE = "Itemsound.quest_before_battle";
    public static final byte DROP_DIVMOD = 0;
    public static final byte DROP_FIXED_RATE = 1;
    public static final byte DROP_FIXED_COUNT = 2;
    public static final byte DROP_FIXED_BOTH = 3;
    private static final CLogger LOGGER = new CLogger(QuestState.class.getName());
    private static final String QUEST_SET_VAR = "REPLACE INTO character_quests (charId,name,var,value) VALUES (?,?,?,?)";
    private static final String QUEST_DEL_VAR = "DELETE FROM character_quests WHERE charId=? AND name=? AND var=?";
    private static final String QUEST_DELETE = "DELETE FROM character_quests WHERE charId=? AND name=?";
    private static final String QUEST_COMPLETE = "DELETE FROM character_quests WHERE charId=? AND name=? AND var<>'<state>'";
    private final Player _player;

    private final Quest _quest;
    private final Map<String, String> _vars = new HashMap<>();
    private byte _state;

    public QuestState(Player player, Quest quest, byte state) {
        this._player = player;
        this._quest = quest;
        this._state = state;
        this._player.setQuestState(this);
    }

    public Player getPlayer() {
        return this._player;
    }

    public Quest getQuest() {
        return this._quest;
    }

    public byte getState() {
        return this._state;
    }

    public void setState(byte state) {
        if (this._state != state) {
            this._state = state;
            setQuestVarInDb("<state>", String.valueOf(this._state));
            this._player.sendPacket(new QuestList(this._player));
        }
    }

    public boolean isCreated() {
        return (this._state == 0);
    }

    public boolean isCompleted() {
        return (this._state == 2);
    }

    public boolean isStarted() {
        return (this._state == 1);
    }

    public void exitQuest(boolean repeatable) {
        if (!isStarted())
            return;
        this._player.removeNotifyQuestOfDeath(this);
        if (repeatable) {
            this._player.delQuestState(this);
            this._player.sendPacket(new QuestList(this._player));
        } else {
            setState((byte) 2);
        }
        this._vars.clear();
        int[] itemIdList = this._quest.getItemsIds();
        if (itemIdList != null)
            for (int itemId : itemIdList)
                takeItems(itemId, -1);
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement(repeatable ? "DELETE FROM character_quests WHERE charId=? AND name=?" : "DELETE FROM character_quests WHERE charId=? AND name=? AND var<>'<state>'");
                try {
                    ps.setInt(1, this._player.getObjectId());
                    ps.setString(2, this._quest.getName());
                    ps.executeUpdate();
                    if (ps != null)
                        ps.close();
                } catch (Throwable throwable) {
                    if (ps != null)
                        try {
                            ps.close();
                        } catch (Throwable throwable1) {
                            throwable.addSuppressed(throwable1);
                        }
                    throw throwable;
                }
                if (con != null)
                    con.close();
            } catch (Throwable throwable) {
                if (con != null)
                    try {
                        con.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                throw throwable;
            }
        } catch (Exception e) {
            LOGGER.error("Couldn't delete quest.", e);
        }
    }

    public void addNotifyOfDeath() {
        if (this._player != null)
            this._player.addNotifyQuestOfDeath(this);
    }

    public void set(String var, String value) {
        if (var == null || var.isEmpty() || value == null || value.isEmpty())
            return;
        String old = this._vars.put(var, value);
        setQuestVarInDb(var, value);
        if ("cond".equals(var))
            try {
                int previousVal = 0;
                try {
                    previousVal = Integer.parseInt(old);
                } catch (Exception ex) {
                    previousVal = 0;
                }
                setCond(Integer.parseInt(value), previousVal);
            } catch (Exception e) {
                LOGGER.error("{}, {} cond [{}] is not an integer. Value stored, but no packet was sent.", e, this._player.getName(), this._quest.getName(), value);
            }
    }

    public void setInternal(String var, String value) {
        if (var == null || var.isEmpty() || value == null || value.isEmpty())
            return;
        this._vars.put(var, value);
    }

    private void setCond(int cond, int old) {
        if (cond == old)
            return;
        int completedStateFlags = 0;
        if (cond < 3 || cond > 31) {
            unset("__compltdStateFlags");
        } else {
            completedStateFlags = getInt("__compltdStateFlags");
        }
        if (completedStateFlags == 0) {
            if (cond > old + 1) {
                completedStateFlags = -2147483647;
                completedStateFlags |= (1 << old) - 1;
                completedStateFlags |= 1 << cond - 1;
                set("__compltdStateFlags", String.valueOf(completedStateFlags));
            }
        } else if (cond < old) {
            completedStateFlags &= (1 << cond) - 1;
            if (completedStateFlags == (1 << cond) - 1) {
                unset("__compltdStateFlags");
            } else {
                completedStateFlags |= 0x80000001;
                set("__compltdStateFlags", String.valueOf(completedStateFlags));
            }
        } else {
            completedStateFlags |= 1 << cond - 1;
            set("__compltdStateFlags", String.valueOf(completedStateFlags));
        }
        this._player.sendPacket(new QuestList(this._player));
        if (this._quest.isRealQuest() && cond > 0)
            this._player.sendPacket(new ExShowQuestMark(this._quest.getQuestId()));
    }

    public void unset(String var) {
        if (this._vars.remove(var) != null)
            removeQuestVarInDb(var);
    }

    public String get(String var) {
        return this._vars.get(var);
    }

    public int getInt(String var) {
        String variable = this._vars.get(var);
        if (variable == null || variable.isEmpty())
            return 0;
        int value = 0;
        try {
            value = Integer.parseInt(variable);
        } catch (Exception e) {
            LOGGER.error("{}: variable {} isn't an integer: {}.", e, this._player.getName(), var, Integer.valueOf(value));
        }
        return value;
    }

    private void setQuestVarInDb(String var, String value) {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("REPLACE INTO character_quests (charId,name,var,value) VALUES (?,?,?,?)");
                try {
                    ps.setInt(1, this._player.getObjectId());
                    ps.setString(2, this._quest.getName());
                    ps.setString(3, var);
                    ps.setString(4, value);
                    ps.executeUpdate();
                    if (ps != null)
                        ps.close();
                } catch (Throwable throwable) {
                    if (ps != null)
                        try {
                            ps.close();
                        } catch (Throwable throwable1) {
                            throwable.addSuppressed(throwable1);
                        }
                    throw throwable;
                }
                if (con != null)
                    con.close();
            } catch (Throwable throwable) {
                if (con != null)
                    try {
                        con.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                throw throwable;
            }
        } catch (Exception e) {
            LOGGER.error("Couldn't insert quest.", e);
        }
    }

    private void removeQuestVarInDb(String var) {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("DELETE FROM character_quests WHERE charId=? AND name=? AND var=?");
                try {
                    ps.setInt(1, this._player.getObjectId());
                    ps.setString(2, this._quest.getName());
                    ps.setString(3, var);
                    ps.executeUpdate();
                    if (ps != null)
                        ps.close();
                } catch (Throwable throwable) {
                    if (ps != null)
                        try {
                            ps.close();
                        } catch (Throwable throwable1) {
                            throwable.addSuppressed(throwable1);
                        }
                    throw throwable;
                }
                if (con != null)
                    con.close();
            } catch (Throwable throwable) {
                if (con != null)
                    try {
                        con.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                throw throwable;
            }
        } catch (Exception e) {
            LOGGER.error("Couldn't delete quest.", e);
        }
    }

    public boolean hasQuestItems(int itemId) {
        return (this._player.getInventory().getItemByItemId(itemId) != null);
    }

    public boolean hasQuestItems(int... itemIds) {
        PcInventory inv = this._player.getInventory();
        for (int itemId : itemIds) {
            if (inv.getItemByItemId(itemId) == null)
                return false;
        }
        return true;
    }

    public boolean hasAtLeastOneQuestItem(int... itemIds) {
        return this._player.getInventory().hasAtLeastOneItem(itemIds);
    }

    public int getQuestItemsCount(int itemId) {
        int count = 0;
        for (ItemInstance item : this._player.getInventory().getItems()) {
            if (item != null && item.getItemId() == itemId)
                count += item.getCount();
        }
        return count;
    }

    public int getItemEquipped(int loc) {
        return this._player.getInventory().getPaperdollItemId(loc);
    }

    public int getEnchantLevel(int itemId) {
        ItemInstance enchanteditem = this._player.getInventory().getItemByItemId(itemId);
        if (enchanteditem == null)
            return 0;
        return enchanteditem.getEnchantLevel();
    }

    public void giveItems(int itemId, int itemCount) {
        giveItems(itemId, itemCount, 0);
    }

    public void giveItems(int itemId, int itemCount, int enchantLevel) {
        if (itemCount <= 0)
            return;
        ItemInstance item = this._player.getInventory().addItem("Quest", itemId, itemCount, this._player, this._player);
        if (item == null)
            return;
        if (enchantLevel > 0)
            item.setEnchantLevel(enchantLevel);
        if (itemId == 57) {
            SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_ADENA);
            smsg.addItemNumber(itemCount);
            this._player.sendPacket(smsg);
        } else if (itemCount > 1) {
            SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
            smsg.addItemName(itemId);
            smsg.addItemNumber(itemCount);
            this._player.sendPacket(smsg);
        } else {
            SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
            smsg.addItemName(itemId);
            this._player.sendPacket(smsg);
        }
        StatusUpdate su = new StatusUpdate(this._player);
        su.addAttribute(14, this._player.getCurrentLoad());
        this._player.sendPacket(su);
    }

    public void takeItems(int itemId, int itemCount) {
        ItemInstance item = this._player.getInventory().getItemByItemId(itemId);
        if (item == null)
            return;
        if (itemCount < 0 || itemCount > item.getCount())
            itemCount = item.getCount();
        if (item.isEquipped()) {
            ItemInstance[] unequiped = this._player.getInventory().unEquipItemInBodySlotAndRecord(item);
            InventoryUpdate iu = new InventoryUpdate();
            for (ItemInstance itm : unequiped)
                iu.addModifiedItem(itm);
            this._player.sendPacket(iu);
            this._player.broadcastUserInfo();
        }
        this._player.destroyItemByItemId("Quest", itemId, itemCount, this._player, true);
    }

    public boolean dropItemsAlways(int itemId, int count, int neededCount) {
        return dropItems(itemId, count, neededCount, 1000000, (byte) 1);
    }

    public boolean dropItems(int itemId, int count, int neededCount, int dropChance) {
        return dropItems(itemId, count, neededCount, dropChance, (byte) 0);
    }

    public boolean dropItems(int itemId, int count, int neededCount, int dropChance, byte type) {
        int currentCount = getQuestItemsCount(itemId);
        if (neededCount > 0 && currentCount >= neededCount)
            return true;
        int amount = 0;
        switch (type) {
            case 0:
                dropChance = (int) (dropChance * Config.RATE_QUEST_DROP);
                amount = count * dropChance / 1000000;
                if (Rnd.get(1000000) < dropChance % 1000000)
                    amount += count;
                break;
            case 1:
                if (Rnd.get(1000000) < dropChance)
                    amount = (int) (count * Config.RATE_QUEST_DROP);
                break;
            case 2:
                if (Rnd.get(1000000) < dropChance * Config.RATE_QUEST_DROP)
                    amount = count;
                break;
            case 3:
                if (Rnd.get(1000000) < dropChance)
                    amount = count;
                break;
        }
        boolean reached = false;
        if (amount > 0) {
            if (neededCount > 0) {
                reached = (currentCount + amount >= neededCount);
                amount = reached ? (neededCount - currentCount) : amount;
            }
            if (!this._player.getInventory().validateCapacityByItemId(itemId))
                return false;
            giveItems(itemId, amount, 0);
            playSound(reached ? "ItemSound.quest_middle" : "ItemSound.quest_itemget");
        }
        return (neededCount > 0 && reached);
    }

    public boolean dropMultipleItems(int[][] rewardsInfos) {
        return dropMultipleItems(rewardsInfos, (byte) 0);
    }

    public boolean dropMultipleItems(int[][] rewardsInfos, byte type) {
        boolean sendSound = false;
        boolean reached = true;
        for (int[] info : rewardsInfos) {
            int itemId = info[0];
            int currentCount = getQuestItemsCount(itemId);
            int neededCount = info[2];
            if (neededCount > 0 && currentCount >= neededCount)
                continue;
            int count = info[1];
            int dropChance = info[3];
            int amount = 0;
            switch (type) {
                case 0:
                    dropChance = (int) (dropChance * Config.RATE_QUEST_DROP);
                    amount = count * dropChance / 1000000;
                    if (Rnd.get(1000000) < dropChance % 1000000)
                        amount += count;
                    break;
                case 1:
                    if (Rnd.get(1000000) < dropChance)
                        amount = (int) (count * Config.RATE_QUEST_DROP);
                    break;
                case 2:
                    if (Rnd.get(1000000) < dropChance * Config.RATE_QUEST_DROP)
                        amount = count;
                    break;
                case 3:
                    if (Rnd.get(1000000) < dropChance)
                        amount = count;
                    break;
            }
            if (amount > 0) {
                if (neededCount > 0)
                    amount = (currentCount + amount >= neededCount) ? (neededCount - currentCount) : amount;
                if (!this._player.getInventory().validateCapacityByItemId(itemId))
                    continue;
                giveItems(itemId, amount, 0);
                sendSound = true;
            }
            if (neededCount <= 0 || currentCount + amount < neededCount)
                reached = false;
            continue;
        }
        if (sendSound)
            playSound(reached ? "ItemSound.quest_middle" : "ItemSound.quest_itemget");
        return reached;
    }

    public void rewardItems(int itemId, int itemCount) {
        if (itemId == 57) {
            giveItems(itemId, (int) (itemCount * Config.RATE_QUEST_REWARD_ADENA), 0);
        } else {
            giveItems(itemId, (int) (itemCount * Config.RATE_QUEST_REWARD), 0);
        }
    }

    public void rewardExpAndSp(long exp, int sp) {
        this._player.addExpAndSp((long) (exp * Config.RATE_QUEST_REWARD_XP), (int) (sp * Config.RATE_QUEST_REWARD_SP));
    }

    public void addRadar(int x, int y, int z) {
        this._player.getRadarList().addMarker(x, y, z);
    }

    public void removeRadar(int x, int y, int z) {
        this._player.getRadarList().removeMarker(x, y, z);
    }

    public void clearRadar() {
        this._player.getRadarList().removeAllMarkers();
    }

    public void playSound(String sound) {
        this._player.sendPacket(new PlaySound(sound));
    }

    public void showQuestionMark(int number) {
        this._player.sendPacket(new TutorialShowQuestionMark(number));
    }

    public void playTutorialVoice(String voice) {
        this._player.sendPacket(new PlaySound(2, voice, this._player));
    }

    public void showTutorialHTML(String html) {
        this._player.sendPacket(new TutorialShowHtml(HtmCache.getInstance().getHtmForce("data/html/scripts/quests/Tutorial/" + html)));
    }

    public void closeTutorialHtml() {
        this._player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
    }

    public void onTutorialClientEvent(int number) {
        this._player.sendPacket(new TutorialEnableClientEvent(number));
    }
}
