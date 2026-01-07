package net.sf.l2j.gameserver.handler;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;

public interface ISkillHandler {
    CLogger LOGGER = new CLogger(ISkillHandler.class.getName());

    void useSkill(Creature paramCreature, L2Skill paramL2Skill, WorldObject[] paramArrayOfWorldObject);

    L2SkillType[] getSkillIds();
}
