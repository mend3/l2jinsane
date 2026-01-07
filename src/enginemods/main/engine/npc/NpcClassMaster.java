package enginemods.main.engine.npc;

import enginemods.main.engine.AbstractMods;
import enginemods.main.util.Util;
import enginemods.main.util.builders.html.Html;
import enginemods.main.util.builders.html.HtmlBuilder;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.data.xml.PlayerClassData;
import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.TutorialShowQuestionMark;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NpcClassMaster extends AbstractMods {
    private static final int NPC = 60007;
    private static final List<NpcClassMaster.ClassMasterList> ITEM_LIST = new ArrayList();
    private static final List<Location> SPAWNS = new ArrayList();
    private static final String HTML_PATH = "data/html/engine/npc/classmaster/";

    public NpcClassMaster() {
        ITEM_LIST.add(new ClassMasterList(this, 0, 0, 0, 0));
        ITEM_LIST.add(new ClassMasterList(this, 0, 0, 0, 0));
        ITEM_LIST.add(new ClassMasterList(this, 0, 0, 0, 0));
        ITEM_LIST.add(new ClassMasterList(this, 0, 0, 0, 0));
        SPAWNS.add(new Location(11283, 15951, -4584));
        SPAWNS.add(new Location(115774, -178666, -958));
        SPAWNS.add(new Location(45036, 48384, -3060));
        SPAWNS.add(new Location(-44747, -113865, -208));
        SPAWNS.add(new Location(-84466, 243171, -3729));
        this.registerMod(true);
    }

    private static void showHtmlMenu(Player player, int objectId, int level) {
        NpcHtmlMessage html = new NpcHtmlMessage(objectId);
        ClassId currentClassId = player.getClassId();
        if (currentClassId.level() >= level) {
            html.setFile("data/html/engine/npc/classmaster/nomore.htm");
        } else {
            int minLevel = getMinLevel(currentClassId.level());
            if (player.getLevel() >= minLevel) {
                StringBuilder menu = new StringBuilder(100);
                ClassId[] var7 = ClassId.values();
                int var8 = var7.length;

                for (int var9 = 0; var9 < var8; ++var9) {
                    ClassId cid = var7[var9];
                    if (validateClassId(currentClassId, cid) && cid.level() == level) {
                        menu.append("<button value=\"");
                        menu.append(PlayerClassData.getInstance().getClassNameById(cid.getId()) + "\" ");
                        menu.append("action=\"bypass -h Engine NpcClassMaster change_class ");
                        menu.append(cid.getId() + "\"");
                        menu.append(" width=204 height=20 back=\"sek.cbui77\" fore=\"sek.cbui75\"><br>");
                    }
                }

                if (menu.length() > 0) {
                    html.setFile("data/html/engine/npc/classmaster/template.htm");
                    html.replace("%name%", PlayerClassData.getInstance().getClassNameById(currentClassId.getId()));
                    html.replace("%menu%", menu.toString());
                } else {
                    html.setFile("data/html/engine/npc/classmaster/comebacklater.htm");
                    html.replace("%level%", String.valueOf(getMinLevel(level - 1)));
                }
            } else if (minLevel < Integer.MAX_VALUE) {
                html.setFile("data/html/engine/npc/classmaster/comebacklater.htm");
                html.replace("%level%", String.valueOf(minLevel));
            } else {
                html.setFile("data/html/engine/npc/classmaster/nomore.htm");
            }
        }

        html.replace("%objectId%", String.valueOf(objectId));
        html.replace("%req_items%", getRequiredItems(level));
        player.sendPacket(html);
    }

    private static void showQuestionMark(Player player) {
        ClassId classId = player.getClassId();
        if (getMinLevel(classId.level()) <= player.getLevel()) {
            player.sendPacket(new TutorialShowQuestionMark(1001));
        }
    }

    private static int getMinLevel(int level) {
        switch (level) {
            case 0:
                return 20;
            case 1:
                return 40;
            case 2:
                return 76;
            default:
                return Integer.MAX_VALUE;
        }
    }

    private static boolean validateClassId(ClassId oldCID, int val) {
        try {
            return validateClassId(oldCID, ClassId.values()[val]);
        } catch (Exception var3) {
            return false;
        }
    }

    private static boolean validateClassId(ClassId oldCID, ClassId newCID) {
        if (newCID != null && newCID.getRace() != null) {
            return oldCID.equals(newCID.getParent());
        } else {
            return false;
        }
    }

    private static String getRequiredItems(int level) {
        StringBuilder sb = new StringBuilder();
        if (ITEM_LIST.get(level).getPriceItemId() != 0 && ITEM_LIST.get(level).getPriceItemCount() != 0) {
            int count = ITEM_LIST.get(level).getPriceItemCount();
            String itemName = ItemTable.getInstance().getTemplate(ITEM_LIST.get(level).getPriceItemId()).getName();
            sb.append("<tr><td><img src=\"Icon.Item_System04\" width=32 height=32></td>");
            sb.append("<td><font color=\"LEVEL\">[" + count + "]</font></td>");
            sb.append("<td>[" + itemName + "]</td>");
            sb.append("<td><img src=\"Icon.Item_System05\" width=32 height=32></td></tr>");
        } else {
            sb.append("<tr><td>Free</td></tr>");
        }

        return sb.toString();
    }

    private static boolean checkAndChangeClass(Player player, int val) {
        ClassId currentClassId = player.getClassId();
        if (getMinLevel(currentClassId.level()) > player.getLevel()) {
            return false;
        } else if (!validateClassId(currentClassId, val)) {
            return false;
        } else {
            int newJobLevel = currentClassId.level() + 1;
            if (ITEM_LIST.get(newJobLevel).getRewardItemId() == 0 || player.getWeightPenalty() < 3 && (double) player.getInventoryLimit() * 0.8D > (double) player.getInventory().getSize()) {
                int priceCount = ITEM_LIST.get(newJobLevel).getPriceItemCount();
                int priceItemId = ITEM_LIST.get(newJobLevel).getPriceItemId();
                if (priceCount != 0 && priceItemId != 0 && !player.destroyItemByItemId("ClassMaster", priceItemId, priceCount, player, true)) {
                    player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
                    return false;
                } else {
                    int rewardCount = ITEM_LIST.get(newJobLevel).getRewardItemCount();
                    int rewardItemId = ITEM_LIST.get(newJobLevel).getRewardItemId();
                    player.addItem("ClassMaster", rewardItemId, rewardCount, player, true);
                    player.setClassId(val);
                    if (player.isSubClassActive()) {
                        player.getSubClasses().get(player.getClassIndex()).setClassId(player.getActiveClass());
                    } else {
                        player.setBaseClass(player.getActiveClass());
                    }

                    player.broadcastUserInfo();
                    if (player.getClassId().level() == 1 && player.getLevel() >= 40 || player.getClassId().level() == 2 && player.getLevel() >= 76) {
                        showQuestionMark(player);
                    }

                    return true;
                }
            } else {
                player.sendPacket(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT);
                return false;
            }
        }
    }

    public static NpcClassMaster getInstance() {
        return NpcClassMaster.SingletonHolder.INSTANCE;
    }

    public void onModState() {
        switch (this.getState()) {
            case START:
                Iterator var1 = SPAWNS.iterator();

                while (var1.hasNext()) {
                    Location loc = (Location) var1.next();
                    this.addSpawn(60007, loc, false, 0L);
                }
            case END:
            default:
        }
    }

    public boolean onInteract(Player player, Creature npc) {
        if (!Util.areObjectType(Npc.class, npc)) {
            return false;
        } else if (((Npc) npc).getNpcId() != 60007) {
            return false;
        } else {
            HtmlBuilder hb = new HtmlBuilder();
            hb.append("<html><body>");
            hb.append(Html.headHtml("CLASS MASTER"));
            hb.append("<br>");
            hb.append("Welcome my name is ", npc.getName(), " and take care to meet the most famous players in the world.<br>");
            hb.append("You probably want to know who it is!<br>");
            hb.append("I actually have a list, I can show it to you if you want.<br>");
            hb.append("What would you like to see?<br>");
            hb.append("<center>");
            hb.append("<table width=280>");
            hb.append("<tr>");
            hb.append("<td align=center>", Html.newImage("L2UI.bbs_folder", 32, 32), "</td>");
            hb.append("<td><button value=\"Complete the first class transfer\" action=\"bypass -h Engine NpcClassMaster 1stClass\" width=216 height=32 back=L2UI_CH3.refinegrade3_21 fore=L2UI_CH3.refinegrade3_21></td>");
            hb.append("<td align=center>", Html.newImage("L2UI.bbs_folder", 32, 32), "</td>");
            hb.append("</tr>");
            hb.append("<tr>");
            hb.append("<td align=center>", Html.newImage("L2UI.bbs_folder", 32, 32), "</td>");
            hb.append("<td><button value=\"Complete the second class transfer\" action=\"bypass -h Engine NpcClassMaster 2ndClass\" width=216 height=32 back=L2UI_CH3.refinegrade3_21 fore=L2UI_CH3.refinegrade3_21></td>");
            hb.append("<td align=center>", Html.newImage("L2UI.bbs_folder", 32, 32), "</td>");
            hb.append("</tr>");
            hb.append("<tr>");
            hb.append("<td align=center>", Html.newImage("L2UI.bbs_folder", 32, 32), "</td>");
            hb.append("<td><button value=\"Complete the third class transfer\" action=\"bypass -h Engine NpcClassMaster 3rdClass\" width=216 height=32 back=L2UI_CH3.refinegrade3_21 fore=L2UI_CH3.refinegrade3_21></td>");
            hb.append("<td align=center>", Html.newImage("L2UI.bbs_folder", 32, 32), "</td>");
            hb.append("</tr>");
            hb.append("</table>");
            hb.append("</center>");
            hb.append("</body></html>");
            sendHtml(player, (Npc) npc, hb);
            return true;
        }
    }

    public void onEvent(Player player, Creature npc, String command) {
        if (((Npc) npc).getNpcId() == 60007) {
            if (command.startsWith("1stClass")) {
                showHtmlMenu(player, npc.getObjectId(), 1);
            } else if (command.startsWith("2ndClass")) {
                showHtmlMenu(player, npc.getObjectId(), 2);
            } else if (command.startsWith("3rdClass")) {
                showHtmlMenu(player, npc.getObjectId(), 3);
            } else if (command.startsWith("change_class")) {
                int val = Integer.parseInt(command.substring(13));
                if (checkAndChangeClass(player, val)) {
                    player.sendPacket(new PlaySound("ItemSound.quest_fanfare_2"));
                    player.sendPacket(new PlaySound("Systemmsg_e.char_change"));
                    NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
                    html.setFile("data/html/engine/npc/classmaster/ok.htm");
                    html.replace("%name%", PlayerClassData.getInstance().getClassNameById(val));
                    player.sendPacket(html);
                }
            }

        }
    }

    private static class SingletonHolder {
        protected static final NpcClassMaster INSTANCE = new NpcClassMaster();
    }

    public static class ClassMasterList {
        private final int _priceItemId;
        private final int _priceItemCount;
        private final int _rewardItemId;
        private final int _rewardItemCount;

        public ClassMasterList(final NpcClassMaster param1, int priceItemId, int priceItemCount, int rewardItemId, int rewardItemCount) {
            this._priceItemId = priceItemId;
            this._priceItemCount = priceItemCount;
            this._rewardItemId = rewardItemId;
            this._rewardItemCount = rewardItemCount;
        }

        public int getPriceItemId() {
            return this._priceItemId;
        }

        public int getPriceItemCount() {
            return this._priceItemCount;
        }

        public int getRewardItemId() {
            return this._rewardItemId;
        }

        public int getRewardItemCount() {
            return this._rewardItemCount;
        }
    }
}