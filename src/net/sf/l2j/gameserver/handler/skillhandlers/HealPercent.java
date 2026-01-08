package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.events.eventengine.EventListener;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.handler.SkillHandler;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class HealPercent implements ISkillHandler {
    private static final L2SkillType[] SKILL_IDS = new L2SkillType[]{L2SkillType.HEAL_PERCENT, L2SkillType.MANAHEAL_PERCENT};

    public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets) {
        ISkillHandler handler = SkillHandler.getInstance().getHandler(L2SkillType.BUFF);
        if (handler != null)
            handler.useSkill(activeChar, skill, targets);
        boolean hp = false;
        boolean mp = false;
        switch (skill.getSkillType()) {
            case HEAL_PERCENT:
                hp = true;
                break;
            case MANAHEAL_PERCENT:
                mp = true;
                break;
        }
        StatusUpdate su = null;
        double amount = 0.0D;
        boolean full = (skill.getPower() == 100.0D);
        boolean targetPlayer = false;
        for (WorldObject obj : targets) {
            if (!(obj instanceof Creature target))
                continue;
            if (target.isDead() || target.isInvul())
                continue;
            if (target instanceof net.sf.l2j.gameserver.model.actor.instance.Door || target instanceof net.sf.l2j.gameserver.model.actor.instance.SiegeFlag)
                continue;
            if (activeChar instanceof Player && target instanceof Player && !EventListener.canHeal((Player) activeChar, (Player) target))
                continue;
            targetPlayer = target instanceof Player;
            if (target != activeChar) {
                if (activeChar instanceof Player && ((Player) activeChar).isCursedWeaponEquipped())
                    continue;
                if (targetPlayer && ((Player) target).isCursedWeaponEquipped())
                    continue;
            }
            if (hp) {
                amount = Math.min(full ? target.getMaxHp() : (target.getMaxHp() * skill.getPower() / 100.0D), target.getMaxHp() - target.getCurrentHp());
                target.setCurrentHp(amount + target.getCurrentHp());
            } else if (mp) {
                amount = Math.min(full ? target.getMaxMp() : (target.getMaxMp() * skill.getPower() / 100.0D), target.getMaxMp() - target.getCurrentMp());
                target.setCurrentMp(amount + target.getCurrentMp());
            }
            if (targetPlayer) {
                su = new StatusUpdate(target);
                if (hp) {
                    SystemMessage sm;
                    if (activeChar != target) {
                        sm = SystemMessage.getSystemMessage(SystemMessageId.S2_HP_RESTORED_BY_S1).addCharName(activeChar);
                    } else {
                        sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HP_RESTORED);
                    }
                    sm.addNumber((int) amount);
                    target.sendPacket(sm);
                    su.addAttribute(9, (int) target.getCurrentHp());
                } else if (mp) {
                    SystemMessage sm;
                    if (activeChar != target) {
                        sm = SystemMessage.getSystemMessage(SystemMessageId.S2_MP_RESTORED_BY_S1).addCharName(activeChar);
                    } else {
                        sm = SystemMessage.getSystemMessage(SystemMessageId.S1_MP_RESTORED);
                    }
                    sm.addNumber((int) amount);
                    target.sendPacket(sm);
                    su.addAttribute(11, (int) target.getCurrentMp());
                }
                target.sendPacket(su);
            }
        }
    }

    public L2SkillType[] getSkillIds() {
        return SKILL_IDS;
    }
}
