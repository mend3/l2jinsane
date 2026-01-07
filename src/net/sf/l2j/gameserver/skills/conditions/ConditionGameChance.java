package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.skills.Env;

public class ConditionGameChance extends Condition {
    private final int _chance;

    public ConditionGameChance(int chance) {
        this._chance = chance;
    }

    public boolean testImpl(Env env) {
        return (Rnd.get(100) < this._chance);
    }
}
