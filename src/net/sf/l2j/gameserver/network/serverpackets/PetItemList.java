package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Item;

import java.util.Set;

public class PetItemList extends L2GameServerPacket {
    private final Set<ItemInstance> _items;

    public PetItemList(Pet character) {
        this._items = character.getInventory().getItems();
    }

    protected final void writeImpl() {
        writeC(178);
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
        }
    }
}
