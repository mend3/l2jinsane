package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.skills.Env;

public class EffectRemoveTarget extends L2Effect {
    public EffectRemoveTarget(Env env, EffectTemplate template) {
        super(env, template);
    }

    public L2EffectType getEffectType() {
        return L2EffectType.REMOVE_TARGET;
    }

    public boolean onStart() {
        getEffected().setTarget(null);
        getEffected().abortAttack();
        getEffected().abortCast();
        getEffected().getAI().setIntention(IntentionType.IDLE, getEffector());
        return true;
    }

    public void onExit() {
    }

    public boolean onActionTime() {
        return false;
    }
}
