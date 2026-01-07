package net.sf.l2j.gameserver.network.serverpackets;

public class ExPledgeCrestLarge extends L2GameServerPacket {
    private final int _crestId;

    private final byte[] _data;

    public ExPledgeCrestLarge(int crestId, byte[] data) {
        this._crestId = crestId;
        this._data = data;
    }

    protected void writeImpl() {
        writeC(254);
        writeH(40);
        writeD(0);
        writeD(this._crestId);
        if (this._data.length > 0) {
            writeD(this._data.length);
            writeB(this._data);
        } else {
            writeD(0);
        }
    }
}
