package net.sf.l2j.gameserver.network.serverpackets;

public class Snoop extends L2GameServerPacket {
    private final int _convoId;

    private final String _name;

    private final int _type;

    private final String _speaker;

    private final String _msg;

    public Snoop(int id, String name, int type, String speaker, String msg) {
        this._convoId = id;
        this._name = name;
        this._type = type;
        this._speaker = speaker;
        this._msg = msg;
    }

    protected void writeImpl() {
        writeC(213);
        writeD(this._convoId);
        writeS(this._name);
        writeD(0);
        writeD(this._type);
        writeS(this._speaker);
        writeS(this._msg);
    }
}
