package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.olympiad.OlympiadGameManager;
import net.sf.l2j.gameserver.model.olympiad.OlympiadGameTask;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

public final class RequestOlympiadMatchList extends L2GameClientPacket {
    protected void readImpl() {
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null || !activeChar.isInObserverMode())
            return;
        int i = 0;
        StringBuilder sb = new StringBuilder(1500);
        for (OlympiadGameTask task : OlympiadGameManager.getInstance().getOlympiadTasks()) {
            StringUtil.append(sb, "<tr><td fixwidth=10><a action=\"bypass arenachange ", Integer.valueOf(i), "\">", Integer.valueOf(++i), "</a></td><td fixwidth=80>");
            if (task.isGameStarted()) {
                if (task.isInTimerTime()) {
                    StringUtil.append(sb, "&$907;");
                } else if (task.isBattleStarted()) {
                    StringUtil.append(sb, "&$829;");
                } else {
                    StringUtil.append(sb, "&$908;");
                }
                StringUtil.append(sb, "</td><td>", task.getGame().getPlayerNames()[0], "&nbsp; / &nbsp;", task.getGame().getPlayerNames()[1]);
            } else {
                StringUtil.append(sb, "&$906;", "</td><td>&nbsp;");
            }
            StringUtil.append(sb, "</td><td><font color=\"aaccff\"></font></td></tr>");
        }
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setFile("data/html/olympiad/olympiad_arena_observe_list.htm");
        html.replace("%list%", sb.toString());
        activeChar.sendPacket(html);
    }
}
