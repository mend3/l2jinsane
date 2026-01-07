package net.sf.l2j.gameserver.handler.admincommandhandlers;

import mods.dungeon.DungeonManager;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;

public class AdminDungeonReload implements IAdminCommandHandler {
    public boolean useAdminCommand(String command, Player activeChar) {
        if (command.equals("admin_dungeon_reload")) {
            if (DungeonManager.getInstance().isReloading()) {
                activeChar.sendMessage("A reload command has already been issued.");
                return false;
            }
            if (DungeonManager.getInstance().reload()) {
                activeChar.sendMessage("dungeons.xml has been reloaded.");
            } else {
                activeChar.sendMessage("There are currently active dungeons running, the reload will be completed when they finish.");
            }
        }
        return true;
    }

    public String[] getAdminCommandList() {
        return new String[]{"admin_dungeon_reload", "admin_bot_reload"};
    }
}
