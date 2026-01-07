package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.skills.Env;

public class EffectBuff extends L2Effect {
    public EffectBuff(Env env, EffectTemplate template) {
        super(env, template);
    }

    public L2EffectType getEffectType() {
        return L2EffectType.BUFF;
    }

    public boolean onActionTime() {
        return false;
    }
}
