package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.item.instance.ItemInstance;

public class GetItem extends L2GameServerPacket {
    private final ItemInstance _item;

    private final int _playerId;

    public GetItem(ItemInstance item, int playerId) {
        this._item = item;
        this._playerId = playerId;
    }

    protected final void writeImpl() {
        writeC(13);
        writeD(this._playerId);
        writeD(this._item.getObjectId());
        writeD(this._item.getX());
        writeD(this._item.getY());
        writeD(this._item.getZ());
    }
}
