package enginemods.main.engine.mods;

import enginemods.main.data.ConfigData;
import enginemods.main.data.PlayerData;
import enginemods.main.data.SkillData;
import enginemods.main.engine.AbstractMods;
import enginemods.main.holders.PlayerHolder;
import enginemods.main.util.Util;
import enginemods.main.util.builders.html.Html;
import enginemods.main.util.builders.html.HtmlBuilder;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Gatekeeper;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.zone.ZoneType;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

public class SystemAio extends AbstractMods {
    public SystemAio() {
        this.registerMod(true);
    }

    private static void informeExpireAio(Player player, int page) {
        HtmlBuilder hb = new HtmlBuilder(HtmlBuilder.HtmlType.HTML_TYPE);
        hb.append("<html><body>");
        hb.append("<br>");
        hb.append(Html.headHtml("AIO"));
        hb.append("<br>");
        hb.append("<font color=9900CC>AIO Expire Date: </font>", PlayerData.get(player).getAioExpireDateFormat(), "<br>");
        hb.append("<font color=9900CC>The AIO have the skills:</font><br>");
        hb.append("<table>");
        int MAX_PER_PAGE = 12;
        int searchPage = MAX_PER_PAGE * (page - 1);
        int count = 0;

        for (IntIntHolder bh : ConfigData.AIO_LIST_SKILLS) {
            if (count < searchPage) {
                ++count;
            } else if (count < searchPage + MAX_PER_PAGE) {
                hb.append("<tr>");
                hb.append("<td width=32><img src=", SkillData.getSkillIcon(bh.getId()), " width=32 height=16></td>");
                hb.append("<td width=200><font color=LEVEL>", bh.getSkill().getName(), "</font></td>");
                hb.append("</tr>");
                ++count;
            }
        }

        hb.append("</table>");
        hb.append("<center>");
        hb.append("<img src=L2UI.SquareGray width=264 height=1>");
        hb.append("<table bgcolor=CC99FF>");
        hb.append("<tr>");
        int currentPage = 1;

        for (int i = 0; i < ConfigData.AIO_LIST_SKILLS.size(); ++i) {
            if (i % MAX_PER_PAGE == 0) {
                hb.append("<td width=18 align=center><a action=\"bypass -h Engine SystemAio aioInfo ", currentPage, "\">" + currentPage, "</a></td>");
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

    public static SystemAio getInstance() {
        return SystemAio.SingletonHolder.INSTANCE;
    }

    public void onModState() {
        switch (this.getState()) {
            case START:
                this.readAllAios();
            case END:
            default:
        }
    }

    public boolean onInteract(Player player, Creature npc) {
        return PlayerData.get(player.getObjectId()).isAio() && !Util.areObjectType(Gatekeeper.class, npc);
    }

    public void onExitZone(Creature player, ZoneType zone) {
        if (Util.areObjectType(Player.class, player)) {
            if (PlayerData.get(player.getObjectId()).isAio()) {
                if (!player.isInsideZone(ZoneId.TOWN) && !ConfigData.AIO_CAN_EXIT_PEACE_ZONE && player instanceof Player && PlayerData.get(player.getObjectId()).isAio()) {
                    ThreadPool.schedule(() -> player.doCast(SkillTable.getInstance().getInfo(2100, 1)), 10L);
                }

            }
        }
    }

    public void onEvent(Player player, Creature npc, String command) {
        StringTokenizer st = new StringTokenizer(command, " ");
        switch (st.nextToken()) {
            case "allAio":
                if (player.getAccessLevel().getLevel() >= 1) {
                    this.getAllPlayerAios(player, Integer.parseInt(st.nextToken()));
                }
                break;
            case "aioInfo":
                informeExpireAio(player, Integer.parseInt(st.nextToken()));
        }

    }

    public boolean onAdminCommand(Player player, String chat) {
        StringTokenizer st = new StringTokenizer(chat, " ");
        switch (st.nextToken().toLowerCase()) {
            case "allaio":
                this.getAllPlayerAios(player, 1);
                return true;
            case "removeaio":
                if (!checkTarget(player)) {
                    return true;
                }

                this.removeAio((Player) player.getTarget());
                return true;
            case "setaio":
                if (!checkTarget(player)) {
                    return true;
                } else if (!st.hasMoreTokens()) {
                    player.sendMessage("Correct command:");
                    player.sendMessage(".setAio days");
                    return true;
                } else {
                    String days = st.nextToken();
                    if (!Util.isNumber(days)) {
                        player.sendMessage("Correct command:");
                        player.sendMessage(".setAio days");
                        return true;
                    }

                    Player aio = (Player) player.getTarget();
                    Calendar time = new GregorianCalendar();
                    time.add(Calendar.DAY_OF_YEAR, Integer.parseInt(days));
                    this.setValueDB(aio, "aio", "" + time.getTimeInMillis());
                    PlayerData.get(aio).setAio(true);
                    PlayerData.get(aio).setAioExpireDate(time.getTimeInMillis());
                    this.addAio(aio, time.getTimeInMillis());
                    player.sendPacket(new ExShowScreenMessage("player: " + aio.getName() + "is Aio now", 10000, ExShowScreenMessage.SMPOS.TOP_CENTER, false));
                    aio.sendPacket(new ExShowScreenMessage("Dear " + aio.getName() + " your are now Aio", 10000, ExShowScreenMessage.SMPOS.TOP_CENTER, false));
                    informeExpireAio(aio, 1);
                    ItemInstance item = ItemInstance.create(ConfigData.AIO_ITEM_ID, 1, aio, aio);
                    aio.addItem("aio", item, aio, true);
                    return true;
                }
            default:
                return false;
        }
    }

    public void onEnterWorld(Player player) {
        if (PlayerData.get(player).isAio()) {
            if (PlayerData.get(player).getAioExpireDate() < System.currentTimeMillis()) {
                this.removeAio(player);
                return;
            }

            this.addAio(player, PlayerData.get(player).getAioExpireDate());
            informeExpireAio(player, 1);
        }

    }

    public double onStats(Stats stat, Creature character, double value) {
        if (!Util.areObjectType(Player.class, character)) {
            return value;
        } else if (!PlayerData.get(character.getObjectId()).isAio()) {
            return value;
        } else {
            return ConfigData.AIO_STATS.containsKey(stat) ? value * ConfigData.AIO_STATS.get(stat) : value;
        }
    }

    public void addAio(Player player, long dayTime) {
        ThreadPool.schedule(() -> {
            if (player != null) {
                informeExpireAio(player, 1);
                this.removeAio(player);
            }
        }, dayTime - System.currentTimeMillis());
        if (ConfigData.AIO_SET_MAX_LVL) {
            player.getStat().addExp(player.getStat().getExpForLevel(81));
        }

        if (player.getKarma() > 0) {
            player.setKarma(0);
        }

        if (!player.isInsideZone(ZoneId.PEACE)) {
        }

        player.setTitle(ConfigData.AIO_TITLE);

        for (IntIntHolder bh : ConfigData.AIO_LIST_SKILLS) {
            player.addSkill(bh.getSkill(), false);
        }

        if (ConfigData.ALLOW_AIO_NCOLOR) {
            player.getAppearance().setNameColor(ConfigData.AIO_NCOLOR);
        }

        if (ConfigData.ALLOW_AIO_TCOLOR) {
            player.getAppearance().setTitleColor(ConfigData.AIO_TCOLOR);
        }

        player.broadcastUserInfo();
    }

    public void removeAio(Player player) {
        PlayerData.get(player).setAio(false);
        player.setTitle(" ");
        player.broadcastUserInfo();
    }

    public void getAllPlayerAios(Player player, int page) {
        HtmlBuilder hb = new HtmlBuilder(HtmlBuilder.HtmlType.HTML_TYPE);
        hb.append("<html><body>");
        hb.append("<br>");
        hb.append(Html.headHtml("All AIO Players"));
        hb.append("<br>");
        hb.append("<table>");
        hb.append("<tr>");
        hb.append("<td width=64><font color=LEVEL>Player:</font></td><td width=200><font color=LEVEL>ExpireDate:</font></td>");
        hb.append("</tr>");
        hb.append("</table>");
        int MAX_PER_PAGE = 12;
        int searchPage = MAX_PER_PAGE * (page - 1);
        int count = 0;
        int countAio = 0;

        for (PlayerHolder ph : PlayerData.getAllPlayers()) {
            if (ph.isAio()) {
                ++countAio;
                if (count < searchPage) {
                    ++count;
                } else if (count < searchPage + MAX_PER_PAGE) {
                    hb.append("<table", count % 2 == 0 ? " bgcolor=000000>" : ">");
                    hb.append("<tr>");
                    hb.append("<td width=64>", ph.getName(), "</td><td width=200>", ph.getAioExpireDateFormat(), "</td>");
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

        for (int i = 0; i < countAio; ++i) {
            if (i % MAX_PER_PAGE == 0) {
                hb.append("<td width=18 align=center><a action=\"bypass -h Engine SystemAio allAio ", currentPage, "\">", currentPage, "</a></td>");
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

    private void readAllAios() {
        for (PlayerHolder ph : PlayerData.getAllPlayers()) {
            String timeInMillis = this.getValueDB(ph.getObjectId(), "aio");
            if (timeInMillis != null) {
                long dayTime = Long.parseLong(timeInMillis);
                if (dayTime >= System.currentTimeMillis()) {
                    PlayerData.get(ph.getObjectId()).setAio(true);
                    PlayerData.get(ph.getObjectId()).setAioExpireDate(dayTime);
                }
            }
        }

    }

    private static class SingletonHolder {
        protected static final SystemAio INSTANCE = new SystemAio();
    }
}
