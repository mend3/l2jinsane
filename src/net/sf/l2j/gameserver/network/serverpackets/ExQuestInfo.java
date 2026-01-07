package net.sf.l2j.gameserver.network.serverpackets;

public class ExQuestInfo extends L2GameServerPacket {
    public static final ExQuestInfo STATIC_PACKET = new ExQuestInfo();

    protected void writeImpl() {
        writeC(254);
        writeH(25);
    }
}
