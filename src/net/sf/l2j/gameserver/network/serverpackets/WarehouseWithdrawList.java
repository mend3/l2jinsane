package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Item;

import java.util.Set;

public class WarehouseWithdrawList extends L2GameServerPacket {
    public static final int PRIVATE = 1;

    public static final int CLAN = 2;

    public static final int CASTLE = 3;

    public static final int FREIGHT = 4;

    private int _whType;

    private int _playerAdena;

    private Set<ItemInstance> _items;

    public WarehouseWithdrawList(Player player, int type) {
        if (player.getActiveWarehouse() == null)
            return;
        this._whType = type;
        this._playerAdena = player.getAdena();
        this._items = player.getActiveWarehouse().getItems();
    }

    protected final void writeImpl() {
        writeC(66);
        writeH(this._whType);
        writeD(this._playerAdena);
        writeH(this._items.size());
        for (ItemInstance temp : this._items) {
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
            if (temp.isAugmented()) {
                writeD(0xFFFF & temp.getAugmentation().getAugmentationId());
                writeD(temp.getAugmentation().getAugmentationId() >> 16);
                continue;
            }
            writeQ(0L);
        }
    }
}
