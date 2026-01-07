package net.sf.l2j.gameserver.network.serverpackets;

public class AskJoinAlly extends L2GameServerPacket {
    private final String _requestorName;

    private final int _requestorObjId;

    public AskJoinAlly(int requestorObjId, String requestorName) {
        this._requestorName = requestorName;
        this._requestorObjId = requestorObjId;
    }

    protected final void writeImpl() {
        writeC(168);
        writeD(this._requestorObjId);
        writeS(this._requestorName);
    }
}
