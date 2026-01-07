package net.sf.l2j.gameserver.network.serverpackets;

public class SurrenderPledgeWar extends L2GameServerPacket {
    private final String _pledgeName;

    private final String _playerName;

    public SurrenderPledgeWar(String pledge, String charName) {
        this._pledgeName = pledge;
        this._playerName = charName;
    }

    protected final void writeImpl() {
        writeC(105);
        writeS(this._pledgeName);
        writeS(this._playerName);
    }
}
