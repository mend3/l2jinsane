package net.sf.l2j.gameserver.handler.chathandlers;

import net.sf.l2j.gameserver.handler.IChatHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoom;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoomList;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;

public class ChatPartyMatchRoom implements IChatHandler {
    private static final int[] COMMAND_IDS = new int[]{14};

    public void handleChat(int type, Player activeChar, String target, String text) {
        if (!activeChar.isInPartyMatchRoom())
            return;
        PartyMatchRoom room = PartyMatchRoomList.getInstance().getPlayerRoom(activeChar);
        if (room == null)
            return;
        CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), text);
        for (Player member : room.getPartyMembers())
            member.sendPacket(cs);
    }

    public int[] getChatTypeList() {
        return COMMAND_IDS;
    }
}
