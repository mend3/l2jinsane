package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.gameserver.enums.PolyType;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;

import java.util.StringTokenizer;

public class AdminPolymorph implements IAdminCommandHandler {
    private static final String[] ADMIN_COMMANDS = new String[]{"admin_polymorph", "admin_unpolymorph", "admin_polymorph_menu", "admin_unpolymorph_menu"};

    public boolean useAdminCommand(String command, Player activeChar) {
        Player player = null;
        if (activeChar.isMounted())
            return false;
        WorldObject target = activeChar.getTarget();
        if (target == null)
            player = activeChar;
        if (command.startsWith("admin_polymorph")) {
            try {
                StringTokenizer st = new StringTokenizer(command);
                st.nextToken();
                PolyType info = PolyType.NPC;
                if (st.countTokens() > 1)
                    info = Enum.valueOf(PolyType.class, st.nextToken().toUpperCase());
                int npcId = Integer.parseInt(st.nextToken());
                if (!player.polymorph(info, npcId)) {
                    activeChar.sendPacket(SystemMessageId.APPLICANT_INFORMATION_INCORRECT);
                    return true;
                }
                activeChar.sendMessage("You polymorphed " + player.getName() + " into a " + info + " using id: " + npcId + ".");
            } catch (Exception e) {
                activeChar.sendMessage("Usage: //polymorph <type> <id>");
            }
        } else if (command.startsWith("admin_unpolymorph")) {
            if (player.getPolyType() == PolyType.DEFAULT) {
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
                return true;
            }
            player.unpolymorph();
            activeChar.sendMessage("You successfully unpolymorphed " + player.getName() + ".");
        }
        if (command.contains("menu"))
            AdminHelpPage.showHelpPage(activeChar, "effects_menu.htm");
        return true;
    }

    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
}
