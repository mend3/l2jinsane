package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.entity.Castle;

public class TakeCastle implements ISkillHandler {
    private static final L2SkillType[] SKILL_IDS = new L2SkillType[]{L2SkillType.TAKECASTLE};

    public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets) {
        if (!(activeChar instanceof Player player))
            return;
        if (targets.length == 0)
            return;
        if (!player.isClanLeader())
            return;
        Castle castle = CastleManager.getInstance().getCastle(player);
        if (castle == null || !player.checkIfOkToCastSealOfRule(castle, true, skill, targets[0]))
            return;
        castle.engrave(player.getClan(), targets[0]);
    }

    public L2SkillType[] getSkillIds() {
        return SKILL_IDS;
    }
}
