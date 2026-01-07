package net.sf.l2j.gameserver.network.serverpackets;

public class ExConfirmVariationItem extends L2GameServerPacket {
    private final int _itemObjId;

    private final int _unk1;

    private final int _unk2;

    public ExConfirmVariationItem(int itemObjId) {
        this._itemObjId = itemObjId;
        this._unk1 = 1;
        this._unk2 = 1;
    }

    protected void writeImpl() {
        writeC(254);
        writeH(82);
        writeD(this._itemObjId);
        writeD(this._unk1);
        writeD(this._unk2);
    }
}
