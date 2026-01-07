package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.skills.Env;

public class EffectBlockDebuff extends L2Effect {
    public EffectBlockDebuff(Env env, EffectTemplate template) {
        super(env, template);
    }

    public L2EffectType getEffectType() {
        return L2EffectType.BLOCK_DEBUFF;
    }

    public boolean onActionTime() {
        return false;
    }
}
