package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.data.cache.CrestCache;

public class PledgeCrest extends L2GameServerPacket {
    private final int _crestId;

    private final byte[] _data;

    public PledgeCrest(int crestId) {
        this._crestId = crestId;
        this._data = CrestCache.getInstance().getCrest(CrestCache.CrestType.PLEDGE, this._crestId);
    }

    protected final void writeImpl() {
        writeC(108);
        writeD(this._crestId);
        if (this._data != null) {
            writeD(this._data.length);
            writeB(this._data);
        } else {
            writeD(0);
        }
    }
}
