package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.skills.L2EffectFlag;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.skills.Env;

public class EffectProtectionBlessing extends L2Effect {
    public EffectProtectionBlessing(Env env, EffectTemplate template) {
        super(env, template);
    }

    public L2EffectType getEffectType() {
        return L2EffectType.PROTECTION_BLESSING;
    }

    public boolean onStart() {
        return false;
    }

    public void onExit() {
        ((Playable) getEffected()).stopProtectionBlessing(this);
    }

    public boolean onActionTime() {
        return false;
    }

    public int getEffectFlags() {
        return L2EffectFlag.PROTECTION_BLESSING.getMask();
    }
}
