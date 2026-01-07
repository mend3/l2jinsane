package net.sf.l2j.gameserver.network.serverpackets;

public class PledgeShowMemberListDeleteAll extends L2GameServerPacket {
    public static final PledgeShowMemberListDeleteAll STATIC_PACKET = new PledgeShowMemberListDeleteAll();

    protected final void writeImpl() {
        writeC(130);
    }
}
