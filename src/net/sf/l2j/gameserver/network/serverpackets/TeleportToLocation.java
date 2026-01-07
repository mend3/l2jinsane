package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.WorldObject;

public class TeleportToLocation extends L2GameServerPacket {
    private final int _objectId;

    private final int _x;

    private final int _y;

    private final int _z;

    private final boolean _isFastTeleport;

    public TeleportToLocation(WorldObject object, int x, int y, int z, boolean isFastTeleport) {
        this._objectId = object.getObjectId();
        this._x = x;
        this._y = y;
        this._z = z;
        this._isFastTeleport = isFastTeleport;
    }

    protected final void writeImpl() {
        writeC(40);
        writeD(this._objectId);
        writeD(this._x);
        writeD(this._y);
        writeD(this._z);
        writeD(this._isFastTeleport ? 1 : 0);
    }
}
