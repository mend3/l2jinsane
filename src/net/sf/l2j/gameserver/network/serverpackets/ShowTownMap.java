package net.sf.l2j.gameserver.network.serverpackets;

public class ShowTownMap extends L2GameServerPacket {
    private final String _texture;

    private final int _x;

    private final int _y;

    public ShowTownMap(String texture, int x, int y) {
        this._texture = texture;
        this._x = x;
        this._y = y;
    }

    protected final void writeImpl() {
        writeC(222);
        writeS(this._texture);
        writeD(this._x);
        writeD(this._y);
    }
}
