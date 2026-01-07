package net.sf.l2j.gameserver.network.serverpackets;

public class ExAskJoinPartyRoom extends L2GameServerPacket {
    private final String _charName;

    public ExAskJoinPartyRoom(String charName) {
        this._charName = charName;
    }

    protected void writeImpl() {
        writeC(254);
        writeH(52);
        writeS(this._charName);
    }
}
