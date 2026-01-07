package net.sf.l2j.gameserver.network.serverpackets;

public class ExOlympiadMatchEnd extends L2GameServerPacket {
    public static final ExOlympiadMatchEnd STATIC_PACKET = new ExOlympiadMatchEnd();

    protected final void writeImpl() {
        writeC(254);
        writeH(44);
    }
}
