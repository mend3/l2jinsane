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
            return;
        }
        getVarObject(player, name).setValue(value);
        MariaDB.set("UPDATE character_memo_alt SET value=? WHERE obj_id=? AND name=?", value, Integer.valueOf(player.getObjectId()), name);
    }

    public static void setVar(Player player, String name, String value, long expirationTime) {
        if (player.getDungeonVars().containsKey(name))
            getVarObject(player, name).stopExpireTask();
        player.getDungeonVars().put(name, new PlayerVar(player, name, value, expirationTime));
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement stm = con.prepareStatement("REPLACE INTO character_memo_alt (obj_id, name, value, expire_time) VALUES (?,?,?,?)");
                try {
                    stm.setInt(1, player.getObjectId());
                    stm.setString(2, name);
                    stm.setString(3, value);
                    stm.setLong(4, expirationTime);
                    stm.execute();
                    if (stm != null)
                        stm.close();
                } catch (Throwable throwable) {
                    if (stm != null)
                        try {
                            stm.close();
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
            LOGGER.error(e);
        }
    }

    public static void setVar(Player player, String name, int value, long expirationTime) {
        setVar(player, name, String.valueOf(value), expirationTime);
    }

    public static PlayerVar getVarObject(Player player, String name) {
        if (player.getDungeonVars() == null)
            return null;
        return player.getDungeonVars().get(name);
    }

    public static long getVarTimeToExpire(Player player, String name) {
        try {
            return getVarObject(player, name).getTimeToExpire();
        } catch (NullPointerException nullPointerException) {
            return 0L;
        }
    }

    public static void unsetVar(Player player, String name) {
        if (name == null)
            return;
        if (player == null)
            return;
        PlayerVar pv = player.getDungeonVars().remove(name);
        if (pv != null) {
            if (name.contains("delete_temp_item")) {
                pv.getOwner().deleteTempItem(Integer.parseInt(pv.getValue()));
            } else if (name.contains("solo_hero")) {
                pv.getOwner().broadcastCharInfo();
                pv.getOwner().broadcastUserInfo();
            }
            MariaDB.set("DELETE FROM character_memo_alt WHERE obj_id=? AND name=? LIMIT 1", Integer.valueOf(pv.getOwner().getObjectId()), name);
            pv.stopExpireTask();
        }
    }

    public static void deleteExpiredVar(Player player, String name, String value) {
        if (name == null)
            return;
        if (name.contains("delete_temp_item"))
            player.deleteTempItem(Integer.parseInt(value));
        MariaDB.set("DELETE FROM character_memo_alt WHERE obj_id=? AND name=? LIMIT 1", Integer.valueOf(player.getObjectId()), name);
    }

    public static String getVar(Player player, String name) {
        PlayerVar pv = getVarObject(player, name);
        if (pv == null)
            return null;
        return pv.getValue();
    }

    public static long getVarTimeToExpireSQL(Player player, String name) {
        long expireTime = 0L;
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement statement = con.prepareStatement("SELECT expire_time FROM character_memo_alt WHERE obj_id = ? AND name = ?");
                try {
                    statement.setLong(1, player.getObjectId());
                    statement.setString(2, name);
                    ResultSet rset = statement.executeQuery();
                    try {
                        while (rset.next())
                            expireTime = rset.getLong("expire_time");
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
        return expireTime;
    }

    public static boolean getVarB(Player player, String name, boolean defaultVal) {
        PlayerVar pv = getVarObject(player, name);
        if (pv == null)
            return defaultVal;
        return pv.getValueBoolean();
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
        if (var != null)
            if (var.equalsIgnoreCase("true")) {
                result = 1;
            } else if (var.equalsIgnoreCase("false")) {
                result = 0;
            } else {
                result = Integer.parseInt(var);
            }
        return result;
    }

    public static void loadVariables(Player player) {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement offline = con.prepareStatement("SELECT * FROM character_memo_alt WHERE obj_id = ?");
                try {
                    offline.setInt(1, player.getObjectId());
                    ResultSet rs = offline.executeQuery();
                    try {
                        while (rs.next()) {
                            String name = rs.getString("name");
                            String value = rs.getString("value");
                            long expire_time = rs.getLong("expire_time");
                            long curtime = System.currentTimeMillis();
                            if (expire_time <= curtime && expire_time > 0L) {
                                deleteExpiredVar(player, name, rs.getString("value"));
                                continue;
                            }
                            player.getDungeonVars().put(name, new PlayerVar(player, name, value, expire_time));
                        }
                        if (rs != null)
                            rs.close();
                    } catch (Throwable throwable) {
                        if (rs != null)
                            try {
                                rs.close();
                            } catch (Throwable throwable1) {
                                throwable.addSuppressed(throwable1);
                            }
                        throw throwable;
                    }
                    if (offline != null)
                        offline.close();
                } catch (Throwable throwable) {
                    if (offline != null)
                        try {
                            offline.close();
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

    public static String getVarValue(Player player, String var, String defaultString) {
        String value = null;
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement offline = con.prepareStatement("SELECT value FROM character_memo_alt WHERE obj_id = ? AND name = ?");
                try {
                    offline.setInt(1, player.getObjectId());
                    offline.setString(2, var);
                    ResultSet rs = offline.executeQuery();
                    try {
                        if (rs.next())
                            value = rs.getString("value");
                        if (rs != null)
                            rs.close();
                    } catch (Throwable throwable) {
                        if (rs != null)
                            try {
                                rs.close();
                            } catch (Throwable throwable1) {
                                throwable.addSuppressed(throwable1);
                            }
                        throw throwable;
                    }
                    if (offline != null)
                        offline.close();
                } catch (Throwable throwable) {
                    if (offline != null)
                        try {
                            offline.close();
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
        return (value == null) ? defaultString : value;
    }

    public static String getVarValue(int objectId, String var, String defaultString) {
        String value = null;
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement offline = con.prepareStatement("SELECT value FROM character_memo_alt WHERE obj_id = ? AND name = ?");
                try {
                    offline.setInt(1, objectId);
                    offline.setString(2, var);
                    ResultSet rs = offline.executeQuery();
                    try {
                        if (rs.next())
                            value = rs.getString("value");
                        if (rs != null)
                            rs.close();
                    } catch (Throwable throwable) {
                        if (rs != null)
                            try {
                                rs.close();
                            } catch (Throwable throwable1) {
                                throwable.addSuppressed(throwable1);
                            }
                        throw throwable;
                    }
                    if (offline != null)
                        offline.close();
                } catch (Throwable throwable) {
                    if (offline != null)
                        try {
                            offline.close();
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
        return (value == null) ? defaultString : value;
    }

    public void setVar(Player player, String name, long value, long expirationTime) {
        setVar(player, name, String.valueOf(value), expirationTime);
    }

    public long getVarLong(Player player, String name) {
        return getVarLong(player, name, 0L);
    }

    public long getVarLong(Player player, String name, long defaultVal) {
        long result = defaultVal;
        String var = getVar(player, name);
        if (var != null)
            result = Long.parseLong(var);
        return result;
    }
}
