package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Boat;

public class VehicleInfo extends L2GameServerPacket {
    private final int _objectId;

    private final int _x;

    private final int _y;

    private final int _z;

    private final int _heading;

    public VehicleInfo(Boat boat) {
        this._objectId = boat.getObjectId();
        this._x = boat.getX();
        this._y = boat.getY();
        this._z = boat.getZ();
        this._heading = boat.getHeading();
    }

    protected void writeImpl() {
        writeC(89);
        writeD(this._objectId);
        writeD(this._x);
        writeD(this._y);
        writeD(this._z);
        writeD(this._heading);
    }
}
