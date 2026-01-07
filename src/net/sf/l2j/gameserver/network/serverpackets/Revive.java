package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.WorldObject;

public class Revive extends L2GameServerPacket {
    private final int _objectId;

    public Revive(WorldObject obj) {
        this._objectId = obj.getObjectId();
    }

    protected final void writeImpl() {
        writeC(7);
        writeD(this._objectId);
    }
}
