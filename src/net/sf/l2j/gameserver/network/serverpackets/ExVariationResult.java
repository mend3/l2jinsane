package net.sf.l2j.gameserver.network.serverpackets;

public class ExVariationResult extends L2GameServerPacket {
    private final int _stat12;

    private final int _stat34;

    private final int _unk3;

    public ExVariationResult(int unk1, int unk2, int unk3) {
        this._stat12 = unk1;
        this._stat34 = unk2;
        this._unk3 = unk3;
    }

    protected void writeImpl() {
        writeC(254);
        writeH(85);
        writeD(this._stat12);
        writeD(this._stat34);
        writeD(this._unk3);
    }
}
