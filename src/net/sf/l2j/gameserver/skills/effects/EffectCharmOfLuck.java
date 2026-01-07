package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.skills.L2EffectFlag;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.skills.Env;

public class EffectCharmOfLuck extends L2Effect {
    public EffectCharmOfLuck(Env env, EffectTemplate template) {
        super(env, template);
    }

    public L2EffectType getEffectType() {
        return L2EffectType.CHARM_OF_LUCK;
    }

    public boolean onStart() {
        return true;
    }

    public void onExit() {
        ((Playable) getEffected()).stopCharmOfLuck(this);
    }

    public boolean onActionTime() {
        return false;
    }

    public int getEffectFlags() {
        return L2EffectFlag.CHARM_OF_LUCK.getMask();
    }
}
