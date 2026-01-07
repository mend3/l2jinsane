package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.location.Location;

public class ObservationReturn extends L2GameServerPacket {
    private final Location _location;

    public ObservationReturn(Location loc) {
        this._location = loc;
    }

    protected final void writeImpl() {
        writeC(224);
        writeD(this._location.getX());
        writeD(this._location.getY());
        writeD(this._location.getZ());
    }
}
