package net.sf.l2j.gameserver.handler.admincommandhandlers;

import mods.balancer.ClassBalanceGui;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;

public class AdminBalancer implements IAdminCommandHandler {
    private static final String[] ADMIN_COMMANDS = new String[]{"admin_balancer"};

    public void useAdminCommand(String command, Player activeChar) {
        if (command.equals("admin_balancer")) {
            ClassBalanceGui.getInstance().parseCmd("_bbs_balancer", activeChar);
        }
    }

    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
}
