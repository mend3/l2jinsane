package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Item;

public class TradeStart extends L2GameServerPacket {
    private final Player _activeChar;

    private final ItemInstance[] _itemList;

    public TradeStart(Player player) {
        this._activeChar = player;
        this._itemList = player.getInventory().getAvailableItems(true, false);
    }

    protected final void writeImpl() {
        if (this._activeChar.getActiveTradeList() == null || this._activeChar.getActiveTradeList().getPartner() == null)
            return;
        writeC(30);
        writeD(this._activeChar.getActiveTradeList().getPartner().getObjectId());
        writeH(this._itemList.length);
        for (ItemInstance temp : this._itemList) {
            if (temp != null && temp.getItem() != null) {
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
            }
        }
    }
}
