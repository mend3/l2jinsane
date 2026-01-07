package net.sf.l2j.gameserver.network.serverpackets;

public class ExUseSharedGroupItem extends L2GameServerPacket {
    private final int _itemId;

    private final int _grpId;

    private final int _remainedTime;

    private final int _totalTime;

    public ExUseSharedGroupItem(int itemId, int grpId, int remainedTime, int totalTime) {
        this._itemId = itemId;
        this._grpId = grpId;
        this._remainedTime = remainedTime / 1000;
        this._totalTime = totalTime / 1000;
    }

    protected void writeImpl() {
        writeC(254);
        writeH(73);
        writeD(this._itemId);
        writeD(this._grpId);
        writeD(this._remainedTime);
        writeD(this._totalTime);
    }
}
