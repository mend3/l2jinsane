package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.skills.L2EffectFlag;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.skills.Env;

public class EffectStunSelf extends L2Effect {
    public EffectStunSelf(Env env, EffectTemplate template) {
        super(env, template);
    }

    public L2EffectType getEffectType() {
        return L2EffectType.STUN_SELF;
    }

    public boolean onStart() {
        getEffector().startStunning();
        return true;
    }

    public void onExit() {
        getEffector().stopStunning(false);
    }

    public boolean onActionTime() {
        return false;
    }

    public boolean isSelfEffectType() {
        return true;
    }

    public int getEffectFlags() {
        return L2EffectFlag.STUNNED.getMask();
    }
}
