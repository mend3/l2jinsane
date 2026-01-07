package net.sf.l2j.gameserver.communitybbs.Manager;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.data.manager.ClanHallManager;
import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.clanhall.ClanHall;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.pledge.Clan;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.StringTokenizer;

public class RegionBBSManager extends BaseBBSManager {
    private static void showRegionsList(Player player) {
        String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/region/castlelist.htm");
        StringBuilder sb = new StringBuilder(500);
        for (Castle castle : CastleManager.getInstance().getCastles()) {
            Clan owner = ClanTable.getInstance().getClan(castle.getOwnerId());
            StringUtil.append(sb, "<table><tr><td width=5></td><td width=160><a action=\"bypass _bbsloc;", castle.getCastleId(), "\">", castle.getName(), "</a></td><td width=160>", (owner != null) ? ("<a action=\"bypass _bbsclan;home;" + owner.getClanId() + "\">" + owner.getName() + "</a>") : "None", "</td><td width=160>", (owner != null && owner.getAllyId() > 0) ? owner.getAllyName() : "None", "</td><td width=120>", (owner != null) ? Integer.valueOf(castle.getTaxPercent()) : "0",
                    "</td><td width=5></td></tr></table><br1><img src=\"L2UI.Squaregray\" width=605 height=1><br1>");
        }
        separateAndSend(content.replace("%castleList%", sb.toString()), player);
    }

    private static void showRegion(Player player, int castleId) {
        Castle castle = CastleManager.getInstance().getCastleById(castleId);
        Clan owner = ClanTable.getInstance().getClan(castle.getOwnerId());
        String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/region/castle.htm");
        content = content.replace("%castleName%", castle.getName());
        content = content.replace("%tax%", Integer.toString(castle.getTaxPercent()));
        content = content.replace("%lord%", (owner != null) ? owner.getLeaderName() : "None");
        content = content.replace("%clanName%", (owner != null) ? ("<a action=\"bypass _bbsclan;home;" + owner.getClanId() + "\">" + owner.getName() + "</a>") : "None");
        content = content.replace("%allyName%", (owner != null && owner.getAllyId() > 0) ? owner.getAllyName() : "None");
        content = content.replace("%siegeDate%", (new SimpleDateFormat("yyyy-MM-dd HH:mm")).format(castle.getSiegeDate().getTimeInMillis()));
        StringBuilder sb = new StringBuilder(200);
        List<ClanHall> clanHalls = ClanHallManager.getInstance().getClanHallsByLocation(castle.getName());
        if (clanHalls != null && !clanHalls.isEmpty()) {
            sb.append("<br><br><table width=610 bgcolor=A7A19A><tr><td width=5></td><td width=200>Clan Hall Name</td><td width=200>Owning Clan</td><td width=200>Clan Leader Name</td><td width=5></td></tr></table><br1>");
            for (ClanHall ch : clanHalls) {
                Clan chOwner = ClanTable.getInstance().getClan(ch.getOwnerId());
                StringUtil.append(sb, "<table><tr><td width=5></td><td width=200>", ch.getName(), "</td><td width=200>", (chOwner != null) ? ("<a action=\"bypass _bbsclan;home;" + chOwner.getClanId() + "\">" + chOwner.getName() + "</a>") : "None", "</td><td width=200>", (chOwner != null) ? chOwner.getLeaderName() : "None", "</td><td width=5></td></tr></table><br1><img src=\"L2UI.Squaregray\" width=605 height=1><br1>");
            }
        }
        separateAndSend(content.replace("%hallsList%", sb.toString()), player);
    }

    public static RegionBBSManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void parseCmd(String command, Player player) {
        if (command.equals("_bbsloc")) {
            showRegionsList(player);
        } else if (command.startsWith("_bbsloc")) {
            StringTokenizer st = new StringTokenizer(command, ";");
            st.nextToken();
            showRegion(player, Integer.parseInt(st.nextToken()));
        } else {
            super.parseCmd(command, player);
        }
    }

    protected String getFolder() {
        return "region/";
    }

    private static class SingletonHolder {
        protected static final RegionBBSManager INSTANCE = new RegionBBSManager();
    }
}
