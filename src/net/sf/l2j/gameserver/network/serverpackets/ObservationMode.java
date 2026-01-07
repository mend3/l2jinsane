package net.sf.l2j.gameserver.network.serverpackets;

public class ObservationMode extends L2GameServerPacket {
    private final int _x;

    private final int _y;

    private final int _z;

    public ObservationMode(int x, int y, int z) {
        this._x = x;
        this._y = y;
        this._z = z;
    }

    protected final void writeImpl() {
        writeC(223);
        writeD(this._x);
        writeD(this._y);
        writeD(this._z);
        writeC(0);
        writeC(192);
        writeC(0);
    }
}
