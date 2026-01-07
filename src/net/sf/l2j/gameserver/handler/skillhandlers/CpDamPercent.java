package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.gameserver.enums.items.ShotType;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Formulas;

public class CpDamPercent implements ISkillHandler {
    private static final L2SkillType[] SKILL_IDS = new L2SkillType[]{L2SkillType.CPDAMPERCENT};

    public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets) {
        if (activeChar.isAlikeDead())
            return;
        boolean ss = activeChar.isChargedShot(ShotType.SOULSHOT);
        boolean sps = activeChar.isChargedShot(ShotType.SPIRITSHOT);
        boolean bsps = activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT);
        for (WorldObject obj : targets) {
            if (!(obj instanceof Creature target))
                continue;
            if (activeChar instanceof Player && target instanceof Player && ((Player) target).isFakeDeath()) {
                target.stopFakeDeath(true);
            } else if (target.isDead() || target.isInvul()) {
                continue;
            }
            byte shld = Formulas.calcShldUse(activeChar, target, skill);
            int damage = (int) (target.getCurrentCp() * skill.getPower() / 100.0D);
            Formulas.calcCastBreak(target, damage);
            skill.getEffects(activeChar, target, new Env(shld, ss, sps, bsps));
            activeChar.sendDamageMessage(target, damage, false, false, false);
            target.setCurrentCp(target.getCurrentCp() - damage);
            target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_GAVE_YOU_S2_DMG).addCharName(activeChar).addNumber(damage));
            continue;
        }
        activeChar.setChargedShot(ShotType.SOULSHOT, skill.isStaticReuse());
    }

    public L2SkillType[] getSkillIds() {
        return SKILL_IDS;
    }
}
