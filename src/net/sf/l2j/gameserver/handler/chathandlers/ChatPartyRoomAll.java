package net.sf.l2j.gameserver.handler.chathandlers;

import net.sf.l2j.gameserver.handler.IChatHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.group.CommandChannel;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;

public class ChatPartyRoomAll implements IChatHandler {
    private static final int[] COMMAND_IDS = new int[]{16};

    public void handleChat(int type, Player player, String target, String text) {
        Party party = player.getParty();
        if (party == null || !party.isLeader(player))
            return;
        CommandChannel channel = party.getCommandChannel();
        if (channel == null)
            return;
        channel.broadcastCreatureSay(new CreatureSay(player.getObjectId(), type, player.getName(), text), player);
    }

    public int[] getChatTypeList() {
        return COMMAND_IDS;
    }
}
