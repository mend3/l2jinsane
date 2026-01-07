package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.ChanceCondition;
import net.sf.l2j.gameserver.model.IChanceSkillTrigger;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.skills.Env;

public class EffectChanceSkillTrigger extends L2Effect implements IChanceSkillTrigger {
    private final int _triggeredId;

    private final int _triggeredLevel;

    private final ChanceCondition _chanceCondition;

    public EffectChanceSkillTrigger(Env env, EffectTemplate template) {
        super(env, template);
        this._triggeredId = template.triggeredId;
        this._triggeredLevel = template.triggeredLevel;
        this._chanceCondition = template.chanceCondition;
    }

    public L2EffectType getEffectType() {
        return L2EffectType.CHANCE_SKILL_TRIGGER;
    }

    public boolean onStart() {
        getEffected().addChanceTrigger(this);
        getEffected().onStartChanceEffect();
        return super.onStart();
    }

    public boolean onActionTime() {
        getEffected().onActionTimeChanceEffect();
        return false;
    }

    public void onExit() {
        if (getInUse() && getCount() == 0)
            getEffected().onExitChanceEffect();
        getEffected().removeChanceEffect(this);
        super.onExit();
    }

    public int getTriggeredChanceId() {
        return this._triggeredId;
    }

    public int getTriggeredChanceLevel() {
        return this._triggeredLevel;
    }

    public boolean triggersChanceSkill() {
        return (this._triggeredId > 1);
    }

    public ChanceCondition getTriggeredChanceCondition() {
        return this._chanceCondition;
    }
}
