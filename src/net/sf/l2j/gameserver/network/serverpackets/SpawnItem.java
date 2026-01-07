package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;

public class SpawnItem extends L2GameServerPacket {
    private final int _objectId;

    private final int _itemId;

    private final int _x;

    private final int _y;

    private final int _z;

    private final int _stackable;

    private final int _count;

    public SpawnItem(ItemInstance item) {
        this._objectId = item.getObjectId();
        this._itemId = item.getItemId();
        this._x = item.getX();
        this._y = item.getY();
        this._z = item.getZ();
        this._stackable = item.isStackable() ? 1 : 0;
        this._count = item.getCount();
    }

    public SpawnItem(WorldObject object) {
        this._objectId = object.getObjectId();
        this._itemId = object.getPolyId();
        this._x = object.getX();
        this._y = object.getY();
        this._z = object.getZ();
        this._stackable = 0;
        this._count = 1;
    }

    protected final void writeImpl() {
        writeC(11);
        writeD(this._objectId);
        writeD(this._itemId);
        writeD(this._x);
        writeD(this._y);
        writeD(this._z);
        writeD(this._stackable);
        writeD(this._count);
        writeD(0);
    }
}
