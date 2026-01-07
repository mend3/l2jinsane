package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Creature;

public class ExFishingHpRegen extends L2GameServerPacket {
    private final Creature _activeChar;

    private final int _time;

    private final int _fishHP;

    private final int _hpMode;

    private final int _anim;

    private final int _goodUse;

    private final int _penalty;

    private final int _hpBarColor;

    public ExFishingHpRegen(Creature character, int time, int fishHP, int HPmode, int GoodUse, int anim, int penalty, int hpBarColor) {
        this._activeChar = character;
        this._time = time;
        this._fishHP = fishHP;
        this._hpMode = HPmode;
        this._goodUse = GoodUse;
        this._anim = anim;
        this._penalty = penalty;
        this._hpBarColor = hpBarColor;
    }

    protected void writeImpl() {
        writeC(254);
        writeH(22);
        writeD(this._activeChar.getObjectId());
        writeD(this._time);
        writeD(this._fishHP);
        writeC(this._hpMode);
        writeC(this._goodUse);
        writeC(this._anim);
        writeD(this._penalty);
        writeC(this._hpBarColor);
    }
}
