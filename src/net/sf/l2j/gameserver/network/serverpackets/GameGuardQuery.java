package net.sf.l2j.gameserver.network.serverpackets;

public class GameGuardQuery extends L2GameServerPacket {
    public void runImpl() {
        getClient().setGameGuardOk(false);
    }

    public void writeImpl() {
        writeC(249);
    }
}
