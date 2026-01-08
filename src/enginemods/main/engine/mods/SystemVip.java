package enginemods.main.engine.mods;

import enginemods.main.data.ConfigData;
import enginemods.main.data.PlayerData;
import enginemods.main.engine.AbstractMods;
import enginemods.main.enums.ExpSpType;
import enginemods.main.enums.ItemDropType;
import enginemods.main.holders.PlayerHolder;
import enginemods.main.instances.NpcDropsInstance;
import enginemods.main.instances.NpcExpInstance;
import enginemods.main.util.Util;
import enginemods.main.util.builders.html.Html;
import enginemods.main.util.builders.html.HtmlBuilder;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

public class SystemVip extends AbstractMods {
    public SystemVip() {
        this.registerMod(true);
    }

    public static void informeExpireVip(Player player) {
        HtmlBuilder hb = new HtmlBuilder(HtmlBuilder.HtmlType.HTML_TYPE);
        hb.append("<html><body>");
        hb.append(Html.headHtml("VIP"));
        hb.append("<br>");
        hb.append("<font color=9900CC>VIP Expire Date: </font>", PlayerData.get(player).getVipExpireDateFormat(), "<br>");
        hb.append("<font color=LEVEL>The VIP have exp/sp rate:</font><br>");
        hb.append("<img src=L2UI.SquareGray width=264 height=1>");
        hb.append("<table fixwidth=264 border=0 cellspacing=0 cellpadding=0 bgcolor=CC99FF>");
        hb.append("<tr>");
        hb.append("<td fixwidth=100 align=center><button value=\"Type\" action=\"\" width=100 height=19 back=L2UI_CH3.amountbox2 fore=L2UI_CH3.amountbox2></td>");
        hb.append("<td fixwidth=164 align=center><button value=\"Bonus\" action=\"\" width=164 height=19 back=L2UI_CH3.amountbox2 fore=L2UI_CH3.amountbox2></td>");
        hb.append("</tr>");
        hb.append("</table>");
        hb.append("<img src=L2UI.SquareGray width=264 height=1>");
        hb.append("<table fixwidth=264>");
        hb.append("<tr>");
        hb.append("<td fixwidth=100 align=center><font color=3366FF>EXP:</font></td>");
        hb.append("<td fixwidth=164 align=center><font color=LEVEL>", ConfigData.VIP_BONUS_XP * (double) 100.0F, "%</font></td>");
        hb.append("</tr>");
        hb.append("</table>");
        hb.append("<img src=L2UI.SquareGray width=264 height=1>");
        hb.append("<table fixwidth=264>");
        hb.append("<tr>");
        hb.append("<td fixwidth=100 align=center><font color=3366FF>SP:</font></td>");
        hb.append("<td fixwidth=164 align=center><font color=LEVEL>", ConfigData.VIP_BONUS_SP * (double) 100.0F, "%</font></td>");
        hb.append("</tr>");
        hb.append("</table>");
        hb.append("<img src=L2UI.SquareGray width=264 height=1>");
        hb.append("<br><br><font color=LEVEL>The VIP have drop rate:</font><br>");
        hb.append("<img src=L2UI.SquareGray width=264 height=1>");
        hb.append("<table fixwidth=264 border=0 cellspacing=0 cellpadding=0 bgcolor=CC99FF>");
        hb.append("<tr>");
        hb.append("<td fixwidth=100><button value=\"Type\" action=\"\" width=100 height=19 back=L2UI_CH3.amountbox2 fore=L2UI_CH3.amountbox2></td>");
        hb.append("<td fixwidth=82><button value=\"Bonus Amount\" action=\"\" width=82 height=19 back=L2UI_CH3.amountbox2 fore=L2UI_CH3.amountbox2></td>");
        hb.append("<td fixwidth=82><button value=\"Bonus Chance\" action=\"\" width=82 height=19 back=L2UI_CH3.amountbox2 fore=L2UI_CH3.amountbox2></td>");
        hb.append("</tr>");
        hb.append("</table>");
        hb.append("<img src=L2UI.SquareGray width=264 height=1>");
        hb.append("<table fixwidth=264 border=0 cellspacing=0 cellpadding=0>");
        hb.append("<tr>");
        hb.append("<td fixwidth=100 align=center><font color=3366FF>Normal:</font></td>");
        hb.append("<td fixwidth=82 align=center><font color=LEVEL>", ConfigData.VIP_BONUS_DROP_NORMAL_AMOUNT * (double) 100.0F, "%</font></td>");
        hb.append("<td fixwidth=82 align=center><font color=LEVEL>", ConfigData.VIP_BONUS_DROP_NORMAL_AMOUNT * (double) 100.0F, "%</font></td>");
        hb.append("</tr>");
        hb.append("</table>");
        hb.append("<img src=L2UI.SquareGray width=264 height=1>");
        hb.append("<table fixwidth=264 border=0 cellspacing=0 cellpadding=0>");
        hb.append("<tr>");
        hb.append("<td fixwidth=100 align=center><font color=3366FF>Spoil:</font></td>");
        hb.append("<td fixwidth=82 align=center><font color=LEVEL>", ConfigData.VIP_BONUS_DROP_SPOIL_AMOUNT * (double) 100.0F, "%</font></td>");
        hb.append("<td fixwidth=82 align=center><font color=LEVEL>", ConfigData.VIP_BONUS_DROP_SPOIL_CHANCE * (double) 100.0F, "%</font></td>");
        hb.append("</tr>");
        hb.append("</table>");
        hb.append("<img src=L2UI.SquareGray width=264 height=1>");
        hb.append("<table fixwidth=264 border=0 cellspacing=0 cellpadding=0>");
        hb.append("<tr>");
        hb.append("<td fixwidth=100 align=center><font color=3366FF>Seed:</font></td>");
        hb.append("<td fixwidth=82 align=center><font color=LEVEL>", ConfigData.VIP_BONUS_DROP_SEED_AMOUNT * (double) 100.0F, "%</font></td>");
        hb.append("<td fixwidth=82 align=center><font color=LEVEL>", ConfigData.VIP_BONUS_DROP_SEED_AMOUNT * (double) 100.0F, "%</font></td>");
        hb.append("</tr>");
        hb.append("</table>");
        hb.append("<img src=L2UI.SquareGray width=264 height=1>");
        hb.append("<table fixwidth=264 border=0 cellspacing=0 cellpadding=0>");
        hb.append("<tr>");
        hb.append("<td fixwidth=100 align=center><font color=3366FF>Herb:</font></td>");
        hb.append("<td fixwidth=82 align=center><font color=LEVEL>", ConfigData.VIP_BONUS_DROP_HERB_AMOUNT * (double) 100.0F, "%</font></td>");
        hb.append("<td fixwidth=82 align=center><font color=LEVEL>", ConfigData.VIP_BONUS_DROP_HERB_AMOUNT * (double) 100.0F, "%</font></td>");
        hb.append("</tr>");
        hb.append("</table>");
        hb.append("<img src=L2UI.SquareGray width=264 height=1>");
        hb.append("<img src=L2UI.SquareGray width=264 height=1>");
        hb.append("<table fixwidth=264 border=0 cellspacing=0 cellpadding=0>");
        hb.append("<tr>");
        hb.append("<td fixwidth=100 align=center><font color=3366FF>Events: </font></td>");
        hb.append("<td fixwidth=82 align=center><font color=LEVEL> x", ConfigData.VIP_DROP_EVENTS_MULTIPLIER, "</font></td>");
        hb.append("<td fixwidth=82 align=center><font color=LEVEL> x", ConfigData.VIP_DROP_EVENTS_MULTIPLIER, "</font></td>");
        hb.append("</tr>");
        hb.append("</table>");
        hb.append("<img src=L2UI.SquareGray width=264 height=1>");
        hb.append("<br>");
        hb.append("<p><font color=LEVEL>BossEvent - PvPEvent - Tournament - TvT </font></p>");
        hb.append("<br>");
        hb.append("<img src=L2UI.SquareGray width=264 height=1>");
        hb.append("</body></html>");
        sendHtml(player, null, hb);
    }

