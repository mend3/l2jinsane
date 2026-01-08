package net.sf.l2j.gameserver.hwid.hwidmanager;

import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.network.GameClient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class HwidPlayer {
    private static final Logger _log = Logger.getLogger(HwidPlayer.class.getName());
    private static HwidPlayer INSTANCE;
    private final Map<Integer, HWIDInfoList> _list = new HashMap<>();
    private final Map<Integer, Integer> _sessions = new HashMap<>();

    public HwidPlayer() {
        load();
        _log.info("Loaded " + _list.size() + " player(s) HWID(s)");
    }

    public static HwidPlayer getInstance() {
        if (INSTANCE == null)
            INSTANCE = new HwidPlayer();
        return INSTANCE;
    }

    public static void reload() {
        INSTANCE = new HwidPlayer();
    }

    private void load() {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement statement = con.prepareStatement("SELECT * FROM hwid_info");
                try {
                    ResultSet rset = statement.executeQuery();
                    try {
                        int counterHWIDInfo = 0;
                        while (rset.next()) {
                            HWIDInfoList hInfo = new HWIDInfoList(counterHWIDInfo);
                            hInfo.setHWID(rset.getString("HWID"));
                            hInfo.setLogin(rset.getString("Account"));
                            hInfo.setPlayerID(rset.getInt("PlayerID"));
                            hInfo.setLockType(HWIDInfoList.LockType.valueOf(rset.getString("LockType")));
                            _list.put(counterHWIDInfo, hInfo);
                            counterHWIDInfo++;
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

    public int startSession(int WindowsCount) {
        synchronized (_list) {
            _sessions.putIfAbsent(WindowsCount, 0);
            _sessions.put(WindowsCount, _sessions.get(WindowsCount) + 1);
        }
        return _sessions.get(WindowsCount);
    }

    public void updateHWIDInfo(GameClient client) {
        updateHWIDInfo(client, HWIDInfoList.LockType.NONE);
    }

    public void updateHWIDInfo(GameClient client, HWIDInfoList.LockType lockType) {
        int counterHwidInfo = _list.size();
        boolean isFound = false;
        for (int i = 0; i < _list.size(); i++) {
            if (_list.get(i).getHWID().equals(client.getHWID())) {
                isFound = true;
                counterHwidInfo = i;
                break;
            }
        }
        HWIDInfoList hInfo = new HWIDInfoList(counterHwidInfo);
        hInfo.setHWID(client.getHWID());
        hInfo.setLogin(client.getAccountName());
        hInfo.setPlayerID(client.getPlayerId());
        hInfo.setLockType(lockType);
        _list.put(counterHwidInfo, hInfo);
        if (isFound) {
            try {
                Connection con = ConnectionPool.getConnection();
                PreparedStatement statement = con.prepareStatement("UPDATE hwid_info SET Account=?,PlayerID=?,LockType=? WHERE HWID=?");
                statement.setString(1, client.getAccountName());
                statement.setInt(2, client.getPlayerId());
                statement.setString(3, lockType.toString());
                statement.setString(4, client.getHWID());
                statement.execute();
                if (statement != null)
                    statement.close();
                if (con != null)
                    con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                Connection con = ConnectionPool.getConnection();
                PreparedStatement statement = con.prepareStatement("INSERT INTO hwid_info (HWID, Account, PlayerID, LockType) values (?,?,?,?)");
                statement.setString(1, client.getHWID());
                statement.setString(2, client.getAccountName());
                statement.setInt(3, client.getPlayerId());
                statement.setString(4, lockType.toString());
                statement.execute();
                if (statement != null)
                    statement.close();
                if (con != null)
                    con.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean checkLockedHWID(GameClient client) {
        if (_list.isEmpty())
            return false;
        boolean result = false;
        for (int i = 0; i < _list.size(); i++) {
            switch (_list.get(i).getLockType().ordinal()) {
                case 2:
                    if (client.getPlayerId() == 0)
                        break;
                    if (_list.get(i).getPlayerID() != client.getPlayerId())
                        break;
                    if (_list.get(i).getHWID().equals(client.getHWID()))
                        return false;
                    result = true;
                    break;
                case 3:
                    if (!_list.get(i).getLogin().equals(client.getLoginName()))
                        break;
                    if (_list.get(i).getHWID().equals(client.getHWID()))
                        return false;
                    result = true;
                    break;
            }
        }
        return result;
    }

    public int getAllowedWindowsCount(GameClient client) {
        if (_list.isEmpty())
            return -1;
        int i = 0;
        while (i < _list.size()) {
            if (!_list.get(i).getHWID().equals(client.getHWID())) {
                i++;
                continue;
            }
            if (_list.get(i).getHWID().isEmpty())
                return -1;
            return _list.get(i).getCount();
        }
        return -1;
    }

    public int getCountHwidInfo() {
        return _list.size();
    }
}
