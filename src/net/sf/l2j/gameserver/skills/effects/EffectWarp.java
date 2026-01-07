package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.skills.FlyType;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.network.serverpackets.FlyToLocation;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.skills.Env;

public class EffectWarp extends L2Effect {
    private int x;

    private int y;

    private int z;

    private Creature _actor;

    public EffectWarp(Env env, EffectTemplate template) {
        super(env, template);
    }

    public L2EffectType getEffectType() {
        return L2EffectType.WARP;
    }

    public boolean onStart() {
        this._actor = isSelfEffect() ? getEffector() : getEffected();
        if (this._actor.isMovementDisabled())
            return false;
        int _radius = getSkill().getFlyRadius();
        double angle = MathUtil.convertHeadingToDegree(this._actor.getHeading());
        double radian = Math.toRadians(angle);
        double course = Math.toRadians(getSkill().getFlyCourse());
        int x1 = (int) (Math.cos(Math.PI + radian + course) * _radius);
        int y1 = (int) (Math.sin(Math.PI + radian + course) * _radius);
        this.x = this._actor.getX() + x1;
        this.y = this._actor.getY() + y1;
        this.z = this._actor.getZ();
        Location destiny = GeoEngine.getInstance().canMoveToTargetLoc(this._actor.getX(), this._actor.getY(), this._actor.getZ(), this.x, this.y, this.z);
        this.x = destiny.getX();
        this.y = destiny.getY();
        this.z = destiny.getZ();
        this._actor.getAI().setIntention(IntentionType.IDLE);
        this._actor.broadcastPacket(new FlyToLocation(this._actor, this.x, this.y, this.z, FlyType.DUMMY));
        this._actor.abortAttack();
        this._actor.abortCast();
        this._actor.setXYZ(this.x, this.y, this.z);
        this._actor.broadcastPacket(new ValidateLocation(this._actor));
        return true;
    }

    public boolean onActionTime() {
        return false;
    }
}
