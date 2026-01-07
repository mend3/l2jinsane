package net.sf.l2j.gameserver.network.serverpackets;

public class ExAutoSoulShot extends L2GameServerPacket {
    private final int _itemId;

    private final int _type;

    public ExAutoSoulShot(int itemId, int type) {
        this._itemId = itemId;
        this._type = type;
    }

    protected final void writeImpl() {
        writeC(254);
        writeH(18);
        writeD(this._itemId);
        writeD(this._type);
    }
}
