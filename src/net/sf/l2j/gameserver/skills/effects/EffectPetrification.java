package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.skills.AbnormalEffect;
import net.sf.l2j.gameserver.enums.skills.L2EffectFlag;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.skills.Env;

public class EffectPetrification extends L2Effect {
    public EffectPetrification(Env env, EffectTemplate template) {
        super(env, template);
    }

    public L2EffectType getEffectType() {
        return L2EffectType.PETRIFICATION;
    }

    public boolean onStart() {
        getEffected().startAbnormalEffect(AbnormalEffect.HOLD_2);
        getEffected().startParalyze();
        getEffected().setIsInvul(true);
        return true;
    }

    public void onExit() {
        getEffected().stopAbnormalEffect(AbnormalEffect.HOLD_2);
        getEffected().stopParalyze();
        getEffected().setIsInvul(false);
    }

    public boolean onActionTime() {
        return false;
    }

    public int getEffectFlags() {
        return L2EffectFlag.PARALYZED.getMask();
    }
}
