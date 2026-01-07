package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.Fence;

public class ExColosseumFenceInfo extends L2GameServerPacket {
    private final int _objectId;

    private final Fence _fence;

    public ExColosseumFenceInfo(int objectId, Fence fence) {
        this._objectId = objectId;
        this._fence = fence;
    }

    protected void writeImpl() {
        writeC(254);
        writeH(9);
        writeD(this._objectId);
        writeD(this._fence.getType());
        writeD(this._fence.getX());
        writeD(this._fence.getY());
        writeD(this._fence.getZ());
        writeD(this._fence.getSizeX());
        writeD(this._fence.getSizeY());
    }
}
