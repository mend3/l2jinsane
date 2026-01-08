/**/
package net.sf.l2j.gameserver.data.sql;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.model.memo.AbstractMemo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

public class ServerMemoTable extends AbstractMemo {
    private static final CLogger LOGGER = new CLogger(ServerMemoTable.class.getName());
    private static final String SELECT_QUERY = "SELECT * FROM server_memo";
    private static final String DELETE_QUERY = "DELETE FROM server_memo";
    private static final String INSERT_QUERY = "INSERT INTO server_memo (var, value) VALUES (?, ?)";

    protected ServerMemoTable() {
        this.restoreMe();
    }

    public static ServerMemoTable getInstance() {
        return ServerMemoTable.SingletonHolder.INSTANCE;
    }

    public boolean restoreMe() {
        label163:
        {
            boolean result = true;
            try {
                Connection con = ConnectionPool.getConnection();

                try {
                    PreparedStatement ps = con.prepareStatement("SELECT * FROM server_memo");

                    try {
                        ResultSet rs = ps.executeQuery();

                        try {
                            while (rs.next()) {
                                this.set(rs.getString("var"), rs.getString("value"));
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
                break label163;
            } catch (Exception e) {
                LOGGER.error("Couldn't restore server variables.", e);
                result = false;
            } finally {
                this.compareAndSetChanges(true, false);
            }

            return result;
        }

        LOGGER.info("Loaded {} server variables.", this.size());
        return true;
    }

    public boolean storeMe() {
        if (!this.hasChanges()) {
            return false;
        } else {
            label176:
            {
                boolean result = true;
                try {
                    Connection con = ConnectionPool.getConnection();

                    try {
                        PreparedStatement ps = con.prepareStatement("DELETE FROM server_memo");

                        try {
                            ps.executeUpdate();
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

                        ps = con.prepareStatement("INSERT INTO server_memo (var, value) VALUES (?, ?)");

                        try {
                            for (Map.Entry<String, Object> entry : this.entrySet()) {
                                ps.setString(1, (String) entry.getKey());
                                ps.setString(2, String.valueOf(entry.getValue()));
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
                    break label176;
                } catch (Exception e) {
                    LOGGER.error("Couldn't save server variables to database.", e);
                    result = false;
                } finally {
                    this.compareAndSetChanges(true, false);
                }

                return result;
            }

            LOGGER.info("Stored {} server variables.", this.size());
            return true;
        }
    }

    private static class SingletonHolder {
        protected static final ServerMemoTable INSTANCE = new ServerMemoTable();
    }
}
