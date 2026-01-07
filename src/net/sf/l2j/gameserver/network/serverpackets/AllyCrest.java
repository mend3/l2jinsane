package net.sf.l2j.gameserver.network.serverpackets;

public class AllyCrest extends L2GameServerPacket {
    private final int _crestId;

    private final int _crestSize;

    private final byte[] _data;

    public AllyCrest(int crestId, byte[] data) {
        this._crestId = crestId;
        this._data = data;
        this._crestSize = this._data.length;
    }

    protected final void writeImpl() {
        writeC(174);
        writeD(this._crestId);
        writeD(this._crestSize);
        writeB(this._data);
    }
}
