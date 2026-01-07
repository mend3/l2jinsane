package net.sf.l2j.gameserver.network.serverpackets;

public class ExMailArrived extends L2GameServerPacket {
    public static final ExMailArrived STATIC_PACKET = new ExMailArrived();

    protected void writeImpl() {
        writeC(254);
        writeH(45);
    }
}
