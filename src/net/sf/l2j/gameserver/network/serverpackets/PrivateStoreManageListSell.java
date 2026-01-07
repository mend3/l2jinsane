package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.tradelist.TradeItem;

import java.util.List;

public class PrivateStoreManageListSell extends L2GameServerPacket {
    private final int _objId;

    private final int _playerAdena;

    private final boolean _packageSale;

    private final TradeItem[] _itemList;

    private final List<TradeItem> _sellList;

    public PrivateStoreManageListSell(Player player, boolean isPackageSale) {
        this._objId = player.getObjectId();
        this._playerAdena = player.getAdena();
        player.getSellList().updateItems();
        this._packageSale = player.getSellList().isPackaged() || isPackageSale;
        this._itemList = player.getInventory().getAvailableItems(player.getSellList());
        this._sellList = player.getSellList().getItems();
    }

    protected final void writeImpl() {
        writeC(154);
        writeD(this._objId);
        writeD(this._packageSale ? 1 : 0);
        writeD(this._playerAdena);
        writeD(this._itemList.length);
        for (TradeItem item : this._itemList) {
            writeD(item.getItem().getType2());
            writeD(item.getObjectId());
            writeD(item.getItem().getItemId());
            writeD(item.getCount());
            writeH(0);
            writeH(item.getEnchant());
            writeH(0);
            writeD(item.getItem().getBodyPart());
            writeD(item.getPrice());
        }
        writeD(this._sellList.size());
        for (TradeItem item : this._sellList) {
            writeD(item.getItem().getType2());
            writeD(item.getObjectId());
            writeD(item.getItem().getItemId());
            writeD(item.getCount());
            writeH(0);
            writeH(item.getEnchant());
            writeH(0);
            writeD(item.getItem().getBodyPart());
            writeD(item.getPrice());
            writeD(item.getItem().getReferencePrice());
        }
    }
}
