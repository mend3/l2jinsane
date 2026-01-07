package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.skills.Env;

public class EffectIncreaseCharges extends L2Effect {
    public EffectIncreaseCharges(Env env, EffectTemplate template) {
        super(env, template);
    }

    public L2EffectType getEffectType() {
        return L2EffectType.INCREASE_CHARGES;
    }

    public boolean onStart() {
        if (getEffected() == null)
            return false;
        if (!(getEffected() instanceof Player))
            return false;
        ((Player) getEffected()).increaseCharges((int) calc(), getCount());
        return true;
    }

    public boolean onActionTime() {
        return false;
    }
}
