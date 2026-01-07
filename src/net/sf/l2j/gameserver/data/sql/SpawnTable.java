/**/
package net.sf.l2j.gameserver.data.sql;

import net.sf.l2j.Config;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.data.manager.DayNightManager;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.spawn.L2Spawn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SpawnTable {
    private static final CLogger LOGGER = new CLogger(SpawnTable.class.getName());
    private static final String LOAD_SPAWNS = "SELECT * FROM spawnlist";
    private static final String ADD_SPAWN = "INSERT INTO spawnlist (npc_templateid,locx,locy,locz,heading,respawn_delay) values(?,?,?,?,?,?)";
    private static final String DELETE_SPAWN = "DELETE FROM spawnlist WHERE locx=? AND locy=? AND locz=? AND npc_templateid=? AND heading=?";
    private final Set<L2Spawn> _spawns = ConcurrentHashMap.newKeySet();

    protected SpawnTable() {
    }

    public static SpawnTable getInstance() {
        return SpawnTable.SingletonHolder.INSTANCE;
    }

    public void load() {
        if (Config.ALT_DEV_NO_SPAWNS) return;
        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM spawnlist");

                try {
                    ResultSet rs = ps.executeQuery();

                    try {
                        while (rs.next()) {
                            NpcTemplate template = NpcData.getInstance().getTemplate(rs.getInt("npc_templateid"));
                            if (template == null) {
                                LOGGER.warn("Invalid template {} found on spawn load.", rs.getInt("npc_templateid"));
                            } else if (!template.isType("SiegeGuard")) {
                                if (template.isType("RaidBoss")) {
                                    LOGGER.warn("RB template {} is in regular spawnlist, move it in raidboss_spawnlist.", template.getIdTemplate());
                                } else if ((Config.ALLOW_CLASS_MASTERS || !template.isType("ClassMaster")) && (Config.WYVERN_ALLOW_UPGRADER || !template.isType("WyvernManagerNpc"))) {
                                    L2Spawn spawnDat = new L2Spawn(template);
                                    spawnDat.setLoc(rs.getInt("locx"), rs.getInt("locy"), rs.getInt("locz"), rs.getInt("heading"));
                                    spawnDat.setRespawnDelay(rs.getInt("respawn_delay"));
                                    spawnDat.setRespawnRandom(rs.getInt("respawn_rand"));
                                    switch (rs.getInt("periodOfDay")) {
                                        case 0:
                                            spawnDat.setRespawnState(true);
                                            spawnDat.doSpawn(false);
                                            break;
                                        case 1:
                                            DayNightManager.getInstance().addDayCreature(spawnDat);
                                            break;
                                        case 2:
                                            DayNightManager.getInstance().addNightCreature(spawnDat);
                                    }

                                    this._spawns.add(spawnDat);
                                }
                            }
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
            LOGGER.error("Couldn't load spawns.", var12);
        }

        LOGGER.info("Loaded {} spawns.", this._spawns.size());
    }

    public void reload() {
        this._spawns.clear();
        this.load();
    }

    public Set<L2Spawn> getSpawns() {
        return this._spawns;
    }

    public void addSpawn(L2Spawn spawn, boolean storeInDb) {
        this._spawns.add(spawn);
        if (storeInDb) {
            try {
                Connection con = ConnectionPool.getConnection();

                try {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO spawnlist (npc_templateid,locx,locy,locz,heading,respawn_delay) values(?,?,?,?,?,?)");

                    try {
                        ps.setInt(1, spawn.getNpcId());
                        ps.setInt(2, spawn.getLocX());
                        ps.setInt(3, spawn.getLocY());
                        ps.setInt(4, spawn.getLocZ());
                        ps.setInt(5, spawn.getHeading());
                        ps.setInt(6, spawn.getRespawnDelay());
                        ps.execute();
                    } catch (Throwable var9) {
                        if (ps != null) {
                            try {
                                ps.close();
                            } catch (Throwable var8) {
                                var9.addSuppressed(var8);
                            }
                        }

                        throw var9;
                    }

                    if (ps != null) {
                        ps.close();
                    }
                } catch (Throwable var10) {
                    if (con != null) {
                        try {
                            con.close();
                        } catch (Throwable var7) {
                            var10.addSuppressed(var7);
                        }
                    }

                    throw var10;
                }

                if (con != null) {
                    con.close();
                }
            } catch (Exception var11) {
                LOGGER.error("Couldn't add spawn.", var11);
            }
        }

    }

    public void deleteSpawn(L2Spawn spawn, boolean updateDb) {
        if (this._spawns.remove(spawn)) {
            if (updateDb) {
                try {
                    Connection con = ConnectionPool.getConnection();

                    try {
                        PreparedStatement ps = con.prepareStatement("DELETE FROM spawnlist WHERE locx=? AND locy=? AND locz=? AND npc_templateid=? AND heading=?");

                        try {
                            ps.setInt(1, spawn.getLocX());
                            ps.setInt(2, spawn.getLocY());
                            ps.setInt(3, spawn.getLocZ());
                            ps.setInt(4, spawn.getNpcId());
                            ps.setInt(5, spawn.getHeading());
                            ps.execute();
                        } catch (Throwable var9) {
                            if (ps != null) {
                                try {
                                    ps.close();
                                } catch (Throwable var8) {
                                    var9.addSuppressed(var8);
                                }
                            }

                            throw var9;
                        }

                        if (ps != null) {
                            ps.close();
                        }
                    } catch (Throwable var10) {
                        if (con != null) {
                            try {
                                con.close();
                            } catch (Throwable var7) {
                                var10.addSuppressed(var7);
                            }
                        }

                        throw var10;
                    }

                    if (con != null) {
                        con.close();
                    }
                } catch (Exception var11) {
                    LOGGER.error("Couldn't delete spawn.", var11);
                }
            }

        }
    }

    private static class SingletonHolder {
        protected static final SpawnTable INSTANCE = new SpawnTable();
    }
}