package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.skills.Env;

public class ConditionPlayerActiveSkillId extends Condition {
    private final int _skillId;

    private final int _skillLevel;

    public ConditionPlayerActiveSkillId(int skillId) {
        this._skillId = skillId;
        this._skillLevel = -1;
    }

    public ConditionPlayerActiveSkillId(int skillId, int skillLevel) {
        this._skillId = skillId;
        this._skillLevel = skillLevel;
    }

    public boolean testImpl(Env env) {
        L2Skill skill = env.getCharacter().getSkill(this._skillId);
        return (skill != null && this._skillLevel <= skill.getLevel());
    }
}
