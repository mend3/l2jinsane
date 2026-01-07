package net.sf.l2j.gameserver.network.serverpackets;

public class ExShowQuestMark extends L2GameServerPacket {
    private final int _questId;

    public ExShowQuestMark(int questId) {
        this._questId = questId;
    }

    protected void writeImpl() {
        writeC(254);
        writeH(26);
        writeD(this._questId);
    }
}
