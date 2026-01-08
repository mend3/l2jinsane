package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.gameserver.data.DollsData;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.data.xml.PlayerClassData;
import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.network.FloodProtectors;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.*;

import java.util.List;

public final class ClassMaster extends Folk {
    public ClassMaster(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public static void onTutorialLink(Player player, String request) {
        if (Config.ALTERNATE_CLASS_MASTER && request != null && request.startsWith("CO")) {
            if (FloodProtectors.performAction(player.getClient(), FloodProtectors.Action.SERVER_BYPASS)) {
                try {
                    int val = Integer.parseInt(request.substring(2));
                    checkAndChangeClass(player, val);
                } catch (NumberFormatException ignored) {
                }

                player.sendPacket(TutorialCloseHtml.STATIC_PACKET);
            }
        }
    }

    public static void onTutorialQuestionMark(Player player, int number) {
        if (Config.ALTERNATE_CLASS_MASTER && number == 1001) {
            showTutorialHtml(player);
        }
    }

    public static void showQuestionMark(Player player) {
        if (Config.ALLOW_CLASS_MASTERS) {
            if (Config.ALTERNATE_CLASS_MASTER) {
                ClassId classId = player.getClassId();
                if (getMinLevel(classId.level()) <= player.getLevel()) {
                    if (Config.CLASS_MASTER_SETTINGS.isAllowed(classId.level() + 1)) {
                        player.sendPacket(new TutorialShowQuestionMark(1001));
                    }
                }
            }
        }
    }

    static void showHtmlMenu(Player player, int objectId, int level) {
        NpcHtmlMessage html = new NpcHtmlMessage(objectId);
        if (!Config.CLASS_MASTER_SETTINGS.isAllowed(level)) {
            StringBuilder sb = new StringBuilder(100);
            sb.append("<html><body>");
            switch (player.getClassId().level()) {
                case 0:
                    if (Config.CLASS_MASTER_SETTINGS.isAllowed(1)) {
                        sb.append("Come back here when you reached level 20 to change your class.<br>");
                    } else if (Config.CLASS_MASTER_SETTINGS.isAllowed(2)) {
                        sb.append("Come back after your first occupation change.<br>");
                    } else if (Config.CLASS_MASTER_SETTINGS.isAllowed(3)) {
                        sb.append("Come back after your second occupation change.<br>");
                    } else {
                        sb.append("I can't change your occupation.<br>");
                    }
                    break;
                case 1:
                    if (Config.CLASS_MASTER_SETTINGS.isAllowed(2)) {
                        sb.append("Come back here when you reached level 40 to change your class.<br>");
                    } else if (Config.CLASS_MASTER_SETTINGS.isAllowed(3)) {
                        sb.append("Come back after your second occupation change.<br>");
                    } else {
                        sb.append("I can't change your occupation.<br>");
                    }
                    break;
                case 2:
                    if (Config.CLASS_MASTER_SETTINGS.isAllowed(3)) {
                        sb.append("Come back here when you reached level 76 to change your class.<br>");
                    } else {
                        sb.append("I can't change your occupation.<br>");
                    }
                    break;
                case 3:
                    sb.append("There is no class change available for you anymore.<br>");
            }

            sb.append("</body></html>");
            html.setHtml(sb.toString());
        } else {
            ClassId currentClassId = player.getClassId();
            if (currentClassId.level() >= level) {
                html.setFile("data/html/classmaster/nomore.htm");
            } else {
                int minLevel = getMinLevel(currentClassId.level());
                if (player.getLevel() < minLevel && !Config.ALLOW_ENTIRE_TREE) {
                    if (minLevel < Integer.MAX_VALUE) {
                        html.setFile("data/html/classmaster/comebacklater.htm");
                        html.replace("%level%", minLevel);
                    } else {
                        html.setFile("data/html/classmaster/nomore.htm");
                    }
                } else {
                    StringBuilder menu = new StringBuilder(100);

                    for (ClassId cid : ClassId.VALUES) {
                        if (cid.level() == level && validateClassId(currentClassId, cid)) {
                            StringUtil.append(menu, "<a action=\"bypass -h npc_%objectId%_change_class ", cid.getId(), "\">", PlayerClassData.getInstance().getClassNameById(cid.getId()), "</a><br>");
                        }
                    }

                    if (!menu.isEmpty()) {
                        html.setFile("data/html/classmaster/template.htm");
                        html.replace("%name%", PlayerClassData.getInstance().getClassNameById(currentClassId.getId()));
                        html.replace("%menu%", menu.toString());
                    } else {
                        html.setFile("data/html/classmaster/comebacklater.htm");
                        html.replace("%level%", getMinLevel(level - 1));
                    }
                }
            }
        }

        html.replace("%objectId%", objectId);
        html.replace("%req_items%", getRequiredItems(level));
        player.sendPacket(html);
    }

    private static void showTutorialHtml(Player player) {
        ClassId currentClassId = player.getClassId();
        if (getMinLevel(currentClassId.level()) <= player.getLevel() || Config.ALLOW_ENTIRE_TREE) {
            String msg = HtmCache.getInstance().getHtm("data/html/classmaster/tutorialtemplate.htm");
            msg = msg.replaceAll("%name%", PlayerClassData.getInstance().getClassNameById(currentClassId.getId()));
            StringBuilder menu = new StringBuilder(100);

            for (ClassId cid : ClassId.values()) {
                if (validateClassId(currentClassId, cid)) {
                    StringUtil.append(menu, "<a action=\"link CO", String.valueOf(cid.getId()), "\">", PlayerClassData.getInstance().getClassNameById(cid.getId()), "</a><br>");
                }
            }

            msg = msg.replaceAll("%menu%", menu.toString());
            msg = msg.replace("%req_items%", getRequiredItems(currentClassId.level() + 1));
            player.sendPacket(new TutorialShowHtml(msg));
            player.mikadoPlayerUpdate();
        }
    }

    static boolean checkAndChangeClass(Player player, int val) {
        ClassId currentClassId = player.getClassId();
        if (getMinLevel(currentClassId.level()) > player.getLevel() && !Config.ALLOW_ENTIRE_TREE) {
            return false;
        } else if (!validateClassId(currentClassId, val)) {
            return false;
        } else {
            int newJobLevel = currentClassId.level() + 1;
            if (!Config.CLASS_MASTER_SETTINGS.getRewardItems(newJobLevel).isEmpty() && player.getWeightPenalty() > 2) {
                player.sendPacket(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT);
                return false;
            } else {
                List<IntIntHolder> neededItems = Config.CLASS_MASTER_SETTINGS.getRequiredItems(newJobLevel);

                for (IntIntHolder item : neededItems) {
                    if (player.getInventory().getInventoryItemCount(item.getId(), -1) < item.getValue()) {
                        player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
                        return false;
                    }
                }

                for (IntIntHolder item : neededItems) {
                    if (!player.destroyItemByItemId("ClassMaster", item.getId(), item.getValue(), player, true)) {
                        return false;
                    }
                }

                for (IntIntHolder item : Config.CLASS_MASTER_SETTINGS.getRewardItems(newJobLevel)) {
                    player.addItem("ClassMaster", item.getId(), item.getValue(), player, true);
                }

                player.setClassId(val);
                if (player.isSubClassActive()) {
                    (player.getSubClasses().get(player.getClassIndex())).setClassId(player.getActiveClass());
                } else {
                    player.setBaseClass(player.getActiveClass());
                }

                if (Config.REWARD_NOBLE_IN_3RCHANGE_JOB && newJobLevel == 3 && !player.isSubClassActive()) {
                    player.setNoble(true, true);
                }

                DollsData.refreshAllDollSkills(player);
                player.sendPacket(new HennaInfo(player));
                player.broadcastUserInfo();
                if (Config.ALTERNATE_CLASS_MASTER && (player.getClassId().level() == 1 && player.getLevel() >= 40 || player.getClassId().level() == 2 && player.getLevel() >= 76)) {
                    showQuestionMark(player);
                }

                return true;
            }
        }
    }

    private static int getMinLevel(int level) {
        switch (level) {
            case 0 -> {
                return 20;
            }
            case 1 -> {
                return 40;
            }
            case 2 -> {
                return 76;
            }
            default -> {
                return Integer.MAX_VALUE;
            }
        }
    }

    private static boolean validateClassId(ClassId oldCID, int val) {
        try {
            return validateClassId(oldCID, ClassId.VALUES[val]);
        } catch (Exception var3) {
            return false;
        }
    }

    private static boolean validateClassId(ClassId oldCID, ClassId newCID) {
        if (newCID == null) {
            return false;
        } else if (oldCID == newCID.getParent()) {
            return true;
        } else {
            return Config.ALLOW_ENTIRE_TREE && newCID.childOf(oldCID);
        }
    }

    private static String getRequiredItems(int level) {
        List<IntIntHolder> neededItems = Config.CLASS_MASTER_SETTINGS.getRequiredItems(level);
        if (neededItems != null && !neededItems.isEmpty()) {
            StringBuilder sb = new StringBuilder();

            for (IntIntHolder item : neededItems) {
                StringUtil.append(sb, "<tr><td><font color=\"LEVEL\">", item.getValue(), "</font></td><td>", ItemTable.getInstance().getTemplate(item.getId()).getName(), "</td></tr>");
            }

            return sb.toString();
        } else {
            return "<tr><td>none</td></r>";
        }
    }

    public void showChatWindow(Player player) {
        player.sendPacket(ActionFailed.STATIC_PACKET);
        String filename = "data/html/classmaster/disabled.htm";
        if (Config.ALLOW_CLASS_MASTERS) {
            filename = "data/html/classmaster/" + this.getNpcId() + ".htm";
        }

        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
        html.setFile(filename);
        html.replace("%objectId%", this.getObjectId());
        player.sendPacket(html);
    }

    public void onBypassFeedback(Player player, String command) {
        if (Config.ALLOW_CLASS_MASTERS) {
            if (command.startsWith("1stClass")) {
                showHtmlMenu(player, this.getObjectId(), 1);
            } else if (command.startsWith("2ndClass")) {
                showHtmlMenu(player, this.getObjectId(), 2);
            } else if (command.startsWith("3rdClass")) {
                showHtmlMenu(player, this.getObjectId(), 3);
            } else if (command.startsWith("change_class")) {
                int val = Integer.parseInt(command.substring(13));
                if (checkAndChangeClass(player, val)) {
                    NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                    html.setFile("data/html/classmaster/ok.htm");
                    html.replace("%name%", PlayerClassData.getInstance().getClassNameById(val));
                    player.sendPacket(html);
                    DollsData.refreshAllDollSkills(player);
                }
            } else if (command.startsWith("become_noble")) {
                NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                if (!player.isNoble()) {
                    player.setNoble(true, true);
                    player.sendPacket(new UserInfo(player));
                    html.setFile("data/html/classmaster/nobleok.htm");
                    player.sendPacket(html);
                } else {
                    html.setFile("data/html/classmaster/alreadynoble.htm");
                    player.sendPacket(html);
                }
            } else if (command.startsWith("learn_skills")) {
                player.rewardSkills();
            } else {
                super.onBypassFeedback(player, command);
            }

        }
    }
}
