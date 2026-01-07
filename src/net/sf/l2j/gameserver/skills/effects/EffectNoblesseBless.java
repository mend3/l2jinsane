package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.skills.L2EffectFlag;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.skills.Env;

public class EffectNoblesseBless extends L2Effect {
    public EffectNoblesseBless(Env env, EffectTemplate template) {
        super(env, template);
    }

    public L2EffectType getEffectType() {
        return L2EffectType.NOBLESSE_BLESSING;
    }

    public boolean onStart() {
        return true;
    }

    public void onExit() {
    }

    public boolean onActionTime() {
        return false;
    }

    public int getEffectFlags() {
        return L2EffectFlag.NOBLESS_BLESSING.getMask();
    }
}
