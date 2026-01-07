package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.skills.AbnormalEffect;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.skills.Env;

public class EffectGrow extends L2Effect {
    public EffectGrow(Env env, EffectTemplate template) {
        super(env, template);
    }

    public L2EffectType getEffectType() {
        return L2EffectType.BUFF;
    }

    public boolean onStart() {
        if (getEffected() instanceof Npc npc) {
            npc.setCollisionRadius(npc.getCollisionRadius() * 1.19D);
            getEffected().startAbnormalEffect(AbnormalEffect.GROW);
            return true;
        }
        return false;
    }

    public boolean onActionTime() {
        return false;
    }

    public void onExit() {
        if (getEffected() instanceof Npc npc) {
            npc.setCollisionRadius(npc.getTemplate().getCollisionRadius());
            getEffected().stopAbnormalEffect(AbnormalEffect.GROW);
        }
    }
}