    public static void removeVip(Player player) {
        PlayerData.get(player).setVip(false);
        player.broadcastUserInfo();
    }

    private static boolean checkTarget(Player ph) {
        if (ph.getTarget() == null) {
            ph.sendMessage("this command need target");
            return false;
        } else if (!Util.areObjectType(Player.class, ph.getTarget())) {
            ph.sendMessage("this command need player target");
            return false;
        } else {
            return true;
        }
    }

    public static SystemVip getInstance() {
        return SystemVip.SingletonHolder.INSTANCE;
    }

    public void onModState() {
        switch (this.getState()) {
            case START:
                this.readAllVips();
            case END:
            default:
        }
    }

    public void onNpcExpSp(Player killer, Attackable npc, NpcExpInstance instance) {
        if (PlayerData.get(killer).isVip()) {
            instance.increaseRate(ExpSpType.EXP, ConfigData.VIP_BONUS_XP);
            instance.increaseRate(ExpSpType.SP, ConfigData.VIP_BONUS_SP);
        }
    }

    public void onNpcDrop(Player killer, Attackable npc, NpcDropsInstance instance) {
        if (PlayerData.get(killer).isVip()) {
            instance.increaseDrop(ItemDropType.NORMAL, ConfigData.VIP_BONUS_DROP_NORMAL_AMOUNT, ConfigData.VIP_BONUS_DROP_NORMAL_CHANCE);
            instance.increaseDrop(ItemDropType.SPOIL, ConfigData.VIP_BONUS_DROP_SPOIL_AMOUNT, ConfigData.VIP_BONUS_DROP_SPOIL_CHANCE);
            instance.increaseDrop(ItemDropType.HERB, ConfigData.VIP_BONUS_DROP_HERB_AMOUNT, ConfigData.VIP_BONUS_DROP_HERB_CHANCE);
            instance.increaseDrop(ItemDropType.SEED, ConfigData.VIP_BONUS_DROP_SEED_AMOUNT, ConfigData.VIP_BONUS_DROP_SEED_CHANCE);
        }
    }

