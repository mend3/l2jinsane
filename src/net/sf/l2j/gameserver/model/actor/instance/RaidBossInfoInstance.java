package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.data.manager.RaidBossInfoManager;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.item.DropData;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RaidBossInfoInstance extends Folk {
    private final Map<Integer, Integer> _lastPage = new ConcurrentHashMap<>();
    private final String[][] _messages = new String[][]{{"<font color=\"LEVEL\">%player%</font>, are you not afraid?", "Be careful <font color=\"LEVEL\">%player%</font>!"}, {"Here is the drop list of <font color=\"LEVEL\">%boss%</font>!", "Seems that <font color=\"LEVEL\">%boss%</font> has good drops."}};

    public RaidBossInfoInstance(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public void showChatWindow(Player player, int val) {
        this.onBypassFeedback(player, "RaidBossInfo 1");
    }

    public void onBypassFeedback(Player player, String command) {
        StringTokenizer st = new StringTokenizer(command, " ");
        String currentCommand = st.nextToken();
        if (currentCommand.startsWith("RaidBossInfo")) {
            int pageId = Integer.parseInt(st.nextToken());
            this._lastPage.put(player.getObjectId(), pageId);
            this.showRaidBossInfo(player, pageId);
        } else if (currentCommand.startsWith("RaidBossDrop")) {
            int bossId = Integer.parseInt(st.nextToken());
            int pageId = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
            this.showRaidBossDrop(player, bossId, pageId);
        }

        super.onBypassFeedback(player, command);
    }

    private void showRaidBossInfo(Player player, int pageId) {
        List<Integer> infos = new ArrayList<>(Config.LIST_RAID_BOSS_IDS);
        int limit = Config.RAID_BOSS_INFO_PAGE_LIMIT;
        int max = infos.size() / limit + (infos.size() % limit == 0 ? 0 : 1);
        infos = infos.subList((pageId - 1) * limit, Math.min(pageId * limit, infos.size()));
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<center>");
        sb.append("<body>");
        sb.append("<table width=\"256\">");
        sb.append("<tr><td width=\"256\" align=\"center\">%name%</td></tr>");
        sb.append("</table>");
        sb.append("<table width=\"256\">");
        sb.append("<tr><td width=\"256\" align=\"left\">").append(this._messages[0][Rnd.get(this._messages.length)].replace("%player%", player.getName())).append("</td></tr>");
        sb.append("</table>");
        sb.append("<br>");
        sb.append("<table width=\"224\" bgcolor=\"000000\">");
        sb.append("<tr><td width=\"224\" align=\"center\">Raid Boss Infos</td></tr>");
        sb.append("</table>");
        sb.append("<br>");
        sb.append("<table width=\"256\">");

        for (int bossId : infos) {
            NpcTemplate template = NpcData.getInstance().getTemplate(bossId);
            if (template != null) {
                String bossName = template.getName();
                if (bossName.length() > 23) {
                    bossName = bossName.substring(0, 23) + "...";
                }

                long respawnTime = RaidBossInfoManager.getInstance().getRaidBossRespawnTime(bossId);
                if (respawnTime <= System.currentTimeMillis()) {
                    sb.append("<tr>");
                    sb.append("<td width=\"146\" align=\"left\"><a action=\"bypass -h npc_%objectId%_RaidBossDrop ").append(bossId).append("\">").append(bossName).append("</a></td>");
                    sb.append("<td width=\"110\" align=\"right\"><font color=\"9CC300\">Alive</font></td>");
                    sb.append("</tr>");
                } else {
                    sb.append("<tr>");
                    sb.append("<td width=\"146\" align=\"left\"><a action=\"bypass -h npc_%objectId%_RaidBossDrop ").append(bossId).append("\">").append(bossName).append("</a></td>");
                    SimpleDateFormat var10001 = new SimpleDateFormat(Config.RAID_BOSS_DATE_FORMAT);
                    Date var10002 = new Date(respawnTime);
                    sb.append("<td width=\"110\" align=\"right\"><font color=\"FB5858\">Dead</font> ").append(var10001.format(var10002)).append("</td>");
                    sb.append("</tr>");
                }
            }
        }

        sb.append("</table>");
        sb.append("<br>");
        sb.append("<table width=\"224\" cellspacing=\"2\">");
        sb.append("<tr>");

        for (int x = 0; x < max; ++x) {
            int pageNr = x + 1;
            if (pageId == pageNr) {
                sb.append("<td align=\"center\">").append(pageNr).append("</td>");
            } else {
                sb.append("<td align=\"center\"><a action=\"bypass -h npc_%objectId%_RaidBossInfo ").append(pageNr).append("\">").append(pageNr).append("</a></td>");
            }
        }

        sb.append("</tr>");
        sb.append("</table>");
        sb.append("</center>");
        sb.append("</body>");
        sb.append("</html>");
        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
        html.setHtml(sb.toString());
        html.replace("%name%", this.getName());
        html.replace("%objectId%", this.getObjectId());
        player.sendPacket(html);
    }

    private void showRaidBossDrop(Player player, int bossId, int pageId) {
        NpcTemplate template = NpcData.getInstance().getTemplate(bossId);
        if (template != null) {
            List<Integer> drops = new ArrayList<>();

            for (DropData drop : template.getAllDropData()) {
                drops.add(drop.getItemId());
            }

            int limit = Config.RAID_BOSS_DROP_PAGE_LIMIT;
            int max = drops.size() / limit + (drops.size() % limit == 0 ? 0 : 1);
            drops = drops.subList((pageId - 1) * limit, Math.min(pageId * limit, drops.size()));
            StringBuilder sb = new StringBuilder();
            sb.append("<html>");
            sb.append("<center>");
            sb.append("<body>");
            sb.append("<table width=\"256\">");
            sb.append("<tr><td width=\"256\" align=\"center\">%name%</td></tr>");
            sb.append("</table>");
            sb.append("<table width=\"256\">");
            sb.append("<tr><td width=\"256\" align=\"left\">").append(this._messages[1][Rnd.get(this._messages.length)].replace("%boss%", template.getName())).append("</td></tr>");
            sb.append("</table>");
            sb.append("<br>");
            sb.append("<table width=\"224\" bgcolor=\"000000\">");
            sb.append("<tr><td width=\"224\" align=\"center\">Raid Boss Drops</td></tr>");
            sb.append("</table>");
            sb.append("<br>");
            sb.append("<table width=\"256\">");

            for (int itemId : drops) {
                Item currentItem = ItemTable.getInstance().getTemplate(itemId);
                String itemName = currentItem.getName();
                String iconItem = currentItem.getIcon();
                if (itemName.length() > 47) {
                    itemName = itemName.substring(0, 47) + "...";
                }

                sb.append("<tr><td><button width=32 height=32 back=\"").append(iconItem).append("\" fore=\"").append(iconItem).append("\"></td><td width=\"256\" align=\"center\">").append(itemName).append("</td></tr>");
            }

            sb.append("</table>");
            sb.append("<br>");
            sb.append("<table width=\"64\" cellspacing=\"2\">");
            sb.append("<tr>");

            for (int x = 0; x < max; ++x) {
                int pageNr = x + 1;
                if (pageId == pageNr) {
                    sb.append("<td align=\"center\">").append(pageNr).append("</td>");
                } else {
                    sb.append("<td align=\"center\"><a action=\"bypass -h npc_%objectId%_RaidBossDrop ").append(bossId).append(" ").append(pageNr).append("\">").append(pageNr).append("</a></td>");
                }
            }

            sb.append("</tr>");
            sb.append("</table>");
            sb.append("<br>");
            sb.append("<table width=\"160\" cellspacing=\"2\">");
            sb.append("<tr>");
            String var10001 = String.valueOf(this._lastPage.get(player.getObjectId()));
            sb.append("<td width=\"160\" align=\"center\"><a action=\"bypass -h npc_%objectId%_RaidBossInfo ").append(var10001).append("\">Return</a></td>");
            sb.append("</tr>");
            sb.append("</table>");
            sb.append("</center>");
            sb.append("</body>");
            sb.append("</html>");
            NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
            html.setHtml(sb.toString());
            html.replace("%name%", this.getName());
            html.replace("%objectId%", this.getObjectId());
            player.sendPacket(html);
        }
    }
}
