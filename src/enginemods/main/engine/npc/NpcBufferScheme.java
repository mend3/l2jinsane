package enginemods.main.engine.npc;

import enginemods.main.data.SchemeBuffData;
import enginemods.main.data.SkillData;
import enginemods.main.engine.AbstractMods;
import enginemods.main.enums.BuffType;
import enginemods.main.holders.BuffHolder;
import enginemods.main.util.Util;
import enginemods.main.util.UtilInventory;
import enginemods.main.util.builders.html.Html;
import enginemods.main.util.builders.html.HtmlBuilder;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.enums.GaugeColor;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.actor.instance.Cubic;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.model.actor.stat.PlayerStat;
import net.sf.l2j.gameserver.model.actor.stat.SummonStat;
import net.sf.l2j.gameserver.model.actor.status.PlayerStatus;
import net.sf.l2j.gameserver.model.actor.status.SummonStatus;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SetSummonRemainTime;
import net.sf.l2j.gameserver.network.serverpackets.SetupGauge;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class NpcBufferScheme extends AbstractMods {
    private static final int NPC_ID = 60012;
    private static final String TITLE_NAME = "Buffer";
    private static final boolean FREE_BUFFS = true;
    private static final int BUFF_PRICE = 1000;
    private static final int BUFF_SET_PRICE = 1;
    private static final int CONSUMABLE_ID = 57;
    private static final boolean TIME_OUT = true;
    private static final int TIME_OUT_TIME = 1;
    private static final int MIN_LEVEL = 0;
    private static final int BUFF_REMOVE_PRICE = 1000;
    private static final int SCHEME_BUFF_PRICE = 1000;
    private static final int SCHEMES_PER_PLAYER = 3;
    private static final int MAX_SCHEME_BUFFS = 24;
    private static final int MAX_SCHEME_DANCES = 12;

    public NpcBufferScheme() {
        this.registerMod(true);
    }

    private static String getSkillIconHtml(int id, int level) {
        String iconNumber = SkillData.getSkillIcon(id);
        return "<button action=\"bypass -h Engine NpcBufferScheme description " + id + " " + level + " x\" width=32 height=32 back=\"Icon.skill" + iconNumber + "\" fore=\"Icon.skill" + iconNumber + "\">";
    }

    private static String getItemNameHtml(int itemId) {
        return "&#" + itemId + ";";
    }

    private static void heal(Player player, boolean isPet) {
        Summon target = player.getSummon();
        if (!isPet) {
            PlayerStatus pcStatus = player.getStatus();
            PlayerStat pcStat = player.getStat();
            pcStatus.setCurrentHp(pcStat.getMaxHp());
            pcStatus.setCurrentMp(pcStat.getMaxMp());
            pcStatus.setCurrentCp(pcStat.getMaxCp());
        } else if (target != null) {
            SummonStatus petStatus = target.getStatus();
            SummonStat petStat = target.getStat();
            petStatus.setCurrentHp(petStat.getMaxHp());
            petStatus.setCurrentMp(petStat.getMaxMp());
            if (!(target instanceof Pet pet)) {
                throw new RuntimeException();
            }

            pet.setCurrentFed(pet.getPetData().getMaxMeal());
            player.sendPacket(new SetSummonRemainTime(pet.getPetData().getMaxMeal(), pet.getCurrentFed()));
        }

    }

    private static boolean isEnabled(int id, int level) {
        for (BuffHolder bh : SchemeBuffData.getAllGeneralBuffs()) {
            if (bh.getId() == id && bh.getLevel() == level) {
                return true;
            }
        }

        return false;
    }

    private static void showText(Player player, String type, String text, boolean buttonEnabled, String buttonName, String location) {
        HtmlBuilder hb = new HtmlBuilder(HtmlBuilder.HtmlType.HTML_TYPE);
        hb.append("<html><head><title>", "Buffer", "</title></head><body>");
        hb.append(Html.headHtml("BUFFER"));
        hb.append("<center>");
        hb.append("<br>");
        hb.append("<font color=LEVEL>", type, "</font>");
        hb.append("<br>", text, "<br>");
        if (buttonEnabled) {
            hb.append("<button value=\"" + buttonName + "\" action=\"bypass -h Engine NpcBufferScheme redirect_", location, " 0 0\" width=75 height=21  back=", "L2UI_CH3.Btn1_normalOn", " fore=", "L2UI_CH3.Btn1_normal", ">");
        }

        hb.append("<font color=303030>", "Buffer", "</font></center></body></html>");
        sendHtml(player, null, hb);
    }

    private static void createScheme(Player player) {
        HtmlBuilder hb = new HtmlBuilder(HtmlBuilder.HtmlType.HTML_TYPE);
        hb.append("<html><head><title>", "Buffer", "</title></head><body>");
        hb.append(Html.headHtml("BUFFER"));
        hb.append("<center>");
        hb.append("<br><br>");
        hb.append("You MUST seprerate new words with a dot (.)");
        hb.append("<br><br>");
        hb.append("Scheme name: <edit var=\"name\" width=100>");
        hb.append("<br><br>");
        hb.append("<button value=\"Create Scheme\" action=\"bypass -h Engine NpcBufferScheme create $name no_name\" width=75 height=21 back=", "L2UI_CH3.Btn1_normalOn", " fore=", "L2UI_CH3.Btn1_normal", ">");
        hb.append("<br>");
        hb.append("<button value=\"Back\" action=\"bypass -h Engine NpcBufferScheme redirect_main\" width=75 height=21 back=", "L2UI_CH3.Btn1_normalOn", " fore=", "L2UI_CH3.Btn1_normal", ">");
        hb.append("<br>");
        hb.append("<font color=303030>", "Buffer", "</font>");
        hb.append("</center>");
        hb.append("</body></html>");
        sendHtml(player, null, hb);
    }

    private static void buildHtml(Player player, BuffType buffType, int page) {
        HtmlBuilder hb = new HtmlBuilder(HtmlBuilder.HtmlType.HTML_TYPE);
        hb.append("<html><head><title>", "Buffer", "</title></head><body>");
        hb.append("<center><br>");
        List<BuffHolder> buffs = new ArrayList<>();

        for (BuffHolder bh : SchemeBuffData.getAllGeneralBuffs()) {
            if (bh.getSkill() == null) {
                System.out.println("buffId: " + bh.getId());
            } else if (bh.getType() == buffType) {
                buffs.add(bh);
            }
        }

        if (buffs.isEmpty()) {
            hb.append("No buffs are available at this moment!");
        } else {
            hb.append("All buffs are for <font color=LEVEL>free</font>!");
            hb.append("<br1>");
            int MAX_PER_PAGE = 12;
            int searchPage = MAX_PER_PAGE * (page - 1);
            int count = 0;
            hb.append(Html.newImage("L2UI.SquareWhite", 264, 1));

            for (BuffHolder bh : buffs) {
                if (count < searchPage) {
                    ++count;
                } else if (count < searchPage + MAX_PER_PAGE) {
                    hb.append("<table width=264", count % 2 == 0 ? " bgcolor=000000>" : ">");
                    String name = bh.getSkill().getName().replace("+", " ");
                    hb.append("<tr>");
                    hb.append("<td height=32 fixwidth=32>", getSkillIconHtml(bh.getId(), bh.getLevel()), "</td>");
                    hb.append("<td height=32 fixwidth=232 align=center><a action=\"bypass -h Engine NpcBufferScheme giveBuffs ", bh.getId(), " ", bh.getLevel(), " ", buffType.name(), " ", page, "\">", name, "</a></td>");
                    hb.append("<td height=32 fixwidth=32>", getSkillIconHtml(bh.getId(), bh.getLevel()), "</td>");
                    hb.append("</tr>");
                    hb.append("</table>");
                    hb.append(Html.newImage("L2UI.SquareWhite", 264, 1));
                    ++count;
                }
            }

            hb.append("<center>");
            hb.append(Html.newImage("L2UI.SquareWhite", 264, 1));
            hb.append("<table bgcolor=000000>");
            hb.append("<tr>");
            int currentPage = 1;

            for (int i = 0; i < buffs.size(); ++i) {
                if (i % MAX_PER_PAGE == 0) {
                    hb.append("<td width=20 align=center><a action=\"bypass -h Engine NpcBufferScheme redirect_view_", buffType.name().toLowerCase(), " ", currentPage, "\">", currentPage, "</a></td>");
                    ++currentPage;
                }
            }

            hb.append("</tr>");
            hb.append("</table>");
            hb.append(Html.newImage("L2UI.SquareWhite", 264, 1));
            hb.append("</center>");
        }

        hb.append("<br>");
        hb.append("<button value=\"Back\" action=\"bypass -h Engine NpcBufferScheme redirect_main\" width=75 height=21 back=", "L2UI_CH3.Btn1_normalOn", " fore=", "L2UI_CH3.Btn1_normal", ">");
        hb.append("<br>");
        hb.append("<font color=303030>", "Buffer", "</font>");
        hb.append("</center>");
        hb.append("</body></html>");
        sendHtml(player, null, hb);
    }

    public static void getInstance() {
    }

    public void onModState() {
    }

    public void onEvent(Player player, Creature npc, String command) {
        if (((Npc) npc).getNpcId() == 60012) {
            StringTokenizer st = new StringTokenizer(command, " ");
            String bypass = st.hasMoreTokens() ? st.nextToken() : "redirect_main";
            String eventParam1 = st.hasMoreTokens() ? st.nextToken() : "";
            String eventParam2 = st.hasMoreTokens() ? st.nextToken() : "";
            String eventParam3 = st.hasMoreTokens() ? st.nextToken() : "";
            String eventParam4 = st.hasMoreTokens() ? st.nextToken() : "";
            int page = eventParam1.isEmpty() ? 1 : Integer.parseInt(eventParam1);
            switch (bypass) {
                case "reloadscript":
                    if (eventParam1.equals("0")) {
                        this.rebuildMainHtml(player);
                        return;
                    }
                case "redirect_main":
                    this.rebuildMainHtml(player);
                    return;
                case "redirect_view_buff":
                    buildHtml(player, BuffType.BUFF, page);
                    return;
                case "redirect_view_resist":
                    buildHtml(player, BuffType.RESIST, page);
                    return;
                case "redirect_view_song":
                    buildHtml(player, BuffType.SONG, page);
                    return;
                case "redirect_view_dances":
                    buildHtml(player, BuffType.DANCE, page);
                    return;
                case "redirect_view_chants":
                    buildHtml(player, BuffType.CHANT, page);
                    return;
                case "redirect_view_other":
                    buildHtml(player, BuffType.OTHER, page);
                    return;
                case "redirect_view_special":
                    buildHtml(player, BuffType.SPECIAL, page);
                    return;
                case "redirect_view_cubic":
                    buildHtml(player, BuffType.CUBIC, page);
                    return;
                case "buffpet":
                    if (this.checkTimeOut(player)) {
                        this.setValueDB(player.getObjectId(), "Pet-On-Off", eventParam1);
                        this.addTimeout(player, GaugeColor.GREEN, 0, 600);
                    }

                    this.rebuildMainHtml(player);
                    return;
                case "create":
                    String name = eventParam1.replaceAll("[ !\"#$%&'()*+,/:;<=>?@\\[\\\\\\]\\^`{|}~]", "");
                    if (!name.isEmpty() && !name.equals("no_name")) {
                        String allSchemes = this.getValueDB(player.getObjectId(), "schemeName");
                        if (allSchemes == null) {
                            allSchemes = "";
                        } else {
                            for (String s : allSchemes.split(",")) {
                                if (s != null && s.equals(name)) {
                                    player.sendPacket(SystemMessageId.INCORRECT_NAME_TRY_AGAIN);
                                    showText(player, "Info", "The name you are trying to use is already in use!", true, "Return", "main");
                                    return;
                                }
                            }
                        }

                        allSchemes = allSchemes + name + ",";
                        this.setValueDB(player.getObjectId(), "schemeName", allSchemes);
                        this.rebuildMainHtml(player);
                    } else {
                        player.sendPacket(SystemMessageId.INCORRECT_NAME_TRY_AGAIN);
                        showText(player, "Info", "Please, enter the scheme name!", true, "Return", "main");
                    }
                    return;
                case "delete":
                    this.removeValueDB(player.getObjectId(), eventParam1);
                    String schemes = this.getValueDB(player.getObjectId(), "schemeName");
                    if (schemes != null) {
                        if (schemes.contains(eventParam1 + ",")) {
                            schemes = schemes.replace(eventParam1 + ",", "");
                        } else {
                            schemes = schemes.replace(eventParam1, "");
                        }

                        this.setValueDB(player.getObjectId(), "schemeName", schemes);
                    }

                    this.rebuildMainHtml(player);
                    return;
                case "delete_c":
                    String var10002 = Html.headHtml("BUFFER");
                    sendHtml(player, null, "<html><title>Buffer</title><body><center>" + var10002 + "<br>Do you really want to delete '" + eventParam1 + "' scheme?<br><br><button value=\"Yes\" action=\"bypass -h Engine NpcBufferScheme delete " + eventParam1 + "\" width=75 height=21 back=L2UI_CH3.Btn1_normalOn fore=L2UI_CH3.Btn1_normal><button value=\"No\" action=\"bypass -h Engine NpcBufferScheme delete_1\" width=75 height=21 back=L2UI_CH3.Btn1_normalOn fore=L2UI_CH3.Btn1_normal><br><font color=303030>Buffer</font></center></body></html>");
                    return;
                case "create_1":
                    createScheme(player);
                    return;
                case "edit_1":
                    this.editScheme(player);
                    return;
                case "delete_1":
                    this.deleteScheme(player);
                    return;
                case "manage_scheme_add":
                    this.viewAllSchemeBuffs(player, eventParam1, eventParam2, "add");
                    return;
                case "manage_scheme_remove":
                    this.viewAllSchemeBuffs(player, eventParam1, eventParam2, "remove");
                    return;
                case "manage_scheme_select":
                    this.getOptionList(player, eventParam1);
                    return;
                case "remove_buff":
                    String[] split = eventParam1.split("_");
                    String schemeNameRemove = split[0];
                    String id = split[1];
                    String level = split[2];
                    String listBuff = this.getValueDB(player.getObjectId(), schemeNameRemove);
                    listBuff = listBuff.replaceFirst(id + "," + level + ";", "");
                    this.setValueDB(player.getObjectId(), schemeNameRemove, listBuff);
                    int temp = Integer.parseInt(eventParam3) - 1;
                    if (temp <= 0) {
                        this.getOptionList(player, schemeNameRemove);
                    } else {
                        this.viewAllSchemeBuffs(player, schemeNameRemove, eventParam2, "remove");
                    }

                    return;
                case "add_buff":
                    split = eventParam1.split("_");
                    String schemeNameAdd = split[0];
                    id = split[1];
                    level = split[2];
                    listBuff = this.getValueDB(player.getObjectId(), schemeNameAdd);
                    if (listBuff == null) {
                        listBuff = id + "," + level + ";";
                    } else {
                        listBuff = listBuff.concat(id + "," + level + ";");
                    }

                    this.setValueDB(player.getObjectId(), schemeNameAdd, listBuff);
                    temp = Integer.parseInt(eventParam3) + 1;
                    if (temp >= 36) {
                        this.getOptionList(player, schemeNameAdd);
                    } else {
                        this.viewAllSchemeBuffs(player, schemeNameAdd, eventParam2, "add");
                    }

                    return;
                case "heal":
                    if (this.checkTimeOut(player)) {
                        if (UtilInventory.getItemsCount(player, 57) < 1000) {
                            showText(player, "Sorry", "You don't have the enough items:<br>You need: <font color=LEVEL>1000 " + getItemNameHtml(57) + "!", false, "0", "0");
                            return;
                        }

                        boolean getPetbuff = this.isPetBuff(player);
                        if (getPetbuff) {
                            if (player.getSummon() == null) {
                                showText(player, "Info", "You can't use the Pet's options.<br>Summon your pet first!", false, "Return", "main");
                                return;
                            }

                            heal(player, getPetbuff);
                        } else {
                            heal(player, getPetbuff);
                        }

                        UtilInventory.takeItems(player, 57, 1000);
                        this.addTimeout(player, GaugeColor.BLUE, 0, 600);
                    }

                    this.rebuildMainHtml(player);
                    return;
                case "removeBuffs":
                    if (this.checkTimeOut(player)) {
                        if (UtilInventory.getItemsCount(player, 57) < 1000) {
                            showText(player, "Sorry", "You don't have the enough items:<br>You need: <font color=LEVEL>1000 " + getItemNameHtml(57) + "!", false, "0", "0");
                            return;
                        }

                        boolean getPetbuff = this.isPetBuff(player);
                        if (getPetbuff) {
                            if (player.getSummon() == null) {
                                showText(player, "Info", "You can't use the Pet's options.<br>Summon your pet first!", false, "Return", "main");
                                return;
                            }

                            player.getSummon().stopAllEffects();
                        } else {
                            player.stopAllEffects();
                            if (player.getCubics() != null) {
                                for (Cubic cubic : player.getCubics().values()) {
                                    cubic.stopAction();
                                    player.delCubic(cubic.getId());
                                }
                            }
                        }

                        UtilInventory.takeItems(player, 57, 1000);
                        this.addTimeout(player, GaugeColor.RED, 0, 600);
                    }

                    this.rebuildMainHtml(player);
                    return;
                case "cast":
                    if (this.checkTimeOut(player)) {
                        List<BuffHolder> buffs = new ArrayList<>();
                        String buffList = this.getValueDB(player.getObjectId(), eventParam1);
                        if (buffList != null) {
                            for (String buff : buffList.split(";")) {
                                int hId = Integer.parseInt(buff.split(",")[0]);
                                int hLvl = Integer.parseInt(buff.split(",")[1]);
                                if (isEnabled(hId, hLvl)) {
                                    buffs.add(new BuffHolder(hId, hLvl));
                                }
                            }
                        }

                        if (buffs.isEmpty()) {
                            this.viewAllSchemeBuffs(player, eventParam1, "1", "add");
                            return;
                        }

                        boolean getPetbuff = this.isPetBuff(player);

                        for (BuffHolder bh : buffs) {
                            if (!getPetbuff) {
                                SkillTable.getInstance().getInfo(bh.getId(), bh.getLevel()).getEffects(player, player);
                            } else {
                                if (player.getSummon() == null) {
                                    showText(player, "Info", "You can't use the Pet's options.<br>Summon your pet first!", false, "Return", "main");
                                    return;
                                }

                                SkillTable.getInstance().getInfo(bh.getId(), bh.getLevel()).getEffects(player.getSummon(), player.getSummon());
                            }
                        }

                        UtilInventory.takeItems(player, 57, 1000);
                        this.addTimeout(player, GaugeColor.CYAN, 1, 600);
                    }

                    this.rebuildMainHtml(player);
                    return;
                case "giveBuffs":
                    int cost = 1000;
                    int bId = Integer.parseInt(eventParam1);
                    int bLvl = Integer.parseInt(eventParam2);
                    if (!isEnabled(bId, bLvl)) {
                        System.out.println("posible bypass en scheme buff -> " + player.getName());
                        return;
                    } else {
                        if (this.checkTimeOut(player)) {
                            L2Skill skill = SkillTable.getInstance().getInfo(bId, bLvl);
                            if (skill.getSkillType() == L2SkillType.SUMMON && UtilInventory.getItemsCount(player, skill.getItemConsumeId()) < skill.getItemConsume()) {
                                showText(player, "Sorry", "You don't have the enough items:<br>You need: <font color=LEVEL>" + skill.getItemConsume() + " " + getItemNameHtml(skill.getItemConsumeId()) + "!", false, "0", "0");
                                return;
                            }

                            boolean getPetbuff = this.isPetBuff(player);
                            if (!getPetbuff) {
                                if (eventParam3.equals("CUBIC") && !player.getCubics().isEmpty()) {
                                    for (Cubic cubic : player.getCubics().values()) {
                                        cubic.stopAction();
                                        player.delCubic(cubic.getId());
                                    }
                                }
                            } else if (eventParam3.equals("CUBIC")) {
                                if (!player.getCubics().isEmpty()) {
                                    for (Cubic cubic : player.getCubics().values()) {
                                        cubic.stopAction();
                                        player.delCubic(cubic.getId());
                                    }
                                }
                            } else if (player.getSummon() == null) {
                                showText(player, "Info", "You can't use the Pet's options.<br>Summon your pet first!", false, "Return", "main");
                                return;
                            }

                            skill.getEffects(player, player);
                            UtilInventory.takeItems(player, 57, 1000);
                            this.addTimeout(player, GaugeColor.CYAN, 0, 600);
                        }

                        buildHtml(player, BuffType.valueOf(eventParam3), eventParam4.isEmpty() ? 1 : Integer.parseInt(eventParam4));
                        return;
                    }
                case "castBuffSet":
                    if (this.checkTimeOut(player)) {
                        boolean getPetbuff = this.isPetBuff(player);
                        if (!getPetbuff) {
                            for (BuffHolder bh : player.isMageClass() ? SchemeBuffData.getAllMageBuffs() : SchemeBuffData.getAllWarriorBuffs()) {
                                SkillTable.getInstance().getInfo(bh.getId(), bh.getLevel()).getEffects(player, player);
                            }
                        } else {
                            if (player.getSummon() == null) {
                                showText(player, "Info", "You can't use the Pet's options.<br>Summon your pet first!", false, "Return", "main");
                                return;
                            }

                            for (BuffHolder bh : SchemeBuffData.getAllWarriorBuffs()) {
                                SkillTable.getInstance().getInfo(bh.getId(), bh.getLevel()).getEffects(player.getSummon(), player.getSummon());
                            }
                        }

                        UtilInventory.takeItems(player, 57, 1);
                        this.addTimeout(player, GaugeColor.CYAN, 1, 600);
                    }

                    this.rebuildMainHtml(player);
                    return;
                default:
                    this.rebuildMainHtml(player);
            }
        }
    }

    public boolean onInteract(Player player, Creature npc) {
        if (!Util.areObjectType(Npc.class, npc)) {
            return false;
        } else if (((Npc) npc).getNpcId() != 60012) {
            return false;
        } else if (player.isGM()) {
            this.rebuildMainHtml(player);
            return true;
        } else {
            if (this.checkTimeOut(player)) {
                if (player.getLevel() < 0) {
                    showText(player, "Info", "Your level is too low!<br>You have to be at least level <font color=LEVEL>0</font>,<br>to use my services!", false, "Return", "main");
                    return true;
                }

                if (player.isInCombat()) {
                    showText(player, "Info", "You can't buff while you are attacking!<br>Stop your fight and try again!", false, "Return", "main");
                    return true;
                }
            }

            this.rebuildMainHtml(player);
            return true;
        }
    }

    private boolean checkTimeOut(Player player) {
        String blockUntilTime = this.getValueDB(player.getObjectId(), "blockUntilTime");
        return blockUntilTime == null || (int) (System.currentTimeMillis() / 1000L) > Integer.parseInt(blockUntilTime);
    }

    private void addTimeout(Player player, GaugeColor gaugeColor, int amount, int offset) {
        int endtime = (int) ((System.currentTimeMillis() + (amount * 1000L)) / 1000L);
        this.setValueDB(player.getObjectId(), "blockUntilTime", String.valueOf(endtime));
        player.sendPacket(new SetupGauge(gaugeColor, amount * 1000 + offset));
    }

    private void rebuildMainHtml(Player player) {
        HtmlBuilder hb = new HtmlBuilder(HtmlBuilder.HtmlType.HTML_TYPE);
        hb.append("<html><body>");
        hb.append(Html.headHtml("BUFFER"));
        hb.append("<br>");
        hb.append("<center>");
        hb.append(Html.newImage("L2UI.SquareWhite", 264, 1));
        hb.append("<table width=275 border=0 cellspacing=0 cellpadding=1 bgcolor=000000>");
        hb.append("<tr>");
        hb.append("<td align=center><font color=FFFF00>Buffs:</font></td>");
        hb.append("</tr>");
        hb.append("</table>");
        hb.append(Html.newImage("L2UI.SquareWhite", 264, 1));
        hb.append("<br>");
        String pet = this.getValueDB(player.getObjectId(), "Pet-On-Off");
        String bottonA;
        String bottonB;
        String bottonC;
        if (pet != null && !pet.equals("1")) {
            bottonA = "Auto Buff";
            bottonB = "Heal";
            bottonC = "Remove Buffs";
            hb.append("<button value=\"Char Options\" action=\"bypass -h Engine NpcBufferScheme buffpet 1\" width=75 height=21 back=", "L2UI_CH3.Btn1_normalOn", " fore=", "L2UI_CH3.Btn1_normal", ">");
        } else {
            bottonA = "Auto Buff Pet";
            bottonB = "Heal My Pet";
            bottonC = "Remove Buffs";
            hb.append("<button value=\"Pet Options\" action=\"bypass -h Engine NpcBufferScheme buffpet 0\" width=75 height=21  back=", "L2UI_CH3.Btn1_normalOn", " fore=", "L2UI_CH3.Btn1_normal", ">");
        }

        hb.append("<table width=80% cellspacing=0 cellpadding=1>");
        hb.append("<tr>");
        hb.append("<td height=32 align=center><button value=Buffs action=\"bypass -h Engine NpcBufferScheme redirect_view_buff\" width=75 height=21back=", "L2UI_CH3.Btn1_normalOn", " fore=", "L2UI_CH3.Btn1_normal", "></td>");
        hb.append("<td height=32 align=center><button value=Resist action=\"bypass -h Engine NpcBufferScheme redirect_view_resist\" width=75 height=21back=", "L2UI_CH3.Btn1_normalOn", " fore=", "L2UI_CH3.Btn1_normal", "></td>");
        hb.append("</tr>");
        hb.append("<tr>");
        hb.append("<td height=32 align=center><button value=Songs action=\"bypass -h Engine NpcBufferScheme redirect_view_song\" width=75 height=21back=", "L2UI_CH3.Btn1_normalOn", " fore=", "L2UI_CH3.Btn1_normal", "></td>");
        hb.append("<td height=32 align=center><button value=Dances action=\"bypass -h Engine NpcBufferScheme redirect_view_dances\" width=75 height=21back=", "L2UI_CH3.Btn1_normalOn", " fore=", "L2UI_CH3.Btn1_normal", "></td>");
        hb.append("</tr>");
        hb.append("<tr>");
        hb.append("<td height=32 align=center><button value=Chants action=\"bypass -h Engine NpcBufferScheme redirect_view_chants\" width=75 height=21back=", "L2UI_CH3.Btn1_normalOn", " fore=", "L2UI_CH3.Btn1_normal", "></td>");
        hb.append("<td height=32 align=center><button value=Special action=\"bypass -h Engine NpcBufferScheme redirect_view_special\" width=75 height=21back=", "L2UI_CH3.Btn1_normalOn", " fore=", "L2UI_CH3.Btn1_normal", "></td>");
        hb.append("</tr>");
        hb.append("<tr>");
        hb.append("<td height=32 align=center><button value=Others action=\"bypass -h Engine NpcBufferScheme redirect_view_other\" width=75 height=21back=", "L2UI_CH3.Btn1_normalOn", " fore=", "L2UI_CH3.Btn1_normal", "></td>");
        hb.append("<td height=32 align=center><button value=Cubics action=\"bypass -h Engine NpcBufferScheme redirect_view_cubic\" width=75 height=21 back=", "L2UI_CH3.Btn1_normalOn", " fore=", "L2UI_CH3.Btn1_normal", "></td>");
        hb.append("</tr>");
        hb.append("</table>");
        hb.append(Html.newImage("L2UI.SquareWhite", 264, 1));
        hb.append("<table width=275 border=0 cellspacing=0 cellpadding=1 bgcolor=000000>");
        hb.append("<tr>");
        hb.append("<td align=center><font color=FFFF00>Preset:</font></td>");
        hb.append("</tr>");
        hb.append("</table>");
        hb.append(Html.newImage("L2UI.SquareWhite", 264, 1));
        hb.append("<table width=100% height=37 border=0 cellspacing=0 cellpadding=5>");
        hb.append("<tr>");
        hb.append("<td><button value=\"", bottonA, "\" action=\"bypass -h Engine NpcBufferScheme castBuffSet 0 0 0\" width=75 height=21 back=", "L2UI_CH3.Btn1_normalOn", " fore=", "L2UI_CH3.Btn1_normal", "></td>");
        hb.append("<td><button value=\"", bottonB, "\" action=\"bypass -h Engine NpcBufferScheme heal\" width=75 height=21 back=", "L2UI_CH3.Btn1_normalOn", " fore=", "L2UI_CH3.Btn1_normal", "></td>");
        hb.append("<td><button value=\"", bottonC, "\" action=\"bypass -h Engine NpcBufferScheme removeBuffs 0 0 0\" width=75 height=21 back=", "L2UI_CH3.Btn1_normalOn", " fore=", "L2UI_CH3.Btn1_normal", "></td>");
        hb.append("</tr>");
        hb.append("</table>");
        hb.append("");
        hb.append(this.generateScheme(player));
        hb.append("<br>");
        hb.append("<font color=303030>Buffer</font>");
        hb.append("</center>");
        hb.append("</body></html>");
        sendHtml(player, null, hb);
    }

    private String generateScheme(Player player) {
        String schemeNames = this.getValueDB(player.getObjectId(), "schemeName");
        HtmlBuilder hb = new HtmlBuilder(HtmlBuilder.HtmlType.HTML_TYPE);
        hb.append("<br1>");
        hb.append(Html.newImage("L2UI.SquareWhite", 264, 1));
        hb.append("<table width=271 border=0 cellspacing=0 cellpadding=1 bgcolor=000000>");
        hb.append("<tr>");
        hb.append("<td align=center><font color=FFFF00>Scheme:</font></td>");
        hb.append("<td align=right><font color=LEVEL></font></td>");
        hb.append("</tr>");
        hb.append("</table>");
        hb.append(Html.newImage("L2UI.SquareWhite", 264, 1));
        hb.append("<br1>");
        hb.append("<table cellspacing=0 cellpadding=5 height=28>");
        if (schemeNames != null) {
            String[] TRS = new String[]{"<tr><td>", "</td>", "<td>", "</td></tr>"};
            hb.append("<table>");
            int td = 0;

            for (int i = 0; i < schemeNames.split(",").length; ++i) {
                if (td > 2) {
                    td = 0;
                }

                hb.append(TRS[td] + "<button value=\"", schemeNames.split(",")[i], "\" action=\"bypass -h Engine NpcBufferScheme cast ", schemeNames.split(",")[i], "\" width=75 height=21 back=", "L2UI_CH3.Btn1_normalOn", " fore=", "L2UI_CH3.Btn1_normal", ">", TRS[td + 1]);
                td += 2;
            }

            hb.append("</table>");
        }

        if (schemeNames != null && schemeNames.split(",").length >= 3) {
            hb.append("<br1><table width=100><tr>");
        } else {
            hb.append("<br1><table><tr><td><button value=\"Create\" action=\"bypass -h Engine NpcBufferScheme create_1\" width=75 height=21 back=", "L2UI_CH3.Btn1_normalOn", " fore=", "L2UI_CH3.Btn1_normal", "></td>");
        }

        if (schemeNames != null) {
            hb.append("<td><button value=\"Edit\" action=\"bypass -h Engine NpcBufferScheme edit_1\" width=75 height=21 back=", "L2UI_CH3.Btn1_normalOn", " fore=", "L2UI_CH3.Btn1_normal", "></td><td><button value=\"Delete\" action=\"bypass -h Engine NpcBufferScheme delete_1\" width=75 height=21 back=", "L2UI_CH3.Btn1_normalOn", " fore=", "L2UI_CH3.Btn1_normal", "></td></tr></table>");
        } else {
            hb.append("</tr></table>");
        }

        return hb.toString();
    }

    private int getBuffCount(Player player, String schemeName) {
        String buffList = this.getValueDB(player.getObjectId(), schemeName);
        return buffList != null ? buffList.split(";").length : 0;
    }

    private boolean isUsed(Player player, String scheme, int id, int level) {
        String buffList = this.getValueDB(player.getObjectId(), scheme);
        if (buffList == null) {
            return false;
        } else {
            for (String buff : buffList.split(";")) {
                if (Integer.parseInt(buff.split(",")[0]) == id && Integer.parseInt(buff.split(",")[1]) == level) {
                    return true;
                }
            }

            return false;
        }
    }

    private boolean isPetBuff(Player player) {
        String pettBuff = this.getValueDB(player.getObjectId(), "Pet-On-Off");
        return pettBuff == null || pettBuff.equals("1");
    }

    private void deleteScheme(Player player) {
        HtmlBuilder hb = new HtmlBuilder(HtmlBuilder.HtmlType.HTML_TYPE);
        hb.append("<html><head><title>", "Buffer", "</title></head><body>");
        hb.append(Html.headHtml("BUFFER"));
        hb.append("<center>");
        hb.append("<br>Available schemes:<br><br>");
        String schemeNames = this.getValueDB(player.getObjectId(), "schemeName");

        for (String scheme : schemeNames.split(",")) {
            hb.append("<button value=\"", scheme, "\" action=\"bypass -h Engine NpcBufferScheme delete_c ", scheme, " x\" width=75 height=21 back=", "L2UI_CH3.Btn1_normalOn", " fore=", "L2UI_CH3.Btn1_normal", ">");
        }

        hb.append("<br>");
        hb.append("<button value=\"Back\" action=\"bypass -h Engine NpcBufferScheme redirect_main\" width=75 height=21 back=", "L2UI_CH3.Btn1_normalOn", " fore=", "L2UI_CH3.Btn1_normal", ">");
        hb.append("<br>");
        hb.append("<font color=303030>Buffer</font>");
        hb.append("</center>");
        hb.append("</body></html>");
        sendHtml(player, null, hb);
    }

    private void editScheme(Player player) {
        HtmlBuilder hb = new HtmlBuilder(HtmlBuilder.HtmlType.HTML_TYPE);
        hb.append("<html><head><title>", "Buffer", "</title></head><body>");
        hb.append(Html.headHtml("BUFFER"));
        hb.append("<center>");
        hb.append("<br>Select a scheme that you would like to manage:<br><br>");
        String schemeNames = this.getValueDB(player.getObjectId(), "schemeName");

        for (String scheme : schemeNames.split(",")) {
            hb.append("<button value=\"" + scheme + "\" action=\"bypass -h Engine NpcBufferScheme manage_scheme_select " + scheme + "\" width=75 height=21 back=", "L2UI_CH3.Btn1_normalOn", " fore=", "L2UI_CH3.Btn1_normal", ">");
        }

        hb.append("<br>");
        hb.append("<button value=\"Back\" action=\"bypass -h Engine NpcBufferScheme redirect_main\" width=75 height=21 back=", "L2UI_CH3.Btn1_normalOn", " fore=", "L2UI_CH3.Btn1_normal", ">");
        hb.append("<br>");
        hb.append("<font color=303030>Buffer</font>");
        hb.append("</center>");
        hb.append("</body></html>");
        sendHtml(player, null, hb);
    }

    private void getOptionList(Player player, String scheme) {
        int bcount = this.getBuffCount(player, scheme);
        HtmlBuilder hb = new HtmlBuilder(HtmlBuilder.HtmlType.HTML_TYPE);
        hb.append("<html><head><title>", "Buffer", "</title></head><body>");
        hb.append(Html.headHtml("BUFFER"));
        hb.append("<center>");
        hb.append("<br>There are ", Html.newFontColor("LEVEL", bcount), " buffs in current scheme!<br><br>");
        if (bcount < 36) {
            hb.append("<button value=\"Add buffs\" action=\"bypass -h Engine NpcBufferScheme manage_scheme_add ", scheme, " 1\" width=75 height=21 back=", "L2UI_CH3.Btn1_normalOn", " fore=", "L2UI_CH3.Btn1_normal", ">");
        }

        if (bcount > 0) {
            hb.append("<button value=\"Remove buffs\" action=\"bypass -h Engine NpcBufferScheme manage_scheme_remove ", scheme, " 1\" width=75 height=21 back=", "L2UI_CH3.Btn1_normalOn", " fore=", "L2UI_CH3.Btn1_normal", ">");
        }

        hb.append("<br>");
        hb.append("<button value=\"Back\" action=\"bypass -h Engine NpcBufferScheme edit_1\" width=75 height=21 back=", "L2UI_CH3.Btn1_normalOn", " fore=", "L2UI_CH3.Btn1_normal", ">");
        hb.append("<button value=\"Home\" action=\"bypass -h Engine NpcBufferScheme redirect_main\" width=75 height=21 back=", "L2UI_CH3.Btn1_normalOn", " fore=", "L2UI_CH3.Btn1_normal", ">");
        hb.append("<br>");
        hb.append(Html.newFontColor("303030", "Buffer"));
        hb.append("</center>");
        hb.append("</body></html>");
        sendHtml(player, null, hb);
    }

    private String viewAllSchemeBuffsGetBuffCount(Player player, String scheme) {
        int count = 0;
        int D_S_Count = 0;
        int B_Count = 0;
        String buffList = this.getValueDB(player.getObjectId(), scheme);
        if (buffList != null) {
            for (String buff : buffList.split(";")) {
                int id = Integer.parseInt(buff.split(",")[0]);
                int level = Integer.parseInt(buff.split(",")[1]);
                ++count;

                for (BuffHolder bh : SchemeBuffData.getAllGeneralBuffs()) {
                    if (isEnabled(id, level) && bh.getId() == id && bh.getLevel() == level) {
                        if (bh.getType() != BuffType.SONG && bh.getType() != BuffType.DANCE) {
                            ++B_Count;
                            break;
                        }

                        ++D_S_Count;
                        break;
                    }
                }
            }
        }

        return count + " " + B_Count + " " + D_S_Count;
    }

    private void viewAllSchemeBuffs(Player player, String schemeName, String page, String action) {
        HtmlBuilder hb = new HtmlBuilder(HtmlBuilder.HtmlType.HTML_TYPE);
        hb.append("<html><head><title>", "Buffer", "</title></head><body>");
        hb.append(Html.headHtml("BUFFER"));
        hb.append("<center>");
        hb.append("<br>");
        String[] eventSplit = this.viewAllSchemeBuffsGetBuffCount(player, schemeName).split(" ");
        int buffsTotal = Integer.parseInt(eventSplit[0]);
        int buffsCount = Integer.parseInt(eventSplit[1]);
        int daceSong = Integer.parseInt(eventSplit[2]);
        List<BuffHolder> buffs = new ArrayList<>();
        if (action.equals("add")) {
            hb.append("You can add <font color=LEVEL>", 24 - buffsCount, "</font> Buffs and <font color=LEVEL>", 12 - daceSong, "</font> Dances more!");

            for (BuffHolder bh : SchemeBuffData.getAllGeneralBuffs()) {
                if ((daceSong <= 12 || bh.getType() != BuffType.DANCE && bh.getType() != BuffType.SONG) && (buffsCount <= 24 || bh.getType() == BuffType.DANCE || bh.getType() == BuffType.SONG)) {
                    buffs.add(bh);
                }
            }
        } else {
            if (!action.equals("remove")) {
                throw new RuntimeException();
            }

            hb.append("You have <font color=LEVEL>", buffsCount, "</font> Buffs and <font color=LEVEL>", daceSong, "</font> Dances");
            String buffList = this.getValueDB(player.getObjectId(), schemeName);
            if (buffList == null) {
                System.out.println("error en remove buff");
            } else {
                for (String buff : buffList.split(";")) {
                    int id = Integer.parseInt(buff.split(",")[0]);
                    int level = Integer.parseInt(buff.split(",")[1]);
                    buffs.add(new BuffHolder(id, level));
                }
            }
        }

        hb.append("<br1>", Html.newImage("L2UI.SquareWhite", 264, 1), "<table border=0 bgcolor=000000><tr>");
        int buffsPerPage = 10;
        int pc = (buffs.size() - 1) / 10 + 1;
        String width;
        if (pc > 5) {
            width = "25";
        } else {
            width = "50";
        }

        for (int ii = 1; ii <= pc; ++ii) {
            if (ii == Integer.parseInt(page)) {
                hb.append("<td width=", width, " align=center><font color=LEVEL>", ii, "</font></td>");
            } else if (action.equals("add")) {
                hb.append("<td width=", width, ">", "<a action=\"bypass -h Engine NpcBufferScheme manage_scheme_add ", schemeName, " ", ii, " x\">", ii, "</a></td>");
            } else {
                if (!action.equals("remove")) {
                    throw new RuntimeException();
                }

                hb.append("<td width=", width, ">", "<a action=\"bypass -h Engine NpcBufferScheme manage_scheme_remove ", schemeName, " ", ii, " x\">", ii, "</a></td>");
            }
        }

        hb.append("</tr></table>", Html.newImage("L2UI.SquareWhite", 264, 1));
        int limit = 10 * Integer.parseInt(page);
        int start = limit - 10;
        int end = Math.min(limit, buffs.size());
        int k = 0;

        for (int i = start; i < end; ++i) {
            BuffHolder bh = buffs.get(i);
            String name = bh.getSkill().getName();
            int id = bh.getId();
            int level = bh.getLevel();
            if (action.equals("add")) {
                if (!this.isUsed(player, schemeName, id, level)) {
                    if (k % 2 != 0) {
                        hb.append("<br1>", Html.newImage("L2UI.SquareGray", 264, 1), "<table border=0>");
                    } else {
                        hb.append("<br1>", Html.newImage("L2UI.SquareGray", 264, 1), "<table border=0 bgcolor=000000>");
                    }

                    hb.append("<tr>");
                    hb.append("<td width=35>", getSkillIconHtml(id, level), "</td>");
                    hb.append("<td fixwidth=170>", name, "</td>");
                    hb.append("<td><button value=\"Add\" action=\"bypass -h Engine NpcBufferScheme add_buff ", schemeName, "_", id, "_", level, " ", page, " ", buffsTotal, "\" width=75 height=21 back=", "L2UI_CH3.Btn1_normalOn", " fore=", "L2UI_CH3.Btn1_normal", "></td>");
                    hb.append("</tr>");
                    hb.append("</table>");
                    ++k;
                }
            } else if (action.equals("remove")) {
                if (k % 2 != 0) {
                    hb.append("<br1>", Html.newImage("L2UI.SquareGray", 264, 1), "<table border=0>");
                } else {
                    hb.append("<br1>", Html.newImage("L2UI.SquareGray", 264, 1), "<table border=0 bgcolor=000000>");
                }

                hb.append("<tr>");
                hb.append("<td width=35>", getSkillIconHtml(id, level), "</td>");
                hb.append("<td fixwidth=170>", name, "</td>");
                hb.append("<td><button value=\"Remove\" action=\"bypass -h Engine NpcBufferScheme remove_buff ", schemeName, "_", id, "_", level, " ", page, " ", buffsTotal, "\" width=75 height=21 back=", "L2UI_CH3.Btn1_normalOn", " fore=", "L2UI_CH3.Btn1_normal", "></td>");
                hb.append("</tr>");
                hb.append("</table>");
                ++k;
            }
        }

        hb.append("<br><br>");
        hb.append("<button value=Back action=\"bypass -h Engine NpcBufferScheme manage_scheme_select ", schemeName, "\" width=75 height=21 back=", "L2UI_CH3.Btn1_normalOn", " fore=", "L2UI_CH3.Btn1_normal", ">");
        hb.append("<button value=Home action=\"bypass -h Engine NpcBufferScheme redirect_main\" width=75 height=21 back=", "L2UI_CH3.Btn1_normalOn", " fore=", "L2UI_CH3.Btn1_normal", ">");
        hb.append("<br>");
        hb.append("<font color=303030>", "Buffer", "</font>");
        hb.append("</center>");
        hb.append("</body></html>");
        sendHtml(player, null, hb);
    }

    private static class SingletonHolder {
        protected static final NpcBufferScheme INSTANCE = new NpcBufferScheme();
    }
}
