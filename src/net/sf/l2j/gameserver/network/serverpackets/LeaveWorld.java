package net.sf.l2j.gameserver.network.serverpackets;

public class LeaveWorld extends L2GameServerPacket {
    public static final LeaveWorld STATIC_PACKET = new LeaveWorld();

    protected final void writeImpl() {
        writeC(126);
    }
}
