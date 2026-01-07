package net.sf.l2j.gameserver.network.serverpackets;

public class AskJoinParty extends L2GameServerPacket {
    private final String _requestorName;

    private final int _itemDistribution;

    public AskJoinParty(String requestorName, int itemDistribution) {
        this._requestorName = requestorName;
        this._itemDistribution = itemDistribution;
    }

    protected final void writeImpl() {
        writeC(57);
        writeS(this._requestorName);
        writeD(this._itemDistribution);
    }
}
