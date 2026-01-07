package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.skills.L2EffectFlag;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.skills.Env;

final class EffectRoot extends L2Effect {
    public EffectRoot(Env env, EffectTemplate template) {
        super(env, template);
    }

    public L2EffectType getEffectType() {
        return L2EffectType.ROOT;
    }

    public boolean onStart() {
        getEffected().startRooted();
        return true;
    }

    public void onExit() {
        getEffected().stopRooting(false);
    }

    public boolean onActionTime() {
        return false;
    }

    public boolean onSameEffect(L2Effect effect) {
        return false;
    }

    public int getEffectFlags() {
        return L2EffectFlag.ROOTED.getMask();
    }
}
