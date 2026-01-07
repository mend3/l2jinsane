package net.sf.l2j.gameserver.handler;

import net.sf.l2j.gameserver.model.actor.Player;

public interface ICustomByPassHandler {
    String[] getByPassCommands();

    void handleCommand(String paramString1, Player paramPlayer, String paramString2);
}
