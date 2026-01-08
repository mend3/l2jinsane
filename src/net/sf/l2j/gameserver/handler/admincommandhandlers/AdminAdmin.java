package net.sf.l2j.gameserver.handler.admincommandhandlers;

import mods.instance.InstanceManager;
import mods.teleportInterface.TeleportLocationDataGK;
import net.sf.l2j.Config;
import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.gameserver.data.DailyRewardData;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.cache.CrestCache;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.data.manager.CursedWeaponManager;
import net.sf.l2j.gameserver.data.manager.ZoneManager;
import net.sf.l2j.gameserver.data.xml.*;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;

import java.util.StringTokenizer;

public class AdminAdmin implements IAdminCommandHandler {
    private static final String[] ADMIN_COMMANDS = new String[]{"admin_admin", "admin_admin1", "admin_admin2", "admin_admin3", "admin_admin4", "admin_gmlist", "admin_kill", "admin_silence", "admin_tradeoff", "admin_reload"};

    private static void kill(Player activeChar, Creature target) {
        if (target instanceof Player) {
            if (!target.isGM())
                target.stopAllEffects();
            target.reduceCurrentHp((target.getMaxHp() + target.getMaxCp() + 1), activeChar, null);
        } else if (target.isChampion()) {
            target.reduceCurrentHp((target.getMaxHp() * Config.CHAMPION_HP + 1), activeChar, null);
        } else {
            target.reduceCurrentHp((target.getMaxHp() + 1), activeChar, null);
        }
    }

    private static void showMainPage(Player activeChar, String command) {
        int mode = 0;
        String filename = null;
        try {
            mode = Integer.parseInt(command.substring(11));
        } catch (Exception ignored) {
        }
        switch (mode) {
            case 1:
                filename = "main";
                break;
            case 2:
                filename = "game";
                break;
            case 3:
                filename = "effects";
                break;
            case 4:
                filename = "server";
                break;
            default:
                filename = "main";
                break;
        }
        AdminHelpPage.showHelpPage(activeChar, filename + "_menu.htm");
    }

