package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.tradelist.TradeItem;

import java.util.List;

public class PrivateStoreListBuy extends L2GameServerPacket {
    private final Player _storePlayer;

    private final int _playerAdena;

    private final List<TradeItem> _items;

    public PrivateStoreListBuy(Player player, Player storePlayer) {
        this._storePlayer = storePlayer;
        this._storePlayer.getSellList().updateItems();
        this._playerAdena = player.getAdena();
        this._items = this._storePlayer.getBuyList().getAvailableItems(player.getInventory());
    }

    protected final void writeImpl() {
        writeC(184);
        writeD(this._storePlayer.getObjectId());
        writeD(this._playerAdena);
        writeD(this._items.size());
        for (TradeItem item : this._items) {
            writeD(item.getObjectId());
            writeD(item.getItem().getItemId());
            writeH(item.getEnchant());
            writeD(item.getCount());
            writeD(item.getItem().getReferencePrice());
            writeH(0);
            writeD(item.getItem().getBodyPart());
            writeH(item.getItem().getType2());
            writeD(item.getPrice());
            writeD(item.getCount());
        }
    }
}
