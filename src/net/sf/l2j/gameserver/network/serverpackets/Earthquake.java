package net.sf.l2j.gameserver.network.serverpackets;

public class Earthquake extends L2GameServerPacket {
    private final int _x;

    private final int _y;

    private final int _z;

    private final int _intensity;

    private final int _duration;

    public Earthquake(int x, int y, int z, int intensity, int duration) {
        this._x = x;
        this._y = y;
        this._z = z;
        this._intensity = intensity;
        this._duration = duration;
    }

    protected final void writeImpl() {
        writeC(196);
        writeD(this._x);
        writeD(this._y);
        writeD(this._z);
        writeD(this._intensity);
        writeD(this._duration);
        writeD(0);
    }
}
