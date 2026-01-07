package net.sf.l2j.gameserver.data.manager;

import net.sf.l2j.Config;
import net.sf.l2j.commons.pool.ConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class RaidBossInfoManager {
    private static final Logger _log = Logger.getLogger(RaidBossInfoManager.class.getName());

    private final Map<Integer, Long> _raidBosses = new ConcurrentHashMap<>();

    protected RaidBossInfoManager() {
    }

    public static RaidBossInfoManager getInstance() {
        return SingletonHolder._instance;
    }

    public void load() {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement statement = con.prepareStatement("SELECT boss_id, respawn_time FROM grandboss_data UNION SELECT boss_id, respawn_time FROM raidboss_spawnlist ORDER BY boss_id");
                try {
                    ResultSet rs = statement.executeQuery();
                    try {
                        while (rs.next()) {
                            int bossId = rs.getInt("boss_id");
                            if (Config.LIST_RAID_BOSS_IDS.contains(bossId))
                                this._raidBosses.put(bossId, rs.getLong("respawn_time"));
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
            _log.warning("Exception: RaidBossInfoManager load: " + e);
        }
        _log.info("RaidBossInfoManager: Loaded " + this._raidBosses.size() + " instances.");
    }

    public void updateRaidBossInfo(int bossId, long respawnTime) {
        this._raidBosses.put(bossId, respawnTime);
    }

    public long getRaidBossRespawnTime(int bossId) {
        return this._raidBosses.get(bossId);
    }

    private static class SingletonHolder {
        protected static final RaidBossInfoManager _instance = new RaidBossInfoManager();
    }
}
