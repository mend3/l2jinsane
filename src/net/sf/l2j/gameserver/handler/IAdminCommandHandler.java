package net.sf.l2j.gameserver.handler;

import net.sf.l2j.gameserver.model.actor.Player;

public interface IAdminCommandHandler {
    void useAdminCommand(String paramString, Player paramPlayer);

    String[] getAdminCommandList();
}
