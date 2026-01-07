package net.sf.l2j.gameserver.network.serverpackets;

public class MyTargetSelected extends L2GameServerPacket {
    private final int _objectId;

    private final int _color;

    public MyTargetSelected(int objectId, int color) {
        this._objectId = objectId;
        this._color = color;
    }

    protected final void writeImpl() {
        writeC(166);
        writeD(this._objectId);
        writeH(this._color);
    }
}
