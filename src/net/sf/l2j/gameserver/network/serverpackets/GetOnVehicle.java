package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.location.SpawnLocation;

public class GetOnVehicle extends L2GameServerPacket {
    private final int _objectId;

    private final int _boatId;

    private final int _x;

    private final int _y;

    private final int _z;

    public GetOnVehicle(int objectId, int boatId, int x, int y, int z) {
        this._objectId = objectId;
        this._boatId = boatId;
        this._x = x;
        this._y = y;
        this._z = z;
    }

    public GetOnVehicle(int objectId, int boatId, SpawnLocation loc) {
        this._objectId = objectId;
        this._boatId = boatId;
        this._x = loc.getX();
        this._y = loc.getY();
        this._z = loc.getZ();
    }

    protected void writeImpl() {
        writeC(92);
        writeD(this._objectId);
        writeD(this._boatId);
        writeD(this._x);
        writeD(this._y);
        writeD(this._z);
    }
}
