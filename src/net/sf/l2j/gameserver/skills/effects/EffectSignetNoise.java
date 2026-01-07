package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.EffectPoint;
import net.sf.l2j.gameserver.skills.Env;

public class EffectSignetNoise extends L2Effect {
    private EffectPoint _actor;

    public EffectSignetNoise(Env env, EffectTemplate template) {
        super(env, template);
    }

    public L2EffectType getEffectType() {
        return L2EffectType.SIGNET_GROUND;
    }

    public boolean onStart() {
        this._actor = (EffectPoint) getEffected();
        return true;
    }

    public boolean onActionTime() {
        if (getCount() == getTotalCount() - 1)
            return true;
        Player caster = (Player) getEffector();
        for (Creature target : this._actor.getKnownTypeInRadius(Creature.class, getSkill().getSkillRadius())) {
            if (target == caster)
                continue;
            if (caster.canAttackCharacter(target))
                for (L2Effect effect : target.getAllEffects()) {
                    if (effect.getSkill().isDance())
                        effect.exit();
                }
        }
        return true;
    }

    public void onExit() {
        if (this._actor != null)
            this._actor.deleteMe();
    }
}
