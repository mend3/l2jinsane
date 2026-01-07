package net.sf.l2j.gameserver.network.serverpackets;

public class TutorialCloseHtml extends L2GameServerPacket {
    public static final TutorialCloseHtml STATIC_PACKET = new TutorialCloseHtml();

    protected void writeImpl() {
        writeC(163);
    }
}
