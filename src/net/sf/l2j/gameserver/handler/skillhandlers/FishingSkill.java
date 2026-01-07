package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.enums.items.ShotType;
import net.sf.l2j.gameserver.enums.items.WeaponType;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;

public class FishingSkill implements ISkillHandler {
    private static final L2SkillType[] SKILL_IDS = new L2SkillType[]{L2SkillType.PUMPING, L2SkillType.REELING};

    public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets) {
        if (!(activeChar instanceof Player player))
            return;
        boolean isReelingSkill = (skill.getSkillType() == L2SkillType.REELING);
        if (!player.getFishingStance().isUnderFishCombat()) {
            player.sendPacket(isReelingSkill ? SystemMessageId.CAN_USE_REELING_ONLY_WHILE_FISHING : SystemMessageId.CAN_USE_PUMPING_ONLY_WHILE_FISHING);
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        ItemInstance fishingRod = activeChar.getActiveWeaponInstance();
        if (fishingRod == null || fishingRod.getItem().getItemType() != WeaponType.FISHINGROD)
            return;
        int ssBonus = activeChar.isChargedShot(ShotType.FISH_SOULSHOT) ? 2 : 1;
        double gradeBonus = 1.0D + fishingRod.getItem().getCrystalType().getId() * 0.1D;
        int damage = (int) (skill.getPower() * gradeBonus * ssBonus);
        int penalty = 0;
        if (skill.getLevel() - player.getSkillLevel(1315) >= 3) {
            penalty = 50;
            damage -= penalty;
            player.sendPacket(SystemMessageId.REELING_PUMPING_3_LEVELS_HIGHER_THAN_FISHING_PENALTY);
        }
        if (ssBonus > 1)
            fishingRod.setChargedShot(ShotType.FISH_SOULSHOT, false);
        if (isReelingSkill) {
            player.getFishingStance().useRealing(damage, penalty);
        } else {
            player.getFishingStance().usePomping(damage, penalty);
        }
    }

    public L2SkillType[] getSkillIds() {
        return SKILL_IDS;
    }
}
