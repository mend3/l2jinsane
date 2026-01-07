package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.skills.L2EffectFlag;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.skills.Env;

public class EffectPhoenixBless extends L2Effect {
    public EffectPhoenixBless(Env env, EffectTemplate template) {
        super(env, template);
    }

    public L2EffectType getEffectType() {
        return L2EffectType.PHOENIX_BLESSING;
    }

    public boolean onStart() {
        return true;
    }

    public void onExit() {
        if (getEffected() instanceof Playable)
            ((Playable) getEffected()).stopPhoenixBlessing(this);
    }

    public boolean onActionTime() {
        return false;
    }

    public int getEffectFlags() {
        return L2EffectFlag.PHOENIX_BLESSING.getMask();
    }
}
