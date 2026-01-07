package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.gameserver.data.manager.BuyListManager;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.buylist.NpcBuyList;
import net.sf.l2j.gameserver.network.serverpackets.BuyList;

public class AdminShop implements IAdminCommandHandler {
    private static final String[] ADMIN_COMMANDS = new String[]{"admin_buy", "admin_gmshop"};

    public boolean useAdminCommand(String command, Player activeChar) {
        if (command.startsWith("admin_buy")) {
            try {
                int val = Integer.parseInt(command.substring(10));
                NpcBuyList list = BuyListManager.getInstance().getBuyList(val);
                if (list == null) {
                    activeChar.sendMessage("Invalid buylist id.");
                } else {
                    activeChar.sendPacket(new BuyList(list, activeChar.getAdena(), 0.0D));
                }
            } catch (Exception e) {
                activeChar.sendMessage("Invalid buylist id.");
            }
        } else if (command.equals("admin_gmshop")) {
            AdminHelpPage.showHelpPage(activeChar, "gmshops.htm");
        }
        return true;
    }

    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
}
