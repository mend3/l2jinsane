package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.skills.Env;

final class EffectImobilePetBuff extends L2Effect {
    private Summon _pet;

    public EffectImobilePetBuff(Env env, EffectTemplate template) {
        super(env, template);
    }

    public L2EffectType getEffectType() {
        return L2EffectType.BUFF;
    }

    public boolean onStart() {
        this._pet = null;
        if (getEffected() instanceof Summon && getEffector() instanceof net.sf.l2j.gameserver.model.actor.Player && ((Summon) getEffected()).getOwner() == getEffector()) {
            this._pet = (Summon) getEffected();
            this._pet.setIsImmobilized(true);
            return true;
        }
        return false;
    }

    public void onExit() {
        this._pet.setIsImmobilized(false);
    }

    public boolean onActionTime() {
        return false;
    }
}
