package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.manager.ClanHallManager;
import net.sf.l2j.gameserver.data.xml.TeleportLocationData;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.clanhall.ClanHall;
import net.sf.l2j.gameserver.model.clanhall.ClanHallFunction;
import net.sf.l2j.gameserver.model.location.TeleportLocation;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.*;

import java.text.SimpleDateFormat;
import java.util.StringTokenizer;

public class ClanHallManagerNpc extends Merchant {
    protected static final int COND_OWNER_FALSE = 0;

    protected static final int COND_ALL_FALSE = 1;

    protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 2;

    protected static final int COND_OWNER = 3;

    private static final String hp_grade0 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 20\">20%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 40\">40%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 220\">220%</a>]";

    private static final String hp_grade1 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 40\">40%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 100\">100%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 160\">160%</a>]";

    private static final String hp_grade2 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 80\">80%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 140\">140%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 200\">200%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 260\">260%</a>]";

    private static final String hp_grade3 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 80\">80%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 120\">120%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 180\">180%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 240\">240%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 300\">300%</a>]";

    private static final String exp_grade0 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 10\">10%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 25\">25%</a>]";

    private static final String exp_grade1 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 30\">30%</a>]";

    private static final String exp_grade2 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 25\">25%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 40\">40%</a>]";

    private static final String exp_grade3 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 25\">25%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 35\">35%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 50\">50%</a>]";

    private static final String mp_grade0 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 10\">10%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 25\">25%</a>]";

    private static final String mp_grade1 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 25\">25%</a>]";

    private static final String mp_grade2 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 30\">30%</a>]";

    private static final String mp_grade3 = "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 30\">30%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 40\">40%</a>]";

    private static final String tele = "[<a action=\"bypass -h npc_%objectId%_manage other edit_tele 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_tele 2\">Level 2</a>]";

    private static final String support_grade0 = "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 2\">Level 2</a>]";

    private static final String support_grade1 = "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 2\">Level 2</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 4\">Level 4</a>]";

    private static final String support_grade2 = "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 3\">Level 3</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 4\">Level 4</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 5\">Level 5</a>]";

    private static final String support_grade3 = "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 3\">Level 3</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 5\">Level 5</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 7\">Level 7</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 8\">Level 8</a>]";

    private static final String item = "[<a action=\"bypass -h npc_%objectId%_manage other edit_item 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_item 2\">Level 2</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_item 3\">Level 3</a>]";

    private static final String curtains = "[<a action=\"bypass -h npc_%objectId%_manage deco edit_curtains 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage deco edit_curtains 2\">Level 2</a>]";

    private static final String fixtures = "[<a action=\"bypass -h npc_%objectId%_manage deco edit_fixtures 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage deco edit_fixtures 2\">Level 2</a>]";

    private ClanHall _clanHall;

