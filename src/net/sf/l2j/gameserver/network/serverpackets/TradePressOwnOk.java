package net.sf.l2j.gameserver.network.serverpackets;

public final class TradePressOwnOk extends L2GameServerPacket {
    public static final TradePressOwnOk STATIC_PACKET = new TradePressOwnOk();

    protected void writeImpl() {
        writeC(117);
    }
}
