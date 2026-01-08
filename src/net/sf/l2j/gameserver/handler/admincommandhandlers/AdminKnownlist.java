package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

import java.util.List;
import java.util.StringTokenizer;

public class AdminKnownlist implements IAdminCommandHandler {
    private static final int PAGE_LIMIT = 15;

    private static final String[] ADMIN_COMMANDS = new String[]{"admin_knownlist", "admin_knownlist_page"};

    private static void showKnownlist(Player activeChar, WorldObject target, int page) {
        List<WorldObject> knownlist = target.getKnownType(WorldObject.class);
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setFile("data/html/admin/knownlist.htm");
        html.replace("%target%", target.getName());
        html.replace("%size%", knownlist.size());
        if (knownlist.isEmpty()) {
            html.replace("%knownlist%", "<tr><td>No objects in vicinity.</td></tr>");
            html.replace("%pages%", 0);
            activeChar.sendPacket(html);
            return;
        }
        int max = MathUtil.countPagesNumber(knownlist.size(), 15);
        if (page > max)
            page = max;
        knownlist = knownlist.subList((page - 1) * 15, Math.min(page * 15, knownlist.size()));
        StringBuilder sb = new StringBuilder(knownlist.size() * 150);
        for (WorldObject object : knownlist) {
            StringUtil.append(sb, "<tr><td>", object.getName(), "</td><td>", object.getClass().getSimpleName(), "</td></tr>");
        }
        html.replace("%knownlist%", sb.toString());
        sb.setLength(0);
        for (int i = 0; i < max; i++) {
            int pagenr = i + 1;
            if (page == pagenr) {
                StringUtil.append(sb, pagenr, "&nbsp;");
            } else {
                StringUtil.append(sb, "<a action=\"bypass -h admin_knownlist_page ", target.getObjectId(), " ", pagenr, "\">", pagenr, "</a>&nbsp;");
            }
        }
        html.replace("%pages%", sb.toString());
        activeChar.sendPacket(html);
    }

    public void useAdminCommand(String command, Player activeChar) {
        if (command.startsWith("admin_knownlist")) {
            Player player = null;
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            WorldObject target = null;
            if (st.hasMoreTokens()) {
                String parameter = st.nextToken();
                try {
                    int objectId = Integer.parseInt(parameter);
                    target = World.getInstance().getObject(objectId);
                } catch (NumberFormatException nfe) {
                    player = World.getInstance().getPlayer(parameter);
                }
            }
            if (player == null) {
                WorldObject worldObject = activeChar.getTarget();
                if (worldObject == null)
                    player = activeChar;
            }
            int page = 1;
            if (command.startsWith("admin_knownlist_page") && st.hasMoreTokens())
                try {
                    page = Integer.parseInt(st.nextToken());
                } catch (NumberFormatException ignored) {
                }
            showKnownlist(activeChar, player, page);
        }
    }

    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
}
