package net.sf.l2j.gameserver.network.serverpackets;

public class ServerClose extends L2GameServerPacket {
    public static final ServerClose STATIC_PACKET = new ServerClose();

    protected void writeImpl() {
        writeC(38);
    }
}
