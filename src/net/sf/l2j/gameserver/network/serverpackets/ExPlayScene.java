package net.sf.l2j.gameserver.network.serverpackets;

public class ExPlayScene extends L2GameServerPacket {
    public static final ExPlayScene STATIC_PACKET = new ExPlayScene();

    protected void writeImpl() {
        writeC(254);
        writeH(91);
    }
}
