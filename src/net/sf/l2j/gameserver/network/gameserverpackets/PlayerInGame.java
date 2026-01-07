package net.sf.l2j.gameserver.network.gameserverpackets;

import java.util.List;

public class PlayerInGame extends GameServerBasePacket {
    public PlayerInGame(String player) {
        writeC(2);
        writeH(1);
        writeS(player);
    }

    public PlayerInGame(List<String> players) {
        writeC(2);
        writeH(players.size());
        for (String pc : players)
            writeS(pc);
    }

    public byte[] getContent() {
        return getBytes();
    }
}
