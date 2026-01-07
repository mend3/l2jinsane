package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.tradelist.TradeItem;

public class TradeOtherAdd extends L2GameServerPacket {
    private final TradeItem _item;

    public TradeOtherAdd(TradeItem item) {
        this._item = item;
    }

    protected final void writeImpl() {
        writeC(33);
        writeH(1);
        writeH(this._item.getItem().getType1());
        writeD(this._item.getObjectId());
        writeD(this._item.getItem().getItemId());
        writeD(this._item.getCount());
        writeH(this._item.getItem().getType2());
        writeH(0);
        writeD(this._item.getItem().getBodyPart());
        writeH(this._item.getEnchant());
        writeH(0);
        writeH(0);
    }
}
