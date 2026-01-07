package net.sf.l2j.gameserver.network.serverpackets;

import java.util.Map;

public class ExGetBossRecord extends L2GameServerPacket {
    private final Map<Integer, Integer> _bossRecordInfo;

    private final int _ranking;

    private final int _totalPoints;

    public ExGetBossRecord(int ranking, int totalScore, Map<Integer, Integer> list) {
        this._ranking = ranking;
        this._totalPoints = totalScore;
        this._bossRecordInfo = list;
    }

    protected void writeImpl() {
        writeC(254);
        writeH(51);
        writeD(this._ranking);
        writeD(this._totalPoints);
        if (this._bossRecordInfo == null) {
            writeD(0);
            writeD(0);
            writeD(0);
            writeD(0);
        } else {
            writeD(this._bossRecordInfo.size());
            for (Map.Entry<Integer, Integer> bossEntry : this._bossRecordInfo.entrySet()) {
                writeD(bossEntry.getKey());
                writeD(bossEntry.getValue());
                writeD(0);
            }
        }
    }
}
