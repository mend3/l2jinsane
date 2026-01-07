package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;

public class InstantJump implements ISkillHandler {
    private static final L2SkillType[] SKILL_IDS = new L2SkillType[]{L2SkillType.INSTANT_JUMP};

    public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets) {
        Creature target = (Creature) targets[0];
        int px = target.getX();
        int py = target.getY();
        double ph = MathUtil.convertHeadingToDegree(target.getHeading());
        ph += 180.0D;
        if (ph > 360.0D)
            ph -= 360.0D;
        ph = Math.PI * ph / 180.0D;
        int x = (int) (px + 25.0D * Math.cos(ph));
        int y = (int) (py + 25.0D * Math.sin(ph));
        int z = target.getZ();
        activeChar.stopMove(null);
        activeChar.abortAttack();
        activeChar.abortCast();
        activeChar.setXYZ(x, y, z);
        activeChar.broadcastPacket(new ValidateLocation(activeChar));
    }

    public L2SkillType[] getSkillIds() {
        return SKILL_IDS;
    }
}
