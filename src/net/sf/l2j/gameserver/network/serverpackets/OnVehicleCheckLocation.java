package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Creature;

public class OnVehicleCheckLocation extends L2GameServerPacket {
    private final Creature _boat;

    public OnVehicleCheckLocation(Creature boat) {
        this._boat = boat;
    }

    protected void writeImpl() {
        writeC(91);
        writeD(this._boat.getObjectId());
        writeD(this._boat.getX());
        writeD(this._boat.getY());
        writeD(this._boat.getZ());
        writeD(this._boat.getHeading());
    }
}
