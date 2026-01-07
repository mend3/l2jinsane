package net.sf.l2j.gameserver.handler.skillhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.items.ShotType;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.handler.ISkillHandler;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.taskmanager.CancelTaskManager;

import java.util.Vector;

public class Cancel implements ISkillHandler {
    private static final L2SkillType[] SKILL_IDS = new L2SkillType[]{L2SkillType.CANCEL, L2SkillType.MAGE_BANE, L2SkillType.WARRIOR_BANE};

    private static boolean calcCancelSuccess(int effectPeriod, int diffLevel, double baseRate, double vuln, int minRate, int maxRate) {
        double rate = ((2 * diffLevel) + baseRate + (effectPeriod / 120)) * vuln;
        if (Config.DEVELOPER)
            LOGGER.info("calcCancelSuccess(): diffLevel:{}, baseRate:{}, vuln:{}, total:{}.", diffLevel, baseRate, vuln, rate);
        if (rate < minRate) {
            rate = minRate;
        } else if (rate > maxRate) {
            rate = maxRate;
        }
        return (Rnd.get(100) < rate);
    }

    public void useSkill(Creature activeChar, L2Skill skill, WorldObject[] targets) {
        int minRate = (skill.getSkillType() == L2SkillType.CANCEL) ? 25 : 40;
        int maxRate = (skill.getSkillType() == L2SkillType.CANCEL) ? 75 : 95;
        double skillPower = skill.getPower();
        for (WorldObject obj : targets) {
            if (obj instanceof Creature target) {
                if (!target.isDead()) {
                    int lastCanceledSkillId = 0;
                    int count = skill.getMaxNegatedEffects();
                    int diffLevel = skill.getMagicLevel() - target.getLevel();
                    double skillVuln = Formulas.calcSkillVulnerability(activeChar, target, skill, skill.getSkillType());
                    Vector<L2Skill> cancelledBuffs = new Vector<>();
                    for (L2Effect effect : target.getAllEffects()) {
                        if (effect == null || effect.getSkill().isToggle())
                            continue;
                        switch (skill.getSkillType()) {
                            case MAGE_BANE:

                            case WARRIOR_BANE:

                            default:
                                if (effect.getSkill().getId() == lastCanceledSkillId)
                                    break;
                                if (calcCancelSuccess(effect.getPeriod(), diffLevel, skillPower, skillVuln, minRate, maxRate)) {
                                    lastCanceledSkillId = effect.getSkill().getId();
                                    if (!cancelledBuffs.contains(effect.getSkill()) && !((Player) activeChar).isInOlympiadMode() && Config.CANCEL_RETURN)
                                        cancelledBuffs.add(effect.getSkill());
                                    effect.exit();
                                }
                                if (cancelledBuffs.size() > 0 && Config.CANCEL_RETURN)
                                    ThreadPool.schedule(new CancelTaskManager((Player) target, cancelledBuffs), (Config.CANCEL_SECONDS * 1000L));
                                count--;
                                if (count == 0)
                                    break;
                                break;
                        }
                        continue;
                    }
                    Formulas.calcLethalHit(activeChar, target, skill);
                }
            }
        }
        if (skill.hasSelfEffects()) {
            L2Effect effect = activeChar.getFirstEffect(skill.getId());
            if (effect != null && effect.isSelfEffect())
                effect.exit();
            skill.getEffectsSelf(activeChar);
        }
        activeChar.setChargedShot(activeChar.isChargedShot(ShotType.BLESSED_SPIRITSHOT) ? ShotType.BLESSED_SPIRITSHOT : ShotType.SPIRITSHOT, skill.isStaticReuse());
    }

    public L2SkillType[] getSkillIds() {
        return SKILL_IDS;
    }
}
