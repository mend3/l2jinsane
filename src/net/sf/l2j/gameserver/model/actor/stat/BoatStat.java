package net.sf.l2j.gameserver.model.actor.stat;

import net.sf.l2j.gameserver.model.actor.Boat;

public class BoatStat extends CreatureStat {
    private int _moveSpeed = 0;
    private int _rotationSpeed = 0;

    public BoatStat(Boat activeChar) {
        super(activeChar);
    }

    public float getMoveSpeed() {
        return (float) this._moveSpeed;
    }

    public final void setMoveSpeed(int speed) {
        this._moveSpeed = speed;
    }

    public final int getRotationSpeed() {
        return this._rotationSpeed;
    }

    public final void setRotationSpeed(int speed) {
        this._rotationSpeed = speed;
    }
}
