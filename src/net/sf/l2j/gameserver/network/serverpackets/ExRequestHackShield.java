package net.sf.l2j.gameserver.network.serverpackets;

public class ExRequestHackShield extends L2GameServerPacket {
    public static final ExRequestHackShield STATIC_PACKET = new ExRequestHackShield();

    protected void writeImpl() {
        writeC(254);
        writeH(72);
    }
}
