package net.sf.l2j.gameserver.network.serverpackets;

public class StopRotation extends L2GameServerPacket {
    private final int _charObjId;

    private final int _degree;

    private final int _speed;

    public StopRotation(int objid, int degree, int speed) {
        this._charObjId = objid;
        this._degree = degree;
        this._speed = speed;
    }

    protected final void writeImpl() {
        writeC(99);
        writeD(this._charObjId);
        writeD(this._degree);
        writeD(this._speed);
        writeC(this._degree);
    }
}
