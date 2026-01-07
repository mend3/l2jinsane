package net.sf.l2j.gameserver.network.gameserverpackets;

public class ChangeAccessLevel extends GameServerBasePacket {
    public ChangeAccessLevel(String player, int access) {
        writeC(4);
        writeD(access);
        writeS(player);
    }

    public byte[] getContent() {
        return getBytes();
    }
}
