package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.gameserver.data.manager.SevenSignsManager;
import net.sf.l2j.gameserver.data.sql.AutoSpawnTable;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.spawn.AutoSpawn;

public class AdminMammon implements IAdminCommandHandler {
    private static final String[] ADMIN_COMMANDS = new String[]{"admin_mammon_find", "admin_mammon_respawn"};

    public void useAdminCommand(String command, Player activeChar) {
        if (command.startsWith("admin_mammon_find")) {
            int teleportIndex = -1;
            try {
                teleportIndex = Integer.parseInt(command.substring(18));
            } catch (Exception NumberFormatException) {
                activeChar.sendMessage("Usage: //mammon_find [teleportIndex] (1 / 2)");
                return;
            }
            if (!SevenSignsManager.getInstance().isSealValidationPeriod()) {
                activeChar.sendMessage("The competition period is currently in effect.");
                return;
            }
            if (teleportIndex == 1) {
                AutoSpawn blackSpawnInst = AutoSpawnTable.getInstance().getAutoSpawnInstance(31126, false);
                if (blackSpawnInst != null) {
                    Npc[] blackInst = blackSpawnInst.getNPCInstanceList();
                    if (blackInst.length > 0) {
                        int x1 = blackInst[0].getX(), y1 = blackInst[0].getY(), z1 = blackInst[0].getZ();
                        activeChar.sendMessage("Blacksmith of Mammon: " + x1 + " " + y1 + " " + z1);
                        activeChar.teleportTo(x1, y1, z1, 0);
                    }
                } else {
                    activeChar.sendMessage("Blacksmith of Mammon isn't registered.");
                }
            } else if (teleportIndex == 2) {
                AutoSpawn merchSpawnInst = AutoSpawnTable.getInstance().getAutoSpawnInstance(31113, false);
                if (merchSpawnInst != null) {
                    Npc[] merchInst = merchSpawnInst.getNPCInstanceList();
                    if (merchInst.length > 0) {
                        int x2 = merchInst[0].getX(), y2 = merchInst[0].getY(), z2 = merchInst[0].getZ();
                        activeChar.sendMessage("Merchant of Mammon: " + x2 + " " + y2 + " " + z2);
                        activeChar.teleportTo(x2, y2, z2, 0);
                    }
                } else {
                    activeChar.sendMessage("Merchant of Mammon isn't registered.");
                }
            } else {
                activeChar.sendMessage("Invalid parameter '" + teleportIndex + "' for //mammon_find.");
            }
        } else if (command.startsWith("admin_mammon_respawn")) {
            if (!SevenSignsManager.getInstance().isSealValidationPeriod()) {
                activeChar.sendMessage("The competition period is currently in effect.");
                return;
            }
            AutoSpawn merchSpawnInst = AutoSpawnTable.getInstance().getAutoSpawnInstance(31113, false);
            if (merchSpawnInst != null) {
                long merchRespawn = AutoSpawnTable.getInstance().getTimeToNextSpawn(merchSpawnInst);
                activeChar.sendMessage("The Merchant of Mammon will respawn in " + merchRespawn / 60000L + " minute(s).");
            } else {
                activeChar.sendMessage("Merchant of Mammon isn't registered.");
            }
            AutoSpawn blackSpawnInst = AutoSpawnTable.getInstance().getAutoSpawnInstance(31126, false);
            if (blackSpawnInst != null) {
                long blackRespawn = AutoSpawnTable.getInstance().getTimeToNextSpawn(blackSpawnInst);
                activeChar.sendMessage("The Blacksmith of Mammon will respawn in " + blackRespawn / 60000L + " minute(s).");
            } else {
                activeChar.sendMessage("Blacksmith of Mammon isn't registered.");
            }
        }
    }

    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
}
