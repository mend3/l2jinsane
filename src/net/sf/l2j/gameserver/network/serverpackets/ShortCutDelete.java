package net.sf.l2j.gameserver.network.serverpackets;

public class ShortCutDelete extends L2GameServerPacket {
    private final int _slot;

    public ShortCutDelete(int slot) {
        this._slot = slot;
    }

    protected final void writeImpl() {
        writeC(70);
        writeD(this._slot);
        writeD(0);
    }
}
