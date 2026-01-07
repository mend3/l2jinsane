package net.sf.l2j.gameserver.network.serverpackets;

public class L2FriendSay extends L2GameServerPacket {
    private final String _sender;

    private final String _receiver;

    private final String _message;

    public L2FriendSay(String sender, String reciever, String message) {
        this._sender = sender;
        this._receiver = reciever;
        this._message = message;
    }

    protected final void writeImpl() {
        writeC(253);
        writeD(0);
        writeS(this._receiver);
        writeS(this._sender);
        writeS(this._message);
    }
}
