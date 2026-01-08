package enginemods.main.engine.community;

import enginemods.main.data.ConfigData;
import enginemods.main.data.PlayerData;
import enginemods.main.engine.AbstractMods;
import enginemods.main.holders.PlayerHolder;
import enginemods.main.util.Util;
import enginemods.main.util.builders.html.Html;
import enginemods.main.util.builders.html.HtmlBuilder;
import enginemods.main.util.builders.html.HtmlBuilder.HtmlType;
import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.actor.player.Experience;

import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;

public class FavoriteCommunityBoard extends AbstractMods {
    public FavoriteCommunityBoard() {
        this.registerMod(ConfigData.ENABLE_BBS_FAVORITE);
    }

    private static String marcButton(String bypass) {
        HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML_TYPE);
        if (bypass != null) {
            hb.append("<table border=0 cellspacing=0 cellpadding=0>");
            hb.append("<tr>");
            hb.append("<td>", Html.newImage(bypass.equals("main") ? "L2UI_CH3.fishing_bar1" : "L2UI_CH3.ssq_cell1", 100, 1), "</td>");
            hb.append("<td>", Html.newImage(bypass.equals("rebirth") ? "L2UI_CH3.fishing_bar1" : "L2UI_CH3.ssq_cell1", 100, 1), "</td>");
            hb.append("<td>", Html.newImage(bypass.equals("stats") ? "L2UI_CH3.fishing_bar1" : "L2UI_CH3.ssq_cell1", 100, 1), "</td>");
            hb.append("<td>", Html.newImage(bypass.equals("maestrias") ? "L2UI_CH3.fishing_bar1" : "L2UI_CH3.ssq_cell1", 100, 1), "</td>");
            hb.append("</tr>");
            hb.append("</table>");
        } else {
            hb.append(Html.newImage("L2UI.SquareGray", 506, 1));
        }

