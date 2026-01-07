package net.sf.l2j.gameserver.network.serverpackets;

public class TargetSelected extends L2GameServerPacket {
    private final int _objectId;

    private final int _targetObjId;

    private final int _x;

    private final int _y;

    private final int _z;

    public TargetSelected(int objectId, int targetId, int x, int y, int z) {
        this._objectId = objectId;
        this._targetObjId = targetId;
        this._x = x;
        this._y = y;
        this._z = z;
    }

    protected final void writeImpl() {
        writeC(41);
        writeD(this._objectId);
        writeD(this._targetObjId);
        writeD(this._x);
        writeD(this._y);
        writeD(this._z);
    }
}
