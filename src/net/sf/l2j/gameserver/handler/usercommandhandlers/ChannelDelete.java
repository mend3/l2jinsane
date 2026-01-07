package net.sf.l2j.gameserver.handler.usercommandhandlers;

import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.group.CommandChannel;
import net.sf.l2j.gameserver.model.group.Party;

public class ChannelDelete implements IUserCommandHandler {
    private static final int[] COMMAND_IDS = new int[]{93};

    public boolean useUserCommand(int id, Player player) {
        Party party = player.getParty();
        if (party == null || !party.isLeader(player))
            return false;
        CommandChannel channel = party.getCommandChannel();
        if (channel == null || !channel.isLeader(player))
            return false;
        channel.disband();
        return true;
    }

    public int[] getUserCommandList() {
        return COMMAND_IDS;
    }
}
