package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;

public class AdminOlympiad implements IAdminCommandHandler {
    private static final String[] ADMIN_COMMANDS = new String[]{"admin_endoly", "admin_sethero", "admin_setnoble"};

    public boolean useAdminCommand(String command, Player activeChar) {
        if (command.startsWith("admin_endoly")) {
            Olympiad.getInstance().manualSelectHeroes();
            activeChar.sendMessage("Heroes have been formed.");
        } else if (command.startsWith("admin_sethero")) {
            Player target = null;
            if (activeChar.getTarget() instanceof Player) {
                target = (Player) activeChar.getTarget();
            } else {
                target = activeChar;
            }
            target.setHero(!target.isHero());
            target.broadcastUserInfo();
            activeChar.sendMessage("You have modified " + target.getName() + "'s hero status.");
        } else if (command.startsWith("admin_setnoble")) {
            Player target = null;
            if (activeChar.getTarget() instanceof Player) {
                target = (Player) activeChar.getTarget();
            } else {
                target = activeChar;
            }
            target.setNoble(!target.isNoble(), true);
            activeChar.sendMessage("You have modified " + target.getName() + "'s noble status.");
        }
        return true;
    }

    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
}
