package net.sf.l2j.gameserver.skills.l2skills;

import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.EffectPoint;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.location.Location;

public final class L2SkillSignet extends L2Skill {
    private final int _effectNpcId;

    public int effectId;

    public L2SkillSignet(StatSet set) {
        super(set);
        this._effectNpcId = set.getInteger("effectNpcId", -1);
        this.effectId = set.getInteger("effectId", -1);
    }

    public void useSkill(Creature caster, WorldObject[] targets) {
        if (caster.isAlikeDead())
            return;
        NpcTemplate template = NpcData.getInstance().getTemplate(this._effectNpcId);
        EffectPoint effectPoint = new EffectPoint(IdFactory.getInstance().getNextId(), template, caster);
        effectPoint.setCurrentHp(effectPoint.getMaxHp());
        effectPoint.setCurrentMp(effectPoint.getMaxMp());
        Location worldPosition = null;
        if (caster instanceof Player && getTargetType() == L2Skill.SkillTargetType.TARGET_GROUND)
            worldPosition = ((Player) caster).getCurrentSkillWorldPosition();
        getEffects(caster, effectPoint);
        effectPoint.setIsInvul(true);
        effectPoint.spawnMe((worldPosition != null) ? worldPosition : (Location) caster.getPosition());
    }
}
