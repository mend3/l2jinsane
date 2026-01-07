package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.skills.L2EffectFlag;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.skills.Env;

public class EffectSilenceMagicPhysical extends L2Effect {
    public EffectSilenceMagicPhysical(Env env, EffectTemplate template) {
        super(env, template);
    }

    public L2EffectType getEffectType() {
        return L2EffectType.SILENCE_MAGIC_PHYSICAL;
    }

    public boolean onStart() {
        getEffected().startMuted();
        getEffected().startPhysicalMuted();
        return true;
    }

    public boolean onActionTime() {
        return false;
    }

    public void onExit() {
        getEffected().stopMuted(false);
        getEffected().stopPhysicalMuted(false);
    }

    public int getEffectFlags() {
        return L2EffectFlag.MUTED.getMask() | L2EffectFlag.PHYSICAL_MUTED.getMask();
    }
}
