package net.sf.l2j.gameserver.network.serverpackets;

public class ShowPCCafeCouponShowUI extends L2GameServerPacket {
    public static final ShowPCCafeCouponShowUI STATIC_PACKET = new ShowPCCafeCouponShowUI();

    protected void writeImpl() {
        writeC(254);
        writeH(67);
    }
}
