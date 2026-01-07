package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Item;

import java.util.Set;

public class GMViewItemList extends L2GameServerPacket {
    private final Set<ItemInstance> _items;

    private final int _limit;

    private final String _playerName;

    public GMViewItemList(Player cha) {
        this._items = cha.getInventory().getItems();
        this._playerName = cha.getName();
        this._limit = cha.getInventoryLimit();
    }

    public GMViewItemList(Pet cha) {
        this._items = cha.getInventory().getItems();
        this._playerName = cha.getName();
        this._limit = cha.getInventoryLimit();
    }

    protected final void writeImpl() {
        writeC(148);
        writeS(this._playerName);
        writeD(this._limit);
        writeH(1);
        writeH(this._items.size());
        for (ItemInstance temp : this._items) {
            Item item = temp.getItem();
            writeH(item.getType1());
            writeD(temp.getObjectId());
            writeD(temp.getItemId());
            writeD(temp.getCount());
            writeH(item.getType2());
            writeH(temp.getCustomType1());
            writeH(temp.isEquipped() ? 1 : 0);
            writeD(item.getBodyPart());
            writeH(temp.getEnchantLevel());
            writeH(temp.getCustomType2());
            writeD(temp.isAugmented() ? temp.getAugmentation().getAugmentationId() : 0);
            writeD(temp.getMana());
        }
    }
}
