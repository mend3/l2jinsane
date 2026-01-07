package net.sf.l2j.gameserver.handler.chathandlers;

import net.sf.l2j.gameserver.data.xml.MapRegionData;
import net.sf.l2j.gameserver.handler.IChatHandler;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.player.BlockList;
import net.sf.l2j.gameserver.network.FloodProtectors;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;

public class ChatShout implements IChatHandler {
    private static final int[] COMMAND_IDS = new int[]{1};

    public void handleChat(int type, Player activeChar, String target, String text) {
        if (!FloodProtectors.performAction(activeChar.getClient(), FloodProtectors.Action.GLOBAL_CHAT))
            return;
        CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), text);
        int region = MapRegionData.getInstance().getMapRegion(activeChar.getX(), activeChar.getY());
        for (Player player : World.getInstance().getPlayers()) {
            if (!BlockList.isBlocked(player, activeChar) && region == MapRegionData.getInstance().getMapRegion(player.getX(), player.getY()))
                player.sendPacket(cs);
        }
    }

    public int[] getChatTypeList() {
        return COMMAND_IDS;
    }
}
