package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.skills.Env;

public class ConditionSkillStats extends Condition {
    private final Stats _stat;

    public ConditionSkillStats(Stats stat) {
        this._stat = stat;
    }

    public boolean testImpl(Env env) {
        return (env.getSkill() != null && env.getSkill().getStat() == this._stat);
    }
}
