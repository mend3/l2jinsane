package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.taskmanager.DecayTaskManager;

public class AdminRes implements IAdminCommandHandler {
    private static final String[] ADMIN_COMMANDS = new String[]{"admin_res", "admin_res_monster"};

    private static void handleRes(Player activeChar) {
        handleRes(activeChar, null);
    }

    private static void handleRes(Player activeChar, String resParam) {
        Player player = activeChar;
        if(activeChar.getTarget() instanceof Player currentTarget){
            player = currentTarget;
        } else if (resParam != null) {
            Player plyr = World.getInstance().getPlayer(resParam);
            if (plyr != null) {
                player = plyr;
            } else {
                try {
                    int radius = Integer.parseInt(resParam);
                    for (Player knownPlayer : activeChar.getKnownTypeInRadius(Player.class, radius))
                        doResurrect(knownPlayer);
                    activeChar.sendMessage("Resurrected all players within a " + radius + " unit radius.");
                    return;
                } catch (NumberFormatException e) {
                    activeChar.sendMessage("Enter a valid player name or radius.");
                    return;
                }
            }
        }
        doResurrect(player);
    }

    private static void handleNonPlayerRes(Player activeChar) {
        handleNonPlayerRes(activeChar, "");
    }

    private static void handleNonPlayerRes(Player activeChar, String radiusStr) {
        WorldObject obj = activeChar.getTarget();
        try {
            int radius = 0;
            if (!radiusStr.isEmpty()) {
                radius = Integer.parseInt(radiusStr);
                for (Creature knownChar : activeChar.getKnownTypeInRadius(Creature.class, radius)) {
                    if (!(knownChar instanceof Player))
                        doResurrect(knownChar);
                }
                activeChar.sendMessage("Resurrected all non-players within a " + radius + " unit radius.");
            }
        } catch (NumberFormatException e) {
            activeChar.sendMessage("Enter a valid radius.");
            return;
        }
        if (obj instanceof Player) {
            activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
            return;
        }
        doResurrect((Creature) obj);
    }

    private static void doResurrect(Creature targetChar) {
        if (!targetChar.isDead())
            return;
        if (targetChar instanceof Player) {
            ((Player) targetChar).restoreExp(100.0D);
        } else {
            DecayTaskManager.getInstance().cancel(targetChar);
        }
        targetChar.doRevive();
    }

    public void useAdminCommand(String command, Player activeChar) {
        if (command.startsWith("admin_res ")) {
            handleRes(activeChar, command.split(" ")[1]);
        } else if (command.equals("admin_res")) {
            handleRes(activeChar);
        } else if (command.startsWith("admin_res_monster ")) {
            handleNonPlayerRes(activeChar, command.split(" ")[1]);
        } else if (command.equals("admin_res_monster")) {
            handleNonPlayerRes(activeChar);
        }
    }

    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
}
