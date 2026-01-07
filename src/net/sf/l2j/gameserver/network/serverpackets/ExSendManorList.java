package net.sf.l2j.gameserver.network.serverpackets;

public class ExSendManorList extends L2GameServerPacket {
    public static final ExSendManorList STATIC_PACKET = new ExSendManorList();

    private static final String[] MANORS = new String[]{"gludio", "dion", "giran", "oren", "aden", "innadril", "goddard", "rune", "schuttgart"};

    protected void writeImpl() {
        writeC(254);
        writeH(27);
        writeD(MANORS.length);
        for (int i = 0; i < MANORS.length; i++) {
            writeD(i + 1);
            writeS(MANORS[i]);
        }
    }
}
