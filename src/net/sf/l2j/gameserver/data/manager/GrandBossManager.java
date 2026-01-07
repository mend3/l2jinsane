package net.sf.l2j.gameserver.data.manager;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.model.actor.instance.GrandBoss;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class GrandBossManager {
    private static final CLogger LOGGER = new CLogger(GrandBossManager.class.getName());

    private static final String SELECT_GRAND_BOSS_DATA = "SELECT * from grandboss_data ORDER BY boss_id";

    private static final String UPDATE_GRAND_BOSS_DATA = "UPDATE grandboss_data set loc_x = ?, loc_y = ?, loc_z = ?, heading = ?, respawn_time = ?, currentHP = ?, currentMP = ?, status = ? where boss_id = ?";

    private static final String UPDATE_GRAND_BOSS_DATA2 = "UPDATE grandboss_data set status = ? where boss_id = ?";

    private final Map<Integer, GrandBoss> _bosses = new HashMap<>();

    private final Map<Integer, StatSet> _storedInfo = new HashMap<>();

    private final Map<Integer, Integer> _bossStatus = new HashMap<>();

    public static GrandBossManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void load() {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("SELECT * from grandboss_data ORDER BY boss_id");
                try {
                    ResultSet rset = ps.executeQuery();
                    try {
                        while (rset.next()) {
                            int bossId = rset.getInt("boss_id");
                            StatSet info = new StatSet();
                            info.set("loc_x", rset.getInt("loc_x"));
                            info.set("loc_y", rset.getInt("loc_y"));
                            info.set("loc_z", rset.getInt("loc_z"));
                            info.set("heading", rset.getInt("heading"));
                            info.set("respawn_time", rset.getLong("respawn_time"));
                            info.set("currentHP", rset.getDouble("currentHP"));
                            info.set("currentMP", rset.getDouble("currentMP"));
                            this._bossStatus.put(Integer.valueOf(bossId), Integer.valueOf(rset.getInt("status")));
                            this._storedInfo.put(Integer.valueOf(bossId), info);
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
            LOGGER.error("Couldn't load grandboss.", e);
        }
        LOGGER.info("Loaded {} GrandBosses instances.", Integer.valueOf(this._storedInfo.size()));
    }

    public int getBossStatus(int bossId) {
        return this._bossStatus.get(Integer.valueOf(bossId));
    }

    public void setBossStatus(int bossId, int status) {
        this._bossStatus.put(Integer.valueOf(bossId), Integer.valueOf(status));
        LOGGER.info("Updated {} (id: {}) status to {}.", NpcData.getInstance().getTemplate(bossId).getName(), Integer.valueOf(bossId), Integer.valueOf(status));
        updateDb(bossId, true);
    }

    public void addBoss(GrandBoss boss) {
        if (boss != null)
            this._bosses.put(Integer.valueOf(boss.getNpcId()), boss);
    }

    public void addBoss(int npcId, GrandBoss boss) {
        if (boss != null)
            this._bosses.put(Integer.valueOf(npcId), boss);
    }

    public GrandBoss getBoss(int bossId) {
        return this._bosses.get(Integer.valueOf(bossId));
    }

    public StatSet getStatsSet(int bossId) {
        return this._storedInfo.get(Integer.valueOf(bossId));
    }

    public void setStatsSet(int bossId, StatSet info) {
        this._storedInfo.put(Integer.valueOf(bossId), info);
        updateDb(bossId, false);
    }

    private void updateDb(int bossId, boolean statusOnly) {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                StatSet info = this._storedInfo.get(Integer.valueOf(bossId));
                GrandBoss boss = this._bosses.get(Integer.valueOf(bossId));
                if (statusOnly || boss == null || info == null) {
                    PreparedStatement ps = con.prepareStatement("UPDATE grandboss_data set status = ? where boss_id = ?");
                    try {
                        ps.setInt(1, (Integer) this._bossStatus.get(Integer.valueOf(bossId)));
                        ps.setInt(2, bossId);
                        ps.executeUpdate();
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
                } else {
                    PreparedStatement ps = con.prepareStatement("UPDATE grandboss_data set loc_x = ?, loc_y = ?, loc_z = ?, heading = ?, respawn_time = ?, currentHP = ?, currentMP = ?, status = ? where boss_id = ?");
                    try {
                        ps.setInt(1, boss.getX());
                        ps.setInt(2, boss.getY());
                        ps.setInt(3, boss.getZ());
                        ps.setInt(4, boss.getHeading());
                        ps.setLong(5, info.getLong("respawn_time"));
                        ps.setDouble(6, boss.isDead() ? boss.getMaxHp() : boss.getCurrentHp());
                        ps.setDouble(7, boss.isDead() ? boss.getMaxMp() : boss.getCurrentMp());
                        ps.setInt(8, (Integer) this._bossStatus.get(Integer.valueOf(bossId)));
                        ps.setInt(9, bossId);
                        ps.executeUpdate();
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
            LOGGER.error("Couldn't update grandbosses.", e);
        }
    }

    public void cleanUp() {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps1 = con.prepareStatement("UPDATE grandboss_data set status = ? where boss_id = ?");
                try {
                    PreparedStatement ps2 = con.prepareStatement("UPDATE grandboss_data set loc_x = ?, loc_y = ?, loc_z = ?, heading = ?, respawn_time = ?, currentHP = ?, currentMP = ?, status = ? where boss_id = ?");
                    try {
                        for (Map.Entry<Integer, StatSet> infoEntry : this._storedInfo.entrySet()) {
                            int bossId = infoEntry.getKey();
                            StatSet info = infoEntry.getValue();
                            GrandBoss boss = this._bosses.get(Integer.valueOf(bossId));
                            if (boss == null || info == null) {
                                ps1.setInt(1, (Integer) this._bossStatus.get(Integer.valueOf(bossId)));
                                ps1.setInt(2, bossId);
                                ps1.addBatch();
                                continue;
                            }
                            ps2.setInt(1, boss.getX());
                            ps2.setInt(2, boss.getY());
                            ps2.setInt(3, boss.getZ());
                            ps2.setInt(4, boss.getHeading());
                            ps2.setLong(5, info.getLong("respawn_time"));
                            ps2.setDouble(6, boss.isDead() ? boss.getMaxHp() : boss.getCurrentHp());
                            ps2.setDouble(7, boss.isDead() ? boss.getMaxMp() : boss.getCurrentMp());
                            ps2.setInt(8, (Integer) this._bossStatus.get(Integer.valueOf(bossId)));
                            ps2.setInt(9, bossId);
                            ps2.addBatch();
                        }
                        ps1.executeBatch();
                        ps2.executeBatch();
                        if (ps2 != null)
                            ps2.close();
                    } catch (Throwable throwable) {
                        if (ps2 != null)
                            try {
                                ps2.close();
                            } catch (Throwable throwable1) {
                                throwable.addSuppressed(throwable1);
                            }
                        throw throwable;
                    }
                    if (ps1 != null)
                        ps1.close();
                } catch (Throwable throwable) {
                    if (ps1 != null)
                        try {
                            ps1.close();
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
            LOGGER.error("Couldn't store grandbosses.", e);
        }
        this._bosses.clear();
        this._storedInfo.clear();
        this._bossStatus.clear();
    }

    private static class SingletonHolder {
        protected static final GrandBossManager INSTANCE = new GrandBossManager();
    }
}
