package net.sf.l2j.gameserver.handler.usercommandhandlers;

import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.group.CommandChannel;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.network.serverpackets.ExMultiPartyCommandChannelInfo;

public class ChannelListUpdate implements IUserCommandHandler {
    private static final int[] COMMAND_IDS = new int[]{97};

    public boolean useUserCommand(int id, Player player) {
        Party party = player.getParty();
        if (party == null)
            return false;
        CommandChannel channel = party.getCommandChannel();
        if (channel == null)
            return false;
        player.sendPacket(new ExMultiPartyCommandChannelInfo(channel));
        return true;
    }

    public int[] getUserCommandList() {
        return COMMAND_IDS;
    }
}
