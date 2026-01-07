package net.sf.l2j.gameserver.network.serverpackets;

public final class TutorialShowQuestionMark extends L2GameServerPacket {
    private final int _markId;

    public TutorialShowQuestionMark(int blink) {
        this._markId = blink;
    }

    protected void writeImpl() {
        writeC(161);
        writeD(this._markId);
    }
}
