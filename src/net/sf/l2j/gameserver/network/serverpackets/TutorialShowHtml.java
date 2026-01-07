package net.sf.l2j.gameserver.network.serverpackets;

public final class TutorialShowHtml extends L2GameServerPacket {
    private final String _html;

    public TutorialShowHtml(String html) {
        this._html = html;
    }

    protected void writeImpl() {
        writeC(160);
        writeS(this._html);
    }
}
