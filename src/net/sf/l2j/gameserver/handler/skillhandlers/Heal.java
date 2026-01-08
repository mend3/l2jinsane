package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.enums.items.ShotType;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.enums.skills.Stats;
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

public class Heal implements ISkillHandler {
    private static final L2SkillType[] SKILL_IDS = new L2SkillType[]{L2SkillType.HEAL, L2SkillType.HEAL_STATIC};

    public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets) {
        double staticShotBonus;
        int mAtkMul;
        boolean sps = activeChar.isChargedShot(ShotType.SPIRITSHOT);
        boolean bsps = activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT);
        ISkillHandler handler = SkillHandler.getInstance().getHandler(L2SkillType.BUFF);
        if (handler != null)
            handler.useSkill(activeChar, skill, targets);
        double power = skill.getPower() + activeChar.calcStat(Stats.HEAL_PROFICIENCY, 0.0D, null, null);
        switch (skill.getSkillType()) {
            case HEAL_STATIC:
                break;
            default:
                staticShotBonus = 0.0D;
                mAtkMul = 1;
                if (((sps || bsps) && activeChar instanceof Player && activeChar.getActingPlayer().isMageClass()) || activeChar instanceof net.sf.l2j.gameserver.model.actor.Summon) {
                    staticShotBonus = skill.getMpConsume();
                    if (bsps) {
                        mAtkMul = 4;
                        staticShotBonus *= 2.4D;
                    } else {
                        mAtkMul = 2;
                    }
                } else if ((sps || bsps) && activeChar instanceof net.sf.l2j.gameserver.model.actor.Npc) {
                    staticShotBonus = 2.4D * skill.getMpConsume();
                    mAtkMul = 4;
                } else if (bsps) {
                    mAtkMul *= 4;
                } else {
                    mAtkMul++;
                }
                power += staticShotBonus + Math.sqrt((mAtkMul * activeChar.getMAtk(activeChar, null)));
                if (!skill.isPotion())
                    activeChar.setChargedShot(bsps ? ShotType.BLESSED_SPIRITSHOT : ShotType.SPIRITSHOT, skill.isStaticReuse());
                break;
        }
        for (WorldObject obj : targets) {
            double hp;
            if (!(obj instanceof Creature target))
                continue;
            if (target.isDead() || target.isInvul())
                continue;
            if (target instanceof net.sf.l2j.gameserver.model.actor.instance.Door || target instanceof net.sf.l2j.gameserver.model.actor.instance.SiegeFlag)
                continue;
            if (activeChar instanceof Player && target instanceof Player && !EventListener.canHeal((Player) activeChar, (Player) target))
                continue;
            if (target != activeChar) {
                if (target instanceof Player && ((Player) target).isCursedWeaponEquipped())
                    continue;
                if (activeChar instanceof Player && ((Player) activeChar).isCursedWeaponEquipped())
                    continue;
            }
            switch (skill.getSkillType()) {
                case HEAL_PERCENT:
                    hp = target.getMaxHp() * power / 100.0D;
                    break;
                default:
                    hp = power;
                    hp *= target.calcStat(Stats.HEAL_EFFECTIVNESS, 100.0D, null, null) / 100.0D;
                    break;
            }
            if (target.getCurrentHp() + hp >= target.getMaxHp())
                hp = target.getMaxHp() - target.getCurrentHp();
            if (hp < 0.0D)
                hp = 0.0D;
            target.setCurrentHp(hp + target.getCurrentHp());
            StatusUpdate su = new StatusUpdate(target);
            su.addAttribute(9, (int) target.getCurrentHp());
            target.sendPacket(su);
            if (target instanceof Player)
                if (skill.getId() == 4051) {
                    target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.REJUVENATING_HP));
                } else if (activeChar instanceof Player && activeChar != target) {
                    target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_HP_RESTORED_BY_S1).addCharName(activeChar).addNumber((int) hp));
                } else {
                    target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HP_RESTORED).addNumber((int) hp));
                }
        }
    }

    public L2SkillType[] getSkillIds() {
        return SKILL_IDS;
    }
}
