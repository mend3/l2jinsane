package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.player.Experience;
import net.sf.l2j.gameserver.network.SystemMessageId;

import java.util.StringTokenizer;

public class AdminLevel implements IAdminCommandHandler {
    private static final String[] ADMIN_COMMANDS = new String[]{"admin_addlevel", "admin_setlevel"};

    public void useAdminCommand(String command, Player activeChar) {
        if (activeChar == null)
            return;
        WorldObject targetChar = activeChar.getTarget();
        StringTokenizer st = new StringTokenizer(command, " ");
        String actualCommand = st.nextToken();
        String val = "";
        if (st.countTokens() >= 1)
            val = st.nextToken();
        if (actualCommand.equalsIgnoreCase("admin_addlevel")) {
            try {
                if (targetChar instanceof Playable)
                    ((Playable) targetChar).getStat().addLevel(Byte.parseByte(val));
            } catch (NumberFormatException e) {
                activeChar.sendMessage("Wrong number format.");
            }
        } else if (actualCommand.equalsIgnoreCase("admin_setlevel")) {
            try {
                if (!(targetChar instanceof Player targetPlayer)) {
                    activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
                    return;
                }
                byte lvl = Byte.parseByte(val);
                if (lvl >= 1 && lvl <= 81) {
                    long pXp = targetPlayer.getExp();
                    long tXp = Experience.LEVEL[lvl];
                    if (pXp > tXp) {
                        targetPlayer.removeExpAndSp(pXp - tXp, 0);
                    } else if (pXp < tXp) {
                        targetPlayer.addExpAndSp(tXp - pXp, 0);
                    }
                } else {
                    activeChar.sendMessage("You must specify level between 1 and 81.");
                }
            } catch (NumberFormatException e) {
                activeChar.sendMessage("You must specify level between 1 and 81.");
            }
        }
    }

    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
}