    public boolean useAdminCommand(String command, Player activeChar) {
        if (command.startsWith("admin_admin")) {
            showMainPage(activeChar, command);
        } else if (command.startsWith("admin_gmlist")) {
            activeChar.sendMessage(AdminData.getInstance().showOrHideGm(activeChar) ? "Removed from GMList." : "Registered into GMList.");
        } else if (command.startsWith("admin_kill")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            if (!st.hasMoreTokens()) {
                WorldObject obj = activeChar.getTarget();
                if (!(obj instanceof Creature)) {
                    activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
                } else {
                    kill(activeChar, (Creature) obj);
                }
                return true;
            }
            String firstParam = st.nextToken();
            Player player = World.getInstance().getPlayer(firstParam);
            if (player != null) {
                if (st.hasMoreTokens()) {
                    String secondParam = st.nextToken();
                    if (StringUtil.isDigit(secondParam)) {
                        int radius = Integer.parseInt(secondParam);
                        for (Creature knownChar : player.getKnownTypeInRadius(Creature.class, radius)) {
                            if (knownChar.equals(activeChar))
                                continue;
                            kill(activeChar, knownChar);
                        }
                        activeChar.sendMessage("Killed all characters within a " + radius + " unit radius around " + player.getName() + ".");
                    } else {
                        activeChar.sendMessage("Invalid radius.");
                    }
                } else {
                    kill(activeChar, player);
                }
            } else if (StringUtil.isDigit(firstParam)) {
                int radius = Integer.parseInt(firstParam);
                for (Creature knownChar : activeChar.getKnownTypeInRadius(Creature.class, radius))
                    kill(activeChar, knownChar);
                activeChar.sendMessage("Killed all characters within a " + radius + " unit radius.");
            }
        } else if (command.startsWith("admin_silence")) {
            if (activeChar.isInRefusalMode()) {
                activeChar.setInRefusalMode(false);
                activeChar.sendPacket(SystemMessageId.MESSAGE_ACCEPTANCE_MODE);
            } else {
                activeChar.setInRefusalMode(true);
                activeChar.sendPacket(SystemMessageId.MESSAGE_REFUSAL_MODE);
            }
        } else if (command.startsWith("admin_tradeoff")) {
            try {
                String mode = command.substring(15);
                if (mode.equalsIgnoreCase("on")) {
                    activeChar.setTradeRefusal(true);
                    activeChar.sendMessage("Trade refusal enabled");
                } else if (mode.equalsIgnoreCase("off")) {
                    activeChar.setTradeRefusal(false);
                    activeChar.sendMessage("Trade refusal disabled");
                }
            } catch (Exception e) {
                if (activeChar.getTradeRefusal()) {
                    activeChar.setTradeRefusal(false);
                    activeChar.sendMessage("Trade refusal disabled");
                } else {
                    activeChar.setTradeRefusal(true);
                    activeChar.sendMessage("Trade refusal enabled");
                }
            }
        } else if (command.startsWith("admin_reload")) {
            StringTokenizer st = new StringTokenizer(command);
            st.nextToken();
            try {
                do {
                    String type = st.nextToken();
                    if (type.startsWith("admin")) {
                        AdminData.getInstance().reload();
                        activeChar.sendMessage("Admin data has been reloaded.");
                    } else if (type.startsWith("announcement")) {
                        AnnouncementData.getInstance().reload();
                        activeChar.sendMessage("The content of announcements.xml has been reloaded.");
                    } else if (type.startsWith("config")) {
                        Config.loadGameServer();
                        activeChar.sendMessage("Configs files have been reloaded.");
                    } else if (type.startsWith("crest")) {
                        CrestCache.getInstance().reload();
                        activeChar.sendMessage("Crests have been reloaded.");
                    } else if (type.startsWith("cw")) {
                        CursedWeaponManager.getInstance().reload();
                        activeChar.sendMessage("Cursed weapons have been reloaded.");
                    } else if (type.startsWith("door")) {
                        DoorData.getInstance().reload();
                        activeChar.sendMessage("Doors instance has been reloaded.");
                    } else if (type.startsWith("htm")) {
                        HtmCache.getInstance().reload();
                        activeChar.sendMessage("The HTM cache has been reloaded.");
                    } else if (type.startsWith("item")) {
                        ItemTable.getInstance().reload();
                        activeChar.sendMessage("Items' templates have been reloaded.");
                    } else if (type.equals("multisell")) {
                        MultisellData.getInstance().reload();
                        activeChar.sendMessage("The multisell instance has been reloaded.");
                    } else if (type.equals("npc")) {
                        NpcData.getInstance().reload();
                        activeChar.sendMessage("NPCs templates have been reloaded.");
                    } else if (type.startsWith("npcwalker")) {
                        WalkerRouteData.getInstance().reload();
                        activeChar.sendMessage("Walker routes have been reloaded.");
                    } else if (type.startsWith("skill")) {
                        SkillTable.getInstance().reload();
                        activeChar.sendMessage("Skills' XMLs have been reloaded.");
                    } else if (type.startsWith("teleport")) {
                        TeleportLocationData.getInstance().reload();
                        activeChar.sendMessage("Teleport locations have been reloaded.");
                    } else if (type.equals("teleto")) {
                        TeleportLocationDataGK.getInstance().reload();
                        activeChar.sendMessage("Teleport Reloads.");
                    } else if (type.startsWith("zone")) {
                        ZoneManager.getInstance().reload();
                        activeChar.sendMessage("Zones have been reloaded.");
                    } else if (type.startsWith("fpc")) {
                        FakePcsTable.getInstance().reload();
                        activeChar.sendMessage("Fake PC templates have been reloaded.");
                    } else if (type.startsWith("dailyreward")) {
                        DailyRewardData.getInstance().reload();
                        activeChar.sendMessage("Daily rewards have been reloaded.");
                    } else {
                        activeChar.sendMessage("Usage : //reload <admin|announcement|config|crest|cw>");
                        activeChar.sendMessage("Usage : //reload <door|htm|item|multisell|npc>");
                        activeChar.sendMessage("Usage : //reload <npcwalker|skill|teleport|zone>");
                        activeChar.sendMessage("Usage : //reload <spawnlist|sysstring|teleto|capsule>");
                    }
                } while (st.hasMoreTokens());
            } catch (Exception e) {
                activeChar.sendMessage("Usage : //reload <admin|announcement|config|crest|cw>");
                activeChar.sendMessage("Usage : //reload <door|htm|item|multisell|npc>");
                activeChar.sendMessage("Usage : //reload <npcwalker|skill|teleport|zone>");
                activeChar.sendMessage("Usage : //reload <spawnlist|sysstring|teleto|capsule>");
            }
        } else if (command.startsWith("admin_getinstance")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            if (!st.hasMoreTokens()) {
                activeChar.sendMessage("Write the name.");
                return false;
            }
            String target_name = st.nextToken();
            Player player = World.getInstance().getPlayer(target_name);
            if (player == null) {
                activeChar.sendMessage("Player is offline");
                return false;
            }
            activeChar.setInstance(player.getInstance(), false);
            activeChar.sendMessage("You are with the same instance of player " + target_name);
        } else if (command.startsWith("admin_resetmyinstance")) {
            activeChar.setInstance(InstanceManager.getInstance().getInstance(0), false);
            activeChar.sendMessage("Your instance is now default");
        }
        return true;
    }

    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
}
