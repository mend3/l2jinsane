package net.sf.l2j.gameserver.hwid.hwidmanager;

import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.network.GameClient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class HwidBan {
    private static final Logger _log = Logger.getLogger(HwidBan.class.getName());
    private static final Map<Integer, HwidBanList> _lists = new HashMap<>();
    private static HwidBan INSTANCE;

    public HwidBan() {
        load();
        _log.info("Loaded " + _lists.size() + " banned(s) HWID(s)");
    }

    public static HwidBan getInstance() {
        if (INSTANCE == null)
            INSTANCE = new HwidBan();
        return INSTANCE;
    }

    private static void load() {
        String HWID = "";
        int counterHWIDBan = 0;
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement statement = con.prepareStatement("SELECT * FROM hwid_bans");
                try {
                    ResultSet rset = statement.executeQuery();
                    try {
                        while (rset.next()) {
                            HWID = rset.getString("HWID");
                            HwidBanList hb = new HwidBanList(counterHWIDBan);
                            hb.setHWIDBan(HWID);
                            _lists.put(counterHWIDBan, hb);
                            counterHWIDBan++;
                        }
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
    }

    public static void reload() {
        INSTANCE = new HwidBan();
    }

    public static int getCountHWIDBan() {
        return _lists.size();
    }

    public static void addHWIDBan(GameClient client) {
        String HWID = client.getHWID();
        int counterHwidBan = _lists.size();
        HwidBanList hb = new HwidBanList(counterHwidBan);
        hb.setHWIDBan(HWID);
        _lists.put(counterHwidBan, hb);
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement statement = con.prepareStatement("INSERT INTO hwid_bans SET HWID=?");
                try {
                    statement.setString(1, HWID);
                    statement.execute();
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

    public boolean checkFullHWIDBanned(GameClient client) {
        if (_lists.size() == 0)
            return false;
        for (int i = 0; i < _lists.size(); i++) {
            if (_lists.get(i).getHWID().equals(client.getHWID()))
                return true;
        }
        return false;
    }
}