        return hb.toString();
    }

    private static String bbsHead(String bypass) {
        HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY_TYPE);
        hb.append(marcButton(bypass));
        hb.append("<table border=0 cellspacing=0 cellpadding=0>");
        hb.append("<tr>");
        hb.append(newMenu("MAIN", "main"));
        hb.append(newMenu("REBIRTH", "rebirth"));
        hb.append(newMenu("STATS", "stats"));
        hb.append(newMenu("MASTERIES", "masters"));
        hb.append("</tr>");
        hb.append("</table>");
        hb.append(marcButton(bypass));
        hb.append("<br>");
        return hb.toString();
    }

    private static String bbsBodyPanelStats(Player player) {
        HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY_TYPE);
        hb.append("<br>");
        hb.append("<table bgcolor=000000 height=22 width=282 border=0 cellspacing=0 cellpadding=0>");
        hb.append("<tr>");
        hb.append("<td width=16 valign=top align=center height=22>", Html.newImage("L2UI_CH3.FrameBackLeft", 16, 22), "</td>");
        hb.append("<td width=250 align=center height=22><button value=\"EXTRA POINTS: ", PlayerData.get(player).getStatsPoints().get(), "\" width=250 height=22 back=L2UI_CH3.FrameBackMid fore=L2UI_CH3.FrameBackMid></td>");
        hb.append("<td width=16 valign=top align=center height=22>", Html.newImage("L2UI_CH3.FrameBackRight", 16, 22), "</td>");
        hb.append("</tr>");
        hb.append("</table>");
        hb.append("<br>");
        hb.append("<table height=22 width=282 border=0 cellspacing=1 cellpadding=0>");
        hb.append("<tr>");
        hb.append("<td width=91 align=center>", Html.newFontColor("FF8000", "STAT"), "</td>");
        hb.append("<td width=125 align=center>", Html.newFontColor("FF8000", "POINTS"), "</td>");
        hb.append("<td width=66 align=center>", Html.newFontColor("FF8000", "ACTION"), "</td>");
        hb.append("</tr>");
        hb.append("</table>");
        hb.append("<br>");
        hb.append("<table width=282 height=22 border=0 cellspacing=0 cellpadding=0>");
        hb.append("<tr>");
        hb.append("<td width=16 valign=top align=center height=22>", Html.newImage("L2UI_CH3.FrameBackLeft", 16, 22), "</td>");
        hb.append("<td width=75 align=center height=22><button value=STR width=75 height=22 back=L2UI_CH3.FrameBackMid fore=L2UI_CH3.FrameBackMid></td>");
        hb.append("<td width=125 align=center height=22><button value=", player.getSTR(), " width=125 height=22 back=L2UI_CH3.FrameBackMid fore=L2UI_CH3.FrameBackMid></td>");
        hb.append("<td width=16 valign=top align=center height=22>", Html.newImage("L2UI_CH3.FrameBackRight", 16, 22), "</td>");
        hb.append("<td width=50 align=center height=22><button value=\"\" action=\"bypass _bbsgetfav;stats;STAT_STR;add\" width=32 height=22 back=L2UI_CH3.mapbutton_zoomin1_over fore=L2UI_CH3.mapbutton_zoomin1_over></td>");
        hb.append("</tr>");
        hb.append("</table>");
        hb.append("<table width=282 border=0 cellspacing=1 cellpadding=0>");
        hb.append("<tr><td width=20 height=16>", Html.newImage("L2UI_CH3.ps_sizecontrol2_over", 16, 16), "</td><td width=230>", Html.newFontColor("FF8000", "P Atk: "), player.getPAtk(null), "</td></tr>");
        hb.append("</table>");
        hb.append("<table height=22 width=282 border=0 cellspacing=0 cellpadding=0>");
        hb.append("<tr>");
        hb.append("<td width=16 valign=top align=center height=22>", Html.newImage("L2UI_CH3.FrameBackLeft", 16, 22), "</td>");
        hb.append("<td width=75 align=center height=22><button value=DEX width=75 height=22 back=L2UI_CH3.FrameBackMid fore=L2UI_CH3.FrameBackMid></td>");
        hb.append("<td width=125 align=center height=22><button value=", player.getDEX(), " width=125 height=22 back=L2UI_CH3.FrameBackMid fore=L2UI_CH3.FrameBackMid></td>");
        hb.append("<td width=16 valign=top align=center height=22>", Html.newImage("L2UI_CH3.FrameBackRight", 16, 22), "</td>");
        hb.append("<td width=50 align=center height=22><button value=\"\" action=\"bypass _bbsgetfav;stats;STAT_DEX;add\" width=32 height=22 back=L2UI_CH3.mapbutton_zoomin1_over fore=L2UI_CH3.mapbutton_zoomin1_over></td>");
        hb.append("</tr>");
        hb.append("</table>");
        hb.append("<table width=282 border=0 cellspacing=1 cellpadding=0>");
        hb.append("<tr><td width=20 height=16>", Html.newImage("L2UI_CH3.ps_sizecontrol2_over", 16, 16), "</td><td width=230>", Html.newFontColor("FF8000", "Atk Spd: "), player.getPAtkSpd(), "</td></tr>");
        hb.append("<tr><td width=20 height=16>", Html.newImage("L2UI_CH3.ps_sizecontrol2_over", 16, 16), "</td><td width=230>", Html.newFontColor("FF8000", "Accuracy: "), player.getAccuracy(), "</td></tr>");
        hb.append("<tr><td width=20 height=16>", Html.newImage("L2UI_CH3.ps_sizecontrol2_over", 16, 16), "</td><td width=230>", Html.newFontColor("FF8000", "Evasion: "), player.getEvasionRate(null), "</td></tr>");
        hb.append("<tr><td width=20 height=16>", Html.newImage("L2UI_CH3.ps_sizecontrol2_over", 16, 16), "</td><td width=230>", Html.newFontColor("FF8000", "P Critical Rate: "), player.getCriticalHit(null, null), "</td></tr>");
        hb.append("</table>");
        hb.append("<table height=22 width=282 border=0 cellspacing=0 cellpadding=0>");
        hb.append("<tr>");
        hb.append("<td width=16 valign=top align=center height=22>", Html.newImage("L2UI_CH3.FrameBackLeft", 16, 22), "</td>");
        hb.append("<td width=75 align=center height=22><button value=CON width=75 height=22 back=L2UI_CH3.FrameBackMid fore=L2UI_CH3.FrameBackMid></td>");
        hb.append("<td width=125 align=center height=22><button value=", player.getCON(), " width=125 height=22 back=L2UI_CH3.FrameBackMid fore=L2UI_CH3.FrameBackMid></td>");
        hb.append("<td width=16 valign=top align=center height=22>", Html.newImage("L2UI_CH3.FrameBackRight", 16, 22), "</td>");
        hb.append("<td width=50 align=center height=22><button value=\"\" action=\"bypass _bbsgetfav;stats;STAT_CON;add\" width=32 height=22 back=L2UI_CH3.mapbutton_zoomin1_over fore=L2UI_CH3.mapbutton_zoomin1_over></td>");
        hb.append("</tr>");
        hb.append("</table>");
        hb.append("<table width=282 border=0 cellspacing=1 cellpadding=0>");
        hb.append("<tr><td width=20 height=16>", Html.newImage("L2UI_CH3.ps_sizecontrol2_over", 16, 16), "</td><td width=230>", Html.newFontColor("FF8000", "MaxHp: "), player.getMaxHp(), "</td></tr>");
        hb.append("<tr><td width=20 height=16>", Html.newImage("L2UI_CH3.ps_sizecontrol2_over", 16, 16), "</td><td width=230>", Html.newFontColor("FF8000", "MaxCp: "), player.getMaxCp(), "</td></tr>");
        hb.append("</table>");
        hb.append("<table height=22 width=282 border=0 cellspacing=0 cellpadding=0>");
        hb.append("<tr>");
        hb.append("<td width=16 valign=top align=center height=22>", Html.newImage("L2UI_CH3.FrameBackLeft", 16, 22), "</td>");
        hb.append("<td width=75 align=center height=22><button value=INT width=75 height=22 back=L2UI_CH3.FrameBackMid fore=L2UI_CH3.FrameBackMid></td>");
        hb.append("<td width=125 align=center height=22><button value=", player.getINT(), " width=125 height=22 back=L2UI_CH3.FrameBackMid fore=L2UI_CH3.FrameBackMid></td>");
        hb.append("<td width=16 valign=top align=center height=22>", Html.newImage("L2UI_CH3.FrameBackRight", 16, 22), "</td>");
        hb.append("<td width=50 align=center height=22><button value=\"\" action=\"bypass _bbsgetfav;stats;STAT_INT;add\" width=32 height=22 back=L2UI_CH3.mapbutton_zoomin1_over fore=L2UI_CH3.mapbutton_zoomin1_over></td>");
        hb.append("</tr>");
        hb.append("</table>");
        hb.append("<table width=282 border=0 cellspacing=1 cellpadding=0>");
        hb.append("<tr><td width=20 height=16>", Html.newImage("L2UI_CH3.ps_sizecontrol2_over", 16, 16), "</td><td width=230>", Html.newFontColor("FF8000", "M Atk: "), player.getMAtk(null, null), "</td></tr>");
        hb.append("</table>");
        hb.append("<table height=22 width=282 border=0 cellspacing=0 cellpadding=0>");
        hb.append("<tr>");
        hb.append("<td width=16 valign=top align=center height=22>", Html.newImage("L2UI_CH3.FrameBackLeft", 16, 22), "</td>");
        hb.append("<td width=75 align=center height=22><button value=WIT width=75 height=22 back=L2UI_CH3.FrameBackMid fore=L2UI_CH3.FrameBackMid></td>");
        hb.append("<td width=125 align=center height=22><button value=", player.getWIT(), " width=125 height=22 back=L2UI_CH3.FrameBackMid fore=L2UI_CH3.FrameBackMid></td>");
        hb.append("<td width=16 valign=top align=center height=22>", Html.newImage("L2UI_CH3.FrameBackRight", 16, 22), "</td>");
        hb.append("<td width=50 align=center height=22><button value=\"\" action=\"bypass _bbsgetfav;stats;STAT_WIT;add\" width=32 height=22 back=L2UI_CH3.mapbutton_zoomin1_over fore=L2UI_CH3.mapbutton_zoomin1_over></td>");
        hb.append("</tr>");
        hb.append("</table>");
        hb.append("<table width=282 border=0 cellspacing=1 cellpadding=0>");
        hb.append("<tr><td width=20 height=16>", Html.newImage("L2UI_CH3.ps_sizecontrol2_over", 16, 16), "</td><td width=230>", Html.newFontColor("FF8000", "M Spd: "), player.getMAtkSpd(), "</td></tr>");
        hb.append("<tr><td width=20 height=16>", Html.newImage("L2UI_CH3.ps_sizecontrol2_over", 16, 16), "</td><td width=230>", Html.newFontColor("FF8000", "M Critical Rate: "), player.getMCriticalHit(null, null), "</td></tr>");
        hb.append("</table>");
        hb.append("<table height=22 width=282 border=0 cellspacing=0 cellpadding=0>");
        hb.append("<tr>");
        hb.append("<td width=16 valign=top align=center height=22>", Html.newImage("L2UI_CH3.FrameBackLeft", 16, 22), "</td>");
        hb.append("<td width=75 align=center height=22><button value=MEN width=75 height=22 back=L2UI_CH3.FrameBackMid fore=L2UI_CH3.FrameBackMid></td>");
        hb.append("<td width=125 align=center height=22><button value=", player.getMEN(), " width=125 height=22 back=L2UI_CH3.FrameBackMid fore=L2UI_CH3.FrameBackMid></td>");
        hb.append("<td width=16 valign=top align=center height=22>", Html.newImage("L2UI_CH3.FrameBackRight", 16, 22), "</td>");
        hb.append("<td width=50 align=center height=22><button value=\"\" action=\"bypass _bbsgetfav;stats;STAT_MEN;add\" width=32 height=22 back=L2UI_CH3.mapbutton_zoomin1_over fore=L2UI_CH3.mapbutton_zoomin1_over></td>");
        hb.append("</tr>");
        hb.append("</table>");
        hb.append("<table width=282 border=0 cellspacing=1 cellpadding=0>");
        hb.append("<tr><td width=20 height=16>", Html.newImage("L2UI_CH3.ps_sizecontrol2_over", 16, 16), "</td><td width=230>", Html.newFontColor("FF8000", "MaxMp: "), player.getMaxMp(), "</td></tr>");
        hb.append("</table>");
        return hb.toString();
    }

    private static String newMenu(String butonName, String bypass) {
        HtmlBuilder hb = new HtmlBuilder();
        hb.append("<td><button value=\"", butonName, "\" action=\"bypass _bbsgetfav;", bypass, "\" width=100 height=32 back=L2UI_CH3.refinegrade3_21 fore=L2UI_CH3.refinegrade3_22></td>");
        return hb.toString();
    }

    public static FavoriteCommunityBoard getInstance() {
        return FavoriteCommunityBoard.SingletonHolder.INSTANCE;
    }

    public void onModState() {
        switch (this.getState()) {
            case START:
                this.readAllRebirths();
            case END:
            default:
        }
    }

    public boolean onCommunityBoard(Player player, String command) {
        if (command.startsWith("_bbsgetfav")) {
            StringTokenizer st = new StringTokenizer(command, ";");
            st.nextToken();
            String bypass = st.hasMoreTokens() ? st.nextToken() : "main";
            HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY_TYPE);
            hb.append("<html><body>");
            hb.append("<br>");
            hb.append("<center>");
            hb.append(bbsHead(bypass));
            byte var7 = -1;
            switch (bypass.hashCode()) {
                case 3343801:
                    if (bypass.equals("main")) {
                        var7 = 0;
                    }
                    break;
                case 109757599:
                    if (bypass.equals("stats")) {
                        var7 = 2;
                    }
                    break;
                case 656409371:
                    if (bypass.equals("maestrias")) {
                        var7 = 1;
                    }
                    break;
                case 1081495148:
                    if (bypass.equals("rebirth")) {
                        var7 = 3;
                    }
            }

            switch (var7) {
                case 0:
                    hb.append(this.bbsBodyMain(player));
                case 1:
                default:
                    break;
                case 2:
                    hb.append(bbsBodyPanelStats(player));
                    if (st.hasMoreTokens() && PlayerData.get(player).getStatsPoints().get() > 0) {
                        PlayerData.get(player).getStatsPoints().decrementAndGet();
                        Stats stat = Enum.valueOf(Stats.class, st.nextToken());
                        String var9 = st.nextToken();
                        byte var10 = -1;
                        switch (var9.hashCode()) {
                            case 96417:
                                if (var9.equals("add")) {
                                    var10 = 0;
                                }
                                break;
                            case 114240:
                                if (var9.equals("sub")) {
                                    var10 = 1;
                                }
                        }

                        switch (var10) {
                            case 0:
                                PlayerData.get(player).addCustomStat(stat, 1);
                                break;
                            case 1:
                                PlayerData.get(player).addCustomStat(stat, -1);
                        }

                        this.setValueDB(player.getObjectId(), stat.name(), String.valueOf(PlayerData.get(player).getCustomStat(stat)));
                        this.setValueDB(player.getObjectId(), "stats", String.valueOf(PlayerData.get(player).getStatsPoints().get()));
                        player.broadcastUserInfo();
                        hb.clean();
                        hb.append("<html><body>");
                        hb.append("<br>");
                        hb.append("<center>");
                        hb.append(bbsHead("stats"));
                        hb.append(bbsBodyPanelStats(player));
                    }
                    break;
                case 3:
                    if (!st.hasMoreTokens()) {
                        hb.append("<br><br><br><br>", Html.newFontColor("LEVEL", "Do you want to \"Rebirth\"???<br>"));
                        hb.append("<td><button value=\"REBIRTH\" action=\"bypass _bbsgetfav;rebirth;yes\" width=75 height=22 back=L2UI_CH3.Btn1_normalOn fore=L2UI_CH3.Btn1_normal></td>");
                    } else {
                        hb.append(this.bbsBodyRebirth(player));
                    }
            }

            hb.append("</center>");
            hb.append("</body></html>");
            sendCommunity(player, hb.toString());
            return true;
        } else {
            return false;
        }
    }

    public double onStats(Stats stat, Creature character, double value) {
        if (!Util.areObjectType(Playable.class, character)) {
            return value;
        } else {
            switch (stat) {
                case STAT_STR:
                case STAT_CON:
                case STAT_DEX:
                case STAT_INT:
                case STAT_WIT:
                case STAT_MEN:
                    value += PlayerData.get(character.getActingPlayer()).getCustomStat(stat);
                default:
                    return value;
            }
        }
    }

    public void onKill(Creature killer, Creature victim, boolean isPet) {
        if (Util.areObjectType(Monster.class, victim) && killer.getActingPlayer() != null) {
            if (killer.getLevel() == 81) {
                killer.sendMessage("Top level!");
            }

        }
    }

    private String bbsBodyRebirth(Player player) {
        HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY_TYPE);
        if (PlayerData.get(player).getRebirth() >= ConfigData.MAX_REBIRTH) {
            hb.append("<br><br><br><br>", Html.newFontColor("LEVEL", "You cannot be reborn any more times!<br>"));
            hb.append("Remember that the maximum number of rebirths is ", ConfigData.MAX_REBIRTH);
            return hb.toString();
        } else if (player.getLevel() < 80) {
            hb.append("<br><br><br><br>", Html.newFontColor("LEVEL", "You haven't reached the maximum level yet!<br>"));
            hb.append("Remember that the level for rebirth is ", 80);
            return hb.toString();
        } else {
            player.removeExpAndSp(player.getExp() - Experience.LEVEL[ConfigData.LVL_REBIRTH], 0);
            PlayerData.get(player).increaseRebirth();
            this.setValueDB(player.getObjectId(), "rebirth", String.valueOf(PlayerData.get(player).getRebirth()));
            int var10001 = player.getObjectId();
            AtomicInteger var10003 = PlayerData.get(player).getMaestriasPoints();
            this.setValueDB(var10001, "maestrias", String.valueOf(var10003.addAndGet(ConfigData.MASTERY_POINT_PER_REBIRTH)));
            var10001 = player.getObjectId();
            var10003 = PlayerData.get(player).getStatsPoints();
            this.setValueDB(var10001, "stats", String.valueOf(var10003.addAndGet(ConfigData.STAT_POINT_PER_REBIRTH)));
            this.setValueDB(player.getObjectId(), "STAT_STR", "0");
            this.setValueDB(player.getObjectId(), "STAT_CON", "0");
            this.setValueDB(player.getObjectId(), "STAT_DEX", "0");
            this.setValueDB(player.getObjectId(), "STAT_INT", "0");
            this.setValueDB(player.getObjectId(), "STAT_WIT", "0");
            this.setValueDB(player.getObjectId(), "STAT_MEN", "0");
            hb.append("<br><br><br><br>", Html.newFontColor("LEVEL", "Congratulations, you have successfully completed the rebirth!<br>"));
            hb.append("Don't forget to add up your points and improve your masteries.<br>");
            hb.append("");
            hb.append("");
            hb.append("");
            return hb.toString();
        }
    }

    public String bbsBodyMain(Player player) {
        HtmlBuilder hb = new HtmlBuilder(HtmlType.COMUNITY_TYPE);
        hb.append("<br>");
        hb.append("<center>");
        hb.append("Welcome ", Html.newFontColor("LEVEL", player.getName()), " to the rebirth system.<br>");
        hb.append("If you have reached level ", Html.newFontColor("LEVEL", 81), ",  you are ready to be reborn.<br>");
        hb.append("and become a more powerful warrior....<br>");
        hb.append("you might even reach the power of a god!<br>");
        hb.append("<br>");
        hb.append("You currently have ", Html.newFontColor("LEVEL", PlayerData.get(player).getRebirth()), " rebirths, and you will be reborn ", Html.newFontColor("LEVEL", ConfigData.MAX_REBIRTH), " more times.<br>");
        hb.append("<br>");
        hb.append("With each rebirth you will gain:<br>");
        hb.append("* ", Html.newFontColor("LEVEL", ConfigData.STAT_POINT_PER_REBIRTH), " that you can add them to the stas you like.<br>");
        hb.append("* ", Html.newFontColor("LEVEL", ConfigData.MASTERY_POINT_PER_REBIRTH), " you will be able to improve your mastery tree.<br>");
        return hb.toString();
    }

    private void readAllRebirths() {

        for (PlayerHolder ph : PlayerData.getAllPlayers()) {
            String rebirthCount = this.getValueDB(ph.getObjectId(), "rebirth");
            if (rebirthCount != null) {
                int rebirth = Integer.parseInt(rebirthCount);
                if (rebirth != 0) {
                    PlayerData.get(ph.getObjectId()).setRebirth(rebirth);

                    try {
                        String mCount = this.getValueDB(ph.getObjectId(), "maestrias");
                        PlayerData.get(ph.getObjectId()).getMaestriasPoints().set(Integer.parseInt(mCount));
                        String sCount = this.getValueDB(ph.getObjectId(), "stats");
                        PlayerData.get(ph.getObjectId()).getStatsPoints().set(Integer.parseInt(sCount));
                        int stat_str = Integer.parseInt(this.getValueDB(ph.getObjectId(), "STAT_STR"));
                        int stat_con = Integer.parseInt(this.getValueDB(ph.getObjectId(), "STAT_CON"));
                        int stat_dex = Integer.parseInt(this.getValueDB(ph.getObjectId(), "STAT_DEX"));
                        int stat_int = Integer.parseInt(this.getValueDB(ph.getObjectId(), "STAT_INT"));
                        int stat_wit = Integer.parseInt(this.getValueDB(ph.getObjectId(), "STAT_WIT"));
                        int stat_men = Integer.parseInt(this.getValueDB(ph.getObjectId(), "STAT_MEN"));
                        PlayerData.get(ph.getObjectId()).addCustomStat(Stats.STAT_STR, stat_str);
                        PlayerData.get(ph.getObjectId()).addCustomStat(Stats.STAT_CON, stat_con);
                        PlayerData.get(ph.getObjectId()).addCustomStat(Stats.STAT_DEX, stat_dex);
                        PlayerData.get(ph.getObjectId()).addCustomStat(Stats.STAT_INT, stat_int);
                        PlayerData.get(ph.getObjectId()).addCustomStat(Stats.STAT_WIT, stat_wit);
                        PlayerData.get(ph.getObjectId()).addCustomStat(Stats.STAT_MEN, stat_men);
                    } catch (Exception var13) {
                        var13.printStackTrace();
                    }
                }
            }
        }

    }

    private static class SingletonHolder {
        protected static final FavoriteCommunityBoard INSTANCE = new FavoriteCommunityBoard();
    }
}