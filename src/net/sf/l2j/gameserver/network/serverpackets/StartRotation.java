package net.sf.l2j.gameserver.network.serverpackets;

public class StartRotation extends L2GameServerPacket {
    private final int _charObjId;

    private final int _degree;

    private final int _side;

    private final int _speed;

    public StartRotation(int objId, int degree, int side, int speed) {
        this._charObjId = objId;
        this._degree = degree;
        this._side = side;
        this._speed = speed;
    }

    protected final void writeImpl() {
        writeC(98);
        writeD(this._charObjId);
        writeD(this._degree);
        writeD(this._side);
        writeD(this._speed);
    }
}
