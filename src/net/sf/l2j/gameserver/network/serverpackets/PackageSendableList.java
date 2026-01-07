package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Item;

public class PackageSendableList extends L2GameServerPacket {
    private final ItemInstance[] _items;

    private final int _playerObjId;

    public PackageSendableList(ItemInstance[] items, int playerObjId) {
        this._items = items;
        this._playerObjId = playerObjId;
    }

    protected void writeImpl() {
        writeC(195);
        writeD(this._playerObjId);
        writeD(getClient().getPlayer().getAdena());
        writeD(this._items.length);
        for (ItemInstance temp : this._items) {
            if (temp != null && temp.getItem() != null) {
                Item item = temp.getItem();
                writeH(item.getType1());
                writeD(temp.getObjectId());
                writeD(temp.getItemId());
                writeD(temp.getCount());
                writeH(item.getType2());
                writeH(temp.getCustomType1());
                writeD(item.getBodyPart());
                writeH(temp.getEnchantLevel());
                writeH(temp.getCustomType2());
                writeH(0);
                writeD(temp.getObjectId());
            }
        }
    }
}
