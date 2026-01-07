package net.sf.l2j.gameserver.network.serverpackets;

public class ExShowAdventurerGuideBook extends L2GameServerPacket {
    public static final ExShowAdventurerGuideBook STATIC_PACKET = new ExShowAdventurerGuideBook();

    protected void writeImpl() {
        writeC(254);
        writeH(55);
    }
}
