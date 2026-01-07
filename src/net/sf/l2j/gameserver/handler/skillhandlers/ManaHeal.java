package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.enums.items.ShotType;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class ManaHeal implements ISkillHandler {
    private static final L2SkillType[] SKILL_IDS = new L2SkillType[]{L2SkillType.MANAHEAL, L2SkillType.MANARECHARGE};

    public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets) {
        for (WorldObject obj : targets) {
            if (obj instanceof Creature target) {
                if (!target.isInvul()) {
                    double mp = skill.getPower();
                    if (skill.getSkillType() == L2SkillType.MANAHEAL_PERCENT) {
                        mp = target.getMaxMp() * mp / 100.0D;
                    } else {
                        mp = (skill.getSkillType() == L2SkillType.MANARECHARGE) ? target.calcStat(Stats.RECHARGE_MP_RATE, mp, null, null) : mp;
                    }
                    if (target.getCurrentMp() + mp >= target.getMaxMp())
                        mp = target.getMaxMp() - target.getCurrentMp();
                    target.setCurrentMp(mp + target.getCurrentMp());
                    StatusUpdate sump = new StatusUpdate(target);
                    sump.addAttribute(11, (int) target.getCurrentMp());
                    target.sendPacket(sump);
                    if (activeChar instanceof net.sf.l2j.gameserver.model.actor.Player && activeChar != target) {
                        target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_MP_RESTORED_BY_S1).addCharName(activeChar).addNumber((int) mp));
                    } else {
                        target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_MP_RESTORED).addNumber((int) mp));
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
        if (!skill.isPotion())
            activeChar.setChargedShot(activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT) ? ShotType.BLESSED_SPIRITSHOT : ShotType.SPIRITSHOT, skill.isStaticReuse());
    }

    public L2SkillType[] getSkillIds() {
        return SKILL_IDS;
    }
}
