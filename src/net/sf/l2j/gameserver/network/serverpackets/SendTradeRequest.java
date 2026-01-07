package net.sf.l2j.gameserver.network.serverpackets;

public class SendTradeRequest extends L2GameServerPacket {
    private final int _senderID;

    public SendTradeRequest(int senderID) {
        this._senderID = senderID;
    }

    protected final void writeImpl() {
        writeC(94);
        writeD(this._senderID);
    }
}
