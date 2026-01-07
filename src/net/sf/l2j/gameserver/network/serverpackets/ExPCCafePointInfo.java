package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;

public class ExPCCafePointInfo extends L2GameServerPacket {
    private final Player _character;

    private final int m_AddPoint;

    private final int m_PeriodType;

    private final int RemainTime;

    private final int PointType;

    public ExPCCafePointInfo(Player user, int modify, boolean add, int hour, boolean _double) {
        this._character = user;
        this.m_AddPoint = modify;
        if (add) {
            this.m_PeriodType = 1;
            this.PointType = 1;
        } else if (add && _double) {
            this.m_PeriodType = 1;
            this.PointType = 0;
        } else {
            this.m_PeriodType = 2;
            this.PointType = 2;
        }
        this.RemainTime = hour;
    }

    protected void writeImpl() {
        writeC(254);
        writeH(49);
        writeD(this._character.getPcBangScore());
        writeD(this.m_AddPoint);
        writeC(this.m_PeriodType);
        writeD(this.RemainTime);
        writeC(this.PointType);
    }
}
