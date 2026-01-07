package net.sf.l2j.gameserver.network.serverpackets;

public class ExOrcMove extends L2GameServerPacket {
    public static final ExOrcMove STATIC_PACKET = new ExOrcMove();

    protected void writeImpl() {
        writeC(254);
        writeH(68);
    }
}
