package net.sf.l2j.gameserver.handler.admincommandhandlers;

import mods.dungeon.DungeonManager;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.util.variables.MariaDB;

import java.util.StringTokenizer;

public class AdminDungeon implements IAdminCommandHandler {
    public void useAdminCommand(String command, Player activeChar) {
        if (command.startsWith("admin_resetdungeon")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            if (!st.hasMoreTokens()) {
                activeChar.sendMessage("Write the name.");
                return;
            }
            String target_name = st.nextToken();
            Player player = World.getInstance().getPlayer(target_name);
            if (player == null) {
                activeChar.sendMessage("Player is offline");
                return;
            }
            DungeonManager.getInstance().getPlayerData().remove(player.getHWID());
            MariaDB.set("DELETE FROM dungeon WHERE ipaddr=?", player.getHWID());
            activeChar.sendMessage("You cleared the dungeon limits from player: " + target_name + " success");
        }
    }

    public String[] getAdminCommandList() {
        return new String[]{"admin_resetdungeon"};
    }
}
