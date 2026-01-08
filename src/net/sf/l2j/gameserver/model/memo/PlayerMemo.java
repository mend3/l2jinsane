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
        this.restoreMe();
    }

    public boolean restoreMe() {
        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM character_memo WHERE charId = ?");

                try {
                    ps.setInt(1, this._objectId);
                    ResultSet rs = ps.executeQuery();

                    try {
                        while (rs.next()) {
                            this.set(rs.getString("var"), rs.getString("val"));
                        }
                    } catch (Throwable var18) {
                        if (rs != null) {
                            try {
                                rs.close();
                            } catch (Throwable var17) {
                                var18.addSuppressed(var17);
                            }
                        }

                        throw var18;
                    }

                    if (rs != null) {
                        rs.close();
                    }
                } catch (Throwable var19) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var16) {
                            var19.addSuppressed(var16);
                        }
                    }

                    throw var19;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (Throwable var20) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var15) {
                        var20.addSuppressed(var15);
                    }
                }

                throw var20;
            }

            if (con != null) {
                con.close();
            }

            return true;
        } catch (Exception e) {
            LOGGER.error("Couldn't restore variables for player id {}.", e, new Object[]{this._objectId});
        } finally {
            this.compareAndSetChanges(true, false);
        }

        return false;
    }

    public boolean storeMe() {
        if (!this.hasChanges()) {
            return false;
        } else {
            try {
                Connection con = ConnectionPool.getConnection();

                try {
                    PreparedStatement ps = con.prepareStatement("DELETE FROM character_memo WHERE charId = ?");

                    try {
                        ps.setInt(1, this._objectId);
                        ps.execute();
                    } catch (Throwable var17) {
                        if (ps != null) {
                            try {
                                ps.close();
                            } catch (Throwable var16) {
                                var17.addSuppressed(var16);
                            }
                        }

                        throw var17;
                    }

                    if (ps != null) {
                        ps.close();
                    }

                    ps = con.prepareStatement("INSERT INTO character_memo (charId, var, val) VALUES (?, ?, ?)");

                    try {
                        ps.setInt(1, this._objectId);

                        for (Map.Entry<String, Object> entry : this.entrySet()) {
                            ps.setString(2, (String) entry.getKey());
                            ps.setString(3, String.valueOf(entry.getValue()));
                            ps.addBatch();
                        }

                        ps.executeBatch();
                    } catch (Throwable var18) {
                        if (ps != null) {
                            try {
                                ps.close();
                            } catch (Throwable var15) {
                                var18.addSuppressed(var15);
                            }
                        }

                        throw var18;
                    }

                    if (ps != null) {
                        ps.close();
                    }
                } catch (Throwable var19) {
                    if (con != null) {
                        try {
                            con.close();
                        } catch (Throwable var14) {
                            var19.addSuppressed(var14);
                        }
                    }

                    throw var19;
                }

                if (con != null) {
                    con.close();
                }

                return true;
            } catch (Exception e) {
                LOGGER.error("Couldn't update variables for player id {}.", e, new Object[]{this._objectId});
            } finally {
                this.compareAndSetChanges(true, false);
            }

            return false;
        }
    }
}
