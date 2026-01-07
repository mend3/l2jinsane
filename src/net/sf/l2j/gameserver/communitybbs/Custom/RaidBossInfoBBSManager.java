package net.sf.l2j.gameserver.communitybbs.Custom;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.communitybbs.Manager.BaseBBSManager;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.data.manager.RaidBossInfoManager;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.item.DropData;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RaidBossInfoBBSManager extends BaseBBSManager {
    private final Map<Integer, Integer> _lastPage = new ConcurrentHashMap<>();

    public static RaidBossInfoBBSManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void parseCmd(String command, Player player) {
        StringTokenizer st = new StringTokenizer(command, " ");
        st.nextToken();
        if (command.startsWith("_bbsRBinfo")) {
            int pageId = Integer.parseInt(st.nextToken());
            this._lastPage.put(player.getObjectId(), pageId);
            showRaidBossInfo(player, pageId);
        } else if (command.startsWith("_bbsRBdrop")) {
            int bossId = Integer.parseInt(st.nextToken());
            int pageId = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
            showRaidBossDrop(player, bossId, pageId);
        } else if (command.startsWith("_friend") || command.startsWith("_block")) {
            int pageId = 1;
            this._lastPage.put(player.getObjectId(), pageId);
            showRaidBossInfo(player, pageId);
        } else {
            super.parseCmd(command, player);
        }
    }

    private void showRaidBossInfo(Player player, int pageId) {
        List<Integer> infos = new ArrayList<>();
        infos.addAll(Config.LIST_RAID_BOSS_IDS);
        int limit = Config.RAID_BOSS_INFO_PAGE_LIMIT;
        int max = infos.size() / limit + ((infos.size() % limit == 0) ? 0 : 1);
        infos = infos.subList((pageId - 1) * limit, Math.min(pageId * limit, infos.size()));
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<center>");
        sb.append("<body>");
        sb.append("<br>");
        sb.append("<br>");
        sb.append("<br>");
        sb.append("<table width=\"224\" bgcolor=\"000000\">");
        sb.append("<tr><td width=\"224\" align=\"center\">Raid Boss Infos</td></tr>");
        sb.append("</table>");
        sb.append("<br>");
        sb.append("<table width=\"256\">");
        for (Iterator<Integer> iterator = infos.iterator(); iterator.hasNext(); ) {
            int bossId = iterator.next();
            NpcTemplate template = NpcData.getInstance().getTemplate(bossId);
            if (template == null)
                continue;
            String bossName = template.getName();
            if (bossName.length() > 23)
                bossName = bossName.substring(0, 23) + "...";
            long respawnTime = RaidBossInfoManager.getInstance().getRaidBossRespawnTime(bossId);
            if (respawnTime <= System.currentTimeMillis()) {
                sb.append("<tr>");
                sb.append("<td width=\"146\" align=\"left\"><a action=\"bypass _bbsRBdrop " + bossId + "\">" + bossName + "</a></td>");
                sb.append("<td width=\"110\" align=\"right\"><font color=\"9CC300\">Alive</font></td>");
                sb.append("</tr>");
                continue;
            }
            sb.append("<tr>");
            sb.append("<td width=\"146\" align=\"left\"><a action=\"bypass _bbsRBdrop " + bossId + "\">" + bossName + "</a></td>");
            sb.append("<td width=\"110\" align=\"right\"><font color=\"FB5858\">Dead</font> " + (new SimpleDateFormat(Config.RAID_BOSS_DATE_FORMAT)).format(new Date(respawnTime)) + "</td>");
            sb.append("</tr>");
        }
        sb.append("</table>");
        sb.append("<br>");
        sb.append("<table width=\"224\" cellspacing=\"2\">");
        sb.append("<tr>");
        for (int x = 0; x < max; x++) {
            int pageNr = x + 1;
            if (pageId == pageNr) {
                sb.append("<td align=\"center\">" + pageNr + "</td>");
            } else {
                sb.append("<td align=\"center\"><a action=\"bypass _bbsRBinfo " + pageNr + "\">" + pageNr + "</a></td>");
            }
        }
        sb.append("</tr>");
        sb.append("</table>");
        sb.append("<br>");
        sb.append("<table width=\"160\" cellspacing=\"2\">");
        sb.append("<tr>");
        sb.append("<td width=\"160\" align=\"center\"><a action=\"bypass _bbshome\">Return</a></td>");
        sb.append("</tr>");
        sb.append("</table>");
        sb.append("<br>");
        sb.append("<table width=\"256\">");
        sb.append("<tr><td width=\"256\" align=\"center\"><font color=\"LEVEL\"> - Community Board - Raid Boss Info - </font></td></tr>");
        sb.append("</table>");
        sb.append("</center>");
        sb.append("</body>");
        sb.append("</html>");
        String html = HtmCache.getInstance().getHtm("data/html/CommunityBoard/" + getFolder() + "index.htm");
        html = sb.toString();
        separateAndSend(html, player);
    }

    private void showRaidBossDrop(Player player, int bossId, int pageId) {
        NpcTemplate template = NpcData.getInstance().getTemplate(bossId);
        if (template == null)
            return;
        List<Integer> drops = new ArrayList<>();
        for (DropData drop : template.getAllDropData())
            drops.add(drop.getItemId());
        int limit = Config.RAID_BOSS_DROP_PAGE_LIMIT;
        int max = drops.size() / limit + ((drops.size() % limit == 0) ? 0 : 1);
        drops = drops.subList((pageId - 1) * limit, Math.min(pageId * limit, drops.size()));
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<center>");
        sb.append("<body>");
        sb.append("<br>");
        sb.append("<br>");
        sb.append("<br>");
        sb.append("<table width=\"224\" bgcolor=\"000000\">");
        sb.append("<tr><td width=\"224\" align=\"center\">Raid Boss Drops</td></tr>");
        sb.append("</table>");
        sb.append("<br>");
        sb.append("<table width=\"256\">");
        for (Iterator<Integer> iterator = drops.iterator(); iterator.hasNext(); ) {
            int itemId = iterator.next();
            String itemName = ItemTable.getInstance().getTemplate(itemId).getName();
            if (itemName.length() > 47)
                itemName = itemName.substring(0, 47) + "...";
            sb.append("<tr><td width=\"256\" align=\"center\">" + itemName + "</td></tr>");
        }
        sb.append("</table>");
        sb.append("<br>");
        sb.append("<table width=\"64\" cellspacing=\"2\">");
        sb.append("<tr>");
        for (int x = 0; x < max; x++) {
            int pageNr = x + 1;
            if (pageId == pageNr) {
                sb.append("<td align=\"center\">" + pageNr + "</td>");
            } else {
                sb.append("<td align=\"center\"><a action=\"bypass _bbsRBdrop " + bossId + " " + pageNr + "\">" + pageNr + "</a></td>");
            }
        }
        sb.append("</tr>");
        sb.append("</table>");
        sb.append("<br>");
        sb.append("<table width=\"160\" cellspacing=\"2\">");
        sb.append("<tr>");
        sb.append("<td width=\"160\" align=\"center\"><a action=\"bypass _bbsRBinfo " + this._lastPage.get(player.getObjectId()) + "\">Return</a></td>");
        sb.append("</tr>");
        sb.append("</table>");
        sb.append("<br>");
        sb.append("<table width=\"256\">");
        sb.append("<tr><td width=\"256\" align=\"center\"><font color=\"LEVEL\"> - Community Board - Raid Boss Info - </font></td></tr>");
        sb.append("</table>");
        sb.append("</center>");
        sb.append("</body>");
        sb.append("</html>");
        String html = HtmCache.getInstance().getHtm("data/html/CommunityBoard/" + getFolder() + "index.htm");
        html = sb.toString();
        separateAndSend(html, player);
    }

    protected String getFolder() {
        return "top/mods/raidbossinfo/";
    }

    private static class SingletonHolder {
        protected static final RaidBossInfoBBSManager INSTANCE = new RaidBossInfoBBSManager();
    }
}
