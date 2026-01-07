package net.sf.l2j.gameserver.network.serverpackets;

public class RadarControl extends L2GameServerPacket {
    private final int _showRadar;

    private final int _type;

    private final int _x;

    private final int _y;

    private final int _z;

    public RadarControl(int showRadar, int type, int x, int y, int z) {
        this._showRadar = showRadar;
        this._type = type;
        this._x = x;
        this._y = y;
        this._z = z;
    }

    protected final void writeImpl() {
        writeC(235);
        writeD(this._showRadar);
        writeD(this._type);
        writeD(this._x);
        writeD(this._y);
        writeD(this._z);
    }
}
