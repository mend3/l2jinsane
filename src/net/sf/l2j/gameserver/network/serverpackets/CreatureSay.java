package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.network.SystemMessageId;

import java.util.ArrayList;
import java.util.List;

public class CreatureSay extends L2GameServerPacket {
    private final int _objectId;

    private final int _textType;

    private String _charName = null;

    private int _charId = 0;

    private String _text = null;

    private int _npcString = -1;

    private List<String> _parameters;

    public CreatureSay(int objectId, int messageType, String charName, String text) {
        this._objectId = objectId;
        this._textType = messageType;
        this._charName = charName;
        this._text = text;
    }

    public CreatureSay(int objectId, int messageType, int charId, SystemMessageId sysString) {
        this._objectId = objectId;
        this._textType = messageType;
        this._charId = charId;
        this._npcString = sysString.getId();
    }

    public void addStringParameter(String text) {
        if (this._parameters == null)
            this._parameters = new ArrayList<>();
        this._parameters.add(text);
    }

    protected final void writeImpl() {
        writeC(74);
        writeD(this._objectId);
        writeD(this._textType);
        if (this._charName != null) {
            writeS(this._charName);
        } else {
            writeD(this._charId);
        }
        writeD(this._npcString);
        if (this._text != null) {
            writeS(this._text);
        } else if (this._parameters != null) {
            for (String s : this._parameters)
                writeS(s);
        }
    }
}
