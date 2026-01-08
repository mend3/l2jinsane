package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.gameserver.data.xml.AdminData;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;

public class AdminGmChat implements IAdminCommandHandler {
    private static final String[] ADMIN_COMMANDS = new String[]{"admin_gmchat", "admin_gmchat_menu"};

    public void useAdminCommand(String command, Player activeChar) {
        if (command.startsWith("admin_gmchat")) {
            try {
                AdminData.getInstance().broadcastToGMs(new CreatureSay(0, 9, activeChar.getName(), command.substring(command.startsWith("admin_gmchat_menu") ? 18 : 13)));
            } catch (StringIndexOutOfBoundsException ignored) {
            }
            if (command.startsWith("admin_gmchat_menu"))
                AdminHelpPage.showHelpPage(activeChar, "main_menu.htm");
        }
    }

    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
}
