package net.sf.l2j.gameserver.network.serverpackets;

public class ExDuelReady extends L2GameServerPacket {
    private final int _isPartyDuel;

    public ExDuelReady(boolean isPartyDuel) {
        this._isPartyDuel = isPartyDuel ? 1 : 0;
    }

    protected void writeImpl() {
        writeC(254);
        writeH(76);
        writeD(this._isPartyDuel);
    }
}
