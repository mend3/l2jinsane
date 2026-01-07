package net.sf.l2j.gameserver.network.serverpackets;

public class ExDuelEnd extends L2GameServerPacket {
    private final int _isPartyDuel;

    public ExDuelEnd(boolean isPartyDuel) {
        this._isPartyDuel = isPartyDuel ? 1 : 0;
    }

    protected void writeImpl() {
        writeC(254);
        writeH(78);
        writeD(this._isPartyDuel);
    }
}
