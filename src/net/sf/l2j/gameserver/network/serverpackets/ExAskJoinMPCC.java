package net.sf.l2j.gameserver.network.serverpackets;

public class ExAskJoinMPCC extends L2GameServerPacket {
    private final String _requestorName;

    public ExAskJoinMPCC(String requestorName) {
        this._requestorName = requestorName;
    }

    protected void writeImpl() {
        writeC(254);
        writeH(39);
        writeS(this._requestorName);
    }
}
