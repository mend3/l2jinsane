package net.sf.l2j.gameserver.model.memo;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.util.variables.MariaDB;
import net.sf.l2j.util.variables.PlayerVar;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DungeonMemo {
    private static final CLogger LOGGER = new CLogger(DungeonMemo.class.getName());

    public static void changeValue(Player player, String name, String value) {
        if (!player.getDungeonVars().containsKey(name)) {
            player.sendMessage("Variable is not exist...");
        } else {
            getVarObject(player, name).setValue(value);
            MariaDB.set("UPDATE character_memo_alt SET value=? WHERE obj_id=? AND name=?", new Object[]{value, player.getObjectId(), name});
        }
    }

    public static void setVar(Player player, String name, String value, long expirationTime) {
        if (player.getDungeonVars().containsKey(name)) {
            getVarObject(player, name).stopExpireTask();
        }

        player.getDungeonVars().put(name, new PlayerVar(player, name, value, expirationTime));

        try (
                Connection con = ConnectionPool.getConnection();
                PreparedStatement stm = con.prepareStatement("REPLACE INTO character_memo_alt (obj_id, name, value, expire_time) VALUES (?,?,?,?)");
        ) {
            stm.setInt(1, player.getObjectId());
            stm.setString(2, name);
            stm.setString(3, value);
            stm.setLong(4, expirationTime);
            stm.execute();
        } catch (Exception e) {
            LOGGER.error(e);
        }

    }

    public static void setVar(Player player, String name, int value, long expirationTime) {
        setVar(player, name, String.valueOf(value), expirationTime);
    }

    public static PlayerVar getVarObject(Player player, String name) {
        return player.getDungeonVars() == null ? null : player.getDungeonVars().get(name);
    }

    public static long getVarTimeToExpire(Player player, String name) {
        try {
            return getVarObject(player, name).getTimeToExpire();
        } catch (NullPointerException var3) {
            return 0L;
        }
    }

    public static void unsetVar(Player player, String name) {
        if (name != null) {
            if (player != null) {
                PlayerVar pv = player.getDungeonVars().remove(name);
                if (pv != null) {
                    if (name.contains("delete_temp_item")) {
                        pv.getOwner().deleteTempItem(Integer.parseInt(pv.getValue()));
                    } else if (name.contains("solo_hero")) {
                        pv.getOwner().broadcastCharInfo();
                        pv.getOwner().broadcastUserInfo();
                    }

                    MariaDB.set("DELETE FROM character_memo_alt WHERE obj_id=? AND name=? LIMIT 1", new Object[]{pv.getOwner().getObjectId(), name});
                    pv.stopExpireTask();
                }

            }
        }
    }

    public static void deleteExpiredVar(Player player, String name, String value) {
        if (name != null) {
            if (name.contains("delete_temp_item")) {
                player.deleteTempItem(Integer.parseInt(value));
            }

            MariaDB.set("DELETE FROM character_memo_alt WHERE obj_id=? AND name=? LIMIT 1", new Object[]{player.getObjectId(), name});
        }
    }

    public static String getVar(Player player, String name) {
        PlayerVar pv = getVarObject(player, name);
        return pv == null ? null : pv.getValue();
    }

    public static long getVarTimeToExpireSQL(Player player, String name) {
        long expireTime = 0L;

        try (
                Connection con = ConnectionPool.getConnection();
                PreparedStatement statement = con.prepareStatement("SELECT expire_time FROM character_memo_alt WHERE obj_id = ? AND name = ?");
        ) {
            statement.setLong(1, (long) player.getObjectId());
            statement.setString(2, name);

            try (ResultSet rset = statement.executeQuery()) {
                while (rset.next()) {
                    expireTime = rset.getLong("expire_time");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return expireTime;
    }

    public static boolean getVarB(Player player, String name, boolean defaultVal) {
        PlayerVar pv = getVarObject(player, name);
        return pv == null ? defaultVal : pv.getValueBoolean();
    }

    public static boolean getVarB(Player player, String name) {
        return getVarB(player, name, false);
    }

    public static int getVarInt(Player player, String name) {
        return getVarInt(player, name, 0);
    }

    public static int getVarInt(Player player, String name, int defaultVal) {
        int result = defaultVal;
        String var = getVar(player, name);
        if (var != null) {
            if (var.equalsIgnoreCase("true")) {
                result = 1;
            } else if (var.equalsIgnoreCase("false")) {
                result = 0;
            } else {
                result = Integer.parseInt(var);
            }
        }

        return result;
    }

    public static void loadVariables(Player player) {
        try (
                Connection con = ConnectionPool.getConnection();
                PreparedStatement offline = con.prepareStatement("SELECT * FROM character_memo_alt WHERE obj_id = ?");
        ) {
            offline.setInt(1, player.getObjectId());

            try (ResultSet rs = offline.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("name");
                    String value = rs.getString("value");
                    long expire_time = rs.getLong("expire_time");
                    long curtime = System.currentTimeMillis();
                    if (expire_time <= curtime && expire_time > 0L) {
                        deleteExpiredVar(player, name, rs.getString("value"));
                    } else {
                        player.getDungeonVars().put(name, new PlayerVar(player, name, value, expire_time));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static String getVarValue(Player player, String var, String defaultString) {
        String value = null;

        try (
                Connection con = ConnectionPool.getConnection();
                PreparedStatement offline = con.prepareStatement("SELECT value FROM character_memo_alt WHERE obj_id = ? AND name = ?");
        ) {
            offline.setInt(1, player.getObjectId());
            offline.setString(2, var);

            try (ResultSet rs = offline.executeQuery()) {
                if (rs.next()) {
                    value = rs.getString("value");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return value == null ? defaultString : value;
    }

    public static String getVarValue(int objectId, String var, String defaultString) {
        String value = null;

        try (
                Connection con = ConnectionPool.getConnection();
                PreparedStatement offline = con.prepareStatement("SELECT value FROM character_memo_alt WHERE obj_id = ? AND name = ?");
        ) {
            offline.setInt(1, objectId);
            offline.setString(2, var);

            try (ResultSet rs = offline.executeQuery()) {
                if (rs.next()) {
                    value = rs.getString("value");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return value == null ? defaultString : value;
    }

    public void setVar(Player player, String name, long value, long expirationTime) {
        setVar(player, name, String.valueOf(value), expirationTime);
    }

    public long getVarLong(Player player, String name) {
        return this.getVarLong(player, name, 0L);
    }

    public long getVarLong(Player player, String name, long defaultVal) {
        long result = defaultVal;
        String var = getVar(player, name);
        if (var != null) {
            result = Long.parseLong(var);
        }

        return result;
    }
}
