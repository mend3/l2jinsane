package net.sf.l2j.gameserver.network.serverpackets;

public class ExOlympiadMode extends L2GameServerPacket {
    private final int _mode;

    public ExOlympiadMode(int mode) {
        this._mode = mode;
    }

    protected final void writeImpl() {
        writeC(254);
        writeH(43);
        writeC(this._mode);
    }
}
