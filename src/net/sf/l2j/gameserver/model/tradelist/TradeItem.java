package net.sf.l2j.gameserver.model.tradelist;

import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Item;

public class TradeItem {
    private final Item _item;
    private int _objectId;
    private int _enchant;

    private int _count;

    private int _price;

    public TradeItem(ItemInstance item, int count, int price) {
        this._objectId = item.getObjectId();
        this._item = item.getItem();
        this._enchant = item.getEnchantLevel();
        this._count = count;
        this._price = price;
    }

    public TradeItem(Item item, int count, int price) {
        this._objectId = 0;
        this._item = item;
        this._enchant = 0;
        this._count = count;
        this._price = price;
    }

    public TradeItem(TradeItem item, int count, int price) {
        this._objectId = item.getObjectId();
        this._item = item.getItem();
        this._enchant = item.getEnchant();
        this._count = count;
        this._price = price;
    }

    public int getObjectId() {
        return this._objectId;
    }

    public void setObjectId(int objectId) {
        this._objectId = objectId;
    }

    public Item getItem() {
        return this._item;
    }

    public int getEnchant() {
        return this._enchant;
    }

    public void setEnchant(int enchant) {
        this._enchant = enchant;
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

    public void setPrice(int price) {
        this._price = price;
    }
}
