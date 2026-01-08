package net.sf.l2j.gameserver.handler;

import net.sf.l2j.gameserver.model.actor.Player;

public interface IVoicedCommandHandler {
    void useVoicedCommand(String paramString1, Player paramPlayer, String paramString2);

    String[] getVoicedCommandList();
}
