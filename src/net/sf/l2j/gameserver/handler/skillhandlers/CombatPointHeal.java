package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.handler.SkillHandler;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class CombatPointHeal implements ISkillHandler {
    private static final L2SkillType[] SKILL_IDS = new L2SkillType[]{L2SkillType.COMBATPOINTHEAL};

    public void useSkill(Creature actChar, L2Skill skill, WorldObject[] targets) {
        ISkillHandler handler = SkillHandler.getInstance().getHandler(L2SkillType.BUFF);
        if (handler != null)
            handler.useSkill(actChar, skill, targets);
        for (WorldObject obj : targets) {
            if (obj instanceof Creature target) {
                if (!target.isDead() && !target.isInvul()) {
                    double cp = skill.getPower();
                    if (target.getCurrentCp() + cp >= target.getMaxCp())
                        cp = target.getMaxCp() - target.getCurrentCp();
                    target.setCurrentCp(cp + target.getCurrentCp());
                    StatusUpdate sump = new StatusUpdate(target);
                    sump.addAttribute(33, (int) target.getCurrentCp());
                    target.sendPacket(sump);
                    if (target instanceof net.sf.l2j.gameserver.model.actor.Player)
                        if (actChar instanceof net.sf.l2j.gameserver.model.actor.Player && actChar != target) {
                            target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_CP_WILL_BE_RESTORED_BY_S1).addCharName(actChar).addNumber((int) cp));
                        } else {
                            target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CP_WILL_BE_RESTORED).addNumber((int) cp));
                        }
                }
            }
        }
    }

    public L2SkillType[] getSkillIds() {
        return SKILL_IDS;
    }
}
