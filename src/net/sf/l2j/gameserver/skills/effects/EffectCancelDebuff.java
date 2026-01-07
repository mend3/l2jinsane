package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Formulas;

public class EffectCancelDebuff extends L2Effect {
    public EffectCancelDebuff(Env env, EffectTemplate template) {
        super(env, template);
    }

    private static boolean cancel(Creature caster, Creature target, L2Skill skill, L2SkillType effectType) {
        if (!(target instanceof net.sf.l2j.gameserver.model.actor.Player) || target.isDead())
            return false;
        int cancelLvl = skill.getMagicLevel();
        int count = skill.getMaxNegatedEffects();
        double baseRate = Formulas.calcSkillVulnerability(caster, target, skill, effectType);
        int lastCanceledSkillId = 0;
        L2Effect[] effects = target.getAllEffects();
        int i;
        for (i = effects.length; --i >= 0; ) {
            L2Effect effect = effects[i];
            if (effect == null)
                continue;
            if (!effect.getSkill().isDebuff() || !effect.getSkill().canBeDispeled()) {
                effects[i] = null;
                continue;
            }
            if (effect.getSkill().getId() == lastCanceledSkillId) {
                effect.exit();
                continue;
            }
            if (!calcCancelSuccess(effect, cancelLvl, (int) baseRate))
                continue;
            lastCanceledSkillId = effect.getSkill().getId();
            effect.exit();
            count--;
            if (count == 0)
                break;
        }
        if (count != 0) {
            lastCanceledSkillId = 0;
            for (i = effects.length; --i >= 0; ) {
                L2Effect effect = effects[i];
                if (effect == null)
                    continue;
                if (!effect.getSkill().isDebuff() || !effect.getSkill().canBeDispeled()) {
                    effects[i] = null;
                    continue;
                }
                if (effect.getSkill().getId() == lastCanceledSkillId) {
                    effect.exit();
                    continue;
                }
                if (!calcCancelSuccess(effect, cancelLvl, (int) baseRate))
                    continue;
                lastCanceledSkillId = effect.getSkill().getId();
                effect.exit();
                count--;
                if (count == 0)
                    break;
            }
        }
        return true;
    }

    private static boolean calcCancelSuccess(L2Effect effect, int cancelLvl, int baseRate) {
        int rate = 2 * (cancelLvl - effect.getSkill().getMagicLevel());
        rate += (effect.getPeriod() - effect.getTime()) / 1200;
        rate *= baseRate;
        if (rate < 25) {
            rate = 25;
        } else if (rate > 75) {
            rate = 75;
        }
        return (Rnd.get(100) < rate);
    }

    public L2EffectType getEffectType() {
        return L2EffectType.CANCEL_DEBUFF;
    }

    public boolean onStart() {
        return cancel(getEffector(), getEffected(), getSkill(), (getEffectTemplate()).effectType);
    }

    public boolean onActionTime() {
        return false;
    }
}
