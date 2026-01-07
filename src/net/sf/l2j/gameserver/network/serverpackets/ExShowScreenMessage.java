/**/
package net.sf.l2j.gameserver.network.serverpackets;

public class ExShowScreenMessage extends L2GameServerPacket {
    private final int _type;
    private final int _sysMessageId;
    private final boolean _hide;
    private final int _unk2;
    private final int _unk3;
    private final boolean _fade;
    private final int _size;
    private final int _position;
    private final boolean _effect;
    private final String _text;
    private final int _time;

    public ExShowScreenMessage(String text, int time) {
        this._type = 1;
        this._sysMessageId = -1;
        this._hide = false;
        this._unk2 = 0;
        this._unk3 = 0;
        this._fade = false;
        this._position = 2;
        this._text = text;
        this._time = time;
        this._size = 0;
        this._effect = false;
    }

    public ExShowScreenMessage(String text, int time, ExShowScreenMessage.SMPOS pos, boolean effect) {
        this(text, time, pos.ordinal(), effect);
    }

    public ExShowScreenMessage(String text, int time, int pos, boolean effect) {
        this._type = 1;
        this._sysMessageId = -1;
        this._hide = false;
        this._unk2 = 0;
        this._unk3 = 0;
        this._fade = false;
        this._position = pos;
        this._text = text;
        this._time = time;
        this._size = 0;
        this._effect = effect;
    }

    public ExShowScreenMessage(int type, int messageId, int position, boolean hide, int size, int unk2, int unk3, boolean showEffect, int time, boolean fade, String text) {
        this._type = type;
        this._sysMessageId = messageId;
        this._hide = hide;
        this._unk2 = unk2;
        this._unk3 = unk3;
        this._fade = fade;
        this._position = position;
        this._text = text;
        this._time = time;
        this._size = size;
        this._effect = showEffect;
    }

    protected void writeImpl() {
        this.writeC(254);
        this.writeH(56);
        this.writeD(this._type);
        this.writeD(this._sysMessageId);
        this.writeD(this._position);
        this.writeD(this._hide ? 1 : 0);
        this.writeD(this._size);
        this.writeD(this._unk2);
        this.writeD(this._unk3);
        this.writeD(this._effect ? 1 : 0);
        this.writeD(this._time);
        this.writeD(this._fade ? 1 : 0);
        this.writeS(this._text);
    }

    public enum SMPOS {
        DUMMY,
        TOP_LEFT,
        TOP_CENTER,
        TOP_RIGHT,
        MIDDLE_LEFT,
        MIDDLE_CENTER,
        MIDDLE_RIGHT,
        BOTTOM_CENTER,
        BOTTOM_RIGHT;

        // $FF: synthetic method
        private static ExShowScreenMessage.SMPOS[] $values() {
            return new ExShowScreenMessage.SMPOS[]{DUMMY, TOP_LEFT, TOP_CENTER, TOP_RIGHT, MIDDLE_LEFT, MIDDLE_CENTER, MIDDLE_RIGHT, BOTTOM_CENTER, BOTTOM_RIGHT};
        }
    }
}