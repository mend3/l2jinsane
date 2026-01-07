package net.sf.l2j.gameserver.handler;

import net.sf.l2j.gameserver.model.actor.Player;

public interface IBypassHandler {
    boolean handleBypass(String paramString, Player paramPlayer);

    String[] getBypassHandlersList();
}
