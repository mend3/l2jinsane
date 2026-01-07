package net.sf.l2j.gameserver.network.serverpackets;

public class SendTradeDone extends L2GameServerPacket {
    private final int _num;

    public SendTradeDone(int num) {
        this._num = num;
    }

    protected final void writeImpl() {
        writeC(34);
        writeD(this._num);
    }
}
