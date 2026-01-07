package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Creature;

public class MagicSkillUse extends L2GameServerPacket {
    private final int _targetId;

    private final int _skillId;

    private final int _skillLevel;

    private final int _hitTime;

    private final int _reuseDelay;

    private final int _charObjId;

    private final int _x;

    private final int _y;

    private final int _z;

    private final int _targetx;

    private final int _targety;

    private final int _targetz;

    private boolean _success = false;

    public MagicSkillUse(Creature cha, Creature target, int skillId, int skillLevel, int hitTime, int reuseDelay, boolean crit) {
        this(cha, target, skillId, skillLevel, hitTime, reuseDelay);
        this._success = crit;
    }

    public MagicSkillUse(Creature cha, Creature target, int skillId, int skillLevel, int hitTime, int reuseDelay) {
        this._charObjId = cha.getObjectId();
        this._targetId = target.getObjectId();
        this._skillId = skillId;
        this._skillLevel = skillLevel;
        this._hitTime = hitTime;
        this._reuseDelay = reuseDelay;
        this._x = cha.getX();
        this._y = cha.getY();
        this._z = cha.getZ();
        this._targetx = target.getX();
        this._targety = target.getY();
        this._targetz = target.getZ();
    }

    public MagicSkillUse(Creature cha, int skillId, int skillLevel, int hitTime, int reuseDelay) {
        this._charObjId = cha.getObjectId();
        this._targetId = cha.getTargetId();
        this._skillId = skillId;
        this._skillLevel = skillLevel;
        this._hitTime = hitTime;
        this._reuseDelay = reuseDelay;
        this._x = cha.getX();
        this._y = cha.getY();
        this._z = cha.getZ();
        this._targetx = cha.getX();
        this._targety = cha.getY();
        this._targetz = cha.getZ();
    }

    protected final void writeImpl() {
        writeC(72);
        writeD(this._charObjId);
        writeD(this._targetId);
        writeD(this._skillId);
        writeD(this._skillLevel);
        writeD(this._hitTime);
        writeD(this._reuseDelay);
        writeD(this._x);
        writeD(this._y);
        writeD(this._z);
        if (this._success) {
            writeD(1);
            writeH(0);
        } else {
            writeD(0);
        }
        writeD(this._targetx);
        writeD(this._targety);
        writeD(this._targetz);
    }
}
