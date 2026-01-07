package enginemods.main.engine.community;

import enginemods.main.data.ConfigData;
import enginemods.main.data.PlayerData;
import enginemods.main.engine.AbstractMods;
import enginemods.main.util.builders.html.Html;
import enginemods.main.util.builders.html.HtmlBuilder;
import net.sf.l2j.gameserver.data.xml.MapRegionData;
import net.sf.l2j.gameserver.enums.actors.Sex;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;

import java.util.StringTokenizer;

public class RegionComunityBoard extends AbstractMods {
    public RegionComunityBoard() {
        registerMod(ConfigData.ENABLE_BBS_REGION);
    }

    private static String getIconStatus(boolean status) {
        if (status)
            return Html.newImage("L2UI_CH3.QuestWndInfoIcon_5", 16, 16);
        return "X";
    }

    private static String getIconSex(Sex sex) {
        return (sex == Sex.MALE) ? Html.newImage("L2UI_CH3.msnicon1", 16, 16) : Html.newImage("L2UI_CH3.msnicon4", 16, 16);
    }

    private static String getColorLevel(int lvl) {
        HtmlBuilder hb = new HtmlBuilder();
        if (lvl >= 20 && lvl < 40) {
            hb.append(Html.newFontColor("LEVEL", lvl));
        } else if (lvl >= 40 && lvl < 76) {
            hb.append(Html.newFontColor("9A5C00", lvl));
        } else if (lvl >= 76) {
            hb.append(Html.newFontColor("FF0000", lvl));
        } else {
            hb.append(Integer.valueOf(lvl));
        }
        return hb.toString();
    }

    private static String topMenuList(String text, int widthMid) {
        return "<td fixwidth=" + widthMid + " align=center><button value=\"" + text + "\" width=" + widthMid + " height=21 back=L2UI_CH3.FrameBackMid fore=L2UI_CH3.FrameBackMid></td>";
    }

    public static RegionComunityBoard getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void onModState() {
    }

    public boolean onCommunityBoard(Player player, String command) {
        if (command.startsWith("_bbsloc")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            st.nextToken();
            int page = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
            HtmlBuilder hb = new HtmlBuilder(HtmlBuilder.HtmlType.COMUNITY_TYPE);
            hb.append("<html><body>");
            hb.append("<br>");
            hb.append(Html.htmlHeadCommunity("TOTAL ONLINE: " + World.getInstance().getPlayers().size()));
            hb.append("<br>");
            hb.append("<center>");
            hb.append("<table border=0 cellspacing=0 cellpadding=0>");
            hb.append("<tr>");
            hb.append("<td width=16 valign=top align=center height=22>", Html.newImage("L2UI_CH3.FrameBackLeft", 16, 22), "</td>");
            hb.append(topMenuList("Name", 100));
            hb.append(topMenuList("Lvl", 30));
            hb.append(topMenuList("aio", 30));
            hb.append(topMenuList("vip", 30));
            hb.append(topMenuList("Class", 100));
            hb.append(topMenuList("Clan (Lvl.)", 100));
            hb.append(topMenuList("Town Region", 84));
            hb.append("<td width=16 valign=top align=center height=22>", Html.newImage("L2UI_CH3.FrameBackRight", 16, 22), "</td>");
            hb.append("</tr>");
            hb.append("</table>");
            int MAX_PER_PAGE = 15;
            int searchPage = MAX_PER_PAGE * (page - 1);
            int count = 0;
            for (Player pc : World.getInstance().getPlayers()) {
                if (pc == null)
                    continue;
                if (count < searchPage) {
                    count++;
                    continue;
                }
                if (count >= searchPage + MAX_PER_PAGE)
                    continue;
                hb.append("<table height=22 border=0 cellspacing=0 cellpadding=0>");
                hb.append("<tr>");
                hb.append("<td fixwidth=16 height=22 align=center>", getIconSex(pc.getAppearance().getSex()), "</td>");
                hb.append("<td fixwidth=100 align=center>", pc.getName(), pc.getName().equals("fissban") ? Html.newFontColor("FF0000", "   ADMIN") : ((pc.getAccessLevel().getLevel() > 0) ? Html.newFontColor("LEVEL", "   GM") : ""), "</td>");
                hb.append("<td fixwidth=30 align=center>", getColorLevel(pc.getLevel()), "</td>");
                hb.append("<td fixwidth=30 align=center> ", getIconStatus(PlayerData.get(pc).isAio()), "</td>");
                hb.append("<td fixwidth=30 align=center> ", getIconStatus(PlayerData.get(pc).isVip()), "</td>");
                hb.append("<td fixwidth=100 align=center> ", pc.getClassId().toString(), "</td>");
                hb.append("<td fixwidth=100 align=center> ", (pc.getClan() != null) ? (pc.getClan().getName() + pc.getClan().getName()) : "No Clan", "</td>");
                hb.append("<td fixwidth=100 align=center> ", MapRegionData.getInstance().getClosestTownName(pc.getX(), pc.getY()), "</td>");
                hb.append("</tr>");
                hb.append("</table>");
                hb.append(Html.newImage("L2UI.SquareGray", 506, 1));
                count++;
            }
            hb.append("<br>");
            hb.append("<table border=0 cellspacing=0 cellpadding=0>");
            hb.append("<tr>");
            int currentPage = 1;
            int size = World.getInstance().getPlayers().size();
            for (int i = 0; i < size; i++) {
                if (i % MAX_PER_PAGE == 0) {
                    if (currentPage == page) {
                        hb.append("<td width=20>", Html.newFontColor("LEVEL", currentPage), "</td>");
                    } else {
                        hb.append("<td width=20><a action=\"bypass _bbsloc ", Integer.valueOf(currentPage), "\">", Integer.valueOf(currentPage), "</a></td>");
                    }
                    currentPage++;
                }
            }
            hb.append("</tr>");
            hb.append("</table>");
            hb.append("</center>");
            hb.append("</body></html>");
            sendCommunity(player, hb.toString());
            return true;
        }
        return false;
    }

    private static class SingletonHolder {
        protected static final RegionComunityBoard INSTANCE = new RegionComunityBoard();
    }
}
