package net.sf.l2j.gameserver.network.serverpackets;

public class FriendAddRequest extends L2GameServerPacket {
    private final String _requestorName;

    public FriendAddRequest(String requestorName) {
        this._requestorName = requestorName;
    }

    protected final void writeImpl() {
        writeC(125);
        writeS(this._requestorName);
        writeD(0);
    }
}