    public void onEvent(Player player, Creature npc, String command) {
        StringTokenizer st = new StringTokenizer(command, " ");
        switch (st.nextToken()) {
            case "allVip":
                if (player.getAccessLevel().getLevel() >= 1) {
                    this.getAllPlayerVips(player, Integer.parseInt(st.nextToken()));
                }
            default:
        }
    }

    public boolean onAdminCommand(Player player, String chat) {
        StringTokenizer st = new StringTokenizer(chat, " ");
        switch (st.nextToken().toLowerCase()) {
            case "allvip":
                this.getAllPlayerVips(player, 1);
                return true;
            case "removevip":
                if (!checkTarget(player)) {
                    return true;
                }

                removeVip((Player) player.getTarget());
                return true;
            case "setvip":
                if (!checkTarget(player)) {
                    return true;
                } else if (!st.hasMoreTokens()) {
                    player.sendMessage("Correct command:");
                    player.sendMessage(".setVip days");
                    return true;
                } else {
                    String days = st.nextToken();
                    if (!Util.isNumber(days)) {
                        player.sendMessage("Correct command:");
                        player.sendMessage(".setVip days");
                        return true;
                    }

                    Player vip = (Player) player.getTarget();
                    Calendar time = new GregorianCalendar();
                    time.add(Calendar.DAY_OF_YEAR, Integer.parseInt(days));
                    this.setValueDB(vip, "vip", "" + time.getTimeInMillis());
                    PlayerData.get(vip).setVip(true);
                    PlayerData.get(vip).setVipExpireDate(time.getTimeInMillis());
                    this.addVip(vip, time.getTimeInMillis());
                    player.sendPacket(new ExShowScreenMessage("player: " + vip.getName() + " is Vip now", 10000, ExShowScreenMessage.SMPOS.TOP_CENTER, false));
                    vip.sendPacket(new ExShowScreenMessage("player: " + vip.getName() + " is Vip now", 10000, ExShowScreenMessage.SMPOS.TOP_CENTER, false));
                    informeExpireVip(vip);
                    return true;
                }
            default:
                return false;
        }
    }

