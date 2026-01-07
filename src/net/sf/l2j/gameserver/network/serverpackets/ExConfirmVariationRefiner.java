package net.sf.l2j.gameserver.network.serverpackets;

public class ExConfirmVariationRefiner extends L2GameServerPacket {
    private final int _refinerItemObjId;

    private final int _lifestoneItemId;

    private final int _gemstoneItemId;

    private final int _gemstoneCount;

    private final int _unk2;

    public ExConfirmVariationRefiner(int refinerItemObjId, int lifeStoneId, int gemstoneItemId, int gemstoneCount) {
        this._refinerItemObjId = refinerItemObjId;
        this._lifestoneItemId = lifeStoneId;
        this._gemstoneItemId = gemstoneItemId;
        this._gemstoneCount = gemstoneCount;
        this._unk2 = 1;
    }

    protected void writeImpl() {
        writeC(254);
        writeH(83);
        writeD(this._refinerItemObjId);
        writeD(this._lifestoneItemId);
        writeD(this._gemstoneItemId);
        writeD(this._gemstoneCount);
        writeD(this._unk2);
    }
}
