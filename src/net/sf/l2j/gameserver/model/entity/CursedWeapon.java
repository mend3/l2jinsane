/**/
package net.sf.l2j.gameserver.model.entity;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.enums.MessageType;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ScheduledFuture;

public class CursedWeapon {
    protected static final CLogger LOGGER = new CLogger(CursedWeapon.class.getName());
    private static final String LOAD_CW = "SELECT * FROM cursed_weapons WHERE itemId=?";
    private static final String DELETE_ITEM = "DELETE FROM items WHERE owner_id=? AND item_id=?";
    private static final String UPDATE_PLAYER = "UPDATE characters SET karma=?, pkkills=? WHERE obj_id=?";
    private static final String INSERT_CW = "INSERT INTO cursed_weapons (itemId, playerId, playerKarma, playerPkKills, nbKills, currentStage, numberBeforeNextStage, hungryTime, endTime) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String DELETE_CW = "DELETE FROM cursed_weapons WHERE itemId = ?";
    private static final String UPDATE_CW = "UPDATE cursed_weapons SET nbKills=?, currentStage=?, numberBeforeNextStage=?, hungryTime=?, endTime=? WHERE itemId=?";
    protected final int _itemId;
    private final String _name;
    private final int _skillId;
    private final int _skillMaxLevel;
    private final int _dropRate;
    private final int _dissapearChance;
    private final int _duration;
    private final int _durationLost;
    private final int _stageKills;
    protected Player _player = null;
    protected int _nbKills = 0;
    protected int _currentStage = 1;
    protected int _numberBeforeNextStage = 0;
    protected int _hungryTime = 0;
    protected long _endTime = 0L;
    private ItemInstance _item = null;
    private int _playerId = 0;
    private boolean _isDropped = false;
    private boolean _isActivated = false;
    private ScheduledFuture<?> _overallTimer;
    private ScheduledFuture<?> _dailyTimer;
    private ScheduledFuture<?> _dropTimer;
    private int _playerKarma = 0;
    private int _playerPkKills = 0;

