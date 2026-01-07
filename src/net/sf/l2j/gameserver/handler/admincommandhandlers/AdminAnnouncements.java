package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.gameserver.data.xml.AnnouncementData;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;

public class AdminAnnouncements implements IAdminCommandHandler {
    private static final String[] ADMIN_COMMANDS = new String[]{"admin_announce", "admin_ann", "admin_say"};

    public boolean useAdminCommand(String command, Player activeChar) {
        if (command.startsWith("admin_announce")) {
            try {
                boolean isAuto;
                String[] split;
                boolean crit, auto;
                int idelay, delay, limit;
                String msg;
                String[] tokens = command.split(" ", 3);
                switch (tokens[1]) {
                    case "list":
                        AnnouncementData.getInstance().listAnnouncements(activeChar);
                        return true;
                    case "all":
                    case "all_auto":
                        isAuto = tokens[1].equalsIgnoreCase("all_auto");
                        for (Player player : World.getInstance().getPlayers())
                            AnnouncementData.getInstance().showAnnouncements(player, isAuto);
                        AnnouncementData.getInstance().listAnnouncements(activeChar);
                        return true;
                    case "add":
                        split = tokens[2].split(" ", 2);
                        crit = Boolean.parseBoolean(split[0]);
                        if (!AnnouncementData.getInstance().addAnnouncement(split[1], crit, false, -1, -1, -1))
                            activeChar.sendMessage("Invalid //announce message content ; can't be null or empty.");
                        AnnouncementData.getInstance().listAnnouncements(activeChar);
                        return true;
                    case "add_auto":
                        split = tokens[2].split(" ", 6);
                        crit = Boolean.parseBoolean(split[0]);
                        auto = Boolean.parseBoolean(split[1]);
                        idelay = Integer.parseInt(split[2]);
                        delay = Integer.parseInt(split[3]);
                        limit = Integer.parseInt(split[4]);
                        msg = split[5];
                        if (!AnnouncementData.getInstance().addAnnouncement(msg, crit, auto, idelay, delay, limit))
                            activeChar.sendMessage("Invalid //announce message content ; can't be null or empty.");
                        AnnouncementData.getInstance().listAnnouncements(activeChar);
                        return true;
                    case "del":
                        AnnouncementData.getInstance().delAnnouncement(Integer.parseInt(tokens[2]));
                        AnnouncementData.getInstance().listAnnouncements(activeChar);
                        return true;
                }
                activeChar.sendMessage("Possible //announce parameters : <list|all|add|add_auto|del>");
            } catch (Exception e) {
                activeChar.sendMessage("Possible //announce parameters : <list|all|add|add_auto|del>");
            }
        } else if (command.startsWith("admin_ann") || command.startsWith("admin_say")) {
            AnnouncementData.getInstance().handleAnnounce(command, 10, command.startsWith("admin_say"));
        }
        return true;
    }

    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
}
