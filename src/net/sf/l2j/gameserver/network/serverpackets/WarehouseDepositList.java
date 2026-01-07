package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Item;

import java.util.ArrayList;
import java.util.List;

public final class WarehouseDepositList extends L2GameServerPacket {
    public static final int PRIVATE = 1;

    public static final int CLAN = 2;

    public static final int CASTLE = 3;

    public static final int FREIGHT = 4;

    private final int _playerAdena;

    private final List<ItemInstance> _items;

    private final int _whType;

    public WarehouseDepositList(Player player, int type) {
        this._whType = type;
        this._playerAdena = player.getAdena();
        this._items = new ArrayList<>();
        boolean isPrivate = (this._whType == 1);
        for (ItemInstance temp : player.getInventory().getAvailableItems(true, isPrivate)) {
            if (temp != null && temp.isDepositable(isPrivate))
                this._items.add(temp);
        }
    }

    protected void writeImpl() {
        writeC(65);
        writeH(this._whType);
        writeD(this._playerAdena);
        writeH(this._items.size());
        for (ItemInstance temp : this._items) {
            if (temp == null || temp.getItem() == null)
                continue;
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
        this._items.clear();
    }
}
