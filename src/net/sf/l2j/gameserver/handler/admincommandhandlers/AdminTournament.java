package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.events.tournament.ArenaTask;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;

public class AdminTournament implements IAdminCommandHandler {
    private static final String[] ADMIN_COMMANDS = new String[]{"admin_tour"};

    private static final CLogger LOGGER = new CLogger(AdminTournament.class.getName());

    public static boolean _arena_manual = false;

    private static void initEventArena() {
        ThreadPool.schedule(ArenaTask::SpawnEvent, 10L);
    }

    private static void finishEventArena() {
        ThreadPool.schedule(ArenaTask::finishEvent, 10L);
    }

    public void useAdminCommand(String command, Player activeChar) {
        if (command.equals("admin_tour"))
            if (ArenaTask._started) {
                LOGGER.info("----------------------------------------------------------------------------");
                LOGGER.info("[Tournament]: Event Finished.");
                LOGGER.info("----------------------------------------------------------------------------");
                ArenaTask._aborted = true;
                finishEventArena();
                _arena_manual = true;
                activeChar.sendMessage("SYS: Voce Finalizou o evento Tournament Manualmente..");
            } else {
                LOGGER.info("----------------------------------------------------------------------------");
                LOGGER.info("[Tournament]: Event Started.");
                LOGGER.info("----------------------------------------------------------------------------");
                initEventArena();
                _arena_manual = true;
                activeChar.sendMessage("SYS: Voce ativou o evento Tournament Manualmente..");
            }
    }

    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
}
