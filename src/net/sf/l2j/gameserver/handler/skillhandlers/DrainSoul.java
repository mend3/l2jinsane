package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.scripting.QuestState;

public class DrainSoul implements ISkillHandler {
    private static final String qn = "Q350_EnhanceYourWeapon";

    private static final L2SkillType[] SKILL_IDS = new L2SkillType[]{L2SkillType.DRAIN_SOUL};

    public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets) {
        if (activeChar == null || activeChar.isDead() || !(activeChar instanceof Player player))
            return;
        QuestState st = player.getQuestState("Q350_EnhanceYourWeapon");
        if (st == null || !st.isStarted())
            return;
        WorldObject target = targets[0];
        if (!(target instanceof Monster mob))
            return;
        if (mob.isDead())
            return;
        if (!player.isInsideRadius(mob, skill.getEffectRange(), true, true))
            return;
        mob.registerAbsorber(player);
    }

    public L2SkillType[] getSkillIds() {
        return SKILL_IDS;
    }
}
