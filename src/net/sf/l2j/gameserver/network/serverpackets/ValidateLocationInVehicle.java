package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.SpawnLocation;

public class ValidateLocationInVehicle extends L2GameServerPacket {
    private final int _objectId;

    private final int _boatId;

    private final SpawnLocation _loc;

    public ValidateLocationInVehicle(Player player) {
        this._objectId = player.getObjectId();
        this._boatId = player.getBoat().getObjectId();
        this._loc = player.getBoatPosition();
    }

    protected final void writeImpl() {
        writeC(115);
        writeD(this._objectId);
        writeD(this._boatId);
        writeD(this._loc.getX());
        writeD(this._loc.getY());
        writeD(this._loc.getZ());
        writeD(this._loc.getHeading());
    }
}
