package net.sf.l2j.gameserver.network.serverpackets;

public class AskJoinPledge extends L2GameServerPacket {
    private final int _requestorObjId;

    private final String _pledgeName;

    public AskJoinPledge(int requestorObjId, String pledgeName) {
        this._requestorObjId = requestorObjId;
        this._pledgeName = pledgeName;
    }

    protected final void writeImpl() {
        writeC(50);
        writeD(this._requestorObjId);
        writeS(this._pledgeName);
    }
}
