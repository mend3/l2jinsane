/**/
package net.sf.l2j.gameserver.data.sql;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.model.actor.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public final class PlayerInfoTable {
    private static final CLogger LOGGER = new CLogger(PlayerInfoTable.class.getName());
    private static final String LOAD_DATA = "SELECT account_name, obj_Id, char_name, accesslevel FROM characters";
    private final Map<Integer, PlayerInfoTable.PlayerInfo> _infos = new ConcurrentHashMap<>();

    private PlayerInfoTable() {
    }

    public static PlayerInfoTable getInstance() {
        return PlayerInfoTable.SingletonHolder.INSTANCE;
    }

    public void load() {

        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("SELECT account_name, obj_Id, char_name, accesslevel FROM characters");

                try {
                    ResultSet rs = ps.executeQuery();

                    try {
                        while (rs.next()) {
                            this._infos.put(rs.getInt("obj_Id"), new PlayerInfo(this, rs.getString("account_name"), rs.getString("char_name"), rs.getInt("accesslevel")));
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
            LOGGER.error("Couldn't load player infos.", var12);
        }

        LOGGER.info("Loaded {} player infos.", this._infos.size());
    }

    public void addPlayer(int objectId, String accountName, String playerName, int accessLevel) {
        this._infos.putIfAbsent(objectId, new PlayerInfo(this, accountName, playerName, accessLevel));
    }

    public void updatePlayerData(Player player, boolean onlyAccessLevel) {
        if (player != null) {
            PlayerInfoTable.PlayerInfo data = this._infos.get(player.getObjectId());
            if (data != null) {
                if (onlyAccessLevel) {
                    data.setAccessLevel(player.getAccessLevel().getLevel());
                } else {
                    String playerName = player.getName();
                    if (!data.getPlayerName().equalsIgnoreCase(playerName)) {
                        data.setPlayerName(playerName);
                    }
                }
            }

        }
    }

    public void removePlayer(int objId) {
        this._infos.remove(objId);

    }

    public int getPlayerObjectId(String playerName) {
        return playerName != null && !playerName.isEmpty() ? this._infos.entrySet().stream().filter((m) -> m.getValue().getPlayerName().equalsIgnoreCase(playerName)).map(Entry::getKey).findFirst().orElse(-1) : -1;
    }

    public String getPlayerName(int objId) {
        PlayerInfoTable.PlayerInfo data = this._infos.get(objId);
        return data != null ? data.getPlayerName() : null;
    }

    public int getPlayerAccessLevel(int objId) {
        PlayerInfoTable.PlayerInfo data = this._infos.get(objId);
        return data != null ? data.getAccessLevel() : 0;
    }

    public int getCharactersInAcc(String accountName) {
        return (int) this._infos.entrySet().stream().filter((m) -> m.getValue().getAccountName().equalsIgnoreCase(accountName)).count();
    }

    private static final class SingletonHolder {
        private static final PlayerInfoTable INSTANCE = new PlayerInfoTable();
    }

    private static final class PlayerInfo {
        private final String _accountName;
        private String _playerName;
        private int _accessLevel;

        public PlayerInfo(final PlayerInfoTable param1, String accountName, String playerName, int accessLevel) {
            this._accountName = accountName;
            this._playerName = playerName;
            this._accessLevel = accessLevel;
        }

        public String getAccountName() {
            return this._accountName;
        }

        public String getPlayerName() {
            return this._playerName;
        }

        public void setPlayerName(String playerName) {
            this._playerName = playerName;
        }

        public int getAccessLevel() {
            return this._accessLevel;
        }

        public void setAccessLevel(int accessLevel) {
            this._accessLevel = accessLevel;
        }
    }
}