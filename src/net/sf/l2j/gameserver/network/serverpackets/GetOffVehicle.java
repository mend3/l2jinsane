package net.sf.l2j.gameserver.network.serverpackets;

public class GetOffVehicle extends L2GameServerPacket {
    private final int _charObjId;

    private final int _boatObjId;

    private final int _x;

    private final int _y;

    private final int _z;

    public GetOffVehicle(int charObjId, int boatObjId, int x, int y, int z) {
        this._charObjId = charObjId;
        this._boatObjId = boatObjId;
        this._x = x;
        this._y = y;
        this._z = z;
    }

    protected void writeImpl() {
        writeC(93);
        writeD(this._charObjId);
        writeD(this._boatObjId);
        writeD(this._x);
        writeD(this._y);
        writeD(this._z);
    }
}
