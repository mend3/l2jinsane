package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.enums.items.ShotType;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.basefuncs.Func;

public class Blow implements ISkillHandler {
    public static final int FRONT = 50;
    public static final int SIDE = 60;
    public static final int BEHIND = 70;
    private static final L2SkillType[] SKILL_IDS = new L2SkillType[]{L2SkillType.BLOW};

    public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets) {
        if (activeChar.isAlikeDead())
            return;
        boolean ss = activeChar.isChargedShot(ShotType.SOULSHOT);
        WorldObject[] arrayOfWorldObject = targets;
        int i = arrayOfWorldObject.length;
        byte b = 0;
        while (true) {
            if (b < i) {
                WorldObject obj = arrayOfWorldObject[b];
                if (obj instanceof Creature target) {
                    if (!target.isAlikeDead()) {
                        byte _successChance = 60;
                        if (activeChar.isBehindTarget()) {
                            _successChance = 70;
                        } else if (activeChar.isInFrontOfTarget()) {
                            _successChance = 50;
                        }
                        boolean success = true;
                        if ((skill.getCondition() & 0x8) != 0)
                            success = (_successChance == 70);
                        if ((skill.getCondition() & 0x10) != 0)
                            success = (success && Formulas.calcBlow(activeChar, target, _successChance));
                        if (success) {
                            boolean skillIsEvaded = Formulas.calcPhysicalSkillEvasion(target, skill);
                            if (skillIsEvaded) {
                                if (activeChar instanceof Player)
                                    activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DODGES_ATTACK).addCharName(target));
                                if (target instanceof Player)
                                    target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AVOIDED_S1_ATTACK).addCharName(activeChar));
                            } else {
                                byte reflect = Formulas.calcSkillReflect(target, skill);
                                if (skill.hasEffects())
                                    if (reflect == 1) {
                                        activeChar.stopSkillEffects(skill.getId());
                                        skill.getEffects(target, activeChar);
                                        activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skill));
                                    } else {
                                        byte b1 = Formulas.calcShldUse(activeChar, target, skill);
                                        target.stopSkillEffects(skill.getId());
                                        if (Formulas.calcSkillSuccess(activeChar, target, skill, b1, true)) {
                                            skill.getEffects(activeChar, target, new Env(b1, false, false, false));
                                            target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(skill));
                                        } else {
                                            activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill));
                                        }
                                    }
                                byte shld = Formulas.calcShldUse(activeChar, target, skill);
                                boolean crit = Formulas.calcCrit((skill.getBaseCritRate() * 10) * Formulas.getSTRBonus(activeChar));
                                double damage = (int) Formulas.calcBlowDamage(activeChar, target, skill, shld, ss);
                                if (crit) {
                                    damage *= 2.0D;
                                    L2Effect vicious = activeChar.getFirstEffect(312);
                                    if (vicious != null && damage > 1.0D)
                                        for (Func func : vicious.getStatFuncs()) {
                                            Env env = new Env();
                                            env.setCharacter(activeChar);
                                            env.setTarget(target);
                                            env.setSkill(skill);
                                            env.setValue(damage);
                                            func.calc(env);
                                            damage = (int) env.getValue();
                                        }
                                }
                                target.reduceCurrentHp(damage, activeChar, skill);
                                if ((reflect & 0x2) != 0) {
                                    if (target instanceof Player)
                                        target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.COUNTERED_S1_ATTACK).addCharName(activeChar));
                                    if (activeChar instanceof Player)
                                        activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_PERFORMING_COUNTERATTACK).addCharName(target));
                                    double vegdamage = (700 * target.getPAtk(activeChar) / activeChar.getPDef(target));
                                    activeChar.reduceCurrentHp(vegdamage, target, skill);
                                }
                                Formulas.calcCastBreak(target, damage);
                                if (activeChar instanceof Player)
                                    activeChar.sendDamageMessage(target, (int) damage, false, true, false);
                                Formulas.calcLethalHit(activeChar, target, skill);
                            }
                        } else {
                            activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ATTACK_FAILED));
                            Formulas.calcLethalHit(activeChar, target, skill);
                        }
                    }
                }
            } else {
                break;
            }
            b++;
        }
    }

    public L2SkillType[] getSkillIds() {
        return SKILL_IDS;
    }
}
