package net.sf.l2j.gameserver.network.serverpackets;

public class ExConfirmVariationGemstone extends L2GameServerPacket {
    private final int _gemstoneObjId;

    private final int _unk1;

    private final int _gemstoneCount;

    private final int _unk2;

    private final int _unk3;

    public ExConfirmVariationGemstone(int gemstoneObjId, int count) {
        this._gemstoneObjId = gemstoneObjId;
        this._unk1 = 1;
        this._gemstoneCount = count;
        this._unk2 = 1;
        this._unk3 = 1;
    }

    protected void writeImpl() {
        writeC(254);
        writeH(84);
        writeD(this._gemstoneObjId);
        writeD(this._unk1);
        writeD(this._gemstoneCount);
        writeD(this._unk2);
        writeD(this._unk3);
    }
}
