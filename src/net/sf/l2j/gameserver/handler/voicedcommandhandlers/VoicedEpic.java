package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.manager.RaidBossInfoManager;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class VoicedEpic implements IVoicedCommandHandler {
    private static final String[] _voicedCommands = new String[]{"epic"};

    public void useVoicedCommand(String command, Player activeChar, String target) {
        if (command.startsWith("epic"))
            showMainPage(activeChar);
    }

    public void showMainPage(Player player) {
        List<Integer> infos = new ArrayList<>();
        infos.add(25512);
        infos.add(29001);
        infos.add(29006);
        infos.add(29014);
        infos.add(29019);
        infos.add(29020);
        infos.add(29022);
        infos.add(29028);
        infos.add(29045);
        infos.add(29046);
        infos.add(29047);
        infos.add(29065);
        StringBuilder sb = new StringBuilder();
        sb.append("<html><title>Epic Boss Info</title><body>");
        sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
        sb.append("<table width=\"300\" bgcolor=\"000000\">");
        sb.append("<tr>");
        sb.append("<td><center>SVR Data: <font color=\"ff4d4d\">").append((new SimpleDateFormat("dd/MM/yyyy")).format(new Date(System.currentTimeMillis()))).append("</font></center></td>");
        sb.append("<td><center></center></td>");
        sb.append("<td><center>SVR Time: <font color=\"ff4d4d\">").append((new SimpleDateFormat("HH:mm:ss")).format(new Date(System.currentTimeMillis()))).append("</font></center></td>");
        sb.append("</tr>");
        sb.append("</table>");
        sb.append("<center><img src=\"L2UI.SquareGray\" width=300 height=1></center>");
        sb.append("<br>");
        sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
        sb.append("<table bgcolor=\"000000\" width=\"330\">");
        sb.append("<tr><td><center><font color=\"FF8C00\">Boss Info</font></center></td></tr>");
        sb.append("</table>");
        sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
        sb.append("<table bgcolor=\"000000\" width=\"318\">");
        for (int bossId : infos) {
            NpcTemplate template = NpcData.getInstance().getTemplate(bossId);
            if (template == null)
                continue;
            String bossName = template.getName();
            if (bossName.length() > 23)
                bossName = bossName.substring(0, 23) + "...";
            long respawnTime = RaidBossInfoManager.getInstance().getRaidBossRespawnTime(bossId);
            if (respawnTime <= System.currentTimeMillis()) {
                sb.append("<tr>");
                sb.append("<td><a action=\"").append(bossId).append("\">").append(bossName).append("</a></td>");
                sb.append("<td><font color=\"9CC300\">Alive</font></td>");
                sb.append("</tr>");
                continue;
            }
            sb.append("<tr>");
            sb.append("<td width=\"159\" align=\"left\"><a action=\"").append(bossId).append("\">").append(bossName).append("</a></td>");
            sb.append("<td width=\"159\" align=\"left\"><font color=\"FB5858\">Dead</font> ").append((new SimpleDateFormat(Config.RAID_BOSS_DATE_FORMAT)).format(new Date(respawnTime))).append("</td>");
            sb.append("</tr>");
        }
        sb.append("</table>");
        sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
        sb.append("<table bgcolor=\"000000\" width=\"370\">");
        sb.append("<tr><td><center><a action=\"bypass voiced_menu\">Back</a></center></td></tr>");
        sb.append("</table>");
        sb.append("<img src=\"L2UI.SquareGray\" width=300 height=1>");
        sb.append("</center>");
        sb.append("</body>");
        sb.append("</html>");
        NpcHtmlMessage msg = new NpcHtmlMessage(5);
        msg.setHtml(sb.toString());
        player.sendPacket(msg);
    }

    public String[] getVoicedCommandList() {
        return _voicedCommands;
    }
}
