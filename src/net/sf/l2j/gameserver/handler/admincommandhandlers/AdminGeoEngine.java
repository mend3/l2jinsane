package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.geoengine.geodata.ABlock;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.util.List;

public class AdminGeoEngine implements IAdminCommandHandler {
    private static final String[] ADMIN_COMMANDS = new String[]{"admin_geo_bug", "admin_geo_pos", "admin_geo_see", "admin_geo_move", "admin_path_find", "admin_path_info"};
    private final String Y = "x ";
    private final String N = "   ";

    public boolean useAdminCommand(String command, Player activeChar) {
        if (command.startsWith("admin_geo_bug")) {
            int geoX = GeoEngine.getGeoX(activeChar.getX());
            int geoY = GeoEngine.getGeoY(activeChar.getY());
            if (GeoEngine.getInstance().hasGeoPos(geoX, geoY)) {
                try {
                    String comment = command.substring(14);
                    if (GeoEngine.getInstance().addGeoBug(activeChar.getPosition(), activeChar.getName() + ": " + activeChar.getName()))
                        activeChar.sendMessage("GeoData bug saved.");
                } catch (Exception e) {
                    activeChar.sendMessage("Usage: //admin_geo_bug comments");
                }
            } else {
                activeChar.sendMessage("There is no geodata at this position.");
            }
        } else if (command.equals("admin_geo_pos")) {
            int geoX = GeoEngine.getGeoX(activeChar.getX());
            int geoY = GeoEngine.getGeoY(activeChar.getY());
            int rx = (activeChar.getX() - -131072) / 32768 + 16;
            int ry = (activeChar.getY() - -262144) / 32768 + 10;
            ABlock block = GeoEngine.getInstance().getBlock(geoX, geoY);
            activeChar.sendMessage("Region: " + rx + "_" + ry + "; Block: " + block.getClass().getSimpleName());
            if (block.hasGeoPos()) {
                int geoZ = block.getHeightNearest(geoX, geoY, activeChar.getZ());
                byte nswe = block.getNsweNearest(geoX, geoY, geoZ);
                activeChar.sendMessage("    " + (((nswe & Byte.MIN_VALUE) != 0) ? "x " : "   ") + (((nswe & 0x8) != 0) ? "x " : "   ") + (((nswe & 0x40) != 0) ? "x " : "   ") + "         GeoX=" + geoX);
                activeChar.sendMessage("    " + (((nswe & 0x2) != 0) ? "x " : "   ") + "o " + (((nswe & 0x1) != 0) ? "x " : "   ") + "         GeoY=" + geoY);
                activeChar.sendMessage("    " + (((nswe & 0x20) != 0) ? "x " : "   ") + (((nswe & 0x4) != 0) ? "x " : "   ") + (((nswe & 0x10) != 0) ? "x " : "   ") + "         GeoZ=" + geoZ);
            } else {
                activeChar.sendMessage("There is no geodata at this position.");
            }
        } else if (command.equals("admin_geo_see")) {
            WorldObject target = activeChar.getTarget();
            if (target != null) {
                if (GeoEngine.getInstance().canSeeTarget(activeChar, target)) {
                    activeChar.sendMessage("Can see target.");
                } else {
                    activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.CANT_SEE_TARGET));
                }
            } else {
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
            }
        } else if (command.equals("admin_geo_move")) {
            WorldObject target = activeChar.getTarget();
            if (target != null) {
                if (GeoEngine.getInstance().canMoveToTarget(activeChar.getX(), activeChar.getY(), activeChar.getZ(), target.getX(), target.getY(), target.getZ())) {
                    activeChar.sendMessage("Can move beeline.");
                } else {
                    activeChar.sendMessage("Can not move beeline!");
                }
            } else {
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
            }
        } else if (command.equals("admin_path_find")) {
            if (activeChar.getTarget() != null) {
                List<Location> path = GeoEngine.getInstance().findPath(activeChar.getX(), activeChar.getY(), (short) activeChar.getZ(), activeChar.getTarget().getX(), activeChar.getTarget().getY(), (short) activeChar.getTarget().getZ(), true);
                if (path == null) {
                    activeChar.sendMessage("No route found or pathfinding disabled.");
                } else {
                    for (Location point : path)
                        activeChar.sendMessage("x:" + point.getX() + " y:" + point.getY() + " z:" + point.getZ());
                }
            } else {
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
            }
        } else if (command.equals("admin_path_info")) {
            List<String> info = GeoEngine.getInstance().getStat();
            if (info == null) {
                activeChar.sendMessage("Pathfinding disabled.");
            } else {
                for (String msg : info) {
                    System.out.println(msg);
                    activeChar.sendMessage(msg);
                }
            }
        } else {
            return false;
        }
        return true;
    }

    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
}
