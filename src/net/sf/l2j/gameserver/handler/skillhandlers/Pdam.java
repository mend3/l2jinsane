package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.enums.items.ShotType;
import net.sf.l2j.gameserver.enums.items.WeaponType;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Formulas;

import java.util.List;

public class Pdam implements ISkillHandler {
    private static final L2SkillType[] SKILL_IDS = new L2SkillType[]{L2SkillType.PDAM, L2SkillType.FATAL};

    public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets) {
        if (activeChar.isAlikeDead())
            return;
        int damage = 0;
        boolean ss = activeChar.isChargedShot(ShotType.SOULSHOT);
        ItemInstance weapon = activeChar.getActiveWeaponInstance();
        for (WorldObject obj : targets) {
            if (!(obj instanceof Creature target))
                continue;
            if (activeChar instanceof Player && target instanceof Player && ((Player) target).isFakeDeath()) {
                target.stopFakeDeath(true);
            } else if (target.isDead()) {
                continue;
            }
            if (weapon != null && weapon.getItemType() != WeaponType.BOW && Formulas.calcPhysicalSkillEvasion(target, skill)) {
                if (activeChar instanceof Player)
                    activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DODGES_ATTACK).addCharName(target));
                if (target instanceof Player)
                    target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AVOIDED_S1_ATTACK).addCharName(activeChar));
            } else {
                byte shld = Formulas.calcShldUse(activeChar, target, skill);
                boolean crit = false;
                if (skill.getBaseCritRate() > 0)
                    crit = Formulas.calcCrit((skill.getBaseCritRate() * 10) * Formulas.getSTRBonus(activeChar));
                if (!crit && (skill.getCondition() & 0x10) != 0) {
                    damage = 0;
                } else {
                    damage = (int) Formulas.calcPhysDam(activeChar, target, skill, shld, false, ss);
                }
                if (crit)
                    damage *= 2;
                byte reflect = Formulas.calcSkillReflect(target, skill);
                if (skill.hasEffects() && target.getFirstEffect(L2EffectType.BLOCK_DEBUFF) == null)
                    if ((reflect & 0x1) != 0) {
                        activeChar.stopSkillEffects(skill.getId());
                        List<L2Effect> effects = skill.getEffects(target, activeChar);
                        if (effects != null && !effects.isEmpty())
                            activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skill));
                    } else {
                        target.stopSkillEffects(skill.getId());
                        List<L2Effect> effects = skill.getEffects(activeChar, target, new Env(shld, false, false, false));
                        if (effects != null && !effects.isEmpty())
                            target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skill));
                    }
                if (damage > 0) {
                    activeChar.sendDamageMessage(target, damage, false, crit, false);
                    Formulas.calcLethalHit(activeChar, target, skill);
                    target.reduceCurrentHp(damage, activeChar, skill);
                    if ((reflect & 0x2) != 0) {
                        if (target instanceof Player)
                            target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.COUNTERED_S1_ATTACK).addCharName(activeChar));
                        if (activeChar instanceof Player)
                            activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_PERFORMING_COUNTERATTACK).addCharName(target));
                        double vegdamage = (700 * target.getPAtk(activeChar) / activeChar.getPDef(target));
                        activeChar.reduceCurrentHp(vegdamage, target, skill);
                    }
                } else {
                    activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ATTACK_FAILED));
                }
            }
            continue;
        }
        if (skill.hasSelfEffects()) {
            L2Effect effect = activeChar.getFirstEffect(skill.getId());
            if (effect != null && effect.isSelfEffect())
                effect.exit();
            skill.getEffectsSelf(activeChar);
        }
        if (skill.isSuicideAttack())
            activeChar.doDie(activeChar);
        activeChar.setChargedShot(ShotType.SOULSHOT, skill.isStaticReuse());
    }

    public L2SkillType[] getSkillIds() {
        return SKILL_IDS;
    }
}
