package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.enums.AiEventType;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Formulas;

public class Spoil implements ISkillHandler {
    private static final L2SkillType[] SKILL_IDS = new L2SkillType[]{L2SkillType.SPOIL};

    public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets) {
        if (!(activeChar instanceof net.sf.l2j.gameserver.model.actor.Player))
            return;
        if (targets == null)
            return;
        for (WorldObject tgt : targets) {
            if (tgt instanceof Monster target) {
                if (!target.isDead())
                    if (target.getSpoilerId() != 0) {
                        activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ALREADY_SPOILED));
                    } else {
                        if (Formulas.calcMagicSuccess(activeChar, (Creature) tgt, skill)) {
                            target.setSpoilerId(activeChar.getObjectId());
                            activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SPOIL_SUCCESS));
                        } else {
                            activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill.getId()));
                        }
                        target.getAI().notifyEvent(AiEventType.ATTACKED, activeChar);
                    }
            }
        }
    }

    public L2SkillType[] getSkillIds() {
        return SKILL_IDS;
    }
}
