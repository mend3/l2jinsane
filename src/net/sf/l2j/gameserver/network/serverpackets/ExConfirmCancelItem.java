package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.item.instance.ItemInstance;

public class ExConfirmCancelItem extends L2GameServerPacket {
    private final int _itemObjId;

    private final int _itemId;

    private final int _itemAug1;

    private final int _itemAug2;

    private final int _price;

    public ExConfirmCancelItem(ItemInstance item, int price) {
        this._itemObjId = item.getObjectId();
        this._itemId = item.getItemId();
        this._price = price;
        this._itemAug1 = (short) item.getAugmentation().getAugmentationId();
        this._itemAug2 = item.getAugmentation().getAugmentationId() >> 16;
    }

    protected void writeImpl() {
        writeC(254);
        writeH(86);
        writeD(this._itemObjId);
        writeD(this._itemId);
        writeD(this._itemAug1);
        writeD(this._itemAug2);
        writeQ(this._price);
        writeD(1);
    }
}
