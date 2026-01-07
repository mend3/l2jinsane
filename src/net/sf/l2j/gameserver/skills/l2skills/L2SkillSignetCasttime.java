package net.sf.l2j.gameserver.skills.l2skills;

import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;

public final class L2SkillSignetCasttime extends L2Skill {
    public int _effectNpcId;

    public int effectId;

    public L2SkillSignetCasttime(StatSet set) {
        super(set);
        this._effectNpcId = set.getInteger("effectNpcId", -1);
        this.effectId = set.getInteger("effectId", -1);
    }

    public void useSkill(Creature caster, WorldObject[] targets) {
        if (caster.isAlikeDead())
            return;
        getEffectsSelf(caster);
    }
}
