package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.skills.Env;

public class ConditionTargetHpMinMax extends Condition {
    private final int _minHp;

    private final int _maxHp;

    public ConditionTargetHpMinMax(int minHp, int maxHp) {
        this._minHp = minHp;
        this._maxHp = maxHp;
    }

    public boolean testImpl(Env env) {
        if (env.getTarget() == null)
            return false;
        int _currentHp = (int) env.getTarget().getCurrentHp() * 100 / env.getTarget().getMaxHp();
        return (_currentHp >= this._minHp && _currentHp <= this._maxHp);
    }
}
