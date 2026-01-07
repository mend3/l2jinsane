package net.sf.l2j.gameserver.network.serverpackets;

public class ExOpenMPCC extends L2GameServerPacket {
    public static final ExOpenMPCC STATIC_PACKET = new ExOpenMPCC();

    protected void writeImpl() {
        writeC(254);
        writeH(37);
    }
}
