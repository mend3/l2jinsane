package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.enums.AiEventType;
import net.sf.l2j.gameserver.enums.items.ShotType;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.EffectPoint;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillLaunched;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillSignetCasttime;

import java.util.ArrayList;
import java.util.List;

public class EffectSignetMDam extends L2Effect {
    private EffectPoint _actor;

    public EffectSignetMDam(Env env, EffectTemplate template) {
        super(env, template);
    }

    public L2EffectType getEffectType() {
        return L2EffectType.SIGNET_GROUND;
    }

    public boolean onStart() {
        NpcTemplate template;
        if (getSkill() instanceof L2SkillSignetCasttime) {
            template = NpcData.getInstance().getTemplate(((L2SkillSignetCasttime) getSkill())._effectNpcId);
        } else {
            return false;
        }
        EffectPoint effectPoint = new EffectPoint(IdFactory.getInstance().getNextId(), template, getEffector());
        effectPoint.setCurrentHp(effectPoint.getMaxHp());
        effectPoint.setCurrentMp(effectPoint.getMaxMp());
        Location worldPosition = null;
        if (getEffector() instanceof Player && getSkill().getTargetType() == L2Skill.SkillTargetType.TARGET_GROUND)
            worldPosition = ((Player) getEffector()).getCurrentSkillWorldPosition();
        effectPoint.setIsInvul(true);
        effectPoint.spawnMe((worldPosition != null) ? worldPosition : (Location) getEffector().getPosition());
        this._actor = effectPoint;
        return true;
    }

    public boolean onActionTime() {
        if (getCount() >= getTotalCount() - 2)
            return true;
        Player caster = (Player) getEffector();
        int mpConsume = getSkill().getMpConsume();
        boolean sps = caster.isChargedShot(ShotType.SPIRITSHOT);
        boolean bsps = caster.isChargedShot(ShotType.BLESSED_SPIRITSHOT);
        List<Creature> targets = new ArrayList<>();
        for (Creature cha : this._actor.getKnownTypeInRadius(Creature.class, getSkill().getSkillRadius())) {
            if (cha == caster)
                continue;
            if (cha instanceof net.sf.l2j.gameserver.model.actor.Attackable || cha instanceof net.sf.l2j.gameserver.model.actor.Playable) {
                if (cha.isAlikeDead())
                    continue;
                if (mpConsume > caster.getCurrentMp()) {
                    caster.sendPacket(SystemMessageId.SKILL_REMOVED_DUE_LACK_MP);
                    return false;
                }
                caster.reduceCurrentMp(mpConsume);
                if (cha instanceof net.sf.l2j.gameserver.model.actor.Playable) {
                    if (caster.canAttackCharacter(cha)) {
                        targets.add(cha);
                        caster.updatePvPStatus(cha);
                    }
                    continue;
                }
                targets.add(cha);
            }
        }
        if (!targets.isEmpty()) {
            caster.broadcastPacket(new MagicSkillLaunched(caster, getSkill().getId(), getSkill().getLevel(), (WorldObject[]) targets.toArray((Object[]) new Creature[targets.size()])));
            for (Creature target : targets) {
                boolean mcrit = Formulas.calcMCrit(caster.getMCriticalHit(target, getSkill()));
                byte shld = Formulas.calcShldUse(caster, target, getSkill());
                int mdam = (int) Formulas.calcMagicDam(caster, target, getSkill(), shld, sps, bsps, mcrit);
                if (target instanceof net.sf.l2j.gameserver.model.actor.Summon)
                    target.broadcastStatusUpdate();
                if (mdam > 0) {
                    Formulas.calcCastBreak(target, mdam);
                    caster.sendDamageMessage(target, mdam, mcrit, false, false);
                    target.reduceCurrentHp(mdam, caster, getSkill());
                }
                target.getAI().notifyEvent(AiEventType.ATTACKED, caster);
            }
        }
        return true;
    }

    public void onExit() {
        if (this._actor != null)
            this._actor.deleteMe();
    }
}
