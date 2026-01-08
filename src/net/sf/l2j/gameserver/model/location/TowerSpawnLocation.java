package net.sf.l2j.gameserver.model.location;

import java.util.ArrayList;
import java.util.List;

public class TowerSpawnLocation extends SpawnLocation {
    private final int _npcId;
    private List<Integer> _zoneList;
    private int _upgradeLevel;

    public TowerSpawnLocation(int npcId, SpawnLocation location) {
        super(location);
        this._npcId = npcId;
    }

    public TowerSpawnLocation(int npcId, SpawnLocation location, String[] zoneList) {
        super(location);
        this._npcId = npcId;
        this._zoneList = new ArrayList<>();

        for (String zoneId : zoneList) {
            this._zoneList.add(Integer.parseInt(zoneId));
        }

    }

    public int getId() {
        return this._npcId;
    }

    public List<Integer> getZoneList() {
        return this._zoneList;
    }

    public int getUpgradeLevel() {
        return this._upgradeLevel;
    }

    public void setUpgradeLevel(int level) {
        this._upgradeLevel = level;
    }
}