    public ClanHallManagerNpc(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public void onSpawn() {
        this._clanHall = ClanHallManager.getInstance().getNearestClanHall(getX(), getY(), 500);
        if (this._clanHall == null)
            super.onSpawn();
    }

    public boolean isWarehouse() {
        return true;
    }

    public void onBypassFeedback(Player player, String command) {
        int condition = validateCondition(player);
        if (condition <= 1)
            return;
        if (condition == 3) {
            StringTokenizer st = new StringTokenizer(command, " ");
            String actualCommand = st.nextToken();
            String val = st.hasMoreTokens() ? st.nextToken() : "";
            if (actualCommand.equalsIgnoreCase("banish_foreigner")) {
                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                if ((player.getClanPrivileges() & 0x2000) == 8192) {
                    if (val.equalsIgnoreCase("list")) {
                        html.setFile("data/html/clanHallManager/banish-list.htm");
                    } else if (val.equalsIgnoreCase("banish")) {
                        this._clanHall.banishForeigners();
                        html.setFile("data/html/clanHallManager/banish.htm");
                    }
                } else {
                    html.setFile("data/html/clanHallManager/not_authorized.htm");
                }
                html.replace("%objectId%", getObjectId());
                player.sendPacket(html);
            } else if (actualCommand.equalsIgnoreCase("manage_vault")) {
                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                if ((player.getClanPrivileges() & 0x8) == 8) {
                    html.setFile("data/html/clanHallManager/vault.htm");
                    html.replace("%rent%", this._clanHall.getLease());
                    html.replace("%date%", (new SimpleDateFormat("dd-MM-yyyy HH:mm")).format(this._clanHall.getPaidUntil()));
                } else {
                    html.setFile("data/html/clanHallManager/not_authorized.htm");
                }
                html.replace("%objectId%", getObjectId());
                player.sendPacket(html);
            } else if (actualCommand.equalsIgnoreCase("door")) {
                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                if ((player.getClanPrivileges() & 0x400) == 1024) {
                    if (val.equalsIgnoreCase("open")) {
                        this._clanHall.openCloseDoors(true);
                        html.setFile("data/html/clanHallManager/door-open.htm");
                    } else if (val.equalsIgnoreCase("close")) {
                        this._clanHall.openCloseDoors(false);
                        html.setFile("data/html/clanHallManager/door-close.htm");
                    } else {
                        html.setFile("data/html/clanHallManager/door.htm");
                    }
                } else {
                    html.setFile("data/html/clanHallManager/not_authorized.htm");
                }
                html.replace("%objectId%", getObjectId());
                player.sendPacket(html);
            } else if (actualCommand.equalsIgnoreCase("functions")) {
                if (val.equalsIgnoreCase("tele")) {
                    NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                    ClanHallFunction chf = this._clanHall.getFunction(1);
                    if (chf == null) {
                        html.setFile("data/html/clanHallManager/chamberlain-nac.htm");
                    } else {
                        html.setFile("data/html/clanHallManager/tele" + this._clanHall.getLocation() + chf.getLvl() + ".htm");
                    }
                    html.replace("%objectId%", getObjectId());
                    player.sendPacket(html);
                } else if (val.equalsIgnoreCase("item_creation")) {
                    if (!st.hasMoreTokens())
                        return;
                    ClanHallFunction chf = this._clanHall.getFunction(2);
                    if (chf == null) {
                        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                        html.setFile("data/html/clanHallManager/chamberlain-nac.htm");
                        html.replace("%objectId%", getObjectId());
                        player.sendPacket(html);
                        return;
                    }
                    showBuyWindow(player, Integer.parseInt(st.nextToken()) + chf.getLvl() * 100000);
                } else if (val.equalsIgnoreCase("support")) {
                    NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                    ClanHallFunction chf = this._clanHall.getFunction(6);
                    if (chf == null) {
                        html.setFile("data/html/clanHallManager/chamberlain-nac.htm");
                    } else {
                        html.setFile("data/html/clanHallManager/support" + chf.getLvl() + ".htm");
                        html.replace("%mp%", (int) getCurrentMp());
                    }
                    html.replace("%objectId%", getObjectId());
                    player.sendPacket(html);
                } else if (val.equalsIgnoreCase("back")) {
                    showChatWindow(player);
                } else {
                    NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                    html.setFile("data/html/clanHallManager/functions.htm");
                    ClanHallFunction chfExp = this._clanHall.getFunction(5);
                    if (chfExp != null) {
                        html.replace("%xp_regen%", chfExp.getLvl());
                    } else {
                        html.replace("%xp_regen%", "0");
                    }
                    ClanHallFunction chfHp = this._clanHall.getFunction(3);
                    if (chfHp != null) {
                        html.replace("%hp_regen%", chfHp.getLvl());
                    } else {
                        html.replace("%hp_regen%", "0");
                    }
                    ClanHallFunction chfMp = this._clanHall.getFunction(4);
                    if (chfMp != null) {
                        html.replace("%mp_regen%", chfMp.getLvl());
                    } else {
                        html.replace("%mp_regen%", "0");
                    }
                    html.replace("%npcId%", getNpcId());
                    html.replace("%objectId%", getObjectId());
                    player.sendPacket(html);
                }
            } else if (actualCommand.equalsIgnoreCase("manage")) {
                if ((player.getClanPrivileges() & 0x4000) == 16384) {
                    if (val.equalsIgnoreCase("recovery")) {
                        if (st.hasMoreTokens()) {
                            if (this._clanHall.getOwnerId() == 0)
                                return;
                            val = st.nextToken();
                            if (val.equalsIgnoreCase("hp_cancel")) {
                                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                                html.setFile("data/html/clanHallManager/functions-cancel.htm");
                                html.replace("%apply%", "recovery hp 0");
                                html.replace("%objectId%", getObjectId());
                                player.sendPacket(html);
                            } else if (val.equalsIgnoreCase("mp_cancel")) {
                                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                                html.setFile("data/html/clanHallManager/functions-cancel.htm");
                                html.replace("%apply%", "recovery mp 0");
                                html.replace("%objectId%", getObjectId());
                                player.sendPacket(html);
                            } else if (val.equalsIgnoreCase("exp_cancel")) {
                                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                                html.setFile("data/html/clanHallManager/functions-cancel.htm");
                                html.replace("%apply%", "recovery exp 0");
                                html.replace("%objectId%", getObjectId());
                                player.sendPacket(html);
                            } else if (val.equalsIgnoreCase("edit_hp")) {
                                int cost;
                                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                                html.setFile("data/html/clanHallManager/functions-apply.htm");
                                html.replace("%name%", "Fireplace (HP Recovery Device)");
                                int percent = Integer.parseInt(st.nextToken());
                                switch (percent) {
                                    case 20:
                                        cost = Config.CH_HPREG1_FEE;
                                        break;
                                    case 40:
                                        cost = Config.CH_HPREG2_FEE;
                                        break;
                                    case 80:
                                        cost = Config.CH_HPREG3_FEE;
                                        break;
                                    case 100:
                                        cost = Config.CH_HPREG4_FEE;
                                        break;
                                    case 120:
                                        cost = Config.CH_HPREG5_FEE;
                                        break;
                                    case 140:
                                        cost = Config.CH_HPREG6_FEE;
                                        break;
                                    case 160:
                                        cost = Config.CH_HPREG7_FEE;
                                        break;
                                    case 180:
                                        cost = Config.CH_HPREG8_FEE;
                                        break;
                                    case 200:
                                        cost = Config.CH_HPREG9_FEE;
                                        break;
                                    case 220:
                                        cost = Config.CH_HPREG10_FEE;
                                        break;
                                    case 240:
                                        cost = Config.CH_HPREG11_FEE;
                                        break;
                                    case 260:
                                        cost = Config.CH_HPREG12_FEE;
                                        break;
                                    default:
                                        cost = Config.CH_HPREG13_FEE;
                                        break;
                                }
                                html.replace("%cost%", cost + "</font> adenas / " + cost + " day</font>)");
                                html.replace("%use%", "Provides additional HP recovery for clan members in the clan hall.<font color=\"00FFFF\">" + percent + "%</font>");
                                html.replace("%apply%", "recovery hp " + percent);
                                html.replace("%objectId%", getObjectId());
                                player.sendPacket(html);
                            } else if (val.equalsIgnoreCase("edit_mp")) {
                                int cost;
                                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                                html.setFile("data/html/clanHallManager/functions-apply.htm");
                                html.replace("%name%", "Carpet (MP Recovery)");
                                int percent = Integer.parseInt(st.nextToken());
                                switch (percent) {
                                    case 5:
                                        cost = Config.CH_MPREG1_FEE;
                                        break;
                                    case 10:
                                        cost = Config.CH_MPREG2_FEE;
                                        break;
                                    case 15:
                                        cost = Config.CH_MPREG3_FEE;
                                        break;
                                    case 30:
                                        cost = Config.CH_MPREG4_FEE;
                                        break;
                                    default:
                                        cost = Config.CH_MPREG5_FEE;
                                        break;
                                }
                                html.replace("%cost%", cost + "</font> adenas / " + cost + " day</font>)");
                                html.replace("%use%", "Provides additional MP recovery for clan members in the clan hall.<font color=\"00FFFF\">" + percent + "%</font>");
                                html.replace("%apply%", "recovery mp " + percent);
                                html.replace("%objectId%", getObjectId());
                                player.sendPacket(html);
                            } else if (val.equalsIgnoreCase("edit_exp")) {
                                int cost;
                                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                                html.setFile("data/html/clanHallManager/functions-apply.htm");
                                html.replace("%name%", "Chandelier (EXP Recovery Device)");
                                int percent = Integer.parseInt(st.nextToken());
                                switch (percent) {
                                    case 5:
                                        cost = Config.CH_EXPREG1_FEE;
                                        break;
                                    case 10:
                                        cost = Config.CH_EXPREG2_FEE;
                                        break;
                                    case 15:
                                        cost = Config.CH_EXPREG3_FEE;
                                        break;
                                    case 25:
                                        cost = Config.CH_EXPREG4_FEE;
                                        break;
                                    case 35:
                                        cost = Config.CH_EXPREG5_FEE;
                                        break;
                                    case 40:
                                        cost = Config.CH_EXPREG6_FEE;
                                        break;
                                    default:
                                        cost = Config.CH_EXPREG7_FEE;
                                        break;
                                }
                                html.replace("%cost%", cost + "</font> adenas / " + cost + " day</font>)");
                                html.replace("%use%", "Restores the Exp of any clan member who is resurrected in the clan hall.<font color=\"00FFFF\">" + percent + "%</font>");
                                html.replace("%apply%", "recovery exp " + percent);
                                html.replace("%objectId%", getObjectId());
                                player.sendPacket(html);
                            } else if (val.equalsIgnoreCase("hp")) {
                                int fee;
                                val = st.nextToken();
                                int percent = Integer.parseInt(val);
                                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                                ClanHallFunction chf = this._clanHall.getFunction(3);
                                if (chf != null && chf.getLvl() == percent) {
                                    html.setFile("data/html/clanHallManager/functions-used.htm");
                                    html.replace("%val%", val + "%");
                                    html.replace("%objectId%", getObjectId());
                                    player.sendPacket(html);
                                    return;
                                }
                                html.setFile("data/html/clanHallManager/functions-apply_confirmed.htm");
                                switch (percent) {
                                    case 0:
                                        fee = 0;
                                        html.setFile("data/html/clanHallManager/functions-cancel_confirmed.htm");
                                        break;
                                    case 20:
                                        fee = Config.CH_HPREG1_FEE;
                                        break;
                                    case 40:
                                        fee = Config.CH_HPREG2_FEE;
                                        break;
                                    case 80:
                                        fee = Config.CH_HPREG3_FEE;
                                        break;
                                    case 100:
                                        fee = Config.CH_HPREG4_FEE;
                                        break;
                                    case 120:
                                        fee = Config.CH_HPREG5_FEE;
                                        break;
                                    case 140:
                                        fee = Config.CH_HPREG6_FEE;
                                        break;
                                    case 160:
                                        fee = Config.CH_HPREG7_FEE;
                                        break;
                                    case 180:
                                        fee = Config.CH_HPREG8_FEE;
                                        break;
                                    case 200:
                                        fee = Config.CH_HPREG9_FEE;
                                        break;
                                    case 220:
                                        fee = Config.CH_HPREG10_FEE;
                                        break;
                                    case 240:
                                        fee = Config.CH_HPREG11_FEE;
                                        break;
                                    case 260:
                                        fee = Config.CH_HPREG12_FEE;
                                        break;
                                    default:
                                        fee = Config.CH_HPREG13_FEE;
                                        break;
                                }
                                if (!this._clanHall.updateFunctions(player, 3, percent, fee, Config.CH_HPREG_FEE_RATIO)) {
                                    html.setFile("data/html/clanHallManager/low_adena.htm");
                                } else {
                                    revalidateDeco(player);
                                }
                                html.replace("%objectId%", getObjectId());
                                player.sendPacket(html);
                            } else if (val.equalsIgnoreCase("mp")) {
                                int fee;
                                val = st.nextToken();
                                int percent = Integer.parseInt(val);
                                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                                ClanHallFunction chf = this._clanHall.getFunction(4);
                                if (chf != null && chf.getLvl() == percent) {
                                    html.setFile("data/html/clanHallManager/functions-used.htm");
                                    html.replace("%val%", val + "%");
                                    html.replace("%objectId%", getObjectId());
                                    player.sendPacket(html);
                                    return;
                                }
                                html.setFile("data/html/clanHallManager/functions-apply_confirmed.htm");
                                switch (percent) {
                                    case 0:
                                        fee = 0;
                                        html.setFile("data/html/clanHallManager/functions-cancel_confirmed.htm");
                                        break;
                                    case 5:
                                        fee = Config.CH_MPREG1_FEE;
                                        break;
                                    case 10:
                                        fee = Config.CH_MPREG2_FEE;
                                        break;
                                    case 15:
                                        fee = Config.CH_MPREG3_FEE;
                                        break;
                                    case 30:
                                        fee = Config.CH_MPREG4_FEE;
                                        break;
                                    default:
                                        fee = Config.CH_MPREG5_FEE;
                                        break;
                                }
                                if (!this._clanHall.updateFunctions(player, 4, percent, fee, Config.CH_MPREG_FEE_RATIO)) {
                                    html.setFile("data/html/clanHallManager/low_adena.htm");
                                } else {
                                    revalidateDeco(player);
                                }
                                html.replace("%objectId%", getObjectId());
                                player.sendPacket(html);
                            } else if (val.equalsIgnoreCase("exp")) {
                                int fee;
                                val = st.nextToken();
                                int percent = Integer.parseInt(val);
                                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                                ClanHallFunction chf = this._clanHall.getFunction(5);
                                if (chf != null && chf.getLvl() == percent) {
                                    html.setFile("data/html/clanHallManager/functions-used.htm");
                                    html.replace("%val%", val + "%");
                                    html.replace("%objectId%", getObjectId());
                                    player.sendPacket(html);
                                    return;
                                }
                                html.setFile("data/html/clanHallManager/functions-apply_confirmed.htm");
                                switch (percent) {
                                    case 0:
                                        fee = 0;
                                        html.setFile("data/html/clanHallManager/functions-cancel_confirmed.htm");
                                        break;
                                    case 5:
                                        fee = Config.CH_EXPREG1_FEE;
                                        break;
                                    case 10:
                                        fee = Config.CH_EXPREG2_FEE;
                                        break;
                                    case 15:
                                        fee = Config.CH_EXPREG3_FEE;
                                        break;
                                    case 25:
                                        fee = Config.CH_EXPREG4_FEE;
                                        break;
                                    case 35:
                                        fee = Config.CH_EXPREG5_FEE;
                                        break;
                                    case 40:
                                        fee = Config.CH_EXPREG6_FEE;
                                        break;
                                    default:
                                        fee = Config.CH_EXPREG7_FEE;
                                        break;
                                }
                                if (!this._clanHall.updateFunctions(player, 5, percent, fee, Config.CH_EXPREG_FEE_RATIO)) {
                                    html.setFile("data/html/clanHallManager/low_adena.htm");
                                } else {
                                    revalidateDeco(player);
                                }
                                html.replace("%objectId%", getObjectId());
                                player.sendPacket(html);
                            }
                        } else {
                            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                            html.setFile("data/html/clanHallManager/edit_recovery.htm");
                            int grade = this._clanHall.getGrade();
                            ClanHallFunction chfHp = this._clanHall.getFunction(3);
                            if (chfHp != null) {
                                html.replace("%hp_recovery%", chfHp.getLvl() + "%</font> (<font color=\"FFAABB\">" + chfHp.getLvl() + "</font> adenas / " + chfHp.getLease() + " day)");
                                html.replace("%hp_period%", "Next fee at " + (new SimpleDateFormat("dd-MM-yyyy HH:mm")).format(chfHp.getEndTime()));
                                switch (grade) {
                                    case 0:
                                        html.replace("%change_hp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery hp_cancel\">Remove</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 20\">20%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 40\">40%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 220\">220%</a>]");
                                        break;
                                    case 1:
                                        html.replace("%change_hp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery hp_cancel\">Remove</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 40\">40%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 100\">100%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 160\">160%</a>]");
                                        break;
                                    case 2:
                                        html.replace("%change_hp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery hp_cancel\">Remove</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 80\">80%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 140\">140%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 200\">200%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 260\">260%</a>]");
                                        break;
                                    case 3:
                                        html.replace("%change_hp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery hp_cancel\">Remove</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 80\">80%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 120\">120%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 180\">180%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 240\">240%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 300\">300%</a>]");
                                        break;
                                }
                            } else {
                                html.replace("%hp_recovery%", "none");
                                html.replace("%hp_period%", "none");
                                switch (grade) {
                                    case 0:
                                        html.replace("%change_hp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 20\">20%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 40\">40%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 220\">220%</a>]");
                                        break;
                                    case 1:
                                        html.replace("%change_hp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 40\">40%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 100\">100%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 160\">160%</a>]");
                                        break;
                                    case 2:
                                        html.replace("%change_hp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 80\">80%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 140\">140%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 200\">200%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 260\">260%</a>]");
                                        break;
                                    case 3:
                                        html.replace("%change_hp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 80\">80%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 120\">120%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 180\">180%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 240\">240%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_hp 300\">300%</a>]");
                                        break;
                                }
                            }
                            ClanHallFunction chfExp = this._clanHall.getFunction(5);
                            if (chfExp != null) {
                                html.replace("%exp_recovery%", chfExp.getLvl() + "%</font> (<font color=\"FFAABB\">" + chfExp.getLvl() + "</font> adenas / " + chfExp.getLease() + " day)");
                                html.replace("%exp_period%", "Next fee at " + (new SimpleDateFormat("dd-MM-yyyy HH:mm")).format(chfExp.getEndTime()));
                                switch (grade) {
                                    case 0:
                                        html.replace("%change_exp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery exp_cancel\">Remove</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 10\">10%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 25\">25%</a>]");
                                        break;
                                    case 1:
                                        html.replace("%change_exp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery exp_cancel\">Remove</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 30\">30%</a>]");
                                        break;
                                    case 2:
                                        html.replace("%change_exp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery exp_cancel\">Remove</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 25\">25%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 40\">40%</a>]");
                                        break;
                                    case 3:
                                        html.replace("%change_exp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery exp_cancel\">Remove</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 25\">25%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 35\">35%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 50\">50%</a>]");
                                        break;
                                }
                            } else {
                                html.replace("%exp_recovery%", "none");
                                html.replace("%exp_period%", "none");
                                switch (grade) {
                                    case 0:
                                        html.replace("%change_exp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 10\">10%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 25\">25%</a>]");
                                        break;
                                    case 1:
                                        html.replace("%change_exp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 30\">30%</a>]");
                                        break;
                                    case 2:
                                        html.replace("%change_exp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 25\">25%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 40\">40%</a>]");
                                        break;
                                    case 3:
                                        html.replace("%change_exp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 25\">25%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 35\">35%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_exp 50\">50%</a>]");
                                        break;
                                }
                            }
                            ClanHallFunction chfMp = this._clanHall.getFunction(4);
                            if (chfMp != null) {
                                html.replace("%mp_recovery%", chfMp.getLvl() + "%</font> (<font color=\"FFAABB\">" + chfMp.getLvl() + "</font> adenas / " + chfMp.getLease() + " day)");
                                html.replace("%mp_period%", "Next fee at " + (new SimpleDateFormat("dd-MM-yyyy HH:mm")).format(chfMp.getEndTime()));
                                switch (grade) {
                                    case 0:
                                        html.replace("%change_mp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery mp_cancel\">Remove</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 10\">10%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 25\">25%</a>]");
                                        break;
                                    case 1:
                                        html.replace("%change_mp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery mp_cancel\">Remove</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 25\">25%</a>]");
                                        break;
                                    case 2:
                                        html.replace("%change_mp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery mp_cancel\">Remove</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 30\">30%</a>]");
                                        break;
                                    case 3:
                                        html.replace("%change_mp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery mp_cancel\">Remove</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 30\">30%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 40\">40%</a>]");
                                        break;
                                }
                            } else {
                                html.replace("%mp_recovery%", "none");
                                html.replace("%mp_period%", "none");
                                switch (grade) {
                                    case 0:
                                        html.replace("%change_mp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 10\">10%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 25\">25%</a>]");
                                        break;
                                    case 1:
                                        html.replace("%change_mp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 25\">25%</a>]");
                                        break;
                                    case 2:
                                        html.replace("%change_mp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 30\">30%</a>]");
                                        break;
                                    case 3:
                                        html.replace("%change_mp%", "[<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 5\">5%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 15\">15%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 30\">30%</a>][<a action=\"bypass -h npc_%objectId%_manage recovery edit_mp 40\">40%</a>]");
                                        break;
                                }
                            }
                            html.replace("%objectId%", getObjectId());
                            player.sendPacket(html);
                        }
                    } else if (val.equalsIgnoreCase("other")) {
                        if (st.hasMoreTokens()) {
                            if (this._clanHall.getOwnerId() == 0)
                                return;
                            val = st.nextToken();
                            if (val.equalsIgnoreCase("item_cancel")) {
                                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                                html.setFile("data/html/clanHallManager/functions-cancel.htm");
                                html.replace("%apply%", "other item 0");
                                html.replace("%objectId%", getObjectId());
                                player.sendPacket(html);
                            } else if (val.equalsIgnoreCase("tele_cancel")) {
                                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                                html.setFile("data/html/clanHallManager/functions-cancel.htm");
                                html.replace("%apply%", "other tele 0");
                                html.replace("%objectId%", getObjectId());
                                player.sendPacket(html);
                            } else if (val.equalsIgnoreCase("support_cancel")) {
                                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                                html.setFile("data/html/clanHallManager/functions-cancel.htm");
                                html.replace("%apply%", "other support 0");
                                html.replace("%objectId%", getObjectId());
                                player.sendPacket(html);
                            } else if (val.equalsIgnoreCase("edit_item")) {
                                int cost;
                                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                                html.setFile("data/html/clanHallManager/functions-apply.htm");
                                html.replace("%name%", "Magic Equipment (Item Production Facilities)");
                                int stage = Integer.parseInt(st.nextToken());
                                switch (stage) {
                                    case 1:
                                        cost = Config.CH_ITEM1_FEE;
                                        break;
                                    case 2:
                                        cost = Config.CH_ITEM2_FEE;
                                        break;
                                    default:
                                        cost = Config.CH_ITEM3_FEE;
                                        break;
                                }
                                html.replace("%cost%", cost + "</font> adenas / " + cost + " day</font>)");
                                html.replace("%use%", "Allow the purchase of special items at fixed intervals.");
                                html.replace("%apply%", "other item " + stage);
                                html.replace("%objectId%", getObjectId());
                                player.sendPacket(html);
                            } else if (val.equalsIgnoreCase("edit_support")) {
                                int cost;
                                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                                html.setFile("data/html/clanHallManager/functions-apply.htm");
                                html.replace("%name%", "Insignia (Supplementary Magic)");
                                int stage = Integer.parseInt(st.nextToken());
                                switch (stage) {
                                    case 1:
                                        cost = Config.CH_SUPPORT1_FEE;
                                        break;
                                    case 2:
                                        cost = Config.CH_SUPPORT2_FEE;
                                        break;
                                    case 3:
                                        cost = Config.CH_SUPPORT3_FEE;
                                        break;
                                    case 4:
                                        cost = Config.CH_SUPPORT4_FEE;
                                        break;
                                    case 5:
                                        cost = Config.CH_SUPPORT5_FEE;
                                        break;
                                    case 6:
                                        cost = Config.CH_SUPPORT6_FEE;
                                        break;
                                    case 7:
                                        cost = Config.CH_SUPPORT7_FEE;
                                        break;
                                    default:
                                        cost = Config.CH_SUPPORT8_FEE;
                                        break;
                                }
                                html.replace("%cost%", cost + "</font> adenas / " + cost + " day</font>)");
                                html.replace("%use%", "Enables the use of supplementary magic.");
                                html.replace("%apply%", "other support " + stage);
                                html.replace("%objectId%", getObjectId());
                                player.sendPacket(html);
                            } else if (val.equalsIgnoreCase("edit_tele")) {
                                int cost;
                                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                                html.setFile("data/html/clanHallManager/functions-apply.htm");
                                html.replace("%name%", "Mirror (Teleportation Device)");
                                int stage = Integer.parseInt(st.nextToken());
                                switch (stage) {
                                    case 1:
                                        cost = Config.CH_TELE1_FEE;
                                        break;
                                    default:
                                        cost = Config.CH_TELE2_FEE;
                                        break;
                                }
                                html.replace("%cost%", cost + "</font> adenas / " + cost + " day</font>)");
                                html.replace("%use%", "Teleports clan members in a clan hall to the target <font color=\"00FFFF\">Stage " + stage + "</font> staging area");
                                html.replace("%apply%", "other tele " + stage);
                                html.replace("%objectId%", getObjectId());
                                player.sendPacket(html);
                            } else if (val.equalsIgnoreCase("item")) {
                                int fee;
                                if (this._clanHall.getOwnerId() == 0)
                                    return;
                                val = st.nextToken();
                                int lvl = Integer.parseInt(val);
                                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                                ClanHallFunction chf = this._clanHall.getFunction(2);
                                if (chf != null && chf.getLvl() == lvl) {
                                    html.setFile("data/html/clanHallManager/functions-used.htm");
                                    html.replace("%val%", "Stage " + val);
                                    html.replace("%objectId%", getObjectId());
                                    player.sendPacket(html);
                                    return;
                                }
                                html.setFile("data/html/clanHallManager/functions-apply_confirmed.htm");
                                switch (lvl) {
                                    case 0:
                                        fee = 0;
                                        html.setFile("data/html/clanHallManager/functions-cancel_confirmed.htm");
                                        break;
                                    case 1:
                                        fee = Config.CH_ITEM1_FEE;
                                        break;
                                    case 2:
                                        fee = Config.CH_ITEM2_FEE;
                                        break;
                                    default:
                                        fee = Config.CH_ITEM3_FEE;
                                        break;
                                }
                                if (!this._clanHall.updateFunctions(player, 2, lvl, fee, Config.CH_ITEM_FEE_RATIO)) {
                                    html.setFile("data/html/clanHallManager/low_adena.htm");
                                } else {
                                    revalidateDeco(player);
                                }
                                html.replace("%objectId%", getObjectId());
                                player.sendPacket(html);
                            } else if (val.equalsIgnoreCase("tele")) {
                                int fee;
                                val = st.nextToken();
                                int lvl = Integer.parseInt(val);
                                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                                ClanHallFunction chf = this._clanHall.getFunction(1);
                                if (chf != null && chf.getLvl() == lvl) {
                                    html.setFile("data/html/clanHallManager/functions-used.htm");
                                    html.replace("%val%", "Stage " + val);
                                    html.replace("%objectId%", getObjectId());
                                    player.sendPacket(html);
                                    return;
                                }
                                html.setFile("data/html/clanHallManager/functions-apply_confirmed.htm");
                                switch (lvl) {
                                    case 0:
                                        fee = 0;
                                        html.setFile("data/html/clanHallManager/functions-cancel_confirmed.htm");
                                        break;
                                    case 1:
                                        fee = Config.CH_TELE1_FEE;
                                        break;
                                    default:
                                        fee = Config.CH_TELE2_FEE;
                                        break;
                                }
                                if (!this._clanHall.updateFunctions(player, 1, lvl, fee, Config.CH_TELE_FEE_RATIO)) {
                                    html.setFile("data/html/clanHallManager/low_adena.htm");
                                } else {
                                    revalidateDeco(player);
                                }
                                html.replace("%objectId%", getObjectId());
                                player.sendPacket(html);
                            } else if (val.equalsIgnoreCase("support")) {
                                int fee;
                                val = st.nextToken();
                                int lvl = Integer.parseInt(val);
                                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                                ClanHallFunction chf = this._clanHall.getFunction(6);
                                if (chf != null && chf.getLvl() == lvl) {
                                    html.setFile("data/html/clanHallManager/functions-used.htm");
                                    html.replace("%val%", "Stage " + val);
                                    html.replace("%objectId%", getObjectId());
                                    player.sendPacket(html);
                                    return;
                                }
                                html.setFile("data/html/clanHallManager/functions-apply_confirmed.htm");
                                switch (lvl) {
                                    case 0:
                                        fee = 0;
                                        html.setFile("data/html/clanHallManager/functions-cancel_confirmed.htm");
                                        break;
                                    case 1:
                                        fee = Config.CH_SUPPORT1_FEE;
                                        break;
                                    case 2:
                                        fee = Config.CH_SUPPORT2_FEE;
                                        break;
                                    case 3:
                                        fee = Config.CH_SUPPORT3_FEE;
                                        break;
                                    case 4:
                                        fee = Config.CH_SUPPORT4_FEE;
                                        break;
                                    case 5:
                                        fee = Config.CH_SUPPORT5_FEE;
                                        break;
                                    case 6:
                                        fee = Config.CH_SUPPORT6_FEE;
                                        break;
                                    case 7:
                                        fee = Config.CH_SUPPORT7_FEE;
                                        break;
                                    default:
                                        fee = Config.CH_SUPPORT8_FEE;
                                        break;
                                }
                                if (!this._clanHall.updateFunctions(player, 6, lvl, fee, Config.CH_SUPPORT_FEE_RATIO)) {
                                    html.setFile("data/html/clanHallManager/low_adena.htm");
                                } else {
                                    revalidateDeco(player);
                                }
                                html.replace("%objectId%", getObjectId());
                                player.sendPacket(html);
                            }
                        } else {
                            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                            html.setFile("data/html/clanHallManager/edit_other.htm");
                            ClanHallFunction chfTel = this._clanHall.getFunction(1);
                            if (chfTel != null) {
                                html.replace("%tele%", "- Stage " + chfTel.getLvl() + "</font> (<font color=\"FFAABB\">" + chfTel.getLease() + "</font> adenas / " + Config.CH_TELE_FEE_RATIO / 86400000L + " day)");
                                html.replace("%tele_period%", "Next fee at " + (new SimpleDateFormat("dd-MM-yyyy HH:mm")).format(chfTel.getEndTime()));
                                html.replace("%change_tele%", "[<a action=\"bypass -h npc_%objectId%_manage other tele_cancel\">Remove</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_tele 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_tele 2\">Level 2</a>]");
                            } else {
                                html.replace("%tele%", "none");
                                html.replace("%tele_period%", "none");
                                html.replace("%change_tele%", "[<a action=\"bypass -h npc_%objectId%_manage other edit_tele 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_tele 2\">Level 2</a>]");
                            }
                            int grade = this._clanHall.getGrade();
                            ClanHallFunction chfSup = this._clanHall.getFunction(6);
                            if (chfSup != null) {
                                html.replace("%support%", "- Stage " + chfSup.getLvl() + "</font> (<font color=\"FFAABB\">" + chfSup.getLease() + "</font> adenas / " + Config.CH_SUPPORT_FEE_RATIO / 86400000L + " day)");
                                html.replace("%support_period%", "Next fee at " + (new SimpleDateFormat("dd-MM-yyyy HH:mm")).format(chfSup.getEndTime()));
                                switch (grade) {
                                    case 0:
                                        html.replace("%change_support%", "[<a action=\"bypass -h npc_%objectId%_manage other support_cancel\">Remove</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 2\">Level 2</a>]");
                                        break;
                                    case 1:
                                        html.replace("%change_support%", "[<a action=\"bypass -h npc_%objectId%_manage other support_cancel\">Remove</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 2\">Level 2</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 4\">Level 4</a>]");
                                        break;
                                    case 2:
                                        html.replace("%change_support%", "[<a action=\"bypass -h npc_%objectId%_manage other support_cancel\">Remove</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 3\">Level 3</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 4\">Level 4</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 5\">Level 5</a>]");
                                        break;
                                    case 3:
                                        html.replace("%change_support%", "[<a action=\"bypass -h npc_%objectId%_manage other support_cancel\">Remove</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 3\">Level 3</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 5\">Level 5</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 7\">Level 7</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 8\">Level 8</a>]");
                                        break;
                                }
                            } else {
                                html.replace("%support%", "none");
                                html.replace("%support_period%", "none");
                                switch (grade) {
                                    case 0:
                                        html.replace("%change_support%", "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 2\">Level 2</a>]");
                                        break;
                                    case 1:
                                        html.replace("%change_support%", "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 2\">Level 2</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 4\">Level 4</a>]");
                                        break;
                                    case 2:
                                        html.replace("%change_support%", "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 3\">Level 3</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 4\">Level 4</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 5\">Level 5</a>]");
                                        break;
                                    case 3:
                                        html.replace("%change_support%", "[<a action=\"bypass -h npc_%objectId%_manage other edit_support 3\">Level 3</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 5\">Level 5</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 7\">Level 7</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_support 8\">Level 8</a>]");
                                        break;
                                }
                            }
                            ClanHallFunction chfCreate = this._clanHall.getFunction(2);
                            if (chfCreate != null) {
                                html.replace("%item%", "- Stage " + chfCreate.getLvl() + "</font> (<font color=\"FFAABB\">" + chfCreate.getLease() + "</font> adenas / " + Config.CH_ITEM_FEE_RATIO / 86400000L + " day)");
                                html.replace("%item_period%", "Next fee at " + (new SimpleDateFormat("dd-MM-yyyy HH:mm")).format(chfCreate.getEndTime()));
                                html.replace("%change_item%", "[<a action=\"bypass -h npc_%objectId%_manage other item_cancel\">Remove</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_item 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_item 2\">Level 2</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_item 3\">Level 3</a>]");
                            } else {
                                html.replace("%item%", "none");
                                html.replace("%item_period%", "none");
                                html.replace("%change_item%", "[<a action=\"bypass -h npc_%objectId%_manage other edit_item 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_item 2\">Level 2</a>][<a action=\"bypass -h npc_%objectId%_manage other edit_item 3\">Level 3</a>]");
                            }
                            html.replace("%objectId%", getObjectId());
                            player.sendPacket(html);
                        }
                    } else if (val.equalsIgnoreCase("deco")) {
                        if (st.hasMoreTokens()) {
                            if (this._clanHall.getOwnerId() == 0)
                                return;
                            val = st.nextToken();
                            if (val.equalsIgnoreCase("curtains_cancel")) {
                                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                                html.setFile("data/html/clanHallManager/functions-cancel.htm");
                                html.replace("%apply%", "deco curtains 0");
                                html.replace("%objectId%", getObjectId());
                                player.sendPacket(html);
                            } else if (val.equalsIgnoreCase("fixtures_cancel")) {
                                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                                html.setFile("data/html/clanHallManager/functions-cancel.htm");
                                html.replace("%apply%", "deco fixtures 0");
                                html.replace("%objectId%", getObjectId());
                                player.sendPacket(html);
                            } else if (val.equalsIgnoreCase("edit_curtains")) {
                                int cost;
                                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                                html.setFile("data/html/clanHallManager/functions-apply.htm");
                                html.replace("%name%", "Curtains (Decoration)");
                                int stage = Integer.parseInt(st.nextToken());
                                switch (stage) {
                                    case 1:
                                        cost = Config.CH_CURTAIN1_FEE;
                                        break;
                                    default:
                                        cost = Config.CH_CURTAIN2_FEE;
                                        break;
                                }
                                html.replace("%cost%", cost + "</font> adenas / " + cost + " day</font>)");
                                html.replace("%use%", "These curtains can be used to decorate the clan hall.");
                                html.replace("%apply%", "deco curtains " + stage);
                                html.replace("%objectId%", getObjectId());
                                player.sendPacket(html);
                            } else if (val.equalsIgnoreCase("edit_fixtures")) {
                                int cost;
                                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                                html.setFile("data/html/clanHallManager/functions-apply.htm");
                                html.replace("%name%", "Front Platform (Decoration)");
                                int stage = Integer.parseInt(st.nextToken());
                                switch (stage) {
                                    case 1:
                                        cost = Config.CH_FRONT1_FEE;
                                        break;
                                    default:
                                        cost = Config.CH_FRONT2_FEE;
                                        break;
                                }
                                html.replace("%cost%", cost + "</font> adenas / " + cost + " day</font>)");
                                html.replace("%use%", "Used to decorate the clan hall.");
                                html.replace("%apply%", "deco fixtures " + stage);
                                html.replace("%objectId%", getObjectId());
                                player.sendPacket(html);
                            } else if (val.equalsIgnoreCase("curtains")) {
                                int fee;
                                val = st.nextToken();
                                int lvl = Integer.parseInt(val);
                                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                                ClanHallFunction chf = this._clanHall.getFunction(8);
                                if (chf != null && chf.getLvl() == lvl) {
                                    html.setFile("data/html/clanHallManager/functions-used.htm");
                                    html.replace("%val%", "Stage " + val);
                                    html.replace("%objectId%", getObjectId());
                                    player.sendPacket(html);
                                    return;
                                }
                                html.setFile("data/html/clanHallManager/functions-apply_confirmed.htm");
                                switch (lvl) {
                                    case 0:
                                        fee = 0;
                                        html.setFile("data/html/clanHallManager/functions-cancel_confirmed.htm");
                                        break;
                                    case 1:
                                        fee = Config.CH_CURTAIN1_FEE;
                                        break;
                                    default:
                                        fee = Config.CH_CURTAIN2_FEE;
                                        break;
                                }
                                if (!this._clanHall.updateFunctions(player, 8, lvl, fee, Config.CH_CURTAIN_FEE_RATIO)) {
                                    html.setFile("data/html/clanHallManager/low_adena.htm");
                                } else {
                                    revalidateDeco(player);
                                }
                                html.replace("%objectId%", getObjectId());
                                player.sendPacket(html);
                            } else if (val.equalsIgnoreCase("fixtures")) {
                                int fee;
                                val = st.nextToken();
                                int lvl = Integer.parseInt(val);
                                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                                ClanHallFunction chf = this._clanHall.getFunction(7);
                                if (chf != null && chf.getLvl() == lvl) {
                                    html.setFile("data/html/clanHallManager/functions-used.htm");
                                    html.replace("%val%", "Stage " + val);
                                    html.replace("%objectId%", getObjectId());
                                    player.sendPacket(html);
                                    return;
                                }
                                html.setFile("data/html/clanHallManager/functions-apply_confirmed.htm");
                                switch (lvl) {
                                    case 0:
                                        fee = 0;
                                        html.setFile("data/html/clanHallManager/functions-cancel_confirmed.htm");
                                        break;
                                    case 1:
                                        fee = Config.CH_FRONT1_FEE;
                                        break;
                                    default:
                                        fee = Config.CH_FRONT2_FEE;
                                        break;
                                }
                                if (!this._clanHall.updateFunctions(player, 7, lvl, fee, Config.CH_FRONT_FEE_RATIO)) {
                                    html.setFile("data/html/clanHallManager/low_adena.htm");
                                } else {
                                    revalidateDeco(player);
                                }
                                html.replace("%objectId%", getObjectId());
                                player.sendPacket(html);
                            }
                        } else {
                            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                            html.setFile("data/html/clanHallManager/deco.htm");
                            ClanHallFunction chfCurtains = this._clanHall.getFunction(8);
                            if (chfCurtains != null) {
                                html.replace("%curtain%", "- Stage " + chfCurtains.getLvl() + "</font> (<font color=\"FFAABB\">" + chfCurtains.getLease() + "</font> adenas / " + Config.CH_CURTAIN_FEE_RATIO / 86400000L + " day)");
                                html.replace("%curtain_period%", "Next fee at " + (new SimpleDateFormat("dd-MM-yyyy HH:mm")).format(chfCurtains.getEndTime()));
                                html.replace("%change_curtain%", "[<a action=\"bypass -h npc_%objectId%_manage deco curtains_cancel\">Remove</a>][<a action=\"bypass -h npc_%objectId%_manage deco edit_curtains 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage deco edit_curtains 2\">Level 2</a>]");
                            } else {
                                html.replace("%curtain%", "none");
                                html.replace("%curtain_period%", "none");
                                html.replace("%change_curtain%", "[<a action=\"bypass -h npc_%objectId%_manage deco edit_curtains 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage deco edit_curtains 2\">Level 2</a>]");
                            }
                            ClanHallFunction chfPlateform = this._clanHall.getFunction(7);
                            if (chfPlateform != null) {
                                html.replace("%fixture%", "- Stage " + chfPlateform.getLvl() + "</font> (<font color=\"FFAABB\">" + chfPlateform.getLease() + "</font> adenas / " + Config.CH_FRONT_FEE_RATIO / 86400000L + " day)");
                                html.replace("%fixture_period%", "Next fee at " + (new SimpleDateFormat("dd-MM-yyyy HH:mm")).format(chfPlateform.getEndTime()));
                                html.replace("%change_fixture%", "[<a action=\"bypass -h npc_%objectId%_manage deco fixtures_cancel\">Remove</a>][<a action=\"bypass -h npc_%objectId%_manage deco edit_fixtures 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage deco edit_fixtures 2\">Level 2</a>]");
                            } else {
                                html.replace("%fixture%", "none");
                                html.replace("%fixture_period%", "none");
                                html.replace("%change_fixture%", "[<a action=\"bypass -h npc_%objectId%_manage deco edit_fixtures 1\">Level 1</a>][<a action=\"bypass -h npc_%objectId%_manage deco edit_fixtures 2\">Level 2</a>]");
                            }
                            html.replace("%objectId%", getObjectId());
                            player.sendPacket(html);
                        }
                    } else if (val.equalsIgnoreCase("back")) {
                        showChatWindow(player);
                    } else {
                        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                        html.setFile("data/html/clanHallManager/manage.htm");
                        html.replace("%objectId%", getObjectId());
                        player.sendPacket(html);
                    }
                } else {
                    NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                    html.setFile("data/html/clanHallManager/not_authorized.htm");
                    html.replace("%objectId%", getObjectId());
                    player.sendPacket(html);
                }
            } else if (actualCommand.equalsIgnoreCase("support")) {
                ClanHallFunction chf = this._clanHall.getFunction(6);
                if (chf == null || chf.getLvl() == 0)
                    return;
                if (player.isCursedWeaponEquipped()) {
                    player.sendMessage("The wielder of a cursed weapon cannot receive outside heals or buffs");
                    return;
                }
                setTarget(player);
                try {
                    int id = Integer.parseInt(val);
                    int lvl = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 0;
                    L2Skill skill = SkillTable.getInstance().getInfo(id, lvl);
                    if (skill.getSkillType() == L2SkillType.SUMMON) {
                        player.doSimultaneousCast(skill);
                    } else if ((skill.getMpConsume() + skill.getMpInitialConsume()) <= getCurrentMp()) {
                        doCast(skill);
                    } else {
                        NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());
                        npcHtmlMessage.setFile("data/html/clanHallManager/support-no_mana.htm");
                        npcHtmlMessage.replace("%mp%", (int) getCurrentMp());
                        npcHtmlMessage.replace("%objectId%", getObjectId());
                        player.sendPacket(npcHtmlMessage);
                        return;
                    }
                    NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                    html.setFile("data/html/clanHallManager/support-done.htm");
                    html.replace("%mp%", (int) getCurrentMp());
                    html.replace("%objectId%", getObjectId());
                    player.sendPacket(html);
                } catch (Exception e) {
                    player.sendMessage("Invalid skill, contact your server support.");
                }
            } else if (actualCommand.equalsIgnoreCase("list_back")) {
                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                html.setFile("data/html/clanHallManager/chamberlain.htm");
                html.replace("%npcname%", getName());
                html.replace("%objectId%", getObjectId());
                player.sendPacket(html);
            } else if (actualCommand.equalsIgnoreCase("support_back")) {
                ClanHallFunction chf = this._clanHall.getFunction(6);
                if (chf == null || chf.getLvl() == 0)
                    return;
                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                html.setFile("data/html/clanHallManager/support" + this._clanHall.getFunction(6).getLvl() + ".htm");
                html.replace("%mp%", (int) getStatus().getCurrentMp());
                html.replace("%objectId%", getObjectId());
                player.sendPacket(html);
            } else if (actualCommand.equalsIgnoreCase("goto")) {
                TeleportLocation list = TeleportLocationData.getInstance().getTeleportLocation(Integer.parseInt(val));
                if (list != null && player.reduceAdena("Teleport", list.getPrice(), this, true))
                    player.teleportTo(list, 0);
                player.sendPacket(ActionFailed.STATIC_PACKET);
            } else if (actualCommand.equalsIgnoreCase("WithdrawC")) {
                player.sendPacket(ActionFailed.STATIC_PACKET);
                if ((player.getClanPrivileges() & 0x8) != 8) {
                    player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_USE_CLAN_WAREHOUSE);
                    return;
                }
                if (player.getClan().getLevel() == 0) {
                    player.sendPacket(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE);
                } else {
                    player.setActiveWarehouse(player.getClan().getWarehouse());
                    player.sendPacket(new WarehouseWithdrawList(player, 2));
                }
            } else if (actualCommand.equalsIgnoreCase("DepositC")) {
                player.sendPacket(ActionFailed.STATIC_PACKET);
                if (player.getClan() != null)
                    if (player.getClan().getLevel() == 0) {
                        player.sendPacket(SystemMessageId.ONLY_LEVEL_1_CLAN_OR_HIGHER_CAN_USE_WAREHOUSE);
                    } else {
                        player.setActiveWarehouse(player.getClan().getWarehouse());
                        player.tempInventoryDisable();
                        player.sendPacket(new WarehouseDepositList(player, 2));
                    }
            }
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    public void showChatWindow(Player player) {
        player.sendPacket(ActionFailed.STATIC_PACKET);
        String filename = "data/html/clanHallManager/chamberlain-no.htm";
        int condition = validateCondition(player);
        if (condition == 3)
            filename = "data/html/clanHallManager/chamberlain.htm";
        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
        html.setFile(filename);
        html.replace("%objectId%", getObjectId());
        player.sendPacket(html);
    }

    protected int validateCondition(Player player) {
        if (this._clanHall == null)
            return 1;
        if (player.getClan() != null) {
            if (this._clanHall.getOwnerId() == player.getClanId())
                return 3;
            return 0;
        }
        return 1;
    }

    private void revalidateDeco(Player player) {
        this._clanHall.getZone().broadcastPacket(new ClanHallDecoration(this._clanHall));
    }
}
