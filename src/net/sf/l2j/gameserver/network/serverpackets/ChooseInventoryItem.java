package net.sf.l2j.gameserver.network.serverpackets;

public class ChooseInventoryItem extends L2GameServerPacket {
    private final int _itemId;

    public ChooseInventoryItem(int itemId) {
        this._itemId = itemId;
    }

    protected final void writeImpl() {
        writeC(111);
        writeD(this._itemId);
    }
}
