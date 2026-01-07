package net.sf.l2j.gameserver.handler.chathandlers;

import net.sf.l2j.gameserver.handler.IChatHandler;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.FloodProtectors;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;

public class ChatHeroVoice implements IChatHandler {
    private static final int[] COMMAND_IDS = new int[]{17};

    public void handleChat(int type, Player activeChar, String target, String text) {
        if (!activeChar.isHero())
            return;
        if (!FloodProtectors.performAction(activeChar.getClient(), FloodProtectors.Action.HERO_VOICE))
            return;
        CreatureSay cs = new CreatureSay(activeChar.getObjectId(), type, activeChar.getName(), text);
        for (Player player : World.getInstance().getPlayers())
            player.sendPacket(cs);
    }

    public int[] getChatTypeList() {
        return COMMAND_IDS;
    }
}
