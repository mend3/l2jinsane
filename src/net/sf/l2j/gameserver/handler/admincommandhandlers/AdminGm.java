package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;

import java.util.StringTokenizer;

public class AdminGm implements IAdminCommandHandler {
    private static final String[] ADMIN_COMMANDS = new String[]{"admin_gm"};

    public void useAdminCommand(String command, Player activeChar) {
        if (command.startsWith("admin_gm")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            int numberOfMinutes = 1;
            if (st.hasMoreTokens())
                try {
                    numberOfMinutes = Integer.parseInt(st.nextToken());
                } catch (Exception e) {
                    activeChar.sendMessage("Invalid timer setted for //gm ; default time is used.");
                }
            int previousAccessLevel = activeChar.getAccessLevel().getLevel();
            activeChar.setAccessLevel(0);
            activeChar.sendMessage("You no longer have GM status, but will be rehabilitated after " + numberOfMinutes + " minutes.");
            ThreadPool.schedule(() -> {
                if (!activeChar.isOnline())
                    return;
                activeChar.setAccessLevel(previousAccessLevel);
                activeChar.sendMessage("Your previous access level has been rehabilitated.");
            }, (numberOfMinutes * 60000L));
        }
    }

    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
}
