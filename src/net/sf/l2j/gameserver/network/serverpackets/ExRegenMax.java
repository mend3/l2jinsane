package net.sf.l2j.gameserver.network.serverpackets;

public final class ExRegenMax extends L2GameServerPacket {
    private final int _count;

    private final int _time;

    private final double _hpRegen;

    public ExRegenMax(int count, int time, double hpRegen) {
        this._count = count;
        this._time = time;
        this._hpRegen = hpRegen * 0.66D;
    }

    protected void writeImpl() {
        writeC(254);
        writeH(1);
        writeD(1);
        writeD(this._count);
        writeD(this._time);
        writeF(this._hpRegen);
    }
}
