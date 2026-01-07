package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.item.instance.ItemInstance;

public class DropItem extends L2GameServerPacket {
    private final ItemInstance _item;

    private final int _charObjId;

    public DropItem(ItemInstance item, int playerObjId) {
        this._item = item;
        this._charObjId = playerObjId;
    }

    protected final void writeImpl() {
        writeC(12);
        writeD(this._charObjId);
        writeD(this._item.getObjectId());
        writeD(this._item.getItemId());
        writeD(this._item.getX());
        writeD(this._item.getY());
        writeD(this._item.getZ());
        if (this._item.isStackable()) {
            writeD(1);
        } else {
            writeD(0);
        }
        writeD(this._item.getCount());
        writeD(1);
    }
}
