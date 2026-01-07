package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.skills.L2EffectFlag;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.skills.Env;

import java.util.ArrayList;
import java.util.List;

public class EffectConfuseMob extends L2Effect {
    public EffectConfuseMob(Env env, EffectTemplate template) {
        super(env, template);
    }

    public L2EffectType getEffectType() {
        return L2EffectType.CONFUSE_MOB_ONLY;
    }

    public boolean onStart() {
        getEffected().startConfused();
        onActionTime();
        return true;
    }

    public void onExit() {
        getEffected().stopConfused(this);
    }

    public boolean onActionTime() {
        List<Creature> targetList = new ArrayList<>();
        for (Attackable obj : getEffected().getKnownType(Attackable.class)) {
            if (!(obj instanceof net.sf.l2j.gameserver.model.actor.instance.Chest))
                targetList.add(obj);
        }
        if (targetList.isEmpty())
            return true;
        WorldObject target = Rnd.get(targetList);
        getEffected().setTarget(target);
        getEffected().getAI().setIntention(IntentionType.ATTACK, target);
        int aggro = (5 + Rnd.get(5)) * getEffector().getLevel();
        ((Attackable) getEffected()).addDamageHate((Creature) target, 0, aggro);
        return true;
    }

    public int getEffectFlags() {
        return L2EffectFlag.CONFUSED.getMask();
    }
}
