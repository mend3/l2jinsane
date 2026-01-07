package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.skills.L2EffectFlag;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.skills.Env;

final class EffectStun extends L2Effect {
    public EffectStun(Env env, EffectTemplate template) {
        super(env, template);
    }

    public L2EffectType getEffectType() {
        return L2EffectType.STUN;
    }

    public boolean onStart() {
        getEffected().startStunning();
        return true;
    }

    public void onExit() {
        getEffected().stopStunning(false);
    }

    public boolean onActionTime() {
        return false;
    }

    public boolean onSameEffect(L2Effect effect) {
        return false;
    }

    public int getEffectFlags() {
        return L2EffectFlag.STUNNED.getMask();
    }
}
