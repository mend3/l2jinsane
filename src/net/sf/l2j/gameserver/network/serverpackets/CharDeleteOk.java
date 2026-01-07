package net.sf.l2j.gameserver.network.serverpackets;

public class CharDeleteOk extends L2GameServerPacket {
    public static final CharDeleteOk STATIC_PACKET = new CharDeleteOk();

    protected final void writeImpl() {
        writeC(35);
    }
}
