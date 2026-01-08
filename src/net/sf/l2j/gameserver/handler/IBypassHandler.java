package net.sf.l2j.gameserver.handler;

import net.sf.l2j.gameserver.model.actor.Player;

public interface IBypassHandler {
    void handleBypass(String paramString, Player paramPlayer);

    String[] getBypassHandlersList();
}
