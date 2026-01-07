package enginemods.main.data;

import enginemods.main.engine.AbstractMods;
import enginemods.main.holders.ValuesHolder;
import net.sf.l2j.commons.pool.ConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class ModsData {
    private static final Logger LOG = Logger.getLogger(ModsData.class.getName());

    private static final String UPDATE_DB = "UPDATE engine SET val=? WHERE event=? AND modName=? AND charId=?";

    private static final String INSERT_DB = "INSERT INTO engine (val,event,modName,charId) VALUES (?,?,?,?)";

    private static final String SELECT_DB = "SELECT charId,val,event,modName FROM engine";

    private static final String DELETE_DB_1 = "DELETE FROM engine WHERE modName=? AND event=? AND charId=?";

    private static final String DELETE_DB_2 = "DELETE FROM engine WHERE modName=?";

    private static final Map<Integer, List<ValuesHolder>> _playersValuesDb = new ConcurrentHashMap<>();

    public static void remove(AbstractMods mod) {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement statement = con.prepareStatement("DELETE FROM engine WHERE modName=?");
                try {
                    statement.setString(1, mod.getClass().getSimpleName());
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
            LOG.warning("Can't delete: mod:" + mod.getClass().getSimpleName() + " " + e);
            e.printStackTrace();
        }
    }

    public static void remove(int objectId, String event, AbstractMods mod) {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement statement = con.prepareStatement("DELETE FROM engine WHERE modName=? AND event=? AND charId=?");
                try {
                    statement.setString(1, mod.getClass().getSimpleName());
                    statement.setString(2, event);
                    statement.setInt(3, objectId);
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
            LOG.warning("Can't delete event:" + event + " mod:" + mod.getClass().getSimpleName() + " player objectId :" + objectId + e);
            e.printStackTrace();
        }
    }

    public static String get(int objectId, String event, AbstractMods mod) {
        if (_playersValuesDb.containsKey(objectId))
            for (ValuesHolder vh : _playersValuesDb.get(objectId)) {
                if (vh.getMod().equals(mod.getClass().getSimpleName()))
                    if (vh.getEvent().equals(event))
                        return vh.getValue();
            }
        return null;
    }

    public static void set(int objectId, String event, String value, AbstractMods mod) {
        String modName = mod.getClass().getSimpleName();
        boolean updateInfo = false;
        if (_playersValuesDb.containsKey(objectId)) {
            for (ValuesHolder vh : _playersValuesDb.get(objectId)) {
                if (vh.getEvent().equals(event) && vh.getMod().equals(modName)) {
                    updateInfo = true;
                    vh.setValue(value);
                }
            }
        } else {
            _playersValuesDb.put(objectId, new ArrayList<>());
        }
        if (!updateInfo)
            _playersValuesDb.get(objectId).add(new ValuesHolder(modName, event, value));
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement statement = con.prepareStatement(updateInfo ? "UPDATE engine SET val=? WHERE event=? AND modName=? AND charId=?" : "INSERT INTO engine (val,event,modName,charId) VALUES (?,?,?,?)");
                try {
                    statement.setString(1, value);
                    statement.setString(2, event);
                    statement.setString(3, modName);
                    statement.setInt(4, objectId);
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
            LOG.warning("Can't " + (updateInfo ? "update " : "insert ") + event + " to DB " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void load() {
        _playersValuesDb.clear();
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement statement = con.prepareStatement("SELECT charId,val,event,modName FROM engine");
                try {
                    ResultSet rset = statement.executeQuery();
                    try {
                        while (rset.next()) {
                            int objId = rset.getInt("charId");
                            String value = rset.getString("val");
                            String event = rset.getString("event");
                            String mod = rset.getString("modName");
                            if (!_playersValuesDb.containsKey(objId))
                                _playersValuesDb.put(objId, new ArrayList<>());
                            _playersValuesDb.get(objId).add(new ValuesHolder(mod, event, value));
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
            LOG.warning("Can't load values from DB" + e.getMessage());
            e.printStackTrace();
        }
        LOG.info(ModsData.class.getSimpleName() + " load " + ModsData.class.getSimpleName() + " values from players.");
    }
}
