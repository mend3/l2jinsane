package net.sf.l2j.gameserver.model.memo;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

public class PlayerMemo extends AbstractMemo {
    private static final CLogger LOGGER = new CLogger(PlayerMemo.class.getName());

    private static final String SELECT_QUERY = "SELECT * FROM character_memo WHERE charId = ?";

    private static final String DELETE_QUERY = "DELETE FROM character_memo WHERE charId = ?";

    private static final String INSERT_QUERY = "INSERT INTO character_memo (charId, var, val) VALUES (?, ?, ?)";

    private final int _objectId;

    public PlayerMemo(int objectId) {
        this._objectId = objectId;
        load();
    }

    public boolean load() {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM character_memo WHERE charId = ?");
                try {
                    ps.setInt(1, this._objectId);
                    ResultSet rs = ps.executeQuery();
                    try {
                        while (rs.next())
                            set(rs.getString("var"), rs.getString("val"));
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
            LOGGER.error("Couldn't restore variables for player id {}.", e, this._objectId);
            return false;
        } finally {
            compareAndSetChanges(true, false);
        }
        return true;
    }

    public boolean storeMe() {
        if (!hasChanges())
            return false;
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("DELETE FROM character_memo WHERE charId = ?");
                try {
                    ps.setInt(1, this._objectId);
                    ps.execute();
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
                ps = con.prepareStatement("INSERT INTO character_memo (charId, var, val) VALUES (?, ?, ?)");
                try {
                    ps.setInt(1, this._objectId);
                    for (Map.Entry<String, Object> entry : entrySet()) {
                        ps.setString(2, entry.getKey());
                        ps.setString(3, String.valueOf(entry.getValue()));
                        ps.addBatch();
                    }
                    ps.executeBatch();
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
            LOGGER.error("Couldn't update variables for player id {}.", e, this._objectId);
            return false;
        } finally {
            compareAndSetChanges(true, false);
        }
        return true;
    }
}
