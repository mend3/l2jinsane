package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.group.Party;

public class ExMPCCPartyInfoUpdate extends L2GameServerPacket {
    private final String _name;

    private final int _leaderObjectId;

    private final int _membersCount;

    private final int _mode;

    public ExMPCCPartyInfoUpdate(Party party, int mode) {
        this._name = party.getLeader().getName();
        this._leaderObjectId = party.getLeaderObjectId();
        this._membersCount = party.getMembersCount();
        this._mode = mode;
    }

    protected void writeImpl() {
        writeC(254);
        writeH(90);
        writeS(this._name);
        writeD(this._leaderObjectId);
        writeD(this._membersCount);
        writeD(this._mode);
    }
}
