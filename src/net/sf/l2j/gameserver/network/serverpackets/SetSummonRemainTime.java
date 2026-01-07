package net.sf.l2j.gameserver.network.serverpackets;

public class SetSummonRemainTime extends L2GameServerPacket {
    private final int _maxTime;

    private final int _remainingTime;

    public SetSummonRemainTime(int maxTime, int remainingTime) {
        this._remainingTime = remainingTime;
        this._maxTime = maxTime;
    }

    protected final void writeImpl() {
        writeC(209);
        writeD(this._maxTime);
        writeD(this._remainingTime);
    }
}
