package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.gameserver.data.sql.BookmarkTable;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.Bookmark;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

import java.util.List;
import java.util.StringTokenizer;

public class AdminBookmark implements IAdminCommandHandler {
    private static final int PAGE_LIMIT = 15;

    private static final String[] ADMIN_COMMANDS = new String[]{"admin_bkpage", "admin_bk", "admin_delbk"};

    private static void showBookmarks(Player activeChar, int page) {
        int objId = activeChar.getObjectId();
        List<Bookmark> bookmarks = BookmarkTable.getInstance().getBookmarks(objId);
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setFile("data/html/admin/bk.htm");
        if (bookmarks.isEmpty()) {
            html.replace("%locs%", "<tr><td>No bookmarks are currently registered.</td></tr>");
            activeChar.sendPacket(html);
            return;
        }
        int max = MathUtil.countPagesNumber(bookmarks.size(), 15);
        bookmarks = bookmarks.subList((page - 1) * 15, Math.min(page * 15, bookmarks.size()));
        StringBuilder sb = new StringBuilder(2000);
        for (Bookmark bk : bookmarks) {
            String name = bk.getName();
            int x = bk.getX();
            int y = bk.getY();
            int z = bk.getZ();
            StringUtil.append(sb, "<tr><td><a action=\"bypass -h admin_move_to ", Integer.valueOf(x), " ", Integer.valueOf(y), " ", Integer.valueOf(z), "\">", name, " (", Integer.valueOf(x),
                    " ", Integer.valueOf(y), " ", Integer.valueOf(z), ")", "</a></td><td><a action=\"bypass -h admin_delbk ", name, "\">Remove</a></td></tr>");
        }
        html.replace("%locs%", sb.toString());
        sb.setLength(0);
        for (int i = 0; i < max; i++) {
            int pagenr = i + 1;
            if (page == pagenr) {
                StringUtil.append(sb, Integer.valueOf(pagenr), "&nbsp;");
            } else {
                StringUtil.append(sb, "<a action=\"bypass -h admin_bkpage ", Integer.valueOf(pagenr), "\">", Integer.valueOf(pagenr), "</a>&nbsp;");
            }
        }
        html.replace("%pages%", sb.toString());
        activeChar.sendPacket(html);
    }

    public boolean useAdminCommand(String command, Player activeChar) {
        if (command.startsWith("admin_bkpage")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            int page = 1;
            if (st.hasMoreTokens())
                page = Integer.parseInt(st.nextToken());
            showBookmarks(activeChar, page);
        } else if (command.startsWith("admin_bk")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            if (st.hasMoreTokens()) {
                String name = st.nextToken();
                if (name.length() > 15) {
                    activeChar.sendMessage("The location name is too long.");
                    return true;
                }
                if (BookmarkTable.getInstance().isExisting(name, activeChar.getObjectId())) {
                    activeChar.sendMessage("That location is already existing.");
                    return true;
                }
                BookmarkTable.getInstance().saveBookmark(name, activeChar);
            }
            showBookmarks(activeChar, 1);
        } else if (command.startsWith("admin_delbk")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            if (st.hasMoreTokens()) {
                String name = st.nextToken();
                int objId = activeChar.getObjectId();
                if (!BookmarkTable.getInstance().isExisting(name, objId)) {
                    activeChar.sendMessage("That location doesn't exist.");
                    return true;
                }
                BookmarkTable.getInstance().deleteBookmark(name, objId);
            } else {
                activeChar.sendMessage("The command delbk must be followed by a valid name.");
            }
            showBookmarks(activeChar, 1);
        }
        return true;
    }

    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
}
