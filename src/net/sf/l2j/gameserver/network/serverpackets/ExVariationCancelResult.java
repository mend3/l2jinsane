package net.sf.l2j.gameserver.network.serverpackets;

public class ExVariationCancelResult extends L2GameServerPacket {
    private final int _closeWindow;

    private final int _unk1;

    public ExVariationCancelResult(int result) {
        this._closeWindow = 1;
        this._unk1 = result;
    }

    protected void writeImpl() {
        writeC(254);
        writeH(87);
        writeD(this._closeWindow);
        writeD(this._unk1);
    }
}
