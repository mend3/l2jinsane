package net.sf.l2j.gameserver.network.serverpackets;

public class ExShowVariationMakeWindow extends L2GameServerPacket {
    public static final ExShowVariationMakeWindow STATIC_PACKET = new ExShowVariationMakeWindow();

    protected void writeImpl() {
        writeC(254);
        writeH(80);
    }
}
