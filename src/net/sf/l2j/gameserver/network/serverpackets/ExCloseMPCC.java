package net.sf.l2j.gameserver.network.serverpackets;

public class ExCloseMPCC extends L2GameServerPacket {
    public static final ExCloseMPCC STATIC_PACKET = new ExCloseMPCC();

    protected void writeImpl() {
        writeC(254);
        writeH(38);
    }
}
