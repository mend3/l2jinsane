package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Item;

import java.util.Set;

public class ItemList extends L2GameServerPacket {
    private final Set<ItemInstance> _items;

    private final boolean _showWindow;

    public ItemList(Player cha, boolean showWindow) {
        this._items = cha.getInventory().getItems();
        this._showWindow = showWindow;
    }

    protected final void writeImpl() {
        writeC(27);
        writeH(this._showWindow ? 1 : 0);
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
