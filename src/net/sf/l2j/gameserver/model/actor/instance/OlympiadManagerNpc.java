package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.gameserver.data.manager.HeroManager;
import net.sf.l2j.gameserver.data.xml.MultisellData;
import net.sf.l2j.gameserver.enums.OlympiadType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.model.olympiad.OlympiadGameManager;
import net.sf.l2j.gameserver.model.olympiad.OlympiadGameTask;
import net.sf.l2j.gameserver.model.olympiad.OlympiadManager;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.ExHeroList;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class OlympiadManagerNpc extends Folk {
    private static final List<OlympiadManagerNpc> _managers = new CopyOnWriteArrayList<>();

    private static final int GATE_PASS = 6651;

    public OlympiadManagerNpc(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public static List<OlympiadManagerNpc> getInstances() {
        return _managers;
    }

    public String getHtmlPath(int npcId, int val) {
        String filename = "noble";
        if (val > 0)
            filename = "noble_" + val;
        return filename + ".htm";
    }

    public void showChatWindow(Player player, int val) {
        int npcId = getTemplate().getNpcId();
        String filename = getHtmlPath(npcId, val);
        switch (npcId) {
            case 31688:
                if (player.isNoble() && val == 0)
                    filename = "noble_main.htm";
                break;
            case 31690:
            case 31769:
            case 31770:
            case 31771:
            case 31772:
                if (player.isHero() || HeroManager.getInstance().isInactiveHero(player.getObjectId())) {
                    filename = "hero_main.htm";
                    break;
                }
                filename = "hero_main2.htm";
                break;
        }
        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
        html.setFile("data/html/olympiad/" + filename);
        if (filename == "hero_main.htm") {
            String hiddenText = "";
            if (HeroManager.getInstance().isInactiveHero(player.getObjectId()))
                hiddenText = "<a action=\"bypass -h npc_%objectId%_Olympiad 5\">\"I want to be a Hero.\"</a><br>";
            html.replace("%hero%", hiddenText);
        }
        html.replace("%objectId%", getObjectId());
        player.sendPacket(html);
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }

    public void onBypassFeedback(Player player, String command) {
        if (command.startsWith("OlympiadNoble")) {
            int nonClassed, classed, points;
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            if (player.isCursedWeaponEquipped()) {
                html.setFile("data/html/olympiad/noble_cant_cw.htm");
                player.sendPacket(html);
                return;
            }
            if (player.getClassIndex() != 0) {
                html.setFile("data/html/olympiad/noble_cant_sub.htm");
                html.replace("%objectId%", getObjectId());
                player.sendPacket(html);
                return;
            }
            if (!player.isNoble() || player.getClassId().level() < 3) {
                html.setFile("data/html/olympiad/noble_cant_thirdclass.htm");
                html.replace("%objectId%", getObjectId());
                player.sendPacket(html);
                return;
            }
            int val = Integer.parseInt(command.substring(14));
            switch (val) {
                case 1:
                    OlympiadManager.getInstance().unRegisterNoble(player);
                    break;
                case 2:
                    nonClassed = OlympiadManager.getInstance().getRegisteredNonClassBased().size();
                    classed = OlympiadManager.getInstance().getRegisteredClassBased().size();
                    html.setFile("data/html/olympiad/noble_registered.htm");
                    html.replace("%listClassed%", classed);
                    html.replace("%listNonClassed%", nonClassed);
                    html.replace("%objectId%", getObjectId());
                    player.sendPacket(html);
                    break;
                case 3:
                    points = Olympiad.getInstance().getNoblePoints(player.getObjectId());
                    html.setFile("data/html/olympiad/noble_points1.htm");
                    html.replace("%points%", points);
                    html.replace("%objectId%", getObjectId());
                    player.sendPacket(html);
                    break;
                case 4:
                    OlympiadManager.getInstance().registerNoble(player, OlympiadType.NON_CLASSED);
                    break;
                case 5:
                    OlympiadManager.getInstance().registerNoble(player, OlympiadType.CLASSED);
                    break;
                case 6:
                    html.setFile("data/html/olympiad/" + ((Olympiad.getInstance().getNoblessePasses(player, false) > 0) ? "noble_settle.htm" : "noble_nopoints2.htm"));
                    html.replace("%objectId%", getObjectId());
                    player.sendPacket(html);
                    break;
                case 7:
                    MultisellData.getInstance().separateAndSend("102", player, this, false);
                    break;
                case 10:
                    player.addItem("Olympiad", 6651, Olympiad.getInstance().getNoblessePasses(player, true), player, true);
                    break;
            }
        } else if (command.startsWith("Olympiad")) {
            int classId, i;
            StringBuilder sb;
            String hiddenText;
            int val = Integer.parseInt(command.substring(9, 10));
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            switch (val) {
                case 2:
                    classId = Integer.parseInt(command.substring(11));
                    if (classId >= 88 && classId <= 118) {
                        List<String> names = Olympiad.getInstance().getClassLeaderBoard(classId);
                        html.setFile("data/html/olympiad/noble_ranking.htm");
                        int index = 1;
                        for (String name : names) {
                            html.replace("%place" + index + "%", index);
                            html.replace("%rank" + index + "%", name);
                            index++;
                            if (index > 10)
                                break;
                        }
                        for (; index <= 10; index++) {
                            html.replace("%place" + index + "%", "");
                            html.replace("%rank" + index + "%", "");
                        }
                        html.replace("%objectId%", getObjectId());
                        player.sendPacket(html);
                    }
                    break;
                case 3:
                    html.setFile("data/html/olympiad/olympiad_observe_list.htm");
                    i = 0;
                    sb = new StringBuilder(2000);
                    for (OlympiadGameTask task : OlympiadGameManager.getInstance().getOlympiadTasks()) {
                        StringUtil.append(sb, "<a action=\"bypass arenachange ", i, "\">Arena ", ++i, "&nbsp;");
                        if (task.isGameStarted()) {
                            if (task.isInTimerTime()) {
                                StringUtil.append(sb, "(&$907;)");
                            } else if (task.isBattleStarted()) {
                                StringUtil.append(sb, "(&$829;)");
                            } else {
                                StringUtil.append(sb, "(&$908;)");
                            }
                            StringUtil.append(sb, "&nbsp;", task.getGame().getPlayerNames()[0], "&nbsp; : &nbsp;", task.getGame().getPlayerNames()[1]);
                        } else {
                            StringUtil.append(sb, "(&$906;)</td><td>&nbsp;");
                        }
                        StringUtil.append(sb, "</a><br>");
                    }
                    html.replace("%list%", sb.toString());
                    html.replace("%objectId%", getObjectId());
                    player.sendPacket(html);
                    break;
                case 4:
                    player.sendPacket(new ExHeroList());
                    break;
                case 5:
                    if (HeroManager.getInstance().isInactiveHero(player.getObjectId())) {
                        html.setFile("data/html/olympiad/hero_confirm.htm");
                        html.replace("%objectId%", getObjectId());
                        player.sendPacket(html);
                    }
                    break;
                case 6:
                    if (HeroManager.getInstance().isInactiveHero(player.getObjectId())) {
                        if (player.isSubClassActive() || player.getLevel() < 76) {
                            player.sendMessage("You may only become an hero on a main class whose level is 75 or more.");
                            return;
                        }
                        HeroManager.getInstance().activateHero(player);
                    }
                    break;
                case 7:
                    html.setFile("data/html/olympiad/hero_main.htm");
                    hiddenText = "";
                    if (HeroManager.getInstance().isInactiveHero(player.getObjectId()))
                        hiddenText = "<a action=\"bypass -h npc_%objectId%_Olympiad 5\">\"I want to be a Hero.\"</a><br>";
                    html.replace("%hero%", hiddenText);
                    html.replace("%objectId%", getObjectId());
                    player.sendPacket(html);
                    break;
            }
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    public void onSpawn() {
        super.onSpawn();
        if (getNpcId() == 31688)
            _managers.add(this);
    }

    public void onDecay() {
        _managers.remove(this);
        super.onDecay();
    }
}
