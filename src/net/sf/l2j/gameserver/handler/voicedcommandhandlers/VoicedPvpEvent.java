package net.sf.l2j.gameserver.handler.voicedcommandhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.events.pvpevent.PvPEvent;
import net.sf.l2j.gameserver.handler.IVoicedCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class VoicedPvpEvent implements IVoicedCommandHandler {
    private static final CLogger LOGGER = new CLogger(VoicedPvpEvent.class.getName());

    private static final String[] VOICED_COMMANDS = new String[]{"pvpevent", "pvpEvent"};

    public static void getTopHtml(Player activeChar) {
        if (PvPEvent.getInstance().isActive()) {
            NpcHtmlMessage htm = new NpcHtmlMessage(5);
            StringBuilder tb = new StringBuilder();
            tb.append("<html>");
            tb.append("<body>");
            tb.append("<center>");
            tb.append("<img src=\"l2ui_ch3.herotower_deco\" width=256 height=32><br>");
            tb.append("<table border=\"1\" width=\"300\">");
            tb.append("<tr>");
            tb.append("<td><center>Rank</center></td>");
            tb.append("<td><center>Character</center></td>");
            tb.append("<td><center>Pvp's</center></td>");
            tb.append("<td><center>Status</center></td>");
            tb.append("</tr>");
            try {
                Connection con = ConnectionPool.getConnection();
                try {
                    PreparedStatement statement = con.prepareStatement("SELECT char_name,event_pvp,accesslevel,online FROM characters ORDER BY event_pvp DESC LIMIT 15");
                    try {
                        ResultSet result = statement.executeQuery();
                        try {
                            int pos = 0;
                            while (result.next()) {
                                String status;
                                int accessLevel = result.getInt("accesslevel");
                                if (accessLevel > 0)
                                    continue;
                                int pvpKills = result.getInt("event_pvp");
                                if (pvpKills == 0)
                                    continue;
                                String pl = result.getString("char_name");
                                pos++;
                                String statu = result.getString("online");
                                if (statu.equals("1")) {
                                    status = "<font color=00FF00>Online</font>";
                                } else {
                                    status = "<font color=FF0000>Offline</font>";
                                }
                                tb.append("<tr>");
                                tb.append("<td><center><font color =\"AAAAAA\">" + pos + "</font></center></td>");
                                tb.append("<td><center><font color=00FFFF>" + pl + "</font></center></td>");
                                tb.append("<td><center>" + pvpKills + "</center></td>");
                                tb.append("<td><center>" + status + "</center></td>");
                                tb.append("</tr>");
                            }
                            if (result != null)
                                result.close();
                        } catch (Throwable throwable) {
                            if (result != null)
                                try {
                                    result.close();
                                } catch (Throwable throwable1) {
                                    throwable.addSuppressed(throwable1);
                                }
                            throw throwable;
                        }
                        if (statement != null)
                            statement.close();
                    } catch (Throwable throwable) {
                        if (statement != null)
                            try {
                                statement.close();
                            } catch (Throwable throwable1) {
                                throwable.addSuppressed(throwable1);
                            }
                        throw throwable;
                    }
                    if (con != null)
                        con.close();
                } catch (Throwable throwable) {
                    if (con != null)
                        try {
                            con.close();
                        } catch (Throwable throwable1) {
                            throwable.addSuppressed(throwable1);
                        }
                    throw throwable;
                }
            } catch (Exception e) {
                LOGGER.warn("Error while selecting top 15 pvp from database: " + e);
            }
            tb.append("</table>");
            tb.append("<br>");
            tb.append("<br>");
            tb.append("<a action=\"bypass voiced_ranking\">Back to Rankings</a>");
            tb.append("</center>");
            tb.append("</body>");
            tb.append("</html>");
            htm.setHtml(tb.toString());
            activeChar.sendPacket(htm);
        } else {
            activeChar.sendMessage("PvP Event is not in progress!");
        }
    }

    public boolean useVoicedCommand(String command, Player activeChar, String target) {
        if ((command.equals("pvpEvent") || command.equals("pvpevent")) && Config.PVP_EVENT_ENABLED)
            getTopHtml(activeChar);
        return true;
    }

    public String[] getVoicedCommandList() {
        return VOICED_COMMANDS;
    }
}
