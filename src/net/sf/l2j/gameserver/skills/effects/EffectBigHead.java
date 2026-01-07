package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.skills.AbnormalEffect;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.skills.Env;

public class EffectBigHead extends L2Effect {
    public EffectBigHead(Env env, EffectTemplate template) {
        super(env, template);
    }

    public L2EffectType getEffectType() {
        return L2EffectType.BUFF;
    }

    public boolean onStart() {
        getEffected().startAbnormalEffect(AbnormalEffect.BIG_HEAD);
        return true;
    }

    public void onExit() {
        getEffected().stopAbnormalEffect(AbnormalEffect.BIG_HEAD);
    }

    public boolean onActionTime() {
        return false;
    }
}
