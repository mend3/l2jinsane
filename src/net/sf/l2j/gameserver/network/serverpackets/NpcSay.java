package net.sf.l2j.gameserver.network.serverpackets;

public final class NpcSay extends L2GameServerPacket {
    private final int _objectId;

    private final int _textType;

    private final int _npcId;

    private final String _text;

    public NpcSay(int objectId, int messageType, int npcId, String text) {
        this._objectId = objectId;
        this._textType = messageType;
        this._npcId = 1000000 + npcId;
        this._text = text;
    }

    protected void writeImpl() {
        writeC(2);
        writeD(this._objectId);
        writeD(this._textType);
        writeD(this._npcId);
        writeS(this._text);
    }
}
