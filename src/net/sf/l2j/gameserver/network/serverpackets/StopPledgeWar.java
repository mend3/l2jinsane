package net.sf.l2j.gameserver.network.serverpackets;

public class StopPledgeWar extends L2GameServerPacket {
    private final String _pledgeName;

    private final String _playerName;

    public StopPledgeWar(String pledge, String charName) {
        this._pledgeName = pledge;
        this._playerName = charName;
    }

    protected final void writeImpl() {
        writeC(103);
        writeS(this._pledgeName);
        writeS(this._playerName);
    }
}