    public CursedWeapon(StatSet set) {
        this._name = set.getString("name");
        this._itemId = set.getInteger("id");
        this._skillId = set.getInteger("skillId");
        this._dropRate = set.getInteger("dropRate");
        this._dissapearChance = set.getInteger("dissapearChance");
        this._duration = set.getInteger("duration");
        this._durationLost = set.getInteger("durationLost");
        this._stageKills = set.getInteger("stageKills");
        this._skillMaxLevel = SkillTable.getInstance().getMaxLevel(this._skillId);

        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM cursed_weapons WHERE itemId=?");

                try {
                    ps.setInt(1, this._itemId);
                    ResultSet rs = ps.executeQuery();

                    try {
                        while (rs.next()) {
                            this._playerId = rs.getInt("playerId");
                            this._playerKarma = rs.getInt("playerKarma");
                            this._playerPkKills = rs.getInt("playerPkKills");
                            this._nbKills = rs.getInt("nbKills");
                            this._currentStage = rs.getInt("currentStage");
                            this._numberBeforeNextStage = rs.getInt("numberBeforeNextStage");
                            this._hungryTime = rs.getInt("hungryTime");
                            this._endTime = rs.getLong("endTime");
                            this.reActivate(false);
                        }
                    } catch (Throwable var10) {
                        if (rs != null) {
                            try {
                                rs.close();
                            } catch (Throwable var9) {
                                var10.addSuppressed(var9);
                            }
                        }

                        throw var10;
                    }

                    if (rs != null) {
                        rs.close();
                    }
                } catch (Throwable var11) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var8) {
                            var11.addSuppressed(var8);
                        }
                    }

                    throw var11;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (Throwable var12) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var7) {
                        var12.addSuppressed(var7);
                    }
                }

                throw var12;
            }

            if (con != null) {
                con.close();
            }
        } catch (Exception var13) {
            LOGGER.error("Couldn't restore cursed weapons data.", var13);
        }

    }

    public void setItem(ItemInstance item) {
        this._item = item;
    }

    public boolean isActivated() {
        return this._isActivated;
    }

    public boolean isDropped() {
        return this._isDropped;
    }

    public long getEndTime() {
        return this._endTime;
    }

    public long getDuration() {
        return this._duration;
    }

    public int getDurationLost() {
        return this._durationLost;
    }

    public String getName() {
        return this._name;
    }

    public int getItemId() {
        return this._itemId;
    }

    public int getSkillId() {
        return this._skillId;
    }

    public int getPlayerId() {
        return this._playerId;
    }

    public Player getPlayer() {
        return this._player;
    }

    public void setPlayer(Player player) {
        this._player = player;
    }

    public int getPlayerKarma() {
        return this._playerKarma;
    }

    public int getPlayerPkKills() {
        return this._playerPkKills;
    }

    public int getNbKills() {
        return this._nbKills;
    }

    public int getStageKills() {
        return this._stageKills;
    }

    public boolean isActive() {
        return this._isActivated || this._isDropped;
    }

    public long getTimeLeft() {
        return this._endTime - System.currentTimeMillis();
    }

    public int getCurrentStage() {
        return this._currentStage;
    }

    public int getNumberBeforeNextStage() {
        return this._numberBeforeNextStage;
    }

    public int getHungryTime() {
        return this._hungryTime;
    }

    public void endOfLife() {
        if (this._isActivated) {
            if (this._player != null && this._player.isOnline()) {
                LOGGER.info("{} is being removed online.", this._name);
                this._player.abortAttack();
                this._player.setKarma(this._playerKarma);
                this._player.setPkKills(this._playerPkKills);
                this._player.setCursedWeaponEquippedId(0);
                this.removeDemonicSkills();
                this._player.useEquippableItem(this._item, true);
                this._player.destroyItemByItemId("CW", this._itemId, 1, this._player, false);
                this._player.broadcastUserInfo();
                this._player.store();
            } else {
                LOGGER.info("{} is being removed offline.", this._name);

                try {
                    Connection con = ConnectionPool.getConnection();

                    try {
                        PreparedStatement ps = con.prepareStatement("DELETE FROM items WHERE owner_id=? AND item_id=?");

                        try {
                            ps.setInt(1, this._playerId);
                            ps.setInt(2, this._itemId);
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

                        ps = con.prepareStatement("UPDATE characters SET karma=?, pkkills=? WHERE obj_id=?");

                        try {
                            ps.setInt(1, this._playerKarma);
                            ps.setInt(2, this._playerPkKills);
                            ps.setInt(3, this._playerId);
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
                    LOGGER.error("Couldn't cleanup {} from offline player {}.", var11, this._name, this._playerId);
                }
            }
        } else if (this._player != null && this._player.getInventory().getItemByItemId(this._itemId) != null) {
            this._player.destroyItemByItemId("CW", this._itemId, 1, this._player, false);
            LOGGER.info("{} has been assimilated.", this._name);
        } else if (this._item != null) {
            this._item.decayMe();
            LOGGER.info("{} has been removed from world.", this._name);
        }

        this.cancelDailyTimer();
        this.cancelOverallTimer();
        this.cancelDropTimer();
        this.removeFromDb();
        World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_DISAPPEARED).addItemName(this._itemId));
        this._player = null;
        this._item = null;
        this._isActivated = false;
        this._isDropped = false;
        this._nbKills = 0;
        this._currentStage = 1;
        this._numberBeforeNextStage = 0;
        this._hungryTime = 0;
        this._endTime = 0L;
        this._playerId = 0;
        this._playerKarma = 0;
        this._playerPkKills = 0;
    }

    private void cancelDailyTimer() {
        if (this._dailyTimer != null) {
            this._dailyTimer.cancel(false);
            this._dailyTimer = null;
        }

    }

    private void cancelOverallTimer() {
        if (this._overallTimer != null) {
            this._overallTimer.cancel(false);
            this._overallTimer = null;
        }

    }

    private void cancelDropTimer() {
        if (this._dropTimer != null) {
            this._dropTimer.cancel(false);
            this._dropTimer = null;
        }

    }

    private void dropFromPlayer(Creature killer) {
        this._player.abortAttack();
        this._item.setDestroyProtected(true);
        this._player.dropItem("DieDrop", this._item, killer, true);
        this._isActivated = false;
        this._isDropped = true;
        this._player.setKarma(this._playerKarma);
        this._player.setPkKills(this._playerPkKills);
        this._player.setCursedWeaponEquippedId(0);
        this.removeDemonicSkills();
        this.cancelDailyTimer();
        this._dropTimer = ThreadPool.schedule(new CursedWeapon.DropTimer(), 3600000L);
        this._currentStage = 1;
        this.removeFromDb();
        World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.S2_WAS_DROPPED_IN_THE_S1_REGION).addZoneName(this._player.getPosition()).addItemName(this._itemId));
    }

    private void dropFromMob(Attackable attackable, Player player) {
        this._isActivated = false;
        int x = attackable.getX() + Rnd.get(-70, 70);
        int y = attackable.getY() + Rnd.get(-70, 70);
        int z = GeoEngine.getInstance().getHeight(x, y, attackable.getZ());
        this._item = ItemInstance.create(this._itemId, 1, player, attackable);
        this._item.setDestroyProtected(true);
        this._item.dropMe(attackable, x, y, z);
        World.toAllOnlinePlayers(new ExRedSky(10));
        World.toAllOnlinePlayers(new Earthquake(x, y, z, 14, 3));
        this._isDropped = true;
        World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.S2_WAS_DROPPED_IN_THE_S1_REGION).addZoneName(player.getPosition()).addItemName(this._itemId));
    }

    public void cursedOnLogin() {
        SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.S2_OWNER_HAS_LOGGED_INTO_THE_S1_REGION);
        msg.addZoneName(this._player.getPosition());
        msg.addItemName(this._player.getCursedWeaponEquippedId());
        World.toAllOnlinePlayers(msg);
        int timeLeft = (int) (this.getTimeLeft() / 60000L);
        if (timeLeft > 60) {
            msg = SystemMessage.getSystemMessage(SystemMessageId.S2_HOUR_OF_USAGE_TIME_ARE_LEFT_FOR_S1);
            msg.addItemName(this._player.getCursedWeaponEquippedId());
            msg.addNumber(Math.round((float) (timeLeft / 60)));
        } else {
            msg = SystemMessage.getSystemMessage(SystemMessageId.S2_MINUTE_OF_USAGE_TIME_ARE_LEFT_FOR_S1);
            msg.addItemName(this._player.getCursedWeaponEquippedId());
            msg.addNumber(timeLeft);
        }

        this._player.sendPacket(msg);
    }

    public void giveDemonicSkills() {
        L2Skill skill = SkillTable.getInstance().getInfo(this._skillId, this._currentStage);
        if (skill != null) {
            this._player.addSkill(skill, false);
            this._player.sendSkillList();
        }

    }

    private void removeDemonicSkills() {
        this._player.removeSkill(this._skillId, false);
        this._player.sendSkillList();
    }

    public void reActivate(boolean fromZero) {
        if (fromZero) {
            this._hungryTime = this._durationLost * 60;
            this._endTime = System.currentTimeMillis() + (long) this._duration * 3600000L;
            this._overallTimer = ThreadPool.scheduleAtFixedRate(new CursedWeapon.OverallTimer(), 60000L, 60000L);
        } else {
            this._isActivated = true;
            if (this._endTime - System.currentTimeMillis() <= 0L) {
                this.endOfLife();
            } else {
                this._dailyTimer = ThreadPool.scheduleAtFixedRate(new CursedWeapon.DailyTimer(), 60000L, 60000L);
                this._overallTimer = ThreadPool.scheduleAtFixedRate(new CursedWeapon.OverallTimer(), 60000L, 60000L);
            }
        }

    }

    public boolean checkDrop(Attackable attackable, Player player) {
        if (Rnd.get(1000000) < this._dropRate) {
            this.dropFromMob(attackable, player);
            this._endTime = System.currentTimeMillis() + (long) this._duration * 3600000L;
            this._overallTimer = ThreadPool.scheduleAtFixedRate(new CursedWeapon.OverallTimer(), 60000L, 60000L);
            this._dropTimer = ThreadPool.schedule(new CursedWeapon.DropTimer(), 3600000L);
            return true;
        } else {
            return false;
        }
    }

    public void activate(Player player, ItemInstance item) {
        if (player.isMounted() && !player.dismount()) {
            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1).addItemName(item.getItemId()));
            item.setDestroyProtected(true);
            player.dropItem("InvDrop", item, null, true);
        } else {
            this._isActivated = true;
            this._player = player;
            this._playerId = this._player.getObjectId();
            this._playerKarma = this._player.getKarma();
            this._playerPkKills = this._player.getPkKills();
            this._item = item;
            this._numberBeforeNextStage = Rnd.get((int) Math.round((double) this._stageKills * 0.5D), (int) Math.round((double) this._stageKills * 1.5D));
            this._hungryTime = this._durationLost * 60;
            this._dailyTimer = ThreadPool.scheduleAtFixedRate(new CursedWeapon.DailyTimer(), 60000L, 60000L);
            this.cancelDropTimer();

            try {
                Connection con = ConnectionPool.getConnection();

                try {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO cursed_weapons (itemId, playerId, playerKarma, playerPkKills, nbKills, currentStage, numberBeforeNextStage, hungryTime, endTime) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");

                    try {
                        ps.setInt(1, this._itemId);
                        ps.setInt(2, this._playerId);
                        ps.setInt(3, this._playerKarma);
                        ps.setInt(4, this._playerPkKills);
                        ps.setInt(5, this._nbKills);
                        ps.setInt(6, this._currentStage);
                        ps.setInt(7, this._numberBeforeNextStage);
                        ps.setInt(8, this._hungryTime);
                        ps.setLong(9, this._endTime);
                        ps.executeUpdate();
                    } catch (Throwable var9) {
                        if (ps != null) {
                            try {
                                ps.close();
                            } catch (Throwable var8) {
                                var9.addSuppressed(var8);
                            }
                        }

                        throw var9;
                    }

                    if (ps != null) {
                        ps.close();
                    }
                } catch (Throwable var10) {
                    if (con != null) {
                        try {
                            con.close();
                        } catch (Throwable var7) {
                            var10.addSuppressed(var7);
                        }
                    }

                    throw var10;
                }

                if (con != null) {
                    con.close();
                }
            } catch (Exception var11) {
                LOGGER.error("Failed to insert cursed weapon data.", var11);
            }

            this._player.setCursedWeaponEquippedId(this._itemId);
            this._player.setKarma(9999999);
            this._player.setPkKills(0);
            if (this._player.isInParty()) {
                this._player.getParty().removePartyMember(this._player, MessageType.EXPELLED);
            }

            L2Effect[] var12 = this._player.getAllEffects();
            int var13 = var12.length;

            for (int var5 = 0; var5 < var13; ++var5) {
                L2Effect effect = var12[var5];
                if (effect.getSkill().isToggle()) {
                    effect.exit();
                }
            }

            this.giveDemonicSkills();
            this._player.useEquippableItem(this._item, true);
            this._player.setCurrentHpMp(this._player.getMaxHp(), this._player.getMaxMp());
            this._player.setCurrentCp(this._player.getMaxCp());
            this._player.broadcastUserInfo();
            World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.THE_OWNER_OF_S2_HAS_APPEARED_IN_THE_S1_REGION).addZoneName(this._player.getPosition()).addItemName(this._item.getItemId()));
        }
    }

    private void removeFromDb() {
        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("DELETE FROM cursed_weapons WHERE itemId = ?");

                try {
                    ps.setInt(1, this._itemId);
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
            LOGGER.error("Failed to remove cursed weapon data.", var9);
        }

    }

    public void dropIt(Creature killer) {
        if (Rnd.get(100) <= this._dissapearChance) {
            this.endOfLife();
        } else {
            this.dropFromPlayer(killer);
        }

    }

    public void increaseKills() {
        if (this._player != null && this._player.isOnline()) {
            ++this._nbKills;
            this._hungryTime = this._durationLost * 60;
            this._player.setPkKills(this._player.getPkKills() + 1);
            this._player.sendPacket(new UserInfo(this._player));
            if (this._nbKills >= this._numberBeforeNextStage) {
                this._nbKills = 0;
                this._numberBeforeNextStage = Rnd.get((int) Math.round((double) this._stageKills * 0.5D), (int) Math.round((double) this._stageKills * 1.5D));
                this.rankUp();
            }
        }

    }

    public void rankUp() {
        if (this._currentStage < this._skillMaxLevel) {
            ++this._currentStage;
            this.giveDemonicSkills();
            this._player.broadcastPacket(new SocialAction(this._player, 17));
        }
    }

    public void goTo(Player player) {
        if (player != null) {
            if (this._isActivated) {
                player.teleportTo(this._player.getX(), this._player.getY(), this._player.getZ(), 0);
            } else if (this._isDropped) {
                player.teleportTo(this._item.getX(), this._item.getY(), this._item.getZ(), 0);
            } else {
                player.sendMessage(this._name + " isn't in the world.");
            }

        }
    }

    public Location getWorldPosition() {
        if (this._isActivated && this._player != null) {
            return this._player.getPosition();
        } else {
            return this._isDropped && this._item != null ? this._item.getPosition() : null;
        }
    }

    private class DropTimer implements Runnable {
        protected DropTimer() {
        }

        public void run() {
            if (CursedWeapon.this.isDropped()) {
                CursedWeapon.this.endOfLife();
            }

        }
    }

    private class OverallTimer implements Runnable {
        protected OverallTimer() {
        }

        public void run() {
            if (System.currentTimeMillis() >= CursedWeapon.this._endTime) {
                CursedWeapon.this.endOfLife();
            } else {
                try {
                    Connection con = ConnectionPool.getConnection();

                    try {
                        PreparedStatement ps = con.prepareStatement("UPDATE cursed_weapons SET nbKills=?, currentStage=?, numberBeforeNextStage=?, hungryTime=?, endTime=? WHERE itemId=?");

                        try {
                            ps.setInt(1, CursedWeapon.this._nbKills);
                            ps.setInt(2, CursedWeapon.this._currentStage);
                            ps.setInt(3, CursedWeapon.this._numberBeforeNextStage);
                            ps.setInt(4, CursedWeapon.this._hungryTime);
                            ps.setLong(5, CursedWeapon.this._endTime);
                            ps.setInt(6, CursedWeapon.this._itemId);
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
                    CursedWeapon.LOGGER.error("Failed to update cursed weapon data.", var9);
                }
            }

        }
    }

    private class DailyTimer implements Runnable {
        private int _timer = 0;

        protected DailyTimer() {
        }

        public void run() {
            --CursedWeapon.this._hungryTime;
            ++this._timer;
            if (CursedWeapon.this._hungryTime <= 0) {
                CursedWeapon.this.endOfLife();
            } else if (CursedWeapon.this._player != null && CursedWeapon.this._player.isOnline() && this._timer % 60 == 0) {
                int timeLeft = (int) (CursedWeapon.this.getTimeLeft() / 60000L);
                SystemMessage msg;
                if (timeLeft > 60) {
                    msg = SystemMessage.getSystemMessage(SystemMessageId.S2_HOUR_OF_USAGE_TIME_ARE_LEFT_FOR_S1);
                    msg.addItemName(CursedWeapon.this._player.getCursedWeaponEquippedId());
                    msg.addNumber(Math.round((float) (timeLeft / 60)));
                } else {
                    msg = SystemMessage.getSystemMessage(SystemMessageId.S2_MINUTE_OF_USAGE_TIME_ARE_LEFT_FOR_S1);
                    msg.addItemName(CursedWeapon.this._player.getCursedWeaponEquippedId());
                    msg.addNumber(timeLeft);
                }

                CursedWeapon.this._player.sendPacket(msg);
            }

        }
    }
}