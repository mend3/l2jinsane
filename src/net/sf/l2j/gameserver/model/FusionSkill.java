/**/
package net.sf.l2j.gameserver.model;

import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.skills.effects.EffectFusion;

import java.util.concurrent.Future;
import java.util.logging.Logger;

public final class FusionSkill {
    private static final Logger _log = Logger.getLogger(FusionSkill.class.getName());
    private final int _skillCastRange;
    private final int _fusionId;
    private final int _fusionLevel;
    private final Creature _caster;
    private final Creature _target;
    private final Future<?> _geoCheckTask;

    public FusionSkill(Creature caster, Creature target, L2Skill skill) {
        this._skillCastRange = skill.getCastRange();
        this._caster = caster;
        this._target = target;
        this._fusionId = skill.getTriggeredId();
        this._fusionLevel = skill.getTriggeredLevel();
        L2Effect effect = this._target.getFirstEffect(this._fusionId);
        if (effect != null) {
            ((EffectFusion) effect).increaseEffect();
        } else {
            L2Skill force = SkillTable.getInstance().getInfo(this._fusionId, this._fusionLevel);
            if (force != null) {
                force.getEffects(this._caster, this._target, null);
            } else {
                _log.warning("Triggered skill [" + this._fusionId + ";" + this._fusionLevel + "] not found!");
            }
        }

        this._geoCheckTask = ThreadPool.scheduleAtFixedRate(new FusionSkill.GeoCheckTask(), 1000L, 1000L);
    }

    public Creature getCaster() {
        return this._caster;
    }

    public Creature getTarget() {
        return this._target;
    }

    public void onCastAbort() {
        this._caster.setFusionSkill(null);
        L2Effect effect = this._target.getFirstEffect(this._fusionId);
        if (effect != null) {
            ((EffectFusion) effect).decreaseForce();
        }

        this._geoCheckTask.cancel(true);
    }

    public class GeoCheckTask implements Runnable {
        public void run() {
            try {
                if (!MathUtil.checkIfInRange(FusionSkill.this._skillCastRange, FusionSkill.this._caster, FusionSkill.this._target, true)) {
                    FusionSkill.this._caster.abortCast();
                }

                if (!GeoEngine.getInstance().canSeeTarget(FusionSkill.this._caster, FusionSkill.this._target)) {
                    FusionSkill.this._caster.abortCast();
                }
            } catch (Exception var2) {
            }

        }
    }
}