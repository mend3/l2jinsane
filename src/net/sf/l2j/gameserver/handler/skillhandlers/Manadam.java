package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.enums.items.ShotType;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Formulas;

public class Manadam implements ISkillHandler {
    private static final L2SkillType[] SKILL_IDS = new L2SkillType[]{L2SkillType.MANADAM};

    public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets) {
        if (activeChar.isAlikeDead())
            return;
        boolean sps = activeChar.isChargedShot(ShotType.SPIRITSHOT);
        boolean bsps = activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT);
        for (WorldObject obj : targets) {
            if (obj instanceof Creature target) {
                if (Formulas.calcSkillReflect(target, skill) == 1)
                    target = activeChar;
                boolean acted = Formulas.calcMagicAffected(activeChar, target, skill);
                if (target.isInvul() || !acted) {
                    activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.MISSED_TARGET));
                } else {
                    if (skill.hasEffects()) {
                        byte shld = Formulas.calcShldUse(activeChar, target, skill);
                        target.stopSkillEffects(skill.getId());
                        if (Formulas.calcSkillSuccess(activeChar, target, skill, shld, bsps)) {
                            skill.getEffects(activeChar, target, new Env(shld, sps, false, bsps));
                        } else {
                            activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(skill));
                        }
                    }
                    double damage = Formulas.calcManaDam(activeChar, target, skill, sps, bsps);
                    double mp = (damage > target.getCurrentMp()) ? target.getCurrentMp() : damage;
                    target.reduceCurrentMp(mp);
                    if (damage > 0.0D)
                        target.stopEffectsOnDamage(true);
                    if (target instanceof net.sf.l2j.gameserver.model.actor.Player) {
                        StatusUpdate sump = new StatusUpdate(target);
                        sump.addAttribute(11, (int) target.getCurrentMp());
                        target.sendPacket(sump);
                        target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_MP_HAS_BEEN_DRAINED_BY_S1).addCharName(activeChar).addNumber((int) mp));
                    }
                    if (activeChar instanceof net.sf.l2j.gameserver.model.actor.Player)
                        activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_OPPONENTS_MP_WAS_REDUCED_BY_S1).addNumber((int) mp));
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
