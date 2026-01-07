package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Boat;

public class VehicleDeparture extends L2GameServerPacket {
    private final int _objectId;

    private final int _x;

    private final int _y;

    private final int _z;

    private final int _moveSpeed;

    private final int _rotationSpeed;

    public VehicleDeparture(Boat boat) {
        this._objectId = boat.getObjectId();
        this._x = boat.getXdestination();
        this._y = boat.getYdestination();
        this._z = boat.getZdestination();
        this._moveSpeed = (int) boat.getStat().getMoveSpeed();
        this._rotationSpeed = boat.getStat().getRotationSpeed();
    }

    protected void writeImpl() {
        writeC(90);
        writeD(this._objectId);
        writeD(this._moveSpeed);
        writeD(this._rotationSpeed);
        writeD(this._x);
        writeD(this._y);
        writeD(this._z);
    }
}
