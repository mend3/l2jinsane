/**/
package net.sf.l2j.gameserver.data.sql;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.xml.MapRegionData;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.location.SpawnLocation;
import net.sf.l2j.gameserver.model.spawn.AutoSpawn;
import net.sf.l2j.gameserver.model.spawn.L2Spawn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AutoSpawnTable {
    private static final CLogger LOGGER = new CLogger(AutoSpawnTable.class.getName());
    private static final int DEFAULT_INITIAL_SPAWN = 30000;
    private static final int DEFAULT_RESPAWN = 3600000;
    private static final int DEFAULT_DESPAWN = 3600000;
    private final Map<Integer, AutoSpawn> _registeredSpawns = new ConcurrentHashMap<>();
    private final Map<Integer, ScheduledFuture<?>> _runningSpawns = new ConcurrentHashMap<>();

    public static AutoSpawnTable getInstance() {
        return AutoSpawnTable.SingletonHolder.INSTANCE;
    }

    public void load() {
        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM random_spawn ORDER BY groupId ASC");

                try {
                    ResultSet rs = ps.executeQuery();

                    try {
                        while (rs.next()) {
                            AutoSpawn spawnInst = this.registerSpawn(rs.getInt("npcId"), rs.getInt("initialDelay"), rs.getInt("respawnDelay"), rs.getInt("despawnDelay"));
                            spawnInst.setSpawnCount(rs.getInt("count"));
                            spawnInst.setBroadcast(rs.getBoolean("broadcastSpawn"));
                            spawnInst.setRandomSpawn(rs.getBoolean("randomSpawn"));
                            PreparedStatement ps2 = con.prepareStatement("SELECT * FROM random_spawn_loc WHERE groupId=?");

                            try {
                                ps2.setInt(1, rs.getInt("groupId"));
                                ResultSet rs2 = ps2.executeQuery();

                                try {
                                    while (rs2.next()) {
                                        spawnInst.addSpawnLocation(rs2.getInt("x"), rs2.getInt("y"), rs2.getInt("z"), rs2.getInt("heading"));
                                    }
                                } catch (Throwable var14) {
                                    if (rs2 != null) {
                                        try {
                                            rs2.close();
                                        } catch (Throwable var13) {
                                            var14.addSuppressed(var13);
                                        }
                                    }

                                    throw var14;
                                }

                                if (rs2 != null) {
                                    rs2.close();
                                }
                            } catch (Throwable var15) {
                                if (ps2 != null) {
                                    try {
                                        ps2.close();
                                    } catch (Throwable var12) {
                                        var15.addSuppressed(var12);
                                    }
                                }

                                throw var15;
                            }

                            if (ps2 != null) {
                                ps2.close();
                            }
                        }
                    } catch (Throwable var16) {
                        if (rs != null) {
                            try {
                                rs.close();
                            } catch (Throwable var11) {
                                var16.addSuppressed(var11);
                            }
                        }

                        throw var16;
                    }

                    if (rs != null) {
                        rs.close();
                    }
                } catch (Throwable var17) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var10) {
                            var17.addSuppressed(var10);
                        }
                    }

                    throw var17;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (Throwable var18) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var9) {
                        var18.addSuppressed(var9);
                    }
                }

                throw var18;
            }

            if (con != null) {
                con.close();
            }
        } catch (Exception var19) {
            LOGGER.error("Couldn't restore auto spawn data.", var19);
        }

        LOGGER.info("Loaded {} auto spawns.", this._registeredSpawns.size());
    }

    public AutoSpawn registerSpawn(int npcId, int[][] spawnPoints, int initialDelay, int respawnDelay, int despawnDelay) {
        if (initialDelay < 0) {
            initialDelay = 30000;
        }

        if (respawnDelay < 0) {
            respawnDelay = 3600000;
        }

        if (despawnDelay < 0) {
            despawnDelay = 3600000;
        }

        AutoSpawn newSpawn = new AutoSpawn(npcId, initialDelay, respawnDelay, despawnDelay);
        if (spawnPoints != null) {
            int[][] var7 = spawnPoints;
            int var8 = spawnPoints.length;

            for (int var9 = 0; var9 < var8; ++var9) {
                int[] spawnPoint = var7[var9];
                newSpawn.addSpawnLocation(spawnPoint);
            }
        }

        int newId = IdFactory.getInstance().getNextId();
        newSpawn.setObjectId(newId);
        this._registeredSpawns.put(newId, newSpawn);
        this.setSpawnActive(newSpawn, true);
        return newSpawn;
    }

    public AutoSpawn registerSpawn(int npcId, int initialDelay, int respawnDelay, int despawnDelay) {
        return this.registerSpawn(npcId, null, initialDelay, respawnDelay, despawnDelay);
    }

    public boolean removeSpawn(AutoSpawn spawnInst) {
        if (!this.isSpawnRegistered(spawnInst)) {
            return false;
        } else {
            try {
                this._registeredSpawns.remove(spawnInst.getObjectId());
                ScheduledFuture<?> respawnTask = this._runningSpawns.remove(spawnInst.getObjectId());
                respawnTask.cancel(false);
                return true;
            } catch (Exception var3) {
                LOGGER.error("Couldn't auto spawn NPC {} (Object ID = {}).", var3, spawnInst.getNpcId(), spawnInst.getObjectId());
                return false;
            }
        }
    }

    public void setSpawnActive(AutoSpawn spawnInst, boolean isActive) {
        if (spawnInst != null) {
            int objectId = spawnInst.getObjectId();
            if (this.isSpawnRegistered(objectId)) {
                ScheduledFuture<?> spawnTask = null;
                if (isActive) {
                    AutoSpawnTable.AutoSpawner rs = new AutoSpawnTable.AutoSpawner(objectId);
                    if (spawnInst.getDespawnDelay() > 0) {
                        spawnTask = ThreadPool.scheduleAtFixedRate(rs, spawnInst.getInitialDelay(), spawnInst.getRespawnDelay());
                    } else {
                        spawnTask = ThreadPool.schedule(rs, spawnInst.getInitialDelay());
                    }

                    this._runningSpawns.put(objectId, spawnTask);
                } else {
                    spawnTask = this._runningSpawns.remove(objectId);
                    if (spawnTask != null) {
                        spawnTask.cancel(false);
                    }

                    ThreadPool.execute(new AutoSpawnTable.AutoDespawner(objectId));
                }

                spawnInst.setSpawnActive(isActive);
            }

        }
    }

    public final long getTimeToNextSpawn(AutoSpawn spawnInst) {
        int objectId = spawnInst.getObjectId();
        return !this.isSpawnRegistered(objectId) ? -1L : this._runningSpawns.get(objectId).getDelay(TimeUnit.MILLISECONDS);
    }

    public final AutoSpawn getAutoSpawnInstance(int id, boolean isObjectId) {
        if (isObjectId) {
            if (this.isSpawnRegistered(id)) {
                return this._registeredSpawns.get(id);
            }
        } else {

            for (AutoSpawn spawnInst : this._registeredSpawns.values()) {
                if (spawnInst.getNpcId() == id) {
                    return spawnInst;
                }
            }
        }

        return null;
    }

    public Map<Integer, AutoSpawn> getAutoSpawnInstances(int npcId) {
        Map<Integer, AutoSpawn> spawnInstList = new HashMap<>();

        for (AutoSpawn spawnInst : this._registeredSpawns.values()) {
            if (spawnInst.getNpcId() == npcId) {
                spawnInstList.put(spawnInst.getObjectId(), spawnInst);
            }
        }

        return spawnInstList;
    }

    public final boolean isSpawnRegistered(int objectId) {
        return this._registeredSpawns.containsKey(objectId);
    }

    public final boolean isSpawnRegistered(AutoSpawn spawnInst) {
        return this._registeredSpawns.containsValue(spawnInst);
    }

    private static class SingletonHolder {
        protected static final AutoSpawnTable INSTANCE = new AutoSpawnTable();
    }

    private class AutoSpawner implements Runnable {
        private final int _objectId;

        protected AutoSpawner(int objectId) {
            this._objectId = objectId;
        }

        public void run() {
            try {
                AutoSpawn spawnInst = AutoSpawnTable.this._registeredSpawns.get(this._objectId);
                if (!spawnInst.isSpawnActive()) {
                    return;
                }

                SpawnLocation[] locationList = spawnInst.getLocationList();
                if (locationList.length == 0) {
                    AutoSpawnTable.LOGGER.warn("No coords specified for spawn instance (Object ID = {}).", this._objectId);
                    return;
                }

                int locationCount = locationList.length;
                int locationIndex = Rnd.get(locationCount);
                if (!spawnInst.isRandomSpawn()) {
                    locationIndex = spawnInst.getLastLocIndex();
                    ++locationIndex;
                    if (locationIndex == locationCount) {
                        locationIndex = 0;
                    }

                    spawnInst.setLastLocIndex(locationIndex);
                }

                int x = locationList[locationIndex].getX();
                int y = locationList[locationIndex].getY();
                int z = locationList[locationIndex].getZ();
                int heading = locationList[locationIndex].getHeading();
                NpcTemplate template = NpcData.getInstance().getTemplate(spawnInst.getNpcId());
                if (template == null) {
                    AutoSpawnTable.LOGGER.warn("Couldn't find npc template for id: {}.", spawnInst.getNpcId());
                    return;
                }

                L2Spawn newSpawn = new L2Spawn(template);
                newSpawn.setLoc(x, y, z, heading);
                if (spawnInst.getDespawnDelay() == 0) {
                    newSpawn.setRespawnDelay(spawnInst.getRespawnDelay());
                }

                SpawnTable.getInstance().addSpawn(newSpawn, false);
                Npc npcInst = null;
                if (spawnInst.getSpawnCount() == 1) {
                    npcInst = newSpawn.doSpawn(false);
                    npcInst.setXYZ(npcInst.getX(), npcInst.getY(), npcInst.getZ());
                    spawnInst.addNpcInstance(npcInst);
                } else {
                    for (int i = 0; i < spawnInst.getSpawnCount(); ++i) {
                        npcInst = newSpawn.doSpawn(false);
                        npcInst.setXYZ(npcInst.getX() + Rnd.get(50), npcInst.getY() + Rnd.get(50), npcInst.getZ());
                        spawnInst.addNpcInstance(npcInst);
                    }
                }

                if (npcInst != null && spawnInst.isBroadcasting()) {
                    World.announceToOnlinePlayers("The " + npcInst.getName() + " has spawned near " + MapRegionData.getInstance().getClosestTownName(npcInst.getX(), npcInst.getY()) + "!");
                    LOGGER.info(String.format("%s has spawned in %s,%s,%s", npcInst.getName(), npcInst.getX(), npcInst.getY(), npcInst.getZ()));
                }

                if (spawnInst.getDespawnDelay() > 0) {
                    ThreadPool.schedule(AutoSpawnTable.this.new AutoDespawner(this._objectId), spawnInst.getDespawnDelay() - 1000);
                }
            } catch (Exception var13) {
                AutoSpawnTable.LOGGER.error("Couldn't spawn (Object ID = {}).", var13, this._objectId);
            }

        }
    }

    private class AutoDespawner implements Runnable {
        private final int _objectId;

        protected AutoDespawner(int objectId) {
            this._objectId = objectId;
        }

        public void run() {
            try {
                AutoSpawn spawnInst = AutoSpawnTable.this._registeredSpawns.get(this._objectId);
                if (spawnInst == null) {
                    AutoSpawnTable.LOGGER.info("No spawn registered for object ID = {}.", this._objectId);
                    return;
                }

                Npc[] var2 = spawnInst.getNPCInstanceList();
                int var3 = var2.length;

                for (Npc npcInst : var2) {
                    if (npcInst != null) {
                        SpawnTable.getInstance().deleteSpawn(npcInst.getSpawn(), false);
                        npcInst.deleteMe();
                        spawnInst.removeNpcInstance(npcInst);
                    }
                }
            } catch (Exception var6) {
                AutoSpawnTable.LOGGER.error("Couldn't despawn (Object ID = {}).", var6, this._objectId);
            }

        }
    }
}