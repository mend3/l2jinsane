package enginemods.main.data;

import enginemods.main.holders.PlayerHolder;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.model.actor.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class PlayerData {
    private static final Logger LOG = Logger.getLogger(PlayerData.class.getName());
    private static final String SELECT_CHARACTERS = "SELECT obj_Id,char_name,account_name FROM characters";
    private static final Map<Integer, PlayerHolder> _players = new ConcurrentHashMap();

    public static void load() {
        _players.clear();

        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement statement = con.prepareStatement("SELECT obj_Id,char_name,account_name FROM characters");

                try {
                    ResultSet rset = statement.executeQuery();

                    try {
                        while (rset.next()) {
                            PlayerHolder ph = new PlayerHolder(rset.getInt("obj_Id"), rset.getString("char_name"), rset.getString("account_name"));
                            _players.put(ph.getObjectId(), ph);
                        }
                    } catch (Throwable var8) {
                        if (rset != null) {
                            try {
                                rset.close();
                            } catch (Throwable var7) {
                                var8.addSuppressed(var7);
                            }
                        }

                        throw var8;
                    }

                    if (rset != null) {
                        rset.close();
                    }
                } catch (Throwable var9) {
                    if (statement != null) {
                        try {
                            statement.close();
                        } catch (Throwable var6) {
                            var9.addSuppressed(var6);
                        }
                    }

                    throw var9;
                }

                if (statement != null) {
                    statement.close();
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
            LOG.warning("Could not restore character values: " + var11.getMessage());
            var11.printStackTrace();
        }

        Logger var10000 = LOG;
        String var10001 = PlayerData.class.getSimpleName();
        var10000.info(var10001 + " load " + _players.size() + " players from DB");
    }

    public static synchronized PlayerHolder get(Player player) {
        return _players.get(player.getObjectId());
    }

    public static synchronized PlayerHolder get(int objectId) {
        return _players.get(objectId);
    }

    public static synchronized void add(int objectId, String name, String accountName) {
        _players.put(objectId, new PlayerHolder(objectId, name, accountName));
    }

    public static synchronized Collection<PlayerHolder> getAllPlayers() {
        return _players.values();
    }
}