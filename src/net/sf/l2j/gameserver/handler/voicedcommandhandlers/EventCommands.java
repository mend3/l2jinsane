package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import net.sf.l2j.gameserver.events.eventengine.manager.CtfEventManager;
import net.sf.l2j.gameserver.events.eventengine.manager.DmEventManager;
import net.sf.l2j.gameserver.events.eventengine.manager.TvTEventManager;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;

public class EventCommands implements IVoicedCommandHandler {
    public boolean useVoicedCommand(String command, Player activeChar, String target) {
        if (command.equals("register")) {
            if (TvTEventManager.getInstance().getActiveEvent() != null) {
                TvTEventManager.getInstance().registerPlayer(activeChar);
                return true;
            }
            if (CtfEventManager.getInstance().getActiveEvent() != null) {
                CtfEventManager.getInstance().registerPlayer(activeChar);
                return true;
            }
            if (DmEventManager.getInstance().getActiveEvent() != null) {
                DmEventManager.getInstance().registerPlayer(activeChar);
                return true;
            }
        } else if (command.equals("leave")) {
            if (TvTEventManager.getInstance().getActiveEvent() != null) {
                TvTEventManager.getInstance().removePlayer(activeChar);
                return true;
            }
            if (CtfEventManager.getInstance().getActiveEvent() != null) {
                CtfEventManager.getInstance().removePlayer(activeChar);
                return true;
            }
            if (DmEventManager.getInstance().getActiveEvent() != null) {
                DmEventManager.getInstance().removePlayer(activeChar);
                return true;
            }
        }
        return false;
    }

    public String[] getVoicedCommandList() {
        return new String[]{"register", "leave"};
    }
}
