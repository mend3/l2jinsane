package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.skills.Env;

public class EffectTargetMe extends L2Effect {
    public EffectTargetMe(Env env, EffectTemplate template) {
        super(env, template);
    }

    public L2EffectType getEffectType() {
        return L2EffectType.TARGET_ME;
    }

    public boolean onStart() {
        if (getEffected() instanceof net.sf.l2j.gameserver.model.actor.Player) {
            if (getEffected().getTarget() == getEffector()) {
                getEffected().getAI().setIntention(IntentionType.ATTACK, getEffector());
            } else {
                getEffected().setTarget(getEffector());
            }
            return true;
        }
        return false;
    }

    public void onExit() {
    }

    public boolean onActionTime() {
        return false;
    }
}
