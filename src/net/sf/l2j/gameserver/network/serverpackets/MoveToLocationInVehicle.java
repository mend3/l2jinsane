package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;

public class MoveToLocationInVehicle extends L2GameServerPacket {
    private final int _objectId;

    private final int _boatId;

    private final int _targetX;

    private final int _targetY;

    private final int _targetZ;

    private final int _originX;

    private final int _originY;

    private final int _originZ;

    public MoveToLocationInVehicle(Player player, int targetX, int targetY, int targetZ, int originX, int originY, int originZ) {
        this._objectId = player.getObjectId();
        this._boatId = player.getBoat().getObjectId();
        this._targetX = targetX;
        this._targetY = targetY;
        this._targetZ = targetZ;
        this._originX = originX;
        this._originY = originY;
        this._originZ = originZ;
    }

    protected void writeImpl() {
        writeC(113);
        writeD(this._objectId);
        writeD(this._boatId);
        writeD(this._targetX);
        writeD(this._targetY);
        writeD(this._targetZ);
        writeD(this._originX);
        writeD(this._originY);
        writeD(this._originZ);
    }
}
