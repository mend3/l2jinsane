package net.sf.l2j.gameserver.skills.l2skills;

import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.skills.effects.EffectSeed;

public class L2SkillSeed extends L2Skill {
    public L2SkillSeed(StatSet set) {
        super(set);
    }

    public void useSkill(Creature caster, WorldObject[] targets) {
        if (!caster.isAlikeDead()) {
            for (WorldObject obj : targets) {
                if (obj instanceof Creature) {
                    Creature target = (Creature) obj;
                    if (!target.isAlikeDead() || this.getTargetType() == SkillTargetType.TARGET_CORPSE_MOB) {
                        EffectSeed oldEffect = (EffectSeed) target.getFirstEffect(this.getId());
                        if (oldEffect == null) {
                            this.getEffects(caster, target);
                        } else {
                            oldEffect.increasePower();
                        }

                        L2Effect[] effects = target.getAllEffects();

                        for (L2Effect effect : effects) {
                            if (effect.getEffectType() == L2EffectType.SEED) {
                                effect.rescheduleEffect();
                            }
                        }
                    }
                }
            }

        }
    }
}
