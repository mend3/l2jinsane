package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.tradelist.TradeItem;

import java.util.List;

public class PrivateStoreListSell extends L2GameServerPacket {
    private final int _playerAdena;

    private final Player _storePlayer;

    private final List<TradeItem> _items;

    private final boolean _packageSale;

    public PrivateStoreListSell(Player player, Player storePlayer) {
        this._playerAdena = player.getAdena();
        this._storePlayer = storePlayer;
        this._items = this._storePlayer.getSellList().getItems();
        this._packageSale = this._storePlayer.getSellList().isPackaged();
    }

    protected final void writeImpl() {
        writeC(155);
        writeD(this._storePlayer.getObjectId());
        writeD(this._packageSale ? 1 : 0);
        writeD(this._playerAdena);
        writeD(this._items.size());
        for (TradeItem item : this._items) {
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
