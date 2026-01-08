/**/
package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AdminZoneCreation implements IAdminCommandHandler {
    private static final List<Location> savedLocs = new ArrayList<>();
    private static final String fileName = "coordinates%s.xml";
    private static final int zDifference = 1000;
    private static final String maxLocs = "You have reached the maximum locations for this shape.";
    private static final String[] ADMIN_COMMANDS;
    private static AdminZoneCreation.ZoneShape shape;
    private static int radius;

    static {
        shape = AdminZoneCreation.ZoneShape.NONE;
        radius = 0;
        ADMIN_COMMANDS = new String[]{"admin_create_zone", "admin_setType", "admin_setRad", "admin_saveLoc", "admin_reset", "admin_removeLoc", "admin_storeLocs"};
    }

    private static int calcZ(boolean minZ) {
        return savedLocs.stream().mapToInt(Location::getZ).sum() / savedLocs.size() + (minZ ? -1000 : 1000);
    }

    private static int parseInt(String nextToken) {
        try {
            return Integer.parseInt(nextToken);
        } catch (NumberFormatException var2) {
            return 0;
        }
    }

    private static void store(Player gm) {
        if (canStoreLocs(gm)) {
            Object[] var10001 = new Object[1];
            String var10004 = shape.name();
            var10001[0] = "_" + var10004 + "_" + getTimeStamp();
            String fName = String.format("coordinates%s.xml", var10001);
            String filePath = "";

            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(fName));

                try {
                    File file = new File(fName);
                    filePath = file.getAbsolutePath().replaceAll("\\\\", "/");
                    writer.write(getHeadLine());
                    Iterator<Location> var5 = savedLocs.iterator();

                    while (true) {
                        if (!var5.hasNext()) {
                            writer.write("</zone>");
                            break;
                        }

                        Location loc = var5.next();
                        writer.write(String.format("\t<node X=\"%s\" Y=\"%s\" />\r\n", loc.getX(), loc.getY()));
                    }
                } catch (Throwable var8) {
                    try {
                        writer.close();
                    } catch (Throwable var7) {
                        var8.addSuppressed(var7);
                    }

                    throw var8;
                }

                writer.close();
            } catch (IOException var9) {
                gm.sendMessage(String.format("Couldn't store coordinates in %s file.", fName));
                var9.printStackTrace();
            }

            gm.sendMessage("Coordinates has been successfully stored at " + filePath);
            clear();
            openHtml(gm);
        }
    }

    private static String getTimeStamp() {
        return (new SimpleDateFormat("hh-mm-ss")).format(new Date());
    }

    private static boolean canStoreLocs(Player gm) {
        return switch (shape.ordinal()) {
            case 1 -> {
                if (savedLocs.size() < 3) {
                    gm.sendMessage("You have to set atleast 3 coordinates!");
                    yield false;
                }

                yield true;
            }
            case 2 -> {
                if (savedLocs.size() != 2) {
                    gm.sendMessage("You have to set 2 coordinates.");
                    yield false;
                }

                yield true;
            }
            case 3 -> {
                if (savedLocs.size() != 1) {
                    gm.sendMessage("Only 1 location required for this shape.");
                    yield false;
                }

                yield true;
            }
            default -> {
                gm.sendMessage("You have to select the zone shape first.");
                yield false;
            }
        };
    }

    private static boolean canSaveLoc(Player activeChar) {
        return switch (shape.ordinal()) {
            case 1 -> true;
            case 2 -> {
                if (savedLocs.size() >= 2) {
                    activeChar.sendMessage("You have reached the maximum locations for this shape.");
                    yield false;
                }

                yield true;
            }
            case 3 -> {
                if (!savedLocs.isEmpty()) {
                    activeChar.sendMessage("Only 1 locations required for this shape.");
                    yield false;
                }

                yield true;
            }
            default -> {
                activeChar.sendMessage("You have to select the zone shape first.");
                yield false;
            }
        };
    }

    private static String getHeadLine() {
        return switch (shape.ordinal()) {
            case 1 -> String.format("<zone shape='NPoly' minZ='%s' maxZ='%s'>\r\n", calcZ(true), calcZ(false));
            case 2 -> String.format("<zone shape='Cuboid' minZ='%s' maxZ='%s'>\r\n", calcZ(true), calcZ(false));
            case 3 ->
                    String.format("<zone shape='Cylinder' minZ='%s' maxZ='%s' rad='%s'>\r\n", calcZ(true), calcZ(false), getRad());
            default -> "";
        };
    }

    private static boolean setRadius(int val) {
        if (shape == AdminZoneCreation.ZoneShape.Cylinder) {
            if (val == 0) {
                return false;
            }

            radius = val;
        }

        return shape == AdminZoneCreation.ZoneShape.Cylinder;
    }

    private static int getRad() {
        return radius;
    }

    private static void openHtml(Player activeChar) {
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setFile("data/html/admin/zone_create.htm");
        int var10002;
        switch (shape.ordinal()) {
            case 0:
                html.replace("%zoneShape%", "Empty");
                html.replace("%locsSize%", "Locations Saved: Null");
                html.replace("%proceed%", "");
                html.replace("%undo%", !savedLocs.isEmpty() ? "<button value=\"Undo\" action=\"bypass admin_removeLoc\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\">" : "");
                html.replace("%dist%", "");
                break;
            case 1:
                html.replace("%zoneShape%", shape.name());
                var10002 = savedLocs.size();
                html.replace("%locsSize%", "Locations Saved: " + var10002);
                html.replace("%proceed%", savedLocs.size() > 2 ? "<button value=\"Store\" action=\"bypass admin_storeLocs\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\">" : "");
                html.replace("%undo%", !savedLocs.isEmpty() ? "<button value=\"Undo\" action=\"bypass admin_removeLoc\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\">" : "");
                html.replace("%dist%", "");
                break;
            case 2:
                html.replace("%zoneShape%", shape.name());
                var10002 = savedLocs.size();
                html.replace("%locsSize%", "Locations Saved: " + var10002);
                html.replace("%proceed%", savedLocs.size() == 2 ? "<button value=\"Store\" action=\"bypass admin_storeLocs\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\">" : "");
                html.replace("%undo%", !savedLocs.isEmpty() ? "<button value=\"Undo\" action=\"bypass admin_removeLoc\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\">" : "");
                html.replace("%dist%", "");
                break;
            case 3:
                html.replace("%zoneShape%", shape.name());
                var10002 = savedLocs.size();
                html.replace("%locsSize%", "Locations Saved: " + var10002);
                html.replace("%undo%", savedLocs.size() == 1 ? "<button value=\"Undo\" action=\"bypass admin_removeLoc\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\">" : "");
                if (savedLocs.size() == 1) {
                    if (radius == 0) {
                        html.replace("%proceed%", "");
                        html.replace("%dist%", "Set the radius: <edit var=\"Radius\" width=110 height=15> <button value=\"Save\" action=\"bypass admin_setRad $Radius\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\">");
                    } else {
                        html.replace("%proceed%", "<button value=\"Store\" action=\"bypass admin_storeLocs\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\">");
                        html.replace("%dist%", "");
                    }
                } else {
                    html.replace("%proceed%", "");
                    html.replace("%dist%", "");
                }
        }

        activeChar.sendPacket(html);
    }

    private static void clear() {
        shape = AdminZoneCreation.ZoneShape.NONE;
        savedLocs.clear();
        radius = 0;
    }

    public void useAdminCommand(String command, Player activeChar) {
        StringTokenizer st = new StringTokenizer(command);
        st.nextToken();
        if (command.startsWith("admin_create_zone")) {
            openHtml(activeChar);
        } else if (command.startsWith("admin_setType")) {
            clear();
            shape = AdminZoneCreation.ZoneShape.valueOf(st.nextToken());
            switch (shape.ordinal()) {
                case 1:
                    activeChar.sendMessage("You can add unlimited but atleast 3 coordinates in this shape.");
                    break;
                case 2:
                    activeChar.sendMessage("You must add 2 coordinates (in order to make a square) in this shape.");
                    break;
                case 3:
                    activeChar.sendMessage("You must add 1 coordinates and radius (in order to make a circle) in this shape.");
                    break;
                default:
                    activeChar.sendMessage("You have to select the zone shape first.");
            }

            openHtml(activeChar);
        } else if (command.startsWith("admin_saveLoc")) {
            if (canSaveLoc(activeChar)) {
                Location loc = new Location(activeChar.getX(), activeChar.getY(), activeChar.getZ());
                if (savedLocs.add(loc)) {
                    activeChar.sendMessage(loc + " saved..");
                }
            }

            openHtml(activeChar);
        } else if (command.startsWith("admin_reset")) {
            clear();
            activeChar.sendMessage("Reset completed.");
            openHtml(activeChar);
        } else if (command.startsWith("admin_removeLoc")) {
            if (!savedLocs.isEmpty()) {
                List<Location> var10001 = savedLocs;
                int var10002 = savedLocs.size();
                activeChar.sendMessage(var10001.remove(var10002 - 1) + " removed.");
            }

            openHtml(activeChar);
        } else if (command.startsWith("admin_storeLocs")) {
            if (savedLocs.isEmpty()) {
                activeChar.sendMessage("Empty locs..");
                return;
            }

            store(activeChar);
        } else if (command.startsWith("admin_setRad")) {
            if (st.hasMoreTokens() && setRadius(parseInt(st.nextToken()))) {
                activeChar.sendMessage("Radius stored.");
            } else {
                activeChar.sendMessage("Invalid value or shape.");
            }

            openHtml(activeChar);
        }

    }

    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }

    private enum ZoneShape {
        NONE,
        NPoly,
        Cuboid,
        Cylinder;
    }
}