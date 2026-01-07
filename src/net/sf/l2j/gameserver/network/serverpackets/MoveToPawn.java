package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;

public class MoveToPawn extends L2GameServerPacket {
    private final int _objectId;

    private final int _targetId;

    private final int _distance;

    private final int _x;

    private final int _y;

    private final int _z;

    public MoveToPawn(Creature cha, WorldObject target, int distance) {
        this._objectId = cha.getObjectId();
        this._targetId = target.getObjectId();
        this._distance = distance;
        this._x = cha.getX();
        this._y = cha.getY();
        this._z = cha.getZ();
    }

    protected final void writeImpl() {
        writeC(96);
        writeD(this._objectId);
        writeD(this._targetId);
        writeD(this._distance);
        writeD(this._x);
        writeD(this._y);
        writeD(this._z);
    }
}
