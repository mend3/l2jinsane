package net.sf.l2j.gameserver.model.rift;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.spawn.L2Spawn;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DimensionalRiftRoom {
    public static final int Z_VALUE = -6752;

    private final List<L2Spawn> _spawns = new ArrayList<>();

    private final byte _type;

    private final byte _id;

    private final int _xMin;

    private final int _xMax;

    private final int _yMin;

    private final int _yMax;

    private final Location _teleportLoc;

    private final Shape _shape;

    private final boolean _isBossRoom;

    private boolean _partyInside;

    public DimensionalRiftRoom(byte type, StatSet set) {
        int xMin = set.getInteger("xMin");
        int xMax = set.getInteger("xMax");
        int yMin = set.getInteger("yMin");
        int yMax = set.getInteger("yMax");
        this._type = type;
        this._id = set.getByte("id");
        this._xMin = xMin + 128;
        this._xMax = xMax - 128;
        this._yMin = yMin + 128;
        this._yMax = yMax - 128;
        this._teleportLoc = new Location(set.getInteger("xT"), set.getInteger("yT"), -6752);
        this._isBossRoom = (this._id == 9);
        this._shape = new Polygon(new int[]{xMin, xMax, xMax, xMin}, new int[]{yMin, yMin, yMax, yMax}, 4);
    }

    public String toString() {
        return "RiftRoom #" + this._type + "_" + this._id + ", full: " + this._partyInside + ", tel: " + this._teleportLoc.toString() + ", spawns: " + this._spawns.size();
    }

    public byte getType() {
        return this._type;
    }

    public byte getId() {
        return this._id;
    }

    public int getRandomX() {
        return Rnd.get(this._xMin, this._xMax);
    }

    public int getRandomY() {
        return Rnd.get(this._yMin, this._yMax);
    }

    public Location getTeleportLoc() {
        return this._teleportLoc;
    }

    public boolean checkIfInZone(int x, int y, int z) {
        return (this._shape.contains(x, y) && z >= -6816 && z <= -6240);
    }

    public boolean isBossRoom() {
        return this._isBossRoom;
    }

    public List<L2Spawn> getSpawns() {
        return this._spawns;
    }

    public boolean isPartyInside() {
        return this._partyInside;
    }

    public void setPartyInside(boolean partyInside) {
        this._partyInside = partyInside;
    }

    public void spawn() {
        for (L2Spawn spawn : this._spawns) {
            spawn.doSpawn(false);
            spawn.setRespawnState(true);
        }
    }

    public void unspawn() {
        for (L2Spawn spawn : this._spawns) {
            spawn.setRespawnState(false);
            if (spawn.getNpc() != null)
                spawn.getNpc().deleteMe();
        }
        this._partyInside = false;
    }
}
