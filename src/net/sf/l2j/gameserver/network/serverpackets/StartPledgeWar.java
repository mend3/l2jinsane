package net.sf.l2j.gameserver.network.serverpackets;

public class StartPledgeWar extends L2GameServerPacket {
    private final String _pledgeName;

    private final String _playerName;

    public StartPledgeWar(String pledge, String charName) {
        this._pledgeName = pledge;
        this._playerName = charName;
    }

    protected final void writeImpl() {
        writeC(101);
        writeS(this._playerName);
        writeS(this._pledgeName);
    }
}
