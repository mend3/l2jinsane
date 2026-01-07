package net.sf.l2j.gameserver.network.gameserverpackets;

public class PlayerLogout extends GameServerBasePacket {
    public PlayerLogout(String player) {
        writeC(3);
        writeS(player);
    }

    public byte[] getContent() {
        return getBytes();
    }
}
