package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.skills.Env;

public class EffectInvincible extends L2Effect {
    public EffectInvincible(Env env, EffectTemplate template) {
        super(env, template);
    }

    public L2EffectType getEffectType() {
        return L2EffectType.INVINCIBLE;
    }

    public boolean onStart() {
        getEffected().setIsInvul(true);
        return super.onStart();
    }

    public boolean onActionTime() {
        return false;
    }

    public void onExit() {
        getEffected().setIsInvul(false);
        super.onExit();
    }
}
