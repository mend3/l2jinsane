package enginemods.main.engine.mods;

import enginemods.main.data.ConfigData;
import enginemods.main.data.PlayerData;
import enginemods.main.engine.AbstractMods;
import enginemods.main.enums.ItemIconType;
import enginemods.main.util.Util;
import enginemods.main.util.builders.html.Html;
import enginemods.main.util.builders.html.HtmlBuilder;
import mods.autofarm.AutofarmPlayerRoutine;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.xml.IconsTable;
import net.sf.l2j.gameserver.enums.PunishmentType;
import net.sf.l2j.gameserver.enums.skills.AbnormalEffect;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Monster;

import java.util.Arrays;
import java.util.List;

public class AntiBot extends AbstractMods {
    public AntiBot() {
        this.registerMod(ConfigData.ENABLE_AntiBot);
    }

    private static synchronized void generateHtmlIndex(Player activeChar) {
        HtmlBuilder hb = new HtmlBuilder(HtmlBuilder.HtmlType.HTML_TYPE);
        hb.append("<html><body><center>");
        hb.append("<br>");
        hb.append(Html.headHtml("ANTI BOT"));
        hb.append("<br>");
        hb.append("has ", PlayerData.get(activeChar).getAttempts(), " attemps!<br>");
        List<Integer> aux = Arrays.asList(0, 1, 3, 4, 5);
        ItemIconType itemIconType1 = ItemIconType.values()[aux.get(Rnd.get(aux.size()))];

        ItemIconType itemIconType2;
        for (itemIconType2 = ItemIconType.values()[aux.get(Rnd.get(aux.size()))]; itemIconType1 == itemIconType2; itemIconType2 = ItemIconType.values()[aux.get(Rnd.get(aux.size()))]) {
        }

        hb.append("It indicates which of these items is: <font color=\"LEVEL\">", itemIconType1.name().toLowerCase(), "</font><br>");
        hb.append("<table>");
        hb.append("<tr>");
        int rnd = Rnd.get(0, 3);
        PlayerData.get(activeChar).setAnswerRight("" + rnd);

        for (int i = 0; i <= 3; ++i) {
            String icon = "";
            if (i == rnd) {
                icon = IconsTable.getRandomItemType(itemIconType1, 40);
            } else {
                icon = IconsTable.getRandomItemType(itemIconType2, 40);
            }

            hb.append("<td align=\"center\" fixwidth=\"32\">");
            hb.append("<button value=\"\" action=\"bypass -h Engine AntiBot ", i, "\" width=\"32\" height=\"32\" back=\"", icon, "\" fore=\"", icon, "\">");
            hb.append("</td>");
        }

        hb.append("</tr>");
        hb.append("</table>");
        hb.append("</center></body></html>");
        sendHtml(activeChar, null, hb);
    }

    private static void sendPlayerJail(Player player) {
        if (player != null) {
            player.stopAbnormalEffect(AbnormalEffect.REDCIRCLE);
            player.setIsParalyzed(false);
            player.setIsInvul(false);
            player.getPunishment().setType(PunishmentType.JAIL, 10);
        }

    }

    public static void getInstance() {
    }

    public void onModState() {
        switch (this.getState()) {
            case END:
                this.cancelTimers("sendJail");
            case START:
            default:
        }
    }

    public void onKill(Creature killer, Creature victim, boolean isPet) {
        if (Util.areObjectType(Monster.class, victim) && killer.getActingPlayer() != null) {
            Player activeChar = killer.getActingPlayer();
            if (!PlayerData.get(activeChar).isFake()) {
                PlayerData.get(activeChar).increaseKills();
                int count = ConfigData.KILLER_MONSTERS_ANTIBOT_INCREASE_LEVEL ? ConfigData.KILLER_MONSTERS_ANTIBOT + activeChar.getLevel() * 3 : ConfigData.KILLER_MONSTERS_ANTIBOT;
                if (PlayerData.get(activeChar).getKills() >= count) {
                    PlayerData.get(activeChar).resetKills();
                    PlayerData.get(activeChar).setAnswerRight("");
                    PlayerData.get(activeChar).resetAttempts();
                    AutofarmPlayerRoutine bot = activeChar.getBot();
                    bot.stop();
                    activeChar.setAutoFarm(false);
                    activeChar.abortAttack();
                    activeChar.abortCast();
                    activeChar.stopMove(null);
                    activeChar.startAbnormalEffect(AbnormalEffect.REDCIRCLE);
                    activeChar.setTarget(activeChar);
                    activeChar.setIsParalyzed(true);
                    activeChar.setIsInvul(true);
                    generateHtmlIndex(activeChar);
                    this.startTimer("sendJail", ConfigData.TIME_CHECK_ANTIBOT * 1000L, null, activeChar, false);
                }

            }
        }
    }

    public void onTimer(String timerName, Npc npc, Player player) {
        switch (timerName) {
            case "sendJail":
                if (PlayerData.get(player).getAttempts() <= 0) {
                    sendPlayerJail(player);
                } else {
                    this.startTimer("sendJail", ConfigData.TIME_CHECK_ANTIBOT * 1000L, null, player, false);
                    PlayerData.get(player).decreaseAttempts();
                    generateHtmlIndex(player);
                }
            default:
        }
    }

    public void onEvent(Player player, Creature npc, String command) {
        if (PlayerData.get(player).isAnswerRight(command)) {
            player.sendMessage("Correct Verification!");
            player.stopAbnormalEffect(AbnormalEffect.REDCIRCLE);
            player.setIsParalyzed(false);
            player.setIsInvul(false);
            this.cancelTimer("sendJail", null, player);
        } else {
            player.sendMessage("Incorrect verification!");
            this.cancelTimer("sendJail", null, player);
            if (PlayerData.get(player).getAttempts() <= 0) {
                sendPlayerJail(player);
            } else {
                this.cancelTimer("sendJail", null, player);
                this.startTimer("sendJail", ConfigData.TIME_CHECK_ANTIBOT * 1000L, null, player, false);
                PlayerData.get(player).decreaseAttempts();
                generateHtmlIndex(player);
            }
        }

    }

    public boolean onExitWorld(Player player) {
        return this.getTimer("sendJail", player) != null;
    }

    private static class SingletonHolder {
        protected static final AntiBot INSTANCE = new AntiBot();
    }
}
