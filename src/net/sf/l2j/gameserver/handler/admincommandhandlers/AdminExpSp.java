package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

import java.util.StringTokenizer;

public class AdminExpSp implements IAdminCommandHandler {
    private static final String[] ADMIN_COMMANDS = new String[]{"admin_add_exp_sp_to_character", "admin_add_exp_sp", "admin_remove_exp_sp"};

    private static void addExpSp(Player activeChar) {
        WorldObject target = activeChar.getTarget();
        Player player = null;
        if (target instanceof Player) {
            player = (Player) target;
        } else {
            activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
            return;
        }
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setFile("data/html/admin/expsp.htm");
        html.replace("%name%", player.getName());
        html.replace("%level%", player.getLevel());
        html.replace("%xp%", player.getExp());
        html.replace("%sp%", player.getSp());
        html.replace("%class%", player.getTemplate().getClassName());
        activeChar.sendPacket(html);
    }

    private static boolean adminAddExpSp(Player activeChar, String ExpSp) {
        WorldObject target = activeChar.getTarget();
        Player player = null;
        if (target instanceof Player) {
            player = (Player) target;
        } else {
            activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
            return false;
        }
        StringTokenizer st = new StringTokenizer(ExpSp);
        if (st.countTokens() != 2)
            return false;
        String exp = st.nextToken();
        String sp = st.nextToken();
        long expval = 0L;
        int spval = 0;
        try {
            expval = Long.parseLong(exp);
            spval = Integer.parseInt(sp);
        } catch (Exception e) {
            return false;
        }
        if (expval != 0L || spval != 0) {
            player.sendMessage("Admin is adding you " + expval + " xp and " + spval + " sp.");
            player.addExpAndSp(expval, spval);
            activeChar.sendMessage("Added " + expval + " xp and " + spval + " sp to " + player.getName() + ".");
        }
        return true;
    }

    private static boolean adminRemoveExpSP(Player activeChar, String ExpSp) {
        WorldObject target = activeChar.getTarget();
        Player player = null;
        if (target instanceof Player) {
            player = (Player) target;
        } else {
            activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
            return false;
        }
        StringTokenizer st = new StringTokenizer(ExpSp);
        if (st.countTokens() != 2)
            return false;
        String exp = st.nextToken();
        String sp = st.nextToken();
        long expval = 0L;
        int spval = 0;
        try {
            expval = Long.parseLong(exp);
            spval = Integer.parseInt(sp);
        } catch (Exception e) {
            return false;
        }
        if (expval != 0L || spval != 0) {
            player.sendMessage("Admin is removing you " + expval + " xp and " + spval + " sp.");
            player.removeExpAndSp(expval, spval);
            activeChar.sendMessage("Removed " + expval + " xp and " + spval + " sp from " + player.getName() + ".");
        }
        return true;
    }

    public void useAdminCommand(String command, Player activeChar) {
        if (command.startsWith("admin_add_exp_sp")) {
            try {
                String val = command.substring(16);
                if (!adminAddExpSp(activeChar, val))
                    activeChar.sendMessage("Usage: //add_exp_sp exp sp");
            } catch (StringIndexOutOfBoundsException e) {
                activeChar.sendMessage("Usage: //add_exp_sp exp sp");
            }
        } else if (command.startsWith("admin_remove_exp_sp")) {
            try {
                String val = command.substring(19);
                if (!adminRemoveExpSP(activeChar, val))
                    activeChar.sendMessage("Usage: //remove_exp_sp exp sp");
            } catch (StringIndexOutOfBoundsException e) {
                activeChar.sendMessage("Usage: //remove_exp_sp exp sp");
            }
        }
        addExpSp(activeChar);
    }

    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
}
