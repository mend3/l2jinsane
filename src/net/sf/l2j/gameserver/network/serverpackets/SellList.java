package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.item.instance.ItemInstance;

import java.util.List;

public class SellList extends L2GameServerPacket {
    private final int _money;

    private final List<ItemInstance> _items;

    public SellList(int adena, List<ItemInstance> items) {
        this._money = adena;
        this._items = items;
    }

    protected final void writeImpl() {
        writeC(16);
        writeD(this._money);
        writeD(0);
        writeH(this._items.size());
        for (ItemInstance item : this._items) {
            writeH(item.getItem().getType1());
            writeD(item.getObjectId());
            writeD(item.getItemId());
            writeD(item.getCount());
            writeH(item.getItem().getType2());
            writeH(item.getCustomType1());
            writeD(item.getItem().getBodyPart());
            writeH(item.getEnchantLevel());
            writeH(item.getCustomType2());
            writeH(0);
            writeD(item.getItem().getReferencePrice() / 2);
        }
    }
}
