package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public class AdminHelpPage implements IAdminCommandHandler {
    private static final String[] ADMIN_COMMANDS = new String[]{"admin_help"};

    public static void showHelpPage(Player targetChar, String filename) {
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setFile("data/html/admin/" + filename);
        targetChar.sendPacket(html);
    }

    public boolean useAdminCommand(String command, Player activeChar) {
        if (command.startsWith("admin_help"))
            try {
                String val = command.substring(11);
                showHelpPage(activeChar, val);
            } catch (StringIndexOutOfBoundsException stringIndexOutOfBoundsException) {
            }
        return true;
    }

    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
}
