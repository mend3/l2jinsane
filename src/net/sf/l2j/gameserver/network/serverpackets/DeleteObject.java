package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.WorldObject;

public class DeleteObject extends L2GameServerPacket {
    private final int _objectId;

    private final boolean _isSeated;

    public DeleteObject(WorldObject obj) {
        this._objectId = obj.getObjectId();
        this._isSeated = false;
    }

    public DeleteObject(WorldObject obj, boolean sit) {
        this._objectId = obj.getObjectId();
        this._isSeated = sit;
    }

    protected final void writeImpl() {
        writeC(18);
        writeD(this._objectId);
        writeD(this._isSeated ? 0 : 1);
    }
}
