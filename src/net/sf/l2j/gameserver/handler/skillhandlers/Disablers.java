package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.enums.AiEventType;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.items.ShotType;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.actor.ai.type.AttackableAI;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Formulas;

public class Disablers implements ISkillHandler {
    private static final L2SkillType[] SKILL_IDS = new L2SkillType[]{
            L2SkillType.STUN, L2SkillType.ROOT, L2SkillType.SLEEP, L2SkillType.CONFUSION, L2SkillType.AGGDAMAGE, L2SkillType.AGGREDUCE, L2SkillType.AGGREDUCE_CHAR, L2SkillType.AGGREMOVE, L2SkillType.MUTE, L2SkillType.FAKE_DEATH,
            L2SkillType.NEGATE, L2SkillType.CANCEL_DEBUFF, L2SkillType.PARALYZE, L2SkillType.ERASE, L2SkillType.BETRAY};

    public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets) {
        L2SkillType type = skill.getSkillType();
        boolean ss = activeChar.isChargedShot(ShotType.SOULSHOT);
        boolean sps = activeChar.isChargedShot(ShotType.SPIRITSHOT);
        boolean bsps = activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT);
        for (WorldObject obj : targets) {
            if (obj instanceof Creature target) {
                if (!target.isDead() && (!target.isInvul() || target.isParalyzed()))
                    if (!skill.isOffensive() || target.getFirstEffect(L2EffectType.BLOCK_DEBUFF) == null) {
                        L2Effect[] effects;
                        int count;
                        byte shld = Formulas.calcShldUse(activeChar, target, skill);
                        switch (type) {
                            case BETRAY:
                                if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, bsps)) {
                                    skill.getEffects(activeChar, target, new Env(shld, ss, sps, bsps));
                                    break;
                                }
                                activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill));
                                break;
                            case FAKE_DEATH:
                                skill.getEffects(activeChar, target, new Env(shld, ss, sps, bsps));
                                break;
                            case ROOT:
                            case STUN:
                                if (Formulas.calcSkillReflect(target, skill) == 1)
                                    target = activeChar;
                                if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, bsps)) {
                                    skill.getEffects(activeChar, target, new Env(shld, ss, sps, bsps));
                                    break;
                                }
                                if (activeChar instanceof Player)
                                    activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill.getId()));
                                break;
                            case SLEEP:
                            case PARALYZE:
                                if (Formulas.calcSkillReflect(target, skill) == 1)
                                    target = activeChar;
                                if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, bsps)) {
                                    skill.getEffects(activeChar, target, new Env(shld, ss, sps, bsps));
                                    break;
                                }
                                if (activeChar instanceof Player)
                                    activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill.getId()));
                                break;
                            case MUTE:
                                if (Formulas.calcSkillReflect(target, skill) == 1)
                                    target = activeChar;
                                if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, bsps)) {
                                    L2Effect[] arrayOfL2Effect = target.getAllEffects();
                                    for (L2Effect e : arrayOfL2Effect) {
                                        if (e.getSkill().getSkillType() == type)
                                            e.exit();
                                    }
                                    skill.getEffects(activeChar, target, new Env(shld, ss, sps, bsps));
                                    break;
                                }
                                if (activeChar instanceof Player)
                                    activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill.getId()));
                                break;
                            case CONFUSION:
                                if (target instanceof Attackable) {
                                    if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, bsps)) {
                                        L2Effect[] arrayOfL2Effect = target.getAllEffects();
                                        for (L2Effect e : arrayOfL2Effect) {
                                            if (e.getSkill().getSkillType() == type)
                                                e.exit();
                                        }
                                        skill.getEffects(activeChar, target, new Env(shld, ss, sps, bsps));
                                        break;
                                    }
                                    if (activeChar instanceof Player)
                                        activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill));
                                    break;
                                }
                                activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
                                break;
                            case AGGDAMAGE:
                                if (target instanceof Attackable)
                                    target.getAI().notifyEvent(AiEventType.AGGRESSION, activeChar, Integer.valueOf((int) (150.0D * skill.getPower() / (target.getLevel() + 7))));
                                skill.getEffects(activeChar, target, new Env(shld, ss, sps, bsps));
                                break;
                            case AGGREDUCE:
                                if (target instanceof Attackable) {
                                    skill.getEffects(activeChar, target, new Env(shld, ss, sps, bsps));
                                    double aggdiff = ((Attackable) target).getHating(activeChar) - target.calcStat(Stats.AGGRESSION, ((Attackable) target).getHating(activeChar), target, skill);
                                    if (skill.getPower() > 0.0D) {
                                        ((Attackable) target).reduceHate(null, (int) skill.getPower());
                                        break;
                                    }
                                    if (aggdiff > 0.0D)
                                        ((Attackable) target).reduceHate(null, (int) aggdiff);
                                }
                                break;
                            case AGGREDUCE_CHAR:
                                if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, bsps)) {
                                    if (target instanceof Attackable targ) {
                                        targ.stopHating(activeChar);
                                        if (targ.getMostHated() == null && targ.hasAI() && targ.getAI() instanceof AttackableAI) {
                                            ((AttackableAI) targ.getAI()).setGlobalAggro(-25);
                                            targ.getAggroList().clear();
                                            targ.getAI().setIntention(IntentionType.ACTIVE);
                                            targ.setWalking();
                                        }
                                    }
                                    skill.getEffects(activeChar, target, new Env(shld, ss, sps, bsps));
                                    break;
                                }
                                if (activeChar instanceof Player)
                                    activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill));
                                target.getAI().notifyEvent(AiEventType.ATTACKED, activeChar);
                                break;
                            case AGGREMOVE:
                                if (target instanceof Attackable && !target.isRaidRelated()) {
                                    if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, bsps)) {
                                        if (skill.getTargetType() == L2Skill.SkillTargetType.TARGET_UNDEAD) {
                                            if (target.isUndead())
                                                ((Attackable) target).reduceHate(null, ((Attackable) target).getHating(((Attackable) target).getMostHated()));
                                            break;
                                        }
                                        ((Attackable) target).reduceHate(null, ((Attackable) target).getHating(((Attackable) target).getMostHated()));
                                        break;
                                    }
                                    if (activeChar instanceof Player)
                                        activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill));
                                    target.getAI().notifyEvent(AiEventType.ATTACKED, activeChar);
                                    break;
                                }
                                target.getAI().notifyEvent(AiEventType.ATTACKED, activeChar);
                                break;
                            case ERASE:
                                if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, bsps) && !(target instanceof net.sf.l2j.gameserver.model.actor.instance.SiegeSummon)) {
                                    Player summonOwner = ((Summon) target).getOwner();
                                    Summon summonPet = summonOwner.getSummon();
                                    if (summonPet != null) {
                                        summonPet.unSummon(summonOwner);
                                        summonOwner.sendPacket(SystemMessageId.YOUR_SERVITOR_HAS_VANISHED);
                                    }
                                    break;
                                }
                                if (activeChar instanceof Player)
                                    activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill));
                                break;
                            case CANCEL_DEBUFF:
                                effects = target.getAllEffects();
                                if (effects == null || effects.length == 0)
                                    break;
                                count = (skill.getMaxNegatedEffects() > 0) ? 0 : -2;
                                for (L2Effect e : effects) {
                                    if (e != null && e.getSkill().isDebuff() && e.getSkill().canBeDispeled()) {
                                        e.exit();
                                        if (count > -1) {
                                            count++;
                                            if (count >= skill.getMaxNegatedEffects())
                                                break;
                                        }
                                    }
                                }
                                break;
                            case NEGATE:
                                if (Formulas.calcSkillReflect(target, skill) == 1)
                                    target = activeChar;
                                if ((skill.getNegateId()).length != 0) {
                                    for (int id : skill.getNegateId()) {
                                        if (id != 0)
                                            target.stopSkillEffects(id);
                                    }
                                } else {
                                    int negateLvl = skill.getNegateLvl();
                                    for (L2Effect e : target.getAllEffects()) {
                                        L2Skill effectSkill = e.getSkill();
                                        for (L2SkillType skillType : skill.getNegateStats()) {
                                            if (negateLvl == -1) {
                                                if (effectSkill.getSkillType() == skillType || (effectSkill.getEffectType() != null && effectSkill.getEffectType() == skillType))
                                                    e.exit();
                                            } else if (effectSkill.getEffectType() != null && effectSkill.getEffectAbnormalLvl() >= 0) {
                                                if (effectSkill.getEffectType() == skillType && effectSkill.getEffectAbnormalLvl() <= negateLvl)
                                                    e.exit();
                                            } else if (effectSkill.getSkillType() == skillType && effectSkill.getAbnormalLvl() <= negateLvl) {
                                                e.exit();
                                            }
                                        }
                                    }
                                }
                                skill.getEffects(activeChar, target, new Env(shld, ss, sps, bsps));
                                break;
                        }
                    }
            }
        }
        if (skill.hasSelfEffects()) {
            L2Effect effect = activeChar.getFirstEffect(skill.getId());
            if (effect != null && effect.isSelfEffect())
                effect.exit();
            skill.getEffectsSelf(activeChar);
        }
        activeChar.setChargedShot(bsps ? ShotType.BLESSED_SPIRITSHOT : ShotType.SPIRITSHOT, skill.isStaticReuse());
    }

    public L2SkillType[] getSkillIds() {
        return SKILL_IDS;
    }
}
