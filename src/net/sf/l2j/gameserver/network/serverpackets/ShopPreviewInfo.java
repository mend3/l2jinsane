package net.sf.l2j.gameserver.network.serverpackets;

import java.util.Map;

public class ShopPreviewInfo extends L2GameServerPacket {
    private final Map<Integer, Integer> _itemlist;

    public ShopPreviewInfo(Map<Integer, Integer> itemlist) {
        this._itemlist = itemlist;
    }

    protected final void writeImpl() {
        writeC(240);
        writeD(17);
        writeD(getFromList(2));
        writeD(getFromList(1));
        writeD(getFromList(3));
        writeD(getFromList(5));
        writeD(getFromList(4));
        writeD(getFromList(6));
        writeD(getFromList(7));
        writeD(getFromList(8));
        writeD(getFromList(9));
        writeD(getFromList(10));
        writeD(getFromList(11));
        writeD(getFromList(12));
        writeD(getFromList(13));
        writeD(getFromList(14));
        writeD(getFromList(15));
        writeD(getFromList(16));
        writeD(getFromList(0));
    }

    private int getFromList(int key) {
        return (this._itemlist.get(key) != null) ? this._itemlist.get(key) : 0;
    }
}
