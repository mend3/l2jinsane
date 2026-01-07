/**/
package net.sf.l2j.gameserver.enums.skills;

public enum SkillChangeType {
    Power(0, true, false),
    CastTime(1, true, true),
    Reuse(2, true, true),
    Chance(3, true, false),
    SkillBlow(4, true, false),
    MCrit(5, true, false),
    PCrit(6, true, false);

    public static final SkillChangeType[] VALUES = values();
    private final boolean _forceCheck;
    private final boolean _isOlyVsAll;
    private final int _changeId;

    SkillChangeType(int attackId, boolean ForceCheck, boolean IsOnlyVsAll) {
        this._changeId = attackId;
        this._forceCheck = ForceCheck;
        this._isOlyVsAll = IsOnlyVsAll;
    }

    public boolean isForceCheck() {
        return this._forceCheck;
    }

    public boolean isOnlyVsAll() {
        return this._isOlyVsAll;
    }

    public int getId() {
        return this._changeId;
    }
}
