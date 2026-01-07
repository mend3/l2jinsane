package net.sf.l2j.gameserver.model;

import net.sf.l2j.commons.util.StatSet;

public class NewbieBuff {
    private final int _lowerLevel;

    private final int _upperLevel;

    private final int _skillId;

    private final int _skillLevel;

    private final boolean _isMagicClass;

    public NewbieBuff(StatSet set) {
        this._lowerLevel = set.getInteger("lowerLevel");
        this._upperLevel = set.getInteger("upperLevel");
        this._skillId = set.getInteger("skillId");
        this._skillLevel = set.getInteger("skillLevel");
        this._isMagicClass = set.getBool("isMagicClass");
    }

    public int getLowerLevel() {
        return this._lowerLevel;
    }

    public int getUpperLevel() {
        return this._upperLevel;
    }

    public int getSkillId() {
        return this._skillId;
    }

    public int getSkillLevel() {
        return this._skillLevel;
    }

    public boolean isMagicClassBuff() {
        return this._isMagicClass;
    }
}
