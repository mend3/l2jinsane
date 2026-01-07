package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.skills.Env;

public class ConditionPlayerActiveEffectId extends Condition {
    private final int _effectId;

    private final int _effectLvl;

    public ConditionPlayerActiveEffectId(int effectId) {
        this._effectId = effectId;
        this._effectLvl = -1;
    }

    public ConditionPlayerActiveEffectId(int effectId, int effectLevel) {
        this._effectId = effectId;
        this._effectLvl = effectLevel;
    }

    public boolean testImpl(Env env) {
        L2Effect e = env.getCharacter().getFirstEffect(this._effectId);
        return e != null && (this._effectLvl == -1 || this._effectLvl <= e.getSkill().getLevel());
    }
}
