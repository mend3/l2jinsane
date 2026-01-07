package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.AiEventType;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Formulas;

public class EffectSpoil extends L2Effect {
    public EffectSpoil(Env env, EffectTemplate template) {
        super(env, template);
    }

    public L2EffectType getEffectType() {
        return L2EffectType.SPOIL;
    }

    public boolean onStart() {
        if (!(getEffector() instanceof net.sf.l2j.gameserver.model.actor.Player))
            return false;
        if (!(getEffected() instanceof Monster target))
            return false;
        if (target.isDead())
            return false;
        if (target.getSpoilerId() != 0) {
            getEffector().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_SPOILED));
            return false;
        }
        if (Formulas.calcMagicSuccess(getEffector(), target, getSkill())) {
            target.setSpoilerId(getEffector().getObjectId());
            getEffector().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SPOIL_SUCCESS));
        }
        target.getAI().notifyEvent(AiEventType.ATTACKED, getEffector());
        return true;
    }

    public boolean onActionTime() {
        return false;
    }
}
