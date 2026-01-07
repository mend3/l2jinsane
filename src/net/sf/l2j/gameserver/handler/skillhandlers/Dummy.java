package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;

public class Dummy implements ISkillHandler {
    private static final L2SkillType[] SKILL_IDS = new L2SkillType[]{L2SkillType.DUMMY, L2SkillType.BEAST_FEED, L2SkillType.DELUXE_KEY_UNLOCK};

    public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets) {
        if (!(activeChar instanceof net.sf.l2j.gameserver.model.actor.Player))
            return;
        if (skill.getSkillType() == L2SkillType.BEAST_FEED) {
            WorldObject target = targets[0];
            if (target == null) {
            }
        }
    }

    public L2SkillType[] getSkillIds() {
        return SKILL_IDS;
    }
}
