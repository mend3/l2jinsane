package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.skills.FlyType;
import net.sf.l2j.gameserver.enums.skills.L2EffectFlag;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.network.serverpackets.FlyToLocation;
import net.sf.l2j.gameserver.network.serverpackets.ValidateLocation;
import net.sf.l2j.gameserver.skills.Env;

public class EffectThrowUp extends L2Effect {
    private int _x;

    private int _y;

    private int _z;

    public EffectThrowUp(Env env, EffectTemplate template) {
        super(env, template);
    }

    public L2EffectType getEffectType() {
        return L2EffectType.THROW_UP;
    }

    public boolean onStart() {
        int curX = getEffected().getX();
        int curY = getEffected().getY();
        int curZ = getEffected().getZ();
        double dx = (getEffector().getX() - curX);
        double dy = (getEffector().getY() - curY);
        double dz = (getEffector().getZ() - curZ);
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance < 1.0D || distance > 2000.0D)
            return false;
        int offset = Math.min((int) distance + getSkill().getFlyRadius(), 1400);
        offset = (int) (offset + Math.abs(dz));
        if (offset < 5)
            offset = 5;
        double sin = dy / distance;
        double cos = dx / distance;
        this._x = getEffector().getX() - (int) (offset * cos);
        this._y = getEffector().getY() - (int) (offset * sin);
        this._z = getEffected().getZ();
        Location destiny = GeoEngine.getInstance().canMoveToTargetLoc(getEffected().getX(), getEffected().getY(), getEffected().getZ(), this._x, this._y, this._z);
        this._x = destiny.getX();
        this._y = destiny.getY();
        getEffected().startStunning();
        getEffected().broadcastPacket(new FlyToLocation(getEffected(), this._x, this._y, this._z, FlyType.THROW_UP));
        return true;
    }

    public boolean onActionTime() {
        return false;
    }

    public void onExit() {
        getEffected().stopStunning(false);
        getEffected().setXYZ(this._x, this._y, this._z);
        getEffected().broadcastPacket(new ValidateLocation(getEffected()));
    }

    public int getEffectFlags() {
        return L2EffectFlag.STUNNED.getMask();
    }
}
