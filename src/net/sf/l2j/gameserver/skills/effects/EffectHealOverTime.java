package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.network.serverpackets.ExRegenMax;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.skills.Env;

class EffectHealOverTime extends L2Effect {
    public EffectHealOverTime(Env env, EffectTemplate template) {
        super(env, template);
    }

    public L2EffectType getEffectType() {
        return L2EffectType.HEAL_OVER_TIME;
    }

    public boolean onStart() {
        if (getEffected() instanceof net.sf.l2j.gameserver.model.actor.Player && getTotalCount() > 0 && getPeriod() > 0)
            getEffected().sendPacket(new ExRegenMax(getTotalCount() * getPeriod(), getPeriod(), calc()));
        return true;
    }

    public boolean onActionTime() {
        if (getEffected().isDead() || getEffected() instanceof net.sf.l2j.gameserver.model.actor.instance.Door)
            return false;
        double maxHp = getEffected().getMaxHp();
        double newHp = getEffected().getCurrentHp() + calc();
        if (newHp > maxHp)
            newHp = maxHp;
        getEffected().setCurrentHp(newHp);
        StatusUpdate su = new StatusUpdate(getEffected());
        su.addAttribute(9, (int) newHp);
        getEffected().sendPacket(su);
        return true;
    }
}
