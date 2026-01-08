package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.handler.SkillHandler;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;

import java.util.ArrayList;
import java.util.List;

public class BalanceLife implements ISkillHandler {
    private static final L2SkillType[] SKILL_IDS = new L2SkillType[]{L2SkillType.BALANCE_LIFE};

    public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets) {
        ISkillHandler handler = SkillHandler.getInstance().getHandler(L2SkillType.BUFF);
        if (handler != null)
            handler.useSkill(activeChar, skill, targets);
        Player player = activeChar.getActingPlayer();
        List<Creature> finalList = new ArrayList<>();
        double fullHP = 0.0D;
        double currentHPs = 0.0D;
        for (WorldObject obj : targets) {
            if (!(obj instanceof Creature target))
                continue;
            if (target.isDead())
                continue;
            if (target != activeChar) {
                if (target instanceof Player && ((Player) target).isCursedWeaponEquipped())
                    continue;
                if (player != null && player.isCursedWeaponEquipped())
                    continue;
            }
            fullHP += target.getMaxHp();
            currentHPs += target.getCurrentHp();
            finalList.add(target);
        }
        if (!finalList.isEmpty()) {
            double percentHP = currentHPs / fullHP;
            for (Creature target : finalList) {
                target.setCurrentHp(target.getMaxHp() * percentHP);
                StatusUpdate su = new StatusUpdate(target);
                su.addAttribute(9, (int) target.getCurrentHp());
                target.sendPacket(su);
            }
        }
    }

    public L2SkillType[] getSkillIds() {
        return SKILL_IDS;
    }
}
