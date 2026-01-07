package net.sf.l2j.gameserver.handler.usercommandhandlers;

import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.group.CommandChannel;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class ChannelLeave implements IUserCommandHandler {
    private static final int[] COMMAND_IDS = new int[]{96};

    public boolean useUserCommand(int id, Player player) {
        Party party = player.getParty();
        if (party == null || !party.isLeader(player))
            return false;
        CommandChannel channel = party.getCommandChannel();
        if (channel == null)
            return false;
        channel.removeParty(party);
        party.broadcastMessage(SystemMessageId.LEFT_COMMAND_CHANNEL);
        channel.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_PARTY_LEFT_COMMAND_CHANNEL).addCharName(player));
        return true;
    }

    public int[] getUserCommandList() {
        return COMMAND_IDS;
    }
}
