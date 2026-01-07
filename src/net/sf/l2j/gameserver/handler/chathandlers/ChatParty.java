package net.sf.l2j.gameserver.handler.chathandlers;

import net.sf.l2j.gameserver.handler.IChatHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;

public class ChatParty implements IChatHandler {
    private static final int[] COMMAND_IDS = new int[]{3};

    public void handleChat(int type, Player player, String target, String text) {
        Party party = player.getParty();
        if (party == null)
            return;
        party.broadcastPacket(new CreatureSay(player.getObjectId(), type, player.getName(), text));
    }

    public int[] getChatTypeList() {
        return COMMAND_IDS;
    }
}
