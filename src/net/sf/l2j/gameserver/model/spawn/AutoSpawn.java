package net.sf.l2j.gameserver.model.spawn;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.location.SpawnLocation;

import java.util.ArrayList;
import java.util.List;

public class AutoSpawn {
    private final List<SpawnLocation> _locList = new ArrayList<>();
    private final List<Npc> _npcList = new ArrayList<>();
    protected int _objectId;
    protected int _npcId;
    protected int _initDelay;
    protected int _resDelay;
    protected int _desDelay;
    protected int _spawnCount = 1;
    protected int _lastLocIndex = -1;
    private boolean _spawnActive;
    private boolean _randomSpawn;
    private boolean _broadcastAnnouncement;

    public AutoSpawn(int npcId, int initDelay, int respawnDelay, int despawnDelay) {
        this._npcId = npcId;
        this._initDelay = initDelay;
        this._resDelay = respawnDelay;
        this._desDelay = despawnDelay;
    }

    public boolean addNpcInstance(Npc npcInst) {
        return this._npcList.add(npcInst);
    }

    public boolean removeNpcInstance(Npc npcInst) {
        return this._npcList.remove(npcInst);
    }

    public int getObjectId() {
        return this._objectId;
    }

    public void setObjectId(int objectId) {
        this._objectId = objectId;
    }

    public int getInitialDelay() {
        return this._initDelay;
    }

    public int getRespawnDelay() {
        return this._resDelay;
    }

    public int getDespawnDelay() {
        return this._desDelay;
    }

    public int getNpcId() {
        return this._npcId;
    }

    public int getSpawnCount() {
        return this._spawnCount;
    }

    public void setSpawnCount(int spawnCount) {
        this._spawnCount = spawnCount;
    }

    public int getLastLocIndex() {
        return this._lastLocIndex;
    }

    public void setLastLocIndex(int index) {
        this._lastLocIndex = index;
    }

    public SpawnLocation[] getLocationList() {
        return this._locList.toArray(new SpawnLocation[this._locList.size()]);
    }

    public Npc[] getNPCInstanceList() {
        synchronized (this._npcList) {
            Npc[] ret = new Npc[this._npcList.size()];
            this._npcList.toArray(ret);
            return ret;
        }
    }

    public L2Spawn[] getSpawns() {
        List<L2Spawn> npcSpawns = new ArrayList<>();

        for (Npc npcInst : this._npcList) {
            npcSpawns.add(npcInst.getSpawn());
        }

        return npcSpawns.toArray(new L2Spawn[npcSpawns.size()]);
    }

    public void setBroadcast(boolean broadcastValue) {
        this._broadcastAnnouncement = broadcastValue;
    }

    public boolean isSpawnActive() {
        return this._spawnActive;
    }

    public void setSpawnActive(boolean activeValue) {
        this._spawnActive = activeValue;
    }

    public boolean isRandomSpawn() {
        return this._randomSpawn;
    }

    public void setRandomSpawn(boolean randValue) {
        this._randomSpawn = randValue;
    }

    public boolean isBroadcasting() {
        return this._broadcastAnnouncement;
    }

    public boolean addSpawnLocation(int x, int y, int z, int heading) {
        return this._locList.add(new SpawnLocation(x, y, z, heading));
    }

    public boolean addSpawnLocation(int[] spawnLoc) {
        return spawnLoc.length != 3 ? false : this.addSpawnLocation(spawnLoc[0], spawnLoc[1], spawnLoc[2], -1);
    }
}
