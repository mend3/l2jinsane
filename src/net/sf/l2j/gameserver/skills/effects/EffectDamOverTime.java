package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Env;

public class EffectDamOverTime extends L2Effect {
    public EffectDamOverTime(Env env, EffectTemplate template) {
        super(env, template);
    }

    public L2EffectType getEffectType() {
        return L2EffectType.DMG_OVER_TIME;
    }

    public boolean onActionTime() {
        if (getEffected().isDead())
            return false;
        double damage = calc();
        if (damage >= getEffected().getCurrentHp()) {
            if (getSkill().isToggle()) {
                getEffected().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SKILL_REMOVED_DUE_LACK_HP));
                return false;
            }
            if (!getSkill().killByDOT()) {
                if (getEffected().getCurrentHp() <= 1.0D)
                    return true;
                damage = getEffected().getCurrentHp() - 1.0D;
            }
        }
        getEffected().reduceCurrentHpByDOT(damage, getEffector(), getSkill());
        return true;
    }
}
