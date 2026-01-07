package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Formulas;

public class EffectCancel extends L2Effect {
    public EffectCancel(Env env, EffectTemplate template) {
        super(env, template);
    }

    private static boolean cancel(Creature caster, Creature target, L2Effect effect) {
        if (!(target instanceof net.sf.l2j.gameserver.model.actor.Player) || target.isDead())
            return false;
        int cancelLvl = effect.getSkill().getMagicLevel();
        int count = effect.getSkill().getMaxNegatedEffects();
        double rate = effect.getEffectPower();
        double res = Formulas.calcSkillVulnerability(caster, target, effect.getSkill(), effect.getSkillType());
        rate *= res;
        int lastCanceledSkillId = 0;
        L2Effect[] effects = target.getAllEffects();
        int i;
        for (i = effects.length; --i >= 0; ) {
            L2Effect eff = effects[i];
            if (eff == null)
                continue;
            if (!eff.getSkill().isDance())
                continue;
            if (eff.getSkill().getId() == lastCanceledSkillId) {
                eff.exit();
                continue;
            }
            if (!calcCancelSuccess(eff, cancelLvl, (int) rate))
                continue;
            lastCanceledSkillId = eff.getSkill().getId();
            eff.exit();
            count--;
            if (count == 0)
                break;
        }
        if (count != 0) {
            lastCanceledSkillId = 0;
            for (i = effects.length; --i >= 0; ) {
                L2Effect eff = effects[i];
                if (eff == null)
                    continue;
                if (eff.getSkill().isDance())
                    continue;
                if (eff.getSkill().getId() == lastCanceledSkillId) {
                    eff.exit();
                    continue;
                }
                if (!calcCancelSuccess(eff, cancelLvl, (int) rate))
                    continue;
                lastCanceledSkillId = eff.getSkill().getId();
                eff.exit();
                count--;
                if (count == 0)
                    break;
            }
        }
        return true;
    }

    private static boolean calcCancelSuccess(L2Effect effect, int cancelLvl, int baseRate) {
        int rate = 2 * (cancelLvl - effect.getSkill().getMagicLevel());
        rate += effect.getPeriod() / 120;
        rate += baseRate;
        if (rate < 25) {
            rate = 25;
        } else if (rate > 75) {
            rate = 75;
        }
        return (Rnd.get(100) < rate);
    }

    public L2EffectType getEffectType() {
        return L2EffectType.CANCEL;
    }

    public boolean onStart() {
        return cancel(getEffector(), getEffected(), this);
    }

    public boolean onActionTime() {
        return false;
    }
}
