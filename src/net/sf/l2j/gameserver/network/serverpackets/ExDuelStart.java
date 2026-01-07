package net.sf.l2j.gameserver.network.serverpackets;

public class ExDuelStart extends L2GameServerPacket {
    private final int _isPartyDuel;

    public ExDuelStart(boolean isPartyDuel) {
        this._isPartyDuel = isPartyDuel ? 1 : 0;
    }

    protected void writeImpl() {
        writeC(254);
        writeH(77);
        writeD(this._isPartyDuel);
    }
}
