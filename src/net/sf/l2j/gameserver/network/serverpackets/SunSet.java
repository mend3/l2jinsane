package net.sf.l2j.gameserver.network.serverpackets;

public class SunSet extends L2GameServerPacket {
    public static final SunSet STATIC_PACKET = new SunSet();

    protected final void writeImpl() {
        writeC(29);
    }
}
