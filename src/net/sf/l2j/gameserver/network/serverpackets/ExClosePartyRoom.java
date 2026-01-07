package net.sf.l2j.gameserver.network.serverpackets;

public class ExClosePartyRoom extends L2GameServerPacket {
    public static final ExClosePartyRoom STATIC_PACKET = new ExClosePartyRoom();

    protected void writeImpl() {
        writeC(254);
        writeH(15);
    }
}
