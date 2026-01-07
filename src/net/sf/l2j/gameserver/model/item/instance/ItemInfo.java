package net.sf.l2j.gameserver.model.item.instance;

import net.sf.l2j.gameserver.model.item.kind.Item;

public class ItemInfo {
    private int _objectId;

    private Item _item;

    private int _enchant;

    private int _augmentation;

    private int _count;

    private int _price;

    private int _type1;

    private int _type2;

    private int _equipped;

    private ItemInstance.ItemState _change;

    private int _mana;

    public ItemInfo(ItemInstance item) {
        if (item == null)
            return;
        this._objectId = item.getObjectId();
        this._item = item.getItem();
        this._enchant = item.getEnchantLevel();
        if (item.isAugmented()) {
            this._augmentation = item.getAugmentation().getAugmentationId();
        } else {
            this._augmentation = 0;
        }
        this._count = item.getCount();
        this._type1 = item.getCustomType1();
        this._type2 = item.getCustomType2();
        this._equipped = item.isEquipped() ? 1 : 0;
        this._change = item.getLastChange();
        this._mana = item.getMana();
    }

    public ItemInfo(ItemInstance item, ItemInstance.ItemState change) {
        if (item == null)
            return;
        this._objectId = item.getObjectId();
        this._item = item.getItem();
        this._enchant = item.getEnchantLevel();
        if (item.isAugmented()) {
            this._augmentation = item.getAugmentation().getAugmentationId();
        } else {
            this._augmentation = 0;
        }
        this._count = item.getCount();
        this._type1 = item.getCustomType1();
        this._type2 = item.getCustomType2();
        this._equipped = item.isEquipped() ? 1 : 0;
        this._change = change;
        this._mana = item.getMana();
    }

    public int getObjectId() {
        return this._objectId;
    }

    public Item getItem() {
        return this._item;
    }

    public int getEnchant() {
        return this._enchant;
    }

    public int getAugmentationBoni() {
        return this._augmentation;
    }

    public int getCount() {
        return this._count;
    }

    public int getPrice() {
        return this._price;
    }

    public int getCustomType1() {
        return this._type1;
    }

    public int getCustomType2() {
        return this._type2;
    }

    public int getEquipped() {
        return this._equipped;
    }

    public ItemInstance.ItemState getChange() {
        return this._change;
    }

    public int getMana() {
        return this._mana;
    }
}
