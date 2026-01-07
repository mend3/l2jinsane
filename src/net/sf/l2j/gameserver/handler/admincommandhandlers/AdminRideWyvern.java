package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;

import java.util.StringTokenizer;

public class AdminRideWyvern implements IAdminCommandHandler {
    private static final String[] ADMIN_COMMANDS = new String[]{"admin_ride", "admin_unride"};

    private int _petRideId;

    public boolean useAdminCommand(String command, Player activeChar) {
        if (command.startsWith("admin_ride")) {
            if (activeChar.isCursedWeaponEquipped()) {
                activeChar.sendMessage("You can't use //ride owning a Cursed Weapon.");
                return false;
            }
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            if (st.hasMoreTokens()) {
                String mount = st.nextToken();
                if (mount.equals("wyvern") || mount.equals("2")) {
                    this._petRideId = 12621;
                } else if (mount.equals("strider") || mount.equals("1")) {
                    this._petRideId = 12526;
                } else {
                    activeChar.sendMessage("Parameter '" + mount + "' isn't recognized for that command.");
                    return false;
                }
            } else {
                activeChar.sendMessage("You must enter a parameter for that command.");
                return false;
            }
            if (activeChar.isMounted()) {
                activeChar.dismount();
            } else if (activeChar.getSummon() != null) {
                activeChar.getSummon().unSummon(activeChar);
            }
            activeChar.mount(this._petRideId, 0);
        } else if (command.equals("admin_unride")) {
            activeChar.dismount();
        }
        return true;
    }

    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
}
