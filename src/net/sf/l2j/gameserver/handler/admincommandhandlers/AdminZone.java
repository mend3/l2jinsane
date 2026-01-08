package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.gameserver.data.manager.ZoneManager;
import net.sf.l2j.gameserver.data.xml.MapRegionData;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.ZoneType;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

import java.util.StringTokenizer;

import static net.sf.l2j.gameserver.geoengine.geodata.GeoStructure.GEO_CELLS_Y;
import static net.sf.l2j.gameserver.model.World.*;

public class AdminZone implements IAdminCommandHandler {
    private static final String[] ADMIN_COMMANDS = new String[]{"admin_zone_check", "admin_zone_visual"};

    private static void showHtml(Player player) {
        int x = player.getX();
        int y = player.getY();
        int rx = (x - WORLD_X_MIN) / GEO_CELLS_Y + TILE_X_MIN;
        int ry = (y - WORLD_Y_MIN) / GEO_CELLS_Y + TILE_Y_MIN;
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setFile("data/html/admin/zone.htm");
        html.replace("%MAPREGION%", "[x:" + MapRegionData.getMapRegionX(x) + " y:" + MapRegionData.getMapRegionY(y) + "]");
        html.replace("%GEOREGION%", rx + "_" + ry);
        html.replace("%CLOSESTTOWN%", MapRegionData.getInstance().getClosestTownName(x, y));
        html.replace("%CURRENTLOC%", x + ", " + x + ", " + y);
        StringBuilder sb = new StringBuilder(100);
        for (ZoneId zone : ZoneId.VALUES) {
            if (player.isInsideZone(zone))
                StringUtil.append(sb, zone, "<br1>");
        }
        html.replace("%ZONES%", sb.toString());
        sb.setLength(0);
        for (ZoneType zone : World.getInstance().getRegion(x, y).getZones()) {
            if (zone.isCharacterInZone(player))
                StringUtil.append(sb, zone.getId(), " (you are here)");
        }
        html.replace("%ZLIST%", sb.toString());
        player.sendPacket(html);
    }

    public void useAdminCommand(String command, Player activeChar) {
        if (activeChar == null)
            return;
        if (command.startsWith("admin_zone_check")) {
            showHtml(activeChar);
        } else if (command.startsWith("admin_zone_visual")) {
            try {
                StringTokenizer st = new StringTokenizer(command, " ");
                String next = st.nextToken();
                if (next.equalsIgnoreCase("all")) {
                    for (ZoneType zone : ZoneManager.getInstance().getZones(activeChar))
                        zone.visualizeZone(activeChar.getZ());
                    showHtml(activeChar);
                } else if (next.equalsIgnoreCase("clear")) {
                    ZoneManager.getInstance().clearDebugItems();
                    showHtml(activeChar);
                } else {
                    int zoneId = Integer.parseInt(next);
                    ZoneManager.getInstance().getZoneById(zoneId).visualizeZone(activeChar.getZ());
                }
            } catch (Exception e) {
                activeChar.sendMessage("Invalid parameter for //zone_visual.");
            }
        }
    }

    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
}
