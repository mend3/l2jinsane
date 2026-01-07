package net.sf.l2j.gameserver.handler;

import net.sf.l2j.gameserver.model.actor.Player;

public interface IChatHandler {
    void handleChat(int paramInt, Player paramPlayer, String paramString1, String paramString2);

    int[] getChatTypeList();
}