    public void onEnterWorld(Player player) {
        if (PlayerData.get(player).isVip()) {
            if (PlayerData.get(player).getVipExpireDate() < System.currentTimeMillis()) {
                removeVip(player);
                return;
            }

            this.addVip(player, PlayerData.get(player).getVipExpireDate());
            informeExpireVip(player);
        }

    }

    public double onStats(Stats stat, Creature character, double value) {
        if (!Util.areObjectType(Player.class, character)) {
            return value;
        } else if (!PlayerData.get(character.getObjectId()).isVip()) {
            return value;
        } else {
            return ConfigData.VIP_STATS.containsKey(stat) ? value * ConfigData.VIP_STATS.get(stat) : value;
        }
    }

    public void addVip(Player player, long dayTime) {
        ThreadPool.schedule(() -> {
            if (player != null) {
                informeExpireVip(player);
                removeVip(player);
            }
        }, dayTime - System.currentTimeMillis());
        if (ConfigData.ALLOW_VIP_NCOLOR) {
            player.getAppearance().setNameColor(ConfigData.VIP_NCOLOR);
        }

        if (ConfigData.ALLOW_VIP_TCOLOR) {
            player.getAppearance().setTitleColor(ConfigData.VIP_TCOLOR);
        }

        player.broadcastUserInfo();
    }

    public void getAllPlayerVips(Player player, int page) {
        HtmlBuilder hb = new HtmlBuilder(HtmlBuilder.HtmlType.HTML_TYPE);
        hb.append("<html><body>");
        hb.append("<br>");
        hb.append(Html.headHtml("All VIP Players"));
        hb.append("<br>");
        hb.append("<table>");
        hb.append("<tr>");
        hb.append("<td width=64><font color=LEVEL>Player:</font></td><td width=200><font color=LEVEL>ExpireDate:</font></td>");
        hb.append("</tr>");
        hb.append("</table>");
        int MAX_PER_PAGE = 12;
        int searchPage = MAX_PER_PAGE * (page + 100);
        int count = 0;
        int countVip = 0;

        for (PlayerHolder ph : PlayerData.getAllPlayers()) {
            if (ph.isVip()) {
                ++countVip;
                if (count < searchPage) {
                    ++count;
                } else if (count < searchPage + MAX_PER_PAGE) {
                    hb.append("<table", count % 2 == 0 ? " bgcolor=000000>" : ">");
                    hb.append("<tr>");
                    hb.append("<td width=64>" + ph.getName(), "</td><td width=200>" + ph.getVipExpireDateFormat(), "</td>");
                    hb.append("</tr>");
                    hb.append("</table>");
                    ++count;
                }
            }
        }

        hb.append("<center>");
        hb.append("<img src=L2UI.SquareGray width=264 height=1>");
        hb.append("<table bgcolor=CC99FF>");
        hb.append("<tr>");
        int currentPage = 1;

        for (int i = 0; i < countVip; ++i) {
            if (i % MAX_PER_PAGE == 0) {
                hb.append("<td width=18><center><a action=\"bypass -h Engine SystemVip allVip ", currentPage, "\">" + currentPage, "</center></a></td>");
                ++currentPage;
            }
        }

        hb.append("</tr>");
        hb.append("</table>");
        hb.append("<img src=L2UI.SquareGray width=264 height=1>");
        hb.append("</center>");
        hb.append("</body></html>");
        sendHtml(player, null, hb);
    }

    private void readAllVips() {
        for (PlayerHolder ph : PlayerData.getAllPlayers()) {
            String timeInMillis = this.getValueDB(ph.getObjectId(), "vip");
            if (timeInMillis != null) {
                long dayTime = Long.parseLong(timeInMillis);
                if (dayTime >= System.currentTimeMillis()) {
                    PlayerData.get(ph.getObjectId()).setVip(true);
                    PlayerData.get(ph.getObjectId()).setVipExpireDate(dayTime);
                }
            }
        }

    }

    private static class SingletonHolder {
        protected static final SystemVip INSTANCE = new SystemVip();
    }
}
