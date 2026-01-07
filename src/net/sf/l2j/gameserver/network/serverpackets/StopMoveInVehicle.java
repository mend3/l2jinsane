package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.SpawnLocation;

public class StopMoveInVehicle extends L2GameServerPacket {
    private final int _objectId;

    private final int _boatId;

    private final SpawnLocation _loc;

    public StopMoveInVehicle(Player player, int boatId) {
        this._objectId = player.getObjectId();
        this._boatId = boatId;
        this._loc = player.getBoatPosition();
    }

    protected void writeImpl() {
        writeC(114);
        writeD(this._objectId);
        writeD(this._boatId);
        writeD(this._loc.getX());
        writeD(this._loc.getY());
        writeD(this._loc.getZ());
        writeD(this._loc.getHeading());
    }
}
