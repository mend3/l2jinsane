package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.events.eventengine.manager.CtfEventManager;
import net.sf.l2j.gameserver.events.eventengine.manager.DmEventManager;
import net.sf.l2j.gameserver.events.eventengine.manager.TvTEventManager;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;

public class GetPlayer implements ISkillHandler {
    private static final L2SkillType[] SKILL_IDS = new L2SkillType[]{L2SkillType.GET_PLAYER};

    public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets) {
        if (activeChar.isAlikeDead())
            return;
        for (WorldObject target : targets) {
            Player victim = target.getActingPlayer();
            if (victim != null && !victim.isAlikeDead())
                if ((TvTEventManager.getInstance().getActiveEvent() == null || !TvTEventManager.getInstance().getActiveEvent().isInEvent(victim)) && victim.getDungeon() == null)
                    if ((CtfEventManager.getInstance().getActiveEvent() == null || !CtfEventManager.getInstance().getActiveEvent().isInEvent(victim)) && victim.getDungeon() == null)
                        if ((DmEventManager.getInstance().getActiveEvent() == null || !DmEventManager.getInstance().getActiveEvent().isInEvent(victim)) && victim.getDungeon() == null)
                            victim.instantTeleportTo(activeChar.getPosition(), 0);
        }
    }

    public L2SkillType[] getSkillIds() {
        return SKILL_IDS;
    }
}
