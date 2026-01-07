package net.sf.l2j.gameserver.network.serverpackets;

public class ExFishingEnd extends L2GameServerPacket {
    private final boolean _win;

    private final int _playerId;

    public ExFishingEnd(boolean win, int playerId) {
        this._win = win;
        this._playerId = playerId;
    }

    protected void writeImpl() {
        writeC(254);
        writeH(20);
        writeD(this._playerId);
        writeC(this._win ? 1 : 0);
    }
}
