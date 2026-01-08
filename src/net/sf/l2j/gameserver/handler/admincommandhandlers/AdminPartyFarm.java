package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.events.partyfarm.PartyFarm;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;

import java.util.logging.Logger;

public class AdminPartyFarm implements IAdminCommandHandler {
    protected static final Logger _log = Logger.getLogger(AdminPartyFarm.class.getName());
    private static final String[] ADMIN_COMMANDS = new String[]{"admin_ptfarm"};
    public static boolean _bestfarm_manual = false;

    public static boolean _arena_manual = false;

    private static void initEventPartyFarm() {
        ThreadPool.schedule(PartyFarm::bossSpawnMonster, 1L);
    }

    private static void finishEventPartyFarm() {
        ThreadPool.schedule(PartyFarm::Finish_Event, 1L);
    }

    public boolean useAdminCommand(String command, Player activeChar) {
        if (command.equals("admin_ptfarm"))
            if (PartyFarm._started) {
                _log.info("----------------------------------------------------------------------------");
                _log.info("[Party Farm]: Event Finished.");
                _log.info("----------------------------------------------------------------------------");
                PartyFarm._aborted = true;
                finishEventPartyFarm();
                activeChar.sendMessage("SYS: You have finished the Party Farm Manually...");
            } else {
                _log.info("----------------------------------------------------------------------------");
                _log.info("[Party Farm]: Event Started.");
                _log.info("----------------------------------------------------------------------------");
                initEventPartyFarm();
                _bestfarm_manual = true;
                activeChar.sendMessage("SYS: You have activated Party Farm Manually.");
            }
        return true;
    }

    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
}
