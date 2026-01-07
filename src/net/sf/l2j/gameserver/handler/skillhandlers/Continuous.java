package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.manager.DuelManager;
import net.sf.l2j.gameserver.enums.AiEventType;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.items.ShotType;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
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

public class Continuous implements ISkillHandler {
    private static final L2SkillType[] SKILL_IDS = new L2SkillType[]{
            L2SkillType.BUFF, L2SkillType.DEBUFF, L2SkillType.DOT, L2SkillType.MDOT, L2SkillType.POISON, L2SkillType.BLEED, L2SkillType.HOT, L2SkillType.CPHOT, L2SkillType.MPHOT, L2SkillType.FEAR,
            L2SkillType.CONT, L2SkillType.WEAKNESS, L2SkillType.REFLECT, L2SkillType.UNDEAD_DEFENSE, L2SkillType.AGGDEBUFF, L2SkillType.FUSION};

    public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets) {
        Player player = activeChar.getActingPlayer();
        if (skill.getEffectId() != 0) {
            L2Skill sk = SkillTable.getInstance().getInfo(skill.getEffectId(), (skill.getEffectLvl() == 0) ? 1 : skill.getEffectLvl());
            if (sk != null)
                skill = sk;
        }
        boolean ss = activeChar.isChargedShot(ShotType.SOULSHOT);
        boolean sps = activeChar.isChargedShot(ShotType.SPIRITSHOT);
        boolean bsps = activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT);
        for (WorldObject obj : targets) {
            if (obj instanceof Creature target) {
                boolean acted;
                byte shld;
                if (Formulas.calcSkillReflect(target, skill) == 1)
                    target = activeChar;
                switch (skill.getSkillType()) {
                    case BUFF:
                        if (target.getFirstEffect(L2EffectType.BLOCK_BUFF) != null)
                            break;
                        if (activeChar instanceof Player && target != activeChar && target.isBuffProtected() && !skill.isHeroSkill() && (skill
                                .getSkillType() == L2SkillType.BUFF || skill
                                .getSkillType() == L2SkillType.HEAL_PERCENT || skill
                                .getSkillType() == L2SkillType.MANAHEAL_PERCENT || skill
                                .getSkillType() == L2SkillType.COMBATPOINTHEAL || skill
                                .getSkillType() == L2SkillType.REFLECT))
                            break;
                        if (!(activeChar instanceof net.sf.l2j.gameserver.model.actor.instance.ClanHallManagerNpc) && target != activeChar)
                            if (target instanceof Player) {
                                if (((Player) target).isCursedWeaponEquipped())
                                    break;
                            } else if (player != null && player.isCursedWeaponEquipped()) {
                                break;
                            }
                    case HOT:
                    case CPHOT:
                    case MPHOT:
                        if (activeChar.isInvul())
                            break;
                    default:
                        if (skill.isOffensive() && target.getFirstEffect(L2EffectType.BLOCK_DEBUFF) != null)
                            break;
                        acted = true;
                        shld = 0;
                        if (skill.isOffensive() || skill.isDebuff()) {
                            shld = Formulas.calcShldUse(activeChar, target, skill);
                            acted = Formulas.calcSkillSuccess(activeChar, target, skill, shld, bsps);
                        }
                        if (acted) {
                            if (skill.isToggle())
                                target.stopSkillEffects(skill.getId());
                            if (target instanceof Player && ((Player) target).isInDuel() && (skill.getSkillType() == L2SkillType.DEBUFF || skill.getSkillType() == L2SkillType.BUFF) && player != null && player.getDuelId() == ((Player) target).getDuelId()) {
                                DuelManager dm = DuelManager.getInstance();
                                for (L2Effect buff : skill.getEffects(activeChar, target, new Env(shld, ss, sps, bsps))) {
                                    if (buff != null)
                                        dm.onBuff((Player) target, buff);
                                }
                            } else {
                                skill.getEffects(activeChar, target, new Env(shld, ss, sps, bsps));
                            }
                            if (skill.getSkillType() == L2SkillType.AGGDEBUFF)
                                if (target instanceof net.sf.l2j.gameserver.model.actor.Attackable) {
                                    target.getAI().notifyEvent(AiEventType.AGGRESSION, activeChar, (int) skill.getPower());
                                } else if (target instanceof net.sf.l2j.gameserver.model.actor.Playable) {
                                    if (target.getTarget() == activeChar) {
                                        target.getAI().setIntention(IntentionType.ATTACK, activeChar);
                                    } else {
                                        target.setTarget(activeChar);
                                    }
                                }
                        } else {
                            activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ATTACK_FAILED));
                        }
                        Formulas.calcLethalHit(activeChar, target, skill);
                        break;
                }
            }
        }
        if (skill.hasSelfEffects()) {
            L2Effect effect = activeChar.getFirstEffect(skill.getId());
            if (effect != null && effect.isSelfEffect())
                effect.exit();
            skill.getEffectsSelf(activeChar);
        }
        if (!skill.isPotion())
            activeChar.setChargedShot(bsps ? ShotType.BLESSED_SPIRITSHOT : ShotType.SPIRITSHOT, skill.isStaticReuse());
    }

    public L2SkillType[] getSkillIds() {
        return SKILL_IDS;
    }
}
