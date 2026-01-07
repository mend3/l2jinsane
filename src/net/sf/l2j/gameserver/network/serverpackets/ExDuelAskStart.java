package net.sf.l2j.gameserver.network.serverpackets;

public class ExDuelAskStart extends L2GameServerPacket {
    private final String _requestor;

    private final int _isPartyDuel;

    public ExDuelAskStart(String requestor, boolean isPartyDuel) {
        this._requestor = requestor;
        this._isPartyDuel = isPartyDuel ? 1 : 0;
    }

    protected void writeImpl() {
        writeC(254);
        writeH(75);
        writeS(this._requestor);
        writeD(this._isPartyDuel);
    }
}
