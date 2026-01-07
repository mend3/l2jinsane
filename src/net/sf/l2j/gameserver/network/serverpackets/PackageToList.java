package net.sf.l2j.gameserver.network.serverpackets;

import java.util.Map;

public class PackageToList extends L2GameServerPacket {
    private final Map<Integer, String> _players;

    public PackageToList(Map<Integer, String> players) {
        this._players = players;
    }

    protected void writeImpl() {
        writeC(194);
        writeD(this._players.size());
        for (Map.Entry<Integer, String> playerEntry : this._players.entrySet()) {
            writeD(playerEntry.getKey());
            writeS(playerEntry.getValue());
        }
    }
}
