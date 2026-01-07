package net.sf.l2j.gameserver.events.pvpevent;

import enginemods.main.data.ConfigData;
import net.sf.l2j.Config;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PvPEvent {
    private static final CLogger LOGGER = new CLogger(PvPEvent.class.getName());

    private PvPEventEngineState _state = PvPEventEngineState.INACTIVE;

    public static void pvpSpecialReward(Player player) {
        SystemMessage sm = null;
        for (int[] reward : Config.PVP_SPECIAL_ITEMS_REWARD) {
            if (ItemTable.getInstance().createDummyItem(reward[0]).isStackable()) {
                player.getInventory().addItem("Pvp Reward:", reward[0], reward[1], player, null);
                if (reward[1] > 1) {
                    sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
                    sm.addItemName(reward[0]);
                    sm.addItemNumber(reward[1]);
                } else {
                    sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
                    sm.addItemName(reward[0]);
                }
                player.sendPacket(sm);
                continue;
            }
            for (int i = 0; i < reward[1]; i++) {
                player.getInventory().addItem("Pvp Reward:", reward[0], 1, player, null);
                sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
                sm.addItemName(reward[0]);
                player.sendPacket(sm);
            }
        }
    }

    public static void announce(String msg) {
        CreatureSay cs = new CreatureSay(0, 18, "PvPEvent", "PvPEvent: " + msg);
        World.toAllOnlinePlayers(cs);
    }

    public static void cleanPvpEvent() {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                con.setAutoCommit(false);
                try {
                    PreparedStatement statement = con.prepareStatement("UPDATE characters SET event_pvp=0");
                    try {
                        statement.executeUpdate();
                        con.commit();
                        if (statement != null)
                            statement.close();
                    } catch (Throwable throwable) {
                        if (statement != null)
                            try {
                                statement.close();
                            } catch (Throwable throwable1) {
                                throwable.addSuppressed(throwable1);
                            }
                        throw throwable;
                    }
                } catch (Exception e) {
                    con.rollback();
                    e.printStackTrace();
                } finally {
                    con.setAutoCommit(true);
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
            e.printStackTrace();
        }
    }

    public static int getTopZonePlayerReward() {
        int id = 0;
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement statement = con.prepareStatement("SELECT obj_Id FROM characters ORDER BY event_pvp DESC LIMIT 1");
                try {
                    ResultSet rset = statement.executeQuery();
                    try {
                        while (rset.next())
                            id = rset.getInt("obj_Id");
                        if (rset != null)
                            rset.close();
                    } catch (Throwable throwable) {
                        if (rset != null)
                            try {
                                rset.close();
                            } catch (Throwable throwable1) {
                                throwable.addSuppressed(throwable1);
                            }
                        throw throwable;
                    }
                    if (statement != null)
                        statement.close();
                } catch (Throwable throwable) {
                    if (statement != null)
                        try {
                            statement.close();
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
            e.printStackTrace();
        }
        return id;
    }

    public static int getTopZonePvpCount() {
        int id = 0;
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement statement = con.prepareStatement("SELECT event_pvp FROM characters ORDER BY event_pvp DESC LIMIT 1");
                try {
                    ResultSet rset = statement.executeQuery();
                    try {
                        while (rset.next())
                            id = rset.getInt("event_pvp");
                        if (rset != null)
                            rset.close();
                    } catch (Throwable throwable) {
                        if (rset != null)
                            try {
                                rset.close();
                            } catch (Throwable throwable1) {
                                throwable.addSuppressed(throwable1);
                            }
                        throw throwable;
                    }
                    if (statement != null)
                        statement.close();
                } catch (Throwable throwable) {
                    if (statement != null)
                        try {
                            statement.close();
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
            e.printStackTrace();
        }
        return id;
    }

    public static String getTopZonePvpName() {
        String name = null;
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement statement = con.prepareStatement("SELECT char_name FROM characters ORDER BY event_pvp DESC LIMIT 1");
                try {
                    ResultSet rset = statement.executeQuery();
                    try {
                        while (rset.next())
                            name = rset.getString("char_name");
                        if (rset != null)
                            rset.close();
                    } catch (Throwable throwable) {
                        if (rset != null)
                            try {
                                rset.close();
                            } catch (Throwable throwable1) {
                                throwable.addSuppressed(throwable1);
                            }
                        throw throwable;
                    }
                    if (statement != null)
                        statement.close();
                } catch (Throwable throwable) {
                    if (statement != null)
                        try {
                            statement.close();
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
            e.printStackTrace();
        }
        return name;
    }

    public static void addEventPvp(Player activeChar) {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement statement = con.prepareStatement("UPDATE characters SET event_pvp=? WHERE obj_Id=?");
                try {
                    statement.setInt(1, getEventPvp(activeChar) + 1);
                    statement.setInt(2, activeChar.getObjectId());
                    statement.executeUpdate();
                    if (statement != null)
                        statement.close();
                } catch (Throwable throwable) {
                    if (statement != null)
                        try {
                            statement.close();
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
            e.printStackTrace();
        }
    }

    public static int getEventPvp(Player activeChar) {
        int id = 0;
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement statement = con.prepareStatement("SELECT event_pvp FROM characters WHERE obj_Id=?");
                try {
                    statement.setInt(1, activeChar.getObjectId());
                    ResultSet rset = statement.executeQuery();
                    try {
                        while (rset.next())
                            id = rset.getInt("event_pvp");
                        if (rset != null)
                            rset.close();
                    } catch (Throwable throwable) {
                        if (rset != null)
                            try {
                                rset.close();
                            } catch (Throwable throwable1) {
                                throwable.addSuppressed(throwable1);
                            }
                        throw throwable;
                    }
                    if (statement != null)
                        statement.close();
                } catch (Throwable throwable) {
                    if (statement != null)
                        try {
                            statement.close();
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
            e.printStackTrace();
        }
        return id;
    }

    private static void addReward(int objId) {
        Player player = World.getInstance().getPlayer(objId);
        for (int[] reward : Config.PVP_EVENT_REWARDS) {
            if (player != null && player.isOnline()) {
                InventoryUpdate iu = new InventoryUpdate();
                if (player.isVip()) {
                    player.addItem("Top Reward", reward[0], reward[1] * ConfigData.VIP_DROP_EVENTS_MULTIPLIER, player, true);
                } else {
                    player.addItem("Top Reward", reward[0], reward[1], player, true);
                }
                player.sendPacket(new ExShowScreenMessage("Congratulations " + player.getName() + " you are the winner of PvP Event.", 3000, 2, true));
                player.getInventory().updateDatabase();
                player.sendPacket(iu);
            } else {
                addOfflineItem(objId, reward[0], reward[1]);
            }
        }
    }

    private static void addOfflineItem(int ownerId, int itemId, int count) {
        Item item = ItemTable.getInstance().getTemplate(itemId);
        int objectId = IdFactory.getInstance().getNextId();
        if (count > 1 && !item.isStackable())
            return;
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement statement = con.prepareStatement("INSERT INTO items (owner_id,item_id,count,loc,loc_data,enchant_level,object_id,custom_type1,custom_type2,mana_left,time) VALUES (?,?,?,?,?,?,?,?,?,?,?)");
                try {
                    statement.setInt(1, ownerId);
                    statement.setInt(2, item.getItemId());
                    statement.setInt(3, count);
                    statement.setString(4, "INVENTORY");
                    statement.setInt(5, 0);
                    statement.setInt(6, 0);
                    statement.setInt(7, objectId);
                    statement.setInt(8, 0);
                    statement.setInt(9, 0);
                    statement.setInt(10, -1);
                    statement.setLong(11, 0L);
                    statement.executeUpdate();
                    if (statement != null)
                        statement.close();
                } catch (Throwable throwable) {
                    if (statement != null)
                        try {
                            statement.close();
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
        } catch (SQLException e) {
            LOGGER.warn("Could not update item char: " + e);
            e.printStackTrace();
        }
    }

    public static PvPEvent getInstance() {
        return SingletonHolder._instance;
    }

    public boolean startPartyEvent() {
        setState(PvPEventEngineState.ACTIVE);
        return true;
    }

    public boolean endPartyEvent() {
        setState(PvPEventEngineState.INACTIVE);
        return true;
    }

    private void setState(PvPEventEngineState state) {
        synchronized (state) {
            this._state = state;
        }
    }

    public boolean isActive() {
        synchronized (this._state) {
            return (this._state == PvPEventEngineState.ACTIVE);
        }
    }

    public boolean isInactive() {
        synchronized (this._state) {
            return (this._state == PvPEventEngineState.INACTIVE);
        }
    }

    public void rewardFinish() {
        if (getTopZonePvpCount() == 0) {
            announce(" is finished without winners!");
        } else {
            announce(" Winner is " + getTopZonePvpName() + " with " + getTopZonePvpCount() + " pvp's.");
            addReward(getTopZonePlayerReward());
        }
        cleanPvpEvent();
    }

    private static class SingletonHolder {
        protected static final PvPEvent _instance = new PvPEvent();
    }
}
