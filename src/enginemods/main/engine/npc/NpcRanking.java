package enginemods.main.engine.npc;

import enginemods.main.engine.AbstractMods;
import enginemods.main.util.Util;
import enginemods.main.util.builders.html.Html;
import enginemods.main.util.builders.html.HtmlBuilder;
import enginemods.main.util.builders.html.HtmlBuilder.HtmlType;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NpcRanking extends AbstractMods {
    public static final List<NpcRanking.RankingHolder> _rankingPvP = new ArrayList();
    public static final List<NpcRanking.RankingHolder> _rankingPk = new ArrayList();
    private static final int NPC = 60008;
    private static final String SQL_PVP = "SELECT char_name,pvpkills FROM characters WHERE pvpkills>0 AND accesslevel=0 ORDER BY pvpkills DESC LIMIT 20";
    private static final String SQL_PK = "SELECT char_name,pkkills FROM characters WHERE pkkills>0 AND accesslevel=0 ORDER BY pkkills DESC LIMIT 20";

    public NpcRanking() {
        this.registerMod(true);
    }

    private static HtmlBuilder getRanking(List<NpcRanking.RankingHolder> ranking, String rankingName) {
        HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML_TYPE);
        hb.append("<html><body>");
        hb.append(Html.headHtml("RANKING " + rankingName));
        hb.append("<br>");
        hb.append("<table width=280>");
        hb.append("<tr>");
        hb.append("<td fixwidth=40><button value=\"Pos\" action=\"\" width=40 height=19 back=L2UI_CH3.amountbox2 fore=L2UI_CH3.amountbox2></td>");
        hb.append("<td fixwidth=120><button value=\"Player\" action=\"\" width=120 height=19 back=L2UI_CH3.amountbox2 fore=L2UI_CH3.amountbox2></td>");
        hb.append("<td fixwidth=120><button value=\"Kills\" action=\"\" width=120 height=19 back=L2UI_CH3.amountbox2 fore=L2UI_CH3.amountbox2></td>");
        hb.append("</tr>");
        hb.append("</table>");
        int pos = 1;

        for (Iterator var4 = ranking.iterator(); var4.hasNext(); ++pos) {
            NpcRanking.RankingHolder rh = (NpcRanking.RankingHolder) var4.next();
            hb.append(Html.newImage("L2UI.SquareGray", 280, 1));
            hb.append("<table width=280>");
            hb.append("<tr>");
            hb.append("<td fixwidth=40><center><font color=F7D358>" + pos + "</font></td>");
            hb.append("<td fixwidth=120><center> ", rh.name, " </center></td>");
            hb.append("<td fixwidth=120><center> ", rh.kills, " </center></td>");
            hb.append("</tr>");
            hb.append("</table>");
        }

        hb.append(Html.newImage("L2UI.SquareGray", 280, 1));
        hb.append("</center>");
        hb.append("</body></html>");
        return hb;
    }

    public static NpcRanking getInstance() {
        return NpcRanking.SingletonHolder.INSTANCE;
    }

    public void onModState() {
        switch (this.getState()) {
            case START:
                this.startTimer("loadRankingPvP", 0L, null, null, false);
                this.startTimer("loadRankingPvP", 60000L, null, null, true);
                this.startTimer("loadRankingPk", 0L, null, null, false);
                this.startTimer("loadRankingPk", 60000L, null, null, true);
                break;
            case END:
                _rankingPvP.clear();
                _rankingPk.clear();
                this.cancelTimers("loadRankingPvP");
                this.cancelTimers("loadRankingPk");
        }

    }

    public void onTimer(String timerName, Npc npc, Player player) {
        byte var5 = -1;
        switch (timerName.hashCode()) {
            case -1377717126:
                if (timerName.equals("loadRankingPvP")) {
                    var5 = 0;
                }
                break;
            case 509746827:
                if (timerName.equals("loadRankingPk")) {
                    var5 = 1;
                }
        }

        Connection con;
        PreparedStatement statement;
        ResultSet rset;
        NpcRanking.RankingHolder rh;
        switch (var5) {
            case 0:
                _rankingPvP.clear();

                try {
                    con = ConnectionPool.getConnection();

                    try {
                        statement = con.prepareStatement("SELECT char_name,pvpkills FROM characters WHERE pvpkills>0 AND accesslevel=0 ORDER BY pvpkills DESC LIMIT 20");

                        try {
                            rset = statement.executeQuery();

                            try {
                                while (rset.next()) {
                                    rh = new RankingHolder(this);
                                    rh.name = rset.getString("char_name");
                                    rh.kills = rset.getInt("pvpkills");
                                    _rankingPvP.add(rh);
                                }
                            } catch (Throwable var21) {
                                if (rset != null) {
                                    try {
                                        rset.close();
                                    } catch (Throwable var16) {
                                        var21.addSuppressed(var16);
                                    }
                                }

                                throw var21;
                            }

                            if (rset != null) {
                                rset.close();
                            }
                        } catch (Throwable var22) {
                            if (statement != null) {
                                try {
                                    statement.close();
                                } catch (Throwable var15) {
                                    var22.addSuppressed(var15);
                                }
                            }

                            throw var22;
                        }

                        if (statement != null) {
                            statement.close();
                        }
                    } catch (Throwable var23) {
                        if (con != null) {
                            try {
                                con.close();
                            } catch (Throwable var14) {
                                var23.addSuppressed(var14);
                            }
                        }

                        throw var23;
                    }

                    if (con != null) {
                        con.close();
                    }
                } catch (Exception var24) {
                    var24.printStackTrace();
                }
                break;
            case 1:
                _rankingPk.clear();

                try {
                    con = ConnectionPool.getConnection();

                    try {
                        statement = con.prepareStatement("SELECT char_name,pkkills FROM characters WHERE pkkills>0 AND accesslevel=0 ORDER BY pkkills DESC LIMIT 20");

                        try {
                            rset = statement.executeQuery();

                            try {
                                while (rset.next()) {
                                    rh = new RankingHolder(this);
                                    rh.name = rset.getString("char_name");
                                    rh.kills = rset.getInt("pkkills");
                                    _rankingPk.add(rh);
                                }
                            } catch (Throwable var17) {
                                if (rset != null) {
                                    try {
                                        rset.close();
                                    } catch (Throwable var13) {
                                        var17.addSuppressed(var13);
                                    }
                                }

                                throw var17;
                            }

                            if (rset != null) {
                                rset.close();
                            }
                        } catch (Throwable var18) {
                            if (statement != null) {
                                try {
                                    statement.close();
                                } catch (Throwable var12) {
                                    var18.addSuppressed(var12);
                                }
                            }

                            throw var18;
                        }

                        if (statement != null) {
                            statement.close();
                        }
                    } catch (Throwable var19) {
                        if (con != null) {
                            try {
                                con.close();
                            } catch (Throwable var11) {
                                var19.addSuppressed(var11);
                            }
                        }

                        throw var19;
                    }

                    if (con != null) {
                        con.close();
                    }
                } catch (Exception var20) {
                    var20.printStackTrace();
                }
        }

    }

    public boolean onInteract(Player player, Creature npc) {
        if (!Util.areObjectType(Npc.class, npc)) {
            return false;
        } else if (((Npc) npc).getNpcId() != 60008) {
            return false;
        } else {
            HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML_TYPE);
            hb.append("<html><body>");
            hb.append(Html.headHtml("RANKING"));
            hb.append("<br>");
            hb.append("Welcome my name is ", npc.getName(), " and take care to meet the most famous players in the world.<br>");
            hb.append("You probably want to know who it is!<br>");
            hb.append("I actually have a list, I can show it to you if you want.<br>");
            hb.append("What would you like to see?<br>");
            hb.append("<center>");
            hb.append("<table width=280>");
            hb.append("<tr>");
            hb.append("<td>", Html.newImage("L2UI.bbs_folder", 32, 32), "</td>");
            hb.append("<td><button value=\"Top PvP\" action=\"bypass -h Engine NpcRanking pvp\" width=216 height=32 back=L2UI_CH3.refinegrade3_21 fore=L2UI_CH3.refinegrade3_21></td>");
            hb.append("<td>", Html.newImage("L2UI.bbs_folder", 32, 32), "</td>");
            hb.append("</tr>");
            hb.append("<tr>");
            hb.append("<td>", Html.newImage("L2UI.bbs_folder", 32, 32), "</td>");
            hb.append("<td><button value=\"Top PK\" action=\"bypass -h Engine NpcRanking pk\" width=216 height=32 back=L2UI_CH3.refinegrade3_21 fore=L2UI_CH3.refinegrade3_21></td>");
            hb.append("<td>", Html.newImage("L2UI.bbs_folder", 32, 32), "</td>");
            hb.append("</tr>");
            hb.append("</table>");
            hb.append("</center>");
            hb.append("</body></html>");
            sendHtml(player, (Npc) npc, hb);
            return true;
        }
    }

    public void onEvent(Player player, Creature npc, String command) {
        if (((Npc) npc).getNpcId() == 60008) {
            byte var5 = -1;
            switch (command.hashCode()) {
                case 3579:
                    if (command.equals("pk")) {
                        var5 = 1;
                    }
                    break;
                case 111402:
                    if (command.equals("pvp")) {
                        var5 = 0;
                    }
            }

            switch (var5) {
                case 0:
                    sendHtml(player, null, getRanking(_rankingPvP, "PVP"));
                    break;
                case 1:
                    sendHtml(player, null, getRanking(_rankingPk, "PK"));
            }

        }
    }

    public static class RankingHolder {
        String name;
        int kills;

        public RankingHolder(final NpcRanking param1) {
        }
    }

    private static class SingletonHolder {
        protected static final NpcRanking INSTANCE = new NpcRanking();
    }
}