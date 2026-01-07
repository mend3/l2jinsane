package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.skills.L2EffectFlag;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.skills.Env;

public class EffectImmobileUntilAttacked extends L2Effect {
    public EffectImmobileUntilAttacked(Env env, EffectTemplate template) {
        super(env, template);
    }

    public L2EffectType getEffectType() {
        return L2EffectType.IMMOBILEUNTILATTACKED;
    }

    public boolean onStart() {
        getEffected().startImmobileUntilAttacked();
        return true;
    }

    public void onExit() {
        getEffected().stopImmobileUntilAttacked(this);
    }

    public boolean onActionTime() {
        getEffected().stopImmobileUntilAttacked(this);
        return false;
    }

    public int getEffectFlags() {
        return L2EffectFlag.MEDITATING.getMask();
    }
}
