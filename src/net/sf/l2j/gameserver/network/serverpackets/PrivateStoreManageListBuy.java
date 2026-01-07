package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.tradelist.TradeItem;

import java.util.List;

public class PrivateStoreManageListBuy extends L2GameServerPacket {
    private final int _objId;

    private final int _playerAdena;

    private final ItemInstance[] _itemList;

    private final List<TradeItem> _buyList;

    public PrivateStoreManageListBuy(Player player) {
        this._objId = player.getObjectId();
        this._playerAdena = player.getAdena();
        this._itemList = player.getInventory().getUniqueItems(false, true);
        this._buyList = player.getBuyList().getItems();
    }

    protected final void writeImpl() {
        writeC(183);
        writeD(this._objId);
        writeD(this._playerAdena);
        writeD(this._itemList.length);
        for (ItemInstance item : this._itemList) {
            writeD(item.getItemId());
            writeH(item.getEnchantLevel());
            writeD(item.getCount());
            writeD(item.getReferencePrice());
            writeH(0);
            writeD(item.getItem().getBodyPart());
            writeH(item.getItem().getType2());
        }
        writeD(this._buyList.size());
        for (TradeItem item : this._buyList) {
            writeD(item.getItem().getItemId());
            writeH(item.getEnchant());
            writeD(item.getCount());
            writeD(item.getItem().getReferencePrice());
            writeH(0);
            writeD(item.getItem().getBodyPart());
            writeH(item.getItem().getType2());
            writeD(item.getPrice());
            writeD(item.getItem().getReferencePrice());
        }
    }
}
