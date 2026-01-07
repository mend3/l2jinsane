package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.tradelist.TradeItem;
import net.sf.l2j.gameserver.model.tradelist.TradeList;

import java.util.List;
import java.util.Set;

public class TradeItemUpdate extends L2GameServerPacket {
    private final Set<ItemInstance> _items;

    private final List<TradeItem> _currentTrade;

    public TradeItemUpdate(TradeList trade, Player activeChar) {
        this._items = activeChar.getInventory().getItems();
        this._currentTrade = trade.getItems();
    }

    private int getItemCount(int objectId) {
        for (ItemInstance item : this._items) {
            if (item.getObjectId() == objectId)
                return item.getCount();
        }
        return 0;
    }

    protected final void writeImpl() {
        writeC(116);
        writeH(this._currentTrade.size());
        for (TradeItem item : this._currentTrade) {
            int availableCount = getItemCount(item.getObjectId()) - item.getCount();
            boolean stackable = item.getItem().isStackable();
            if (availableCount == 0) {
                availableCount = 1;
                stackable = false;
            }
            writeH(stackable ? 3 : 2);
            writeH(item.getItem().getType1());
            writeD(item.getObjectId());
            writeD(item.getItem().getItemId());
            writeD(availableCount);
            writeH(item.getItem().getType2());
            writeH(0);
            writeD(item.getItem().getBodyPart());
            writeH(item.getEnchant());
            writeH(0);
            writeH(0);
        }
    }
}
