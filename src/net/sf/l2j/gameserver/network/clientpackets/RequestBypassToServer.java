package net.sf.l2j.gameserver.network.clientpackets;

import enginemods.main.EngineModsManager;
import mods.autofarm.AutofarmPlayerRoutine;
import mods.dressme.DressMeData;
import mods.dressme.SkinPackage;
import mods.teleportInterface.TeleLocation;
import mods.teleportInterface.TeleportLocationDataGK;
import net.sf.l2j.Config;
import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.communitybbs.CommunityBoard;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.data.manager.HeroManager;
import net.sf.l2j.gameserver.data.xml.AdminData;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.enums.items.WeaponType;
import net.sf.l2j.gameserver.handler.*;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.model.olympiad.OlympiadManager;
import net.sf.l2j.gameserver.network.FloodProtectors;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.*;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class RequestBypassToServer extends L2GameClientPacket {
    private static final Logger GMAUDIT_LOG = Logger.getLogger("gmaudit");
    private static final String ACTIVED = "<font color=00FF00>STARTED</font>";
    private static final String DESATIVED = "<font color=FF0000>STOPPED</font>";
    private static final String STOP = "STOP";
    private static final String START = "START";
    static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
    private String _command;

    public static void showAutoFarm(Player activeChar) {
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setFile("data/html/mods/menu/AutoFarm.htm");
        html.replace("%player%", activeChar.getName());
        html.replace("%page%", StringUtil.formatNumber((activeChar.getPage() + 1)));
        html.replace("%heal%", StringUtil.formatNumber(activeChar.getHealPercent()));
        html.replace("%radius%", StringUtil.formatNumber(activeChar.getRadius()));
        html.replace("%summonSkill%", StringUtil.formatNumber(activeChar.getSummonSkillPercent()));
        html.replace("%hpPotion%", StringUtil.formatNumber(activeChar.getHpPotionPercentage()));
        html.replace("%mpPotion%", StringUtil.formatNumber(activeChar.getMpPotionPercentage()));
        html.replace("%noBuff%", activeChar.isNoBuffProtected() ? "back=L2UI.CheckBox_checked fore=L2UI.CheckBox_checked" : "back=L2UI.CheckBox fore=L2UI.CheckBox");
        html.replace("%summonAtk%", activeChar.isSummonAttack() ? "back=L2UI.CheckBox_checked fore=L2UI.CheckBox_checked" : "back=L2UI.CheckBox fore=L2UI.CheckBox");
        html.replace("%antiKs%", activeChar.isAntiKsProtected() ? "back=L2UI.CheckBox_checked fore=L2UI.CheckBox_checked" : "back=L2UI.CheckBox fore=L2UI.CheckBox");
        html.replace("%autofarm%", activeChar.isAutoFarm() ? "<font color=00FF00>STARTED</font>" : "<font color=FF0000>STOPPED</font>");
        html.replace("%button%", activeChar.isAutoFarm() ? "STOP" : "START");
        activeChar.sendPacket(html);
    }

    private static int extractLastInteger(String input) {
        String[] parts = input.split("\\.");
        if (parts.length > 0) {
            String lastPart = parts[parts.length - 1];
            try {
                return Integer.parseInt(lastPart);
            } catch (NumberFormatException ignored) {
            }
        }
        return -1;
    }

    private static boolean isNoble(Player player, int teleportId) {
        int[] nobleZones = {
                154754, 154761, 154755, 154756, 154757, 154758, 154759, 154760, 159761, 154762,
                154768, 154763, 154764, 154765, 154767};
        for (int zone : nobleZones) {
            if (teleportId == zone && !player.isNoble())
                return false;
        }
        return true;
    }

    private static boolean shouldChargePrice(int teleportId) {
        int[] chargeAlwaysIds = {151782, 151791, 151785, 151787, 151786, 151783, 151788, 151784};
        for (int id : chargeAlwaysIds) {
            if (teleportId == id)
                return true;
        }
        return false;
    }

    public static boolean checkallowed(Player activeChar) {
        String msg = null;
        if (activeChar.isSitting()) {
            msg = "No Puedes Usar El Teleport Sentado";
        } else if (activeChar.isDead()) {
            msg = "No Puedes Usar El Teleport Muerto";
        } else if (activeChar.getPvpFlag() > 0) {
            msg = "Estas En Modo Flag No Puedes Teleport";
        } else if (activeChar.getKarma() > 0) {
            msg = "Estas En Modo Pk No Puedes Teleport";
        } else if (activeChar.isInCombat()) {
            msg = "No Puedes Usar El Teleport En Combate";
        } else if (activeChar.isInDuel()) {
            msg = "No Puedes Usar El Teleport En Duelos";
        } else if (activeChar.isInOlympiadMode()) {
            msg = "No Puedes Usar El Teleport En Olympiadas";
        } else if (activeChar.isInJail()) {
            msg = "No Puedes Usar El Teleport En Jail";
        }
        if (msg != null)
            activeChar.sendMessage(msg);
        return (msg == null);
    }

    public static String getItemNameById(int itemId) {
        Item item = ItemTable.getInstance().getTemplate(itemId);
        String itemName = "NoName";
        if (itemId != 0)
            itemName = item.getName();
        return itemName;
    }

    public static void showDressMeMainPage(Player player) {
        NpcHtmlMessage htm = new NpcHtmlMessage(1);
        String text = HtmCache.getInstance().getHtm("data/html/dressme/index.htm");
        htm.setHtml(text);
        htm.replace("%time%", sdf.format(new Date(System.currentTimeMillis())));
        htm.replace("%dat%", (new SimpleDateFormat("dd/MM/yyyy")).format(new Date(System.currentTimeMillis())));
        player.sendPacket(htm);
    }

    private static void showSkinList(Player player, String type, int page) {
        NpcHtmlMessage html = new NpcHtmlMessage(1);
        html.setFile("data/html/dressme/allskins.htm");
        html.replace("%time%", sdf.format(new Date(System.currentTimeMillis())));
        html.replace("%dat%", (new SimpleDateFormat("dd/MM/yyyy")).format(new Date(System.currentTimeMillis())));
        int ITEMS_PER_PAGE = 8;
        int myPage = 1;
        int i = 0;
        int shown = 0;
        boolean hasMore = false;
        int itemId = 0;
        StringBuilder sb = new StringBuilder();
        List<SkinPackage> tempList = switch (type.toLowerCase()) {
            case "armor" ->
                    DressMeData.getInstance().getArmorSkinOptions().values().stream().filter(s -> !player.hasArmorSkin(s.getId())).collect(Collectors.toList());
            case "weapon" ->
                    DressMeData.getInstance().getWeaponSkinOptions().values().stream().filter(s -> !player.hasWeaponSkin(s.getId())).collect(Collectors.toList());
            case "hair" ->
                    DressMeData.getInstance().getHairSkinOptions().values().stream().filter(s -> !player.hasHairSkin(s.getId())).collect(Collectors.toList());
            case "face" ->
                    DressMeData.getInstance().getFaceSkinOptions().values().stream().filter(s -> !player.hasFaceSkin(s.getId())).collect(Collectors.toList());
            case "shield" ->
                    DressMeData.getInstance().getShieldSkinOptions().values().stream().filter(s -> !player.hasShieldSkin(s.getId())).collect(Collectors.toList());
            default -> null;
        };
        if (tempList != null && !tempList.isEmpty())
            for (SkinPackage sp : tempList) {
                if (sp == null)
                    continue;
                if (shown == 8) {
                    hasMore = true;
                    break;
                }
                if (myPage != page) {
                    i++;
                    if (i == 8) {
                        myPage++;
                        i = 0;
                    }
                    continue;
                }
                if (shown == 8) {
                    hasMore = true;
                    break;
                }
                itemId = switch (type.toLowerCase()) {
                    case "armor" -> sp.getChestId();
                    case "weapon" -> sp.getWeaponId();
                    case "hair" -> sp.getHairId();
                    case "face" -> sp.getFaceId();
                    case "shield" -> sp.getShieldId();
                    default -> itemId;
                };
                sb.append("<table border=0 cellspacing=0 cellpadding=2 height=36><tr>");
                sb.append("<td width=32 align=center><button width=32 height=32 back=").append(Item.getItemIcon(itemId)).append(" fore=").append(Item.getItemIcon(itemId)).append("></td>");
                sb.append("<td width=124>").append(sp.getName()).append("<br1> <font color=999999>Price:</font> <font color=339966>").append(Item.getItemNameById(sp.getPriceId())).append("</font> (<font color=LEVEL>").append(sp.getPriceCount()).append("</font>)</td>");
                sb.append("<td align=center width=65><button value=\"Buy\" action=\"bypass -h dressme ").append(page).append(" buyskin  ").append(sp.getId()).append(" ").append(type).append(" ").append(itemId).append("\" width=65 height=19 back=L2UI_ch3.smallbutton2_over fore=L2UI_ch3.smallbutton2></td>");
                sb.append("<td align=center width=65><button value=\"Try\" action=\"bypass -h dressme ").append(page).append(" tryskin  ").append(sp.getId()).append(" ").append(type).append("\" width=65 height=19 back=L2UI_ch3.smallbutton2_over fore=L2UI_ch3.smallbutton2></td>");
                sb.append("</tr></table>");
                sb.append("<img src=\"L2UI.Squaregray\" width=\"300\" height=\"1\">");
                shown++;
            }
        sb.append("<table width=300><tr>");
        sb.append("<td align=center width=70>").append((page > 1) ? ("<button value=\"< PREV\" action=\"bypass -h dressme " + (page - 1) + " skinlist " + type + "\" width=65 height=19 back=L2UI_ch3.smallbutton2_over fore=L2UI_ch3.smallbutton2>") : "").append("</td>");
        sb.append("<td align=center width=140>Page: ").append(page).append("</td>");
        sb.append("<td align=center width=70>").append(hasMore ? ("<button value=\"NEXT >\" action=\"bypass -h dressme " + page + 1 + " skinlist " + type + "\" width=65 height=19 back=L2UI_ch3.smallbutton2_over fore=L2UI_ch3.smallbutton2>") : "").append("</td>");
        sb.append("</tr></table>");
        html.replace("%showList%", sb.toString());
        player.sendPacket(html);
    }

    private static void showMySkinList(Player player, int page) {
        NpcHtmlMessage html = new NpcHtmlMessage(1);
        html.setFile("data/html/dressme/myskins.htm");
        html.replace("%time%", sdf.format(new Date(System.currentTimeMillis())));
        html.replace("%dat%", (new SimpleDateFormat("dd/MM/yyyy")).format(new Date(System.currentTimeMillis())));
        int ITEMS_PER_PAGE = 8;
        int itemId = 0;
        int myPage = 1;
        int i = 0;
        int shown = 0;
        boolean hasMore = false;
        StringBuilder sb = new StringBuilder();
        List<SkinPackage> armors = DressMeData.getInstance().getArmorSkinOptions().values().stream().filter(s -> player.hasArmorSkin(s.getId())).toList();
        List<SkinPackage> weapons = DressMeData.getInstance().getWeaponSkinOptions().values().stream().filter(s -> player.hasWeaponSkin(s.getId())).toList();
        List<SkinPackage> hairs = DressMeData.getInstance().getHairSkinOptions().values().stream().filter(s -> player.hasHairSkin(s.getId())).toList();
        List<SkinPackage> shield = DressMeData.getInstance().getShieldSkinOptions().values().stream().filter(s -> player.hasShieldSkin(s.getId())).toList();
        List<SkinPackage> list = Stream.concat(armors.stream(), weapons.stream()).toList();
        List<SkinPackage> list2 = Stream.concat(hairs.stream(), shield.stream()).toList();
        List<SkinPackage> allLists = Stream.concat(list.stream(), list2.stream()).toList();
        if (!allLists.isEmpty())
            for (SkinPackage sp : allLists) {
                if (sp == null)
                    continue;
                if (shown == 8) {
                    hasMore = true;
                    break;
                }
                if (myPage != page) {
                    i++;
                    if (i == 8) {
                        myPage++;
                        i = 0;
                    }
                    continue;
                }
                if (shown == 8) {
                    hasMore = true;
                    break;
                }
                itemId = switch (sp.getType().toLowerCase()) {
                    case "armor" -> sp.getChestId();
                    case "weapon" -> sp.getWeaponId();
                    case "hair" -> sp.getHairId();
                    case "face" -> sp.getFaceId();
                    case "shield" -> sp.getShieldId();
                    default -> itemId;
                };
                sb.append("<table border=0 cellspacing=0 cellpadding=2 height=36><tr>");
                sb.append("<td width=32 align=center><button width=32 height=32 back=").append(Item.getItemIcon(itemId)).append(" fore=").append(Item.getItemIcon(itemId)).append("></td>");
                sb.append("<td width=124>").append(sp.getName()).append("</td>");
                sb.append("<td align=center width=65><button value=\"Equip\" action=\"bypass -h dressme ").append(page).append(" setskin ").append(sp.getId()).append(" ").append(sp.getType()).append(" ").append(itemId).append("\" width=65 height=19 back=L2UI_ch3.smallbutton2_over fore=L2UI_ch3.smallbutton2></td>");
                sb.append("<td align=center width=65><button value=\"Remove\" action=\"bypass -h dressme ").append(page).append(" clean ").append(sp.getType()).append("\" width=65 height=19 back=L2UI_ch3.smallbutton2_over fore=L2UI_ch3.smallbutton2></td>");
                sb.append("</tr></table>");
                sb.append("<img src=\"L2UI.Squaregray\" width=\"300\" height=\"1\">");
                shown++;
            }
        sb.append("<table width=300><tr>");
        sb.append("<td align=center width=70>").append((page > 1) ? ("<button value=\"< PREV\" action=\"bypass -h dressme " + (page - 1) + " myskinlist\" width=65 height=19 back=L2UI_ch3.smallbutton2_over fore=L2UI_ch3.smallbutton2>") : "").append("</td>");
        sb.append("<td align=center width=140>Page: ").append(page).append("</td>");
        sb.append("<td align=center width=70>").append(hasMore ? ("<button value=\"NEXT >\" action=\"bypass -h dressme " + page + 1 + " myskinlist\" width=65 height=19 back=L2UI_ch3.smallbutton2_over fore=L2UI_ch3.smallbutton2>") : "").append("</td>");
        sb.append("</tr></table>");
        html.replace("%showList%", sb.toString());
        player.sendPacket(html);
    }

    public static boolean checkWeapons(Player player, ItemInstance skin, WeaponType weapon1, WeaponType weapon2) {
        return player.getActiveWeaponItem().getItemType() != weapon1 || skin.getItem().getItemType() == weapon2;
    }

    protected void readImpl() {
        this._command = readS();
    }

    protected void runImpl() {
        if (this._command.isEmpty())
            return;
        if (!FloodProtectors.performAction(getClient(), FloodProtectors.Action.SERVER_BYPASS))
            return;
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        AutofarmPlayerRoutine bot = player.getBot();
        if (this._command.startsWith("admin_")) {
            if (EngineModsManager.onVoiced(player, this._command))
                return;
            String command = this._command.split(" ")[0];
            IAdminCommandHandler ach = AdminCommandHandler.getInstance().getHandler(command);
            if (ach == null) {
                if (player.isGM())
                    player.sendMessage("The command " + command.substring(6) + " doesn't exist.");
                LOGGER.warn("No handler registered for admin command '{}'.", command);
                return;
            }
            if (!AdminData.getInstance().hasAccess(command, player.getAccessLevel())) {
                player.sendMessage("You don't have the access rights to use this command.");
                LOGGER.warn("{} tried to use admin command '{}' without proper Access Level.", player.getName(), command);
                return;
            }
            if (Config.GMAUDIT)
                GMAUDIT_LOG.info(player.getName() + " [" + player.getName() + "] used '" + player.getObjectId() + "' command on: " + this._command);
            ach.useAdminCommand(this._command, player);
        } else if (this._command.startsWith("player_help ")) {
            String path = this._command.substring(12);
            if (path.indexOf("..") != -1)
                return;
            StringTokenizer st = new StringTokenizer(path);
            String[] cmd = st.nextToken().split("#");
            NpcHtmlMessage html = new NpcHtmlMessage(0);
            html.setFile("data/html/help/" + cmd[0]);
            if (cmd.length > 1) {
                int itemId = Integer.parseInt(cmd[1]);
                html.setItemId(itemId);
                if (itemId == 7064 && cmd[0].equalsIgnoreCase("lidias_diary/7064-16.htm")) {
                    QuestState qs = player.getQuestState("Q023_LidiasHeart");
                    if (qs != null && qs.getInt("cond") == 5 && qs.getInt("diary") == 0)
                        qs.set("diary", "1");
                }
            }
            html.disableValidation();
            player.sendPacket(html);
        } else if (this._command.startsWith("npc_")) {
            String id;
            if (!player.validateBypass(this._command))
                return;
            int endOfId = this._command.indexOf('_', 5);
            if (endOfId > 0) {
                id = this._command.substring(4, endOfId);
            } else {
                id = this._command.substring(4);
            }
            try {
                WorldObject object = World.getInstance().getObject(Integer.parseInt(id));
                if (object != null && object instanceof Npc && endOfId > 0 && ((Npc) object).canInteract(player))
                    ((Npc) object).onBypassFeedback(player, this._command.substring(endOfId + 1));
                player.sendPacket(ActionFailed.STATIC_PACKET);
            } catch (NumberFormatException ignored) {
            }
        } else if (this._command.startsWith("manor_menu_select?")) {
            WorldObject object = player.getTarget();
            if (object instanceof Npc)
                ((Npc) object).onBypassFeedback(player, this._command);
        } else if (this._command.startsWith("bbs_") || this._command.startsWith("_bbs") || this._command.startsWith("_friend") || this._command.startsWith("_mail") || this._command.startsWith("_block")) {
            CommunityBoard.getInstance().handleCommands(getClient(), this._command);
        } else if (this._command.startsWith("Quest ")) {
            if (!player.validateBypass(this._command))
                return;
            String[] str = this._command.substring(6).trim().split(" ", 2);
            if (str.length == 1) {
                player.processQuestEvent(str[0], "");
            } else {
                player.processQuestEvent(str[0], str[1]);
            }
        } else if (this._command.startsWith("_match")) {
            String params = this._command.substring(this._command.indexOf("?") + 1);
            StringTokenizer st = new StringTokenizer(params, "&");
            int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
            int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
            int heroid = HeroManager.getInstance().getHeroByClass(heroclass);
            if (heroid > 0)
                HeroManager.getInstance().showHeroFights(player, heroclass, heroid, heropage);
        } else if (this._command.startsWith("_diary")) {
            String params = this._command.substring(this._command.indexOf("?") + 1);
            StringTokenizer st = new StringTokenizer(params, "&");
            int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
            int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
            int heroid = HeroManager.getInstance().getHeroByClass(heroclass);
            if (heroid > 0)
                HeroManager.getInstance().showHeroDiary(player, heroclass, heroid, heropage);
        } else if (this._command.startsWith("arenachange")) {
            boolean isManager = player.getCurrentFolk() instanceof net.sf.l2j.gameserver.model.actor.instance.OlympiadManagerNpc;
            if (!isManager)
                if (!player.isInObserverMode() || player.isInOlympiadMode() || player.getOlympiadGameId() < 0)
                    return;
            if (OlympiadManager.getInstance().isRegisteredInComp(player)) {
                player.sendPacket(SystemMessageId.WHILE_YOU_ARE_ON_THE_WAITING_LIST_YOU_ARE_NOT_ALLOWED_TO_WATCH_THE_GAME);
                return;
            }
            int arenaId = Integer.parseInt(this._command.substring(12).trim());
            player.enterOlympiadObserverMode(arenaId);
        } else if (this._command.startsWith("droplist")) {
            StringTokenizer st = new StringTokenizer(this._command, " ");
            st.nextToken();
            int npcId = Integer.parseInt(st.nextToken());
            int page = st.hasMoreTokens() ? Integer.parseInt(st.nextToken()) : 1;
            if (st.hasMoreTokens())
                player.ignored(Integer.parseInt(st.nextToken()));
            Npc.sendNpcDrop(player, npcId, page);
        } else if (this._command.startsWith("bp_")) {
            String command = this._command.split(" ")[0];
            IBypassHandler bh = BypassHandler.getInstance().getBypassHandler(command);
            if (bh == null) {
                LOGGER.warn("No handler registered for bypass '" + command + "'");
                return;
            }
            bh.handleBypass(this._command, player);
        } else if (this._command.startsWith("voiced_")) {
            String command = this._command.split(" ")[0];
            IVoicedCommandHandler ach = VoicedCommandHandler.getInstance().getVoicedCommandHandler(this._command.substring(7));
            if (ach == null) {
                player.sendMessage("The command " + command.substring(7) + " does not exist!");
                LOGGER.warn("No handler registered for command '" + this._command + "'");
                return;
            }
            ach.useVoicedCommand(this._command.substring(7), player, null);
        } else if (this._command.startsWith("Engine")) {
            EngineModsManager.onEvent(player, this._command.replace("Engine ", ""));
        } else if (this._command.startsWith("BuffEngine_Dispel;")) {
            StringTokenizer st = new StringTokenizer(this._command, ";");
            st.nextToken();
            int intid = Integer.parseInt(st.nextToken());
            int level = Integer.parseInt(st.nextToken());
            player.dispelSkillEffect(intid, level);
        } else if (this._command.startsWith("_infosettings")) {
            showAutoFarm(player);
        } else if (this._command.startsWith("_autofarm")) {
            if (player.isAutoFarm()) {
                bot.stop();
                player.setAutoFarm(false);
            } else {
                bot.start();
                player.setAutoFarm(true);
            }
        }
        if (this._command.startsWith("_pageAutoFarm")) {
            StringTokenizer st = new StringTokenizer(this._command, " ");
            st.nextToken();
            try {
                String param = st.nextToken();
                if (param.startsWith("inc_page") || param.startsWith("dec_page")) {
                    int newPage;
                    if (param.startsWith("inc_page")) {
                        newPage = player.getPage() + 1;
                    } else {
                        newPage = player.getPage() - 1;
                    }
                    if (newPage >= 0 && newPage <= 9) {
                        String[] pageStrings = {"F1", "F2", "F3", "F4", "F5", "F6", "F7", "F8", "F9", "F10"};
                        player.setPage(newPage);
                        player.sendPacket(new ExShowScreenMessage("Auto Farm Skill Bar " + pageStrings[newPage], 3000, ExShowScreenMessage.SMPOS.TOP_CENTER, false));
                        player.saveAutoFarmSettings();
                        showAutoFarm(player);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (this._command.startsWith("_enableBuffProtect")) {
            player.setNoBuffProtection(!player.isNoBuffProtected());
            if (player.isNoBuffProtected()) {
                player.sendPacket(new ExShowScreenMessage("Auto Farm Buff Protect On", 3000, ExShowScreenMessage.SMPOS.TOP_CENTER, false));
            } else {
                player.sendPacket(new ExShowScreenMessage("Auto Farm Buff Protect Off", 3000, ExShowScreenMessage.SMPOS.TOP_CENTER, false));
            }
            player.saveAutoFarmSettings();
            showAutoFarm(player);
        }
        if (this._command.startsWith("_enableSummonAttack")) {
            player.setSummonAttack(!player.isSummonAttack());
            if (player.isSummonAttack()) {
                player.sendPacket(new SystemMessage(SystemMessageId.ACTIVATE_SUMMON_ACTACK));
                player.sendPacket(new ExShowScreenMessage("Auto Farm Summon Attack On", 3000, ExShowScreenMessage.SMPOS.TOP_CENTER, false));
            } else {
                player.sendPacket(new SystemMessage(SystemMessageId.DESACTIVATE_SUMMON_ACTACK));
                player.sendPacket(new ExShowScreenMessage("Auto Farm Summon Attack Off", 3000, ExShowScreenMessage.SMPOS.TOP_CENTER, false));
            }
            player.saveAutoFarmSettings();
            showAutoFarm(player);
        }
        if (this._command.startsWith("_enableRespectHunt")) {
            player.setAntiKsProtection(!player.isAntiKsProtected());
            if (player.isAntiKsProtected()) {
                player.sendPacket(new SystemMessage(SystemMessageId.ACTIVATE_RESPECT_HUNT));
                player.sendPacket(new ExShowScreenMessage("Respct Hunt On", 3000, ExShowScreenMessage.SMPOS.TOP_CENTER, false));
            } else {
                player.sendPacket(new SystemMessage(SystemMessageId.DESACTIVATE_RESPECT_HUNT));
                player.sendPacket(new ExShowScreenMessage("Respct Hunt Off", 3000, ExShowScreenMessage.SMPOS.TOP_CENTER, false));
            }
            player.saveAutoFarmSettings();
            showAutoFarm(player);
        }
        if (this._command.startsWith("_radiusAutoFarm")) {
            StringTokenizer st = new StringTokenizer(this._command, " ");
            st.nextToken();
            try {
                String param = st.nextToken();
                if (param.startsWith("inc_radius")) {
                    player.setRadius(player.getRadius() + 200);
                    player.sendPacket(new ExShowScreenMessage("Auto Farm Range: " + player.getRadius(), 3000, ExShowScreenMessage.SMPOS.TOP_CENTER, false));
                } else if (param.startsWith("dec_radius")) {
                    player.setRadius(player.getRadius() - 200);
                    player.sendPacket(new ExShowScreenMessage("Auto Farm Range: " + player.getRadius(), 3000, ExShowScreenMessage.SMPOS.TOP_CENTER, false));
                }
                player.saveAutoFarmSettings();
                showAutoFarm(player);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (this._command.startsWith("goto ")) {
            StringTokenizer st = new StringTokenizer(this._command, " ");
            st.nextToken();
            if (!player.isInsideZone(ZoneId.PEACE)) {
                player.sendMessage("You can't use Gatekeeper Interface outside peace zone.");
                return;
            }
            if (st.hasMoreTokens()) {
                int price;
                String targetLocation = st.nextToken();
                int teleportId = extractLastInteger(targetLocation);
                if (!checkallowed(player))
                    return;
                TeleLocation list = TeleportLocationDataGK.getInstance().getTeleportLocation(teleportId);
                if (list == null)
                    return;
                if (!isNoble(player, teleportId)) {
                    player.sendMessage("Solo Los Nobles Pueden Ir a Esta Zone");
                    return;
                }
                if (shouldChargePrice(teleportId) || player.getLevel() > 40) {
                    price = list.getPrice();
                } else {
                    price = 0;
                }
                if (player.destroyItemByItemId("Teleport", 57, price, player, true)) {
                    MagicSkillUse MSU = new MagicSkillUse(player, player, 2036, 1, 1, 0);
                    player.broadcastPacket(MSU);
                    player.teleportTo(list, 0);
                    player.sendPacket(new SystemMessage(SystemMessageId.WILL_BE_MOVED_INTERFACE));
                }
                player.sendPacket(ActionFailed.STATIC_PACKET);
            }
        }
        if (this._command.startsWith("RequestAutoShot: ShotID=")) {
            String[] parts = this._command.split("=");
            int shotId = Integer.parseInt(parts[1].split(" ")[0]);
            int bEnable = Integer.parseInt(parts[2].trim());
            if (!player.isInStoreMode() && player.getActiveRequester() == null && !player.isDead()) {
                ItemInstance item = player.getInventory().getItemByItemId(shotId);
                if (item == null)
                    return;
                if (bEnable == 1) {
                    if (shotId < 6535 || shotId > 6540)
                        if (shotId == 6645 || shotId == 6646 || shotId == 6647) {
                            if (player.getSummon() != null) {
                                if (shotId == 6645) {
                                    if (player.getSummon().getSoulShotsPerHit() > item.getCount()) {
                                        player.sendPacket(SystemMessageId.NOT_ENOUGH_SOULSHOTS_FOR_PET);
                                        return;
                                    }
                                } else if (player.getSummon().getSpiritShotsPerHit() > item.getCount()) {
                                    player.sendPacket(SystemMessageId.NOT_ENOUGH_SPIRITSHOTS_FOR_PET);
                                    return;
                                }
                                player.addAutoSoulShot(shotId);
                                player.sendPacket(new ExAutoSoulShot(shotId, bEnable));
                                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.USE_OF_S1_WILL_BE_AUTO).addItemName(shotId));
                                player.rechargeShots(true, true);
                                player.getSummon().rechargeShots(true, true);
                            } else {
                                player.sendPacket(SystemMessageId.NO_SERVITOR_CANNOT_AUTOMATE_USE);
                            }
                        } else {
                            player.addAutoSoulShot(shotId);
                            player.sendPacket(new ExAutoSoulShot(shotId, bEnable));
                            if (player.getActiveWeaponInstance() != null && item.getItem().getCrystalType() == player.getActiveWeaponItem().getCrystalType()) {
                                player.rechargeShots(true, true);
                            } else if ((shotId >= 2509 && shotId <= 2514) || (shotId >= 3947 && shotId <= 3952) || shotId == 5790) {
                                player.sendPacket(SystemMessageId.SPIRITSHOTS_GRADE_MISMATCH);
                            } else {
                                player.sendPacket(SystemMessageId.SOULSHOTS_GRADE_MISMATCH);
                            }
                            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.USE_OF_S1_WILL_BE_AUTO).addItemName(shotId));
                        }
                } else if (bEnable == 0) {
                    player.removeAutoSoulShot(shotId);
                    player.sendPacket(new ExAutoSoulShot(shotId, bEnable));
                    player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED).addItemName(shotId));
                }
            }
        } else if (this._command.startsWith("dressme")) {
            if (!Config.ALLOW_DRESS_ME_IN_OLY && player.isInOlympiadMode()) {
                player.sendMessage("DressMe can't be used on The Olympiad game.");
                return;
            }
            StringTokenizer st = new StringTokenizer(this._command, " ");
            st.nextToken();
            if (!st.hasMoreTokens()) {
                showDressMeMainPage(player);
                return;
            }
            int page = Integer.parseInt(st.nextToken());
            if (!st.hasMoreTokens()) {
                showDressMeMainPage(player);
                return;
            }
            String next = st.nextToken();
            if (next.startsWith("skinlist")) {
                String type = st.nextToken();
                showSkinList(player, type, page);
            } else if (next.startsWith("myskinlist")) {
                showMySkinList(player, page);
            }
            if (next.equals("clean")) {
                String type = st.nextToken();
                if (player.isTryingSkin()) {
                    player.sendMessage("You can't do this while trying a skin.");
                    player.sendPacket(new ExShowScreenMessage("You can't do this while trying a skin.", 2000, 2, false));
                    player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
                    showDressMeMainPage(player);
                    return;
                }
                switch (type.toLowerCase()) {
                    case "armor":
                        player.setArmorSkinOption(0);
                        break;
                    case "weapon":
                        player.setWeaponSkinOption(0);
                        break;
                    case "hair":
                        player.setHairSkinOption(0);
                        break;
                    case "face":
                        player.setFaceSkinOption(0);
                        break;
                    case "shield":
                        player.setShieldSkinOption(0);
                        break;
                }
                player.broadcastUserInfo();
                showMySkinList(player, page);
            } else if (next.startsWith("buyskin")) {
                ItemInstance skinWeapon, skinShield;
                if (!st.hasMoreTokens()) {
                    showDressMeMainPage(player);
                    return;
                }
                int skinId = Integer.parseInt(st.nextToken());
                String type = st.nextToken();
                int itemId = Integer.parseInt(st.nextToken());
                SkinPackage sp = null;
                switch (type.toLowerCase()) {
                    case "armor":
                        sp = DressMeData.getInstance().getArmorSkinsPackage(skinId);
                        break;
                    case "weapon":
                        sp = DressMeData.getInstance().getWeaponSkinsPackage(skinId);
                        if (player.getActiveWeaponItem() == null) {
                            player.sendMessage("You can't buy this skin without a weapon.");
                            player.sendPacket(new ExShowScreenMessage("You can't buy this skin without a weapon.", 2000, 2, false));
                            player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
                            showSkinList(player, type, page);
                            return;
                        }
                        skinWeapon = null;
                        if (ItemTable.getInstance().getTemplate(itemId) != null) {
                            skinWeapon = ItemTable.getInstance().createDummyItem(itemId);
                            if (!checkWeapons(player, skinWeapon, WeaponType.BOW, WeaponType.BOW) ||
                                    !checkWeapons(player, skinWeapon, WeaponType.SWORD, WeaponType.SWORD) ||
                                    !checkWeapons(player, skinWeapon, WeaponType.BLUNT, WeaponType.BLUNT) ||
                                    !checkWeapons(player, skinWeapon, WeaponType.DAGGER, WeaponType.DAGGER) ||
                                    !checkWeapons(player, skinWeapon, WeaponType.POLE, WeaponType.POLE) ||
                                    !checkWeapons(player, skinWeapon, WeaponType.DUAL, WeaponType.DUAL) ||
                                    !checkWeapons(player, skinWeapon, WeaponType.DUALFIST, WeaponType.DUALFIST) ||
                                    !checkWeapons(player, skinWeapon, WeaponType.BIGSWORD, WeaponType.BIGSWORD) ||
                                    !checkWeapons(player, skinWeapon, WeaponType.FIST, WeaponType.FIST) ||
                                    !checkWeapons(player, skinWeapon, WeaponType.BIGBLUNT, WeaponType.BIGBLUNT)) {
                                player.sendMessage("This skin is not suitable for your weapon type.");
                                player.sendPacket(new ExShowScreenMessage("This skin is not suitable for your weapon type.", 2000, 2, false));
                                player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
                                showSkinList(player, type, page);
                                return;
                            }
                        }
                        break;
                    case "hair":
                        sp = DressMeData.getInstance().getHairSkinsPackage(skinId);
                        break;
                    case "face":
                        sp = DressMeData.getInstance().getFaceSkinsPackage(skinId);
                        break;
                    case "shield":
                        sp = DressMeData.getInstance().getShieldSkinsPackage(skinId);
                        if (player.getActiveWeaponItem() == null) {
                            player.sendMessage("You can't buy this skin without a weapon.");
                            player.sendPacket(new ExShowScreenMessage("You can't buy this skin without a weapon.", 2000, 2, false));
                            player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
                            showSkinList(player, type, page);
                            return;
                        }
                        skinShield = null;
                        if (ItemTable.getInstance().getTemplate(itemId) != null) {
                            skinShield = ItemTable.getInstance().createDummyItem(itemId);
                            if (!checkWeapons(player, skinShield, WeaponType.BOW, WeaponType.BOW) ||
                                    !checkWeapons(player, skinShield, WeaponType.POLE, WeaponType.POLE) ||
                                    !checkWeapons(player, skinShield, WeaponType.DUAL, WeaponType.DUAL) ||
                                    !checkWeapons(player, skinShield, WeaponType.DUALFIST, WeaponType.DUALFIST) ||
                                    !checkWeapons(player, skinShield, WeaponType.BIGSWORD, WeaponType.BIGSWORD) ||
                                    !checkWeapons(player, skinShield, WeaponType.FIST, WeaponType.FIST) ||
                                    !checkWeapons(player, skinShield, WeaponType.BIGBLUNT, WeaponType.BIGBLUNT)) {
                                player.sendMessage("This skin is not suitable for your weapon type.");
                                player.sendPacket(new ExShowScreenMessage("This skin is not suitable for your weapon type.", 2000, 2, false));
                                player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
                                showSkinList(player, type, page);
                                return;
                            }
                        }
                        break;
                }
                if (sp == null) {
                    player.sendMessage("There is no such skin.");
                    player.sendPacket(new ExShowScreenMessage("There is no such skin.", 2000, 2, false));
                    player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
                    showSkinList(player, type, page);
                    return;
                }
                if (player.destroyItemByItemId("dressme", sp.getPriceId(), sp.getPriceCount(), player, true)) {
                    player.sendMessage("You have successfully purchased " + sp.getName() + " skin.");
                    player.sendPacket(new ExShowScreenMessage("You have successfully purchased " + sp.getName() + " skin.", 2000, 2, false));
                    switch (type.toLowerCase()) {
                        case "armor":
                            player.buyArmorSkin(skinId);
                            player.setArmorSkinOption(skinId);
                            break;
                        case "weapon":
                            player.buyWeaponSkin(skinId);
                            player.setWeaponSkinOption(skinId);
                            break;
                        case "hair":
                            player.buyHairSkin(skinId);
                            player.setHairSkinOption(skinId);
                            break;
                        case "face":
                            player.buyFaceSkin(skinId);
                            player.setFaceSkinOption(skinId);
                            break;
                        case "shield":
                            player.buyShieldSkin(skinId);
                            player.setShieldSkinOption(skinId);
                            break;
                    }
                    player.broadcastUserInfo();
                }
                showSkinList(player, type, page);
            } else if (next.startsWith("tryskin")) {
                int skinId = Integer.parseInt(st.nextToken());
                String type = st.nextToken();
                if (player.isTryingSkin()) {
                    player.sendMessage("You are already trying a skin.");
                    player.sendPacket(new ExShowScreenMessage("You are already trying a skin.", 2000, 2, false));
                    player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
                    showSkinList(player, type, page);
                    return;
                }
                player.setIsTryingSkin(true);
                int oldArmorSkinId = player.getArmorSkinOption();
                int oldWeaponSkinId = player.getWeaponSkinOption();
                int oldHairSkinId = player.getHairSkinOption();
                int oldFaceSkinId = player.getFaceSkinOption();
                int oldShieldSkinId = player.getShieldSkinOption();
                switch (type.toLowerCase()) {
                    case "armor":
                        player.setArmorSkinOption(skinId);
                        break;
                    case "weapon":
                        player.setWeaponSkinOption(skinId);
                        break;
                    case "hair":
                        player.setHairSkinOption(skinId);
                        break;
                    case "face":
                        player.setFaceSkinOption(skinId);
                        break;
                    case "shield":
                        player.setShieldSkinOption(skinId);
                        break;
                }
                player.broadcastUserInfo();
                showSkinList(player, type, page);
                ThreadPool.schedule(() -> {
                    switch (type.toLowerCase()) {
                        case "armor":
                            player.setArmorSkinOption(oldArmorSkinId);
                            break;
                        case "weapon":
                            player.setWeaponSkinOption(oldWeaponSkinId);
                            break;
                        case "hair":
                            player.setHairSkinOption(oldHairSkinId);
                            break;
                        case "face":
                            player.setFaceSkinOption(oldFaceSkinId);
                            break;
                        case "shield":
                            player.setShieldSkinOption(oldShieldSkinId);
                            break;
                    }
                    player.broadcastUserInfo();
                    player.setIsTryingSkin(false);
                }, 5000L);
            } else if (next.startsWith("setskin")) {
                ItemInstance skinWeapon, skinShield;
                int id = Integer.parseInt(st.nextToken());
                String type = st.nextToken();
                int itemId = Integer.parseInt(st.nextToken());
                if (player.isTryingSkin()) {
                    player.sendMessage("You can't do this while trying skins.");
                    player.sendPacket(new ExShowScreenMessage("You can't do this while trying skins.", 2000, 2, false));
                    player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
                    showMySkinList(player, page);
                    return;
                }
                if ((type.toLowerCase().contains("armor") && player.hasEquippedArmorSkin(String.valueOf(id))) || (type.toLowerCase().contains("weapon") && player.hasEquippedWeaponSkin(String.valueOf(id))) || (type
                        .toLowerCase().contains("hair") && player.hasEquippedHairSkin(String.valueOf(id))) || (type.toLowerCase().contains("face") && player.hasEquippedFaceSkin(String.valueOf(id)))) {
                    player.sendMessage("You are already equipped this skin.");
                    player.sendPacket(new ExShowScreenMessage("You are already equipped this skin.", 2000, 2, false));
                    player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
                    showMySkinList(player, page);
                    return;
                }
                switch (type.toLowerCase()) {
                    case "armor":
                        player.setArmorSkinOption(id);
                        break;
                    case "weapon":
                        if (player.getActiveWeaponItem() == null) {
                            player.sendMessage("You can't use this skin without a weapon.");
                            player.sendPacket(new ExShowScreenMessage("You can't use this skin without a weapon.", 2000, 2, false));
                            player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
                            showMySkinList(player, page);
                            return;
                        }
                        skinWeapon = null;
                        if (ItemTable.getInstance().getTemplate(itemId) != null) {
                            skinWeapon = ItemTable.getInstance().createDummyItem(itemId);
                            if (!checkWeapons(player, skinWeapon, WeaponType.BOW, WeaponType.BOW) ||
                                    !checkWeapons(player, skinWeapon, WeaponType.SWORD, WeaponType.SWORD) ||
                                    !checkWeapons(player, skinWeapon, WeaponType.BLUNT, WeaponType.BLUNT) ||
                                    !checkWeapons(player, skinWeapon, WeaponType.DAGGER, WeaponType.DAGGER) ||
                                    !checkWeapons(player, skinWeapon, WeaponType.POLE, WeaponType.POLE) ||
                                    !checkWeapons(player, skinWeapon, WeaponType.DUAL, WeaponType.DUAL) ||
                                    !checkWeapons(player, skinWeapon, WeaponType.DUALFIST, WeaponType.DUALFIST) ||
                                    !checkWeapons(player, skinWeapon, WeaponType.BIGSWORD, WeaponType.BIGSWORD) ||
                                    !checkWeapons(player, skinWeapon, WeaponType.FIST, WeaponType.FIST) ||
                                    !checkWeapons(player, skinWeapon, WeaponType.BIGBLUNT, WeaponType.BIGBLUNT)) {
                                player.sendMessage("This skin is not suitable for your weapon type.");
                                player.sendPacket(new ExShowScreenMessage("This skin is not suitable for your weapon type.", 2000, 2, false));
                                player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
                                showMySkinList(player, page);
                                return;
                            }
                            player.setWeaponSkinOption(id);
                        }
                        break;
                    case "hair":
                        player.setHairSkinOption(id);
                        break;
                    case "face":
                        player.setFaceSkinOption(id);
                        break;
                    case "shield":
                        if (player.getActiveWeaponItem() == null) {
                            player.sendMessage("You can't use this skin without a weapon.");
                            player.sendPacket(new ExShowScreenMessage("You can't use this skin without a weapon.", 2000, 2, false));
                            player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
                            showMySkinList(player, page);
                            return;
                        }
                        skinShield = null;
                        if (ItemTable.getInstance().getTemplate(itemId) != null) {
                            skinShield = ItemTable.getInstance().createDummyItem(itemId);
                            if (!checkWeapons(player, skinShield, WeaponType.BOW, WeaponType.BOW) ||
                                    !checkWeapons(player, skinShield, WeaponType.POLE, WeaponType.POLE) ||
                                    !checkWeapons(player, skinShield, WeaponType.DUAL, WeaponType.DUAL) ||
                                    !checkWeapons(player, skinShield, WeaponType.DUALFIST, WeaponType.DUALFIST) ||
                                    !checkWeapons(player, skinShield, WeaponType.BIGSWORD, WeaponType.BIGSWORD) ||
                                    !checkWeapons(player, skinShield, WeaponType.FIST, WeaponType.FIST) ||
                                    !checkWeapons(player, skinShield, WeaponType.BIGBLUNT, WeaponType.BIGBLUNT)) {
                                player.sendMessage("This skin is not suitable for your weapon type.");
                                player.sendPacket(new ExShowScreenMessage("This skin is not suitable for your weapon type.", 2000, 2, false));
                                player.sendPacket(new PlaySound("ItemSound3.sys_impossible"));
                                showMySkinList(player, page);
                                return;
                            }
                            player.setShieldSkinOption(id);
                        }
                        break;
                }
                player.broadcastUserInfo();
                showMySkinList(player, page);
            }
        }
    }
}
