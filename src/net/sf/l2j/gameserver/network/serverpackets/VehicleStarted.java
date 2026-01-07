package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Creature;

public class VehicleStarted extends L2GameServerPacket {
    private final int _objectId;

    private final int _state;

    public VehicleStarted(Creature boat, int state) {
        this._objectId = boat.getObjectId();
        this._state = state;
    }

    protected void writeImpl() {
        writeC(186);
        writeD(this._objectId);
        writeD(this._state);
    }
}
