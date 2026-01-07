package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.group.CommandChannel;
import net.sf.l2j.gameserver.model.group.Party;

public class ExMultiPartyCommandChannelInfo extends L2GameServerPacket {
    private final CommandChannel _channel;

    public ExMultiPartyCommandChannelInfo(CommandChannel channel) {
        this._channel = channel;
    }

    protected void writeImpl() {
        if (this._channel == null)
            return;
        writeC(254);
        writeH(48);
        writeS(this._channel.getLeader().getName());
        writeD(0);
        writeD(this._channel.getMembersCount());
        writeD(this._channel.getParties().size());
        for (Party party : this._channel.getParties()) {
            writeS(party.getLeader().getName());
            writeD(party.getLeaderObjectId());
            writeD(party.getMembersCount());
        }
    }
}
