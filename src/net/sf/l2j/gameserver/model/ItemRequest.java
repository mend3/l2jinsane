package net.sf.l2j.gameserver.model;

public class ItemRequest {
    int _objectId;

    int _itemId;

    int _count;

    int _price;

    public ItemRequest(int objectId, int count, int price) {
        this._objectId = objectId;
        this._count = count;
        this._price = price;
    }

    public ItemRequest(int objectId, int itemId, int count, int price) {
        this._objectId = objectId;
        this._itemId = itemId;
        this._count = count;
        this._price = price;
    }

    public int getObjectId() {
        return this._objectId;
    }

    public int getItemId() {
        return this._itemId;
    }

    public int getCount() {
        return this._count;
    }

    public void setCount(int count) {
        this._count = count;
    }

    public int getPrice() {
        return this._price;
    }
}
