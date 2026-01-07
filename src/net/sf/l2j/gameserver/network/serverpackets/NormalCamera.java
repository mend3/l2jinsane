package net.sf.l2j.gameserver.network.serverpackets;

public class NormalCamera extends L2GameServerPacket {
    public static final NormalCamera STATIC_PACKET = new NormalCamera();

    public void writeImpl() {
        writeC(200);
    }
}
