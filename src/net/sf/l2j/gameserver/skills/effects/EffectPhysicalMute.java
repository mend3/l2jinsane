package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.skills.L2EffectFlag;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.skills.Env;

public class EffectPhysicalMute extends L2Effect {
    public EffectPhysicalMute(Env env, EffectTemplate template) {
        super(env, template);
    }

    public L2EffectType getEffectType() {
        return L2EffectType.PHYSICAL_MUTE;
    }

    public boolean onStart() {
        getEffected().startPhysicalMuted();
        return true;
    }

    public boolean onActionTime() {
        return false;
    }

    public void onExit() {
        getEffected().stopPhysicalMuted(false);
    }

    public int getEffectFlags() {
        return L2EffectFlag.PHYSICAL_MUTED.getMask();
    }
}
