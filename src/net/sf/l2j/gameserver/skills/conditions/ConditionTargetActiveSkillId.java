package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.skills.Env;

public class ConditionTargetActiveSkillId extends Condition {
    private final int _skillId;

    public ConditionTargetActiveSkillId(int skillId) {
        this._skillId = skillId;
    }

    public boolean testImpl(Env env) {
        return (env.getTarget().getSkill(this._skillId) != null);
    }
}
