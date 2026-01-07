package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.skills.Env;

final class EffectImobileBuff extends L2Effect {
    public EffectImobileBuff(Env env, EffectTemplate template) {
        super(env, template);
    }

    public L2EffectType getEffectType() {
        return L2EffectType.BUFF;
    }

    public boolean onStart() {
        getEffector().setIsImmobilized(true);
        return true;
    }

    public void onExit() {
        getEffector().setIsImmobilized(false);
    }

    public boolean onActionTime() {
        return false;
    }
}
