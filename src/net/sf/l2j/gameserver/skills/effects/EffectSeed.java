package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.skills.Env;

public final class EffectSeed extends L2Effect {
    private int _power = 1;

    public EffectSeed(Env env, EffectTemplate template) {
        super(env, template);
    }

    public L2EffectType getEffectType() {
        return L2EffectType.SEED;
    }

    public boolean onActionTime() {
        return false;
    }

    public int getPower() {
        return this._power;
    }

    public void increasePower() {
        this._power++;
    }
}
