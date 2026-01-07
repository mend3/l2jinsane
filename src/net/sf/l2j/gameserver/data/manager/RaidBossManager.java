package net.sf.l2j.gameserver.data.manager;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.data.sql.SpawnTable;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.enums.BossStatus;
import net.sf.l2j.gameserver.model.actor.instance.RaidBoss;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.spawn.BossSpawn;
import net.sf.l2j.gameserver.model.spawn.L2Spawn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class RaidBossManager {
    protected static final CLogger LOGGER = new CLogger(RaidBossManager.class.getName());

    private static final String LOAD_RAIDBOSSES = "SELECT * from raidboss_spawnlist ORDER BY boss_id";

    private static final String INSERT_RAIDBOSS = "INSERT INTO raidboss_spawnlist (boss_id,loc_x,loc_y,loc_z,heading,respawn_time,currentHp,currentMp) values(?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE respawn_time=VALUES(respawn_time),currentHp=VALUES(currentHp),currentMp=VALUES(currentMp)";

    private static final String SAVE_RAIDBOSS = "UPDATE raidboss_spawnlist SET currentHP = ?, currentMP = ? WHERE boss_id = ?";

    protected final Map<Integer, BossSpawn> _spawns = new HashMap<>();

    public RaidBossManager() {
    }

    public static RaidBossManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void load() {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("SELECT * from raidboss_spawnlist ORDER BY boss_id");
                try {
                    ResultSet rs = ps.executeQuery();
                    try {
                        while (rs.next()) {
                            NpcTemplate template = NpcData.getInstance().getTemplate(rs.getInt("boss_id"));
                            if (template == null || !template.isType("RaidBoss")) {
                                LOGGER.warn("Couldn't load raidboss #{}.", rs.getInt("boss_id"));
                                continue;
                            }
                            L2Spawn spawn = new L2Spawn(template);
                            spawn.setLoc(rs.getInt("loc_x"), rs.getInt("loc_y"), rs.getInt("loc_z"), rs.getInt("heading"));
                            spawn.setRespawnMinDelay(rs.getInt("spawn_time"));
                            spawn.setRespawnMaxDelay(rs.getInt("random_time"));
                            addNewSpawn(spawn, rs.getLong("respawn_time"), rs.getDouble("currentHP"), rs.getDouble("currentMP"), false);
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
            LOGGER.error("Error restoring raid bosses.", e);
        }
        LOGGER.info("Loaded {} raid bosses.", this._spawns.size());
    }

    public void reload() {
        cleanUp(false);
        load();
    }

    public BossSpawn getBossSpawn(int id) {
        return this._spawns.get(id);
    }

    public BossStatus getStatus(int id) {
        BossSpawn bs = this._spawns.get(id);
        return (bs == null) ? BossStatus.UNDEFINED : bs.getStatus();
    }

    public void onDeath(RaidBoss boss) {
        BossSpawn bs = this._spawns.get(boss.getNpcId());
        if (bs != null)
            bs.onDeath();
    }

    public void addNewSpawn(L2Spawn spawn, long respawnTime, double currentHP, double currentMP, boolean forceSave) {
        if (spawn == null)
            return;
        int id = spawn.getNpcId();
        if (this._spawns.containsKey(id))
            return;
        long time = System.currentTimeMillis();
        SpawnTable.getInstance().addSpawn(spawn, false);
        BossSpawn bs = new BossSpawn();
        bs.setSpawn(spawn);
        if (respawnTime == 0L || time > respawnTime) {
            RaidBoss raidboss = (RaidBoss) spawn.doSpawn(false);
            currentHP = (currentHP == 0.0D) ? raidboss.getMaxHp() : currentHP;
            currentMP = (currentMP == 0.0D) ? raidboss.getMaxMp() : currentMP;
            raidboss.setCurrentHpMp(currentHP, currentMP);
            bs.setStatus(BossStatus.ALIVE);
            bs.setCurrentHp(currentHP);
            bs.setCurrentMp(currentMP);
            bs.setRespawnTime(0L);
            if (time > respawnTime || forceSave)
                try {
                    Connection con = ConnectionPool.getConnection();
                    try {
                        PreparedStatement ps = con.prepareStatement("INSERT INTO raidboss_spawnlist (boss_id,loc_x,loc_y,loc_z,heading,respawn_time,currentHp,currentMp) values(?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE respawn_time=VALUES(respawn_time),currentHp=VALUES(currentHp),currentMp=VALUES(currentMp)");
                        try {
                            ps.setInt(1, spawn.getNpcId());
                            ps.setInt(2, spawn.getLocX());
                            ps.setInt(3, spawn.getLocY());
                            ps.setInt(4, spawn.getLocZ());
                            ps.setInt(5, spawn.getHeading());
                            ps.setLong(6, respawnTime);
                            ps.setDouble(7, currentHP);
                            ps.setDouble(8, currentMP);
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
                    LOGGER.error("Couldn't store raid boss #{}.", e, id);
                }
        } else {
            long spawnTime = respawnTime - time;
            bs.setTask(ThreadPool.schedule(() -> bs.onSpawn(), spawnTime));
            bs.setStatus(BossStatus.DEAD);
            bs.setCurrentHp(0.0D);
            bs.setCurrentMp(0.0D);
            bs.setRespawnTime(respawnTime);
        }
        this._spawns.put(id, bs);
    }

    public void deleteSpawn(L2Spawn spawn) {
        if (spawn == null)
            return;
        int id = spawn.getNpcId();
        BossSpawn bs = this._spawns.remove(id);
        if (bs == null)
            return;
        bs.onDespawn();
        SpawnTable.getInstance().deleteSpawn(spawn, false);
    }

    public void cleanUp(boolean saveOnDb) {
        for (BossSpawn bs : this._spawns.values())
            bs.cancelTask();
        if (saveOnDb)
            try {
                Connection con = ConnectionPool.getConnection();
                try {
                    PreparedStatement ps = con.prepareStatement("UPDATE raidboss_spawnlist SET currentHP = ?, currentMP = ? WHERE boss_id = ?");
                    try {
                        for (Map.Entry<Integer, BossSpawn> entry : this._spawns.entrySet()) {
                            BossSpawn bs = entry.getValue();
                            if (bs.getStatus() == BossStatus.ALIVE) {
                                ps.setDouble(1, bs.getBoss().getCurrentHp());
                                ps.setDouble(2, bs.getBoss().getCurrentMp());
                                ps.setInt(3, (Integer) entry.getKey());
                                ps.addBatch();
                            }
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
                LOGGER.error("Couldn't save raid bosses.", e);
            }
        this._spawns.clear();
    }

    private static class SingletonHolder {
        protected static final RaidBossManager INSTANCE = new RaidBossManager();
    }
}
