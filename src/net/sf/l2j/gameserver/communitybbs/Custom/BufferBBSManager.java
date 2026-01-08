package net.sf.l2j.gameserver.communitybbs.Custom;

import net.sf.l2j.Config;
import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.gameserver.communitybbs.Manager.BaseBBSManager;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.data.manager.BufferManager;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;

import java.util.*;

public class BufferBBSManager extends BaseBBSManager {
    int[] magebuffs = new int[]{
            1204, 1048, 1045, 1040, 1035, 1085, 1303, 1304, 1243, 1036,
            1087, 1059, 1078, 1062, 1363, 273, 276, 277, 365, 264,
            265, 266, 267, 268, 270, 304, 349, 364, 1393, 1392,
            1352, 1353, 1354, 311, 307, 309, 306, 308, 1259, 1182,
            1189, 1191, 4703, 1389, 1416, 1323};

    int[] fighterbuffs = new int[]{
            1204, 1048, 1045, 1068, 1040, 1035, 1086, 1242, 1036, 1240,
            1268, 1077, 1087, 1062, 1363, 271, 272, 274, 275, 277,
            310, 264, 265, 266, 267, 268, 269, 270, 304, 305,
            349, 364, 1393, 1392, 1352, 1353, 1354, 311, 307, 309,
            306, 308, 1259, 1182, 1189, 1191, 4703, 4699, 1388, 1416,
            1323};

    private static String getPlayerSchemes(Player player, String schemeName) {
        Map<String, ArrayList<Integer>> schemes = BufferManager.getInstance().getPlayerSchemes(player.getObjectId());
        if (schemes == null || schemes.isEmpty())
            return "Please create at least one scheme.";
        StringBuilder sb = new StringBuilder(200);
        sb.append("<table>");
        for (Map.Entry<String, ArrayList<Integer>> scheme : schemes.entrySet()) {
            if (schemeName.equalsIgnoreCase(scheme.getKey())) {
                StringUtil.append(sb, "<tr><td width=200>", scheme.getKey(), " (<font color=\"LEVEL\">", scheme.getValue().size(), "</font> / ", player.getMaxBuffCount(), " skill(s))</td></tr>");
                continue;
            }
            StringUtil.append(sb, "<tr><td width=200><a action=\"bypass _bbsbuffer;editschemes;none;", scheme.getKey(), "\">", scheme.getKey(), " (", scheme.getValue().size(), " / ", player.getMaxBuffCount(), " skill(s))</a></td></tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }

    private static String getGroupSkillList(Player player, String groupType, String schemeName) {
        List<Integer> skills = new ArrayList<>();
        for (Iterator<Integer> iterator1 = BufferManager.getInstance().getSkillsIdsByType(groupType).iterator(); iterator1.hasNext(); ) {
            int skillId = iterator1.next();
            if (BufferManager.getInstance().getSchemeContainsSkill(player.getObjectId(), schemeName, skillId))
                continue;
            skills.add(skillId);
        }
        if (skills.isEmpty())
            return "That group doesn't contain any skills.";
        StringBuilder sb = new StringBuilder(500);
        sb.append("<table>");
        int count = 0;
        for (Iterator<Integer> iterator2 = skills.iterator(); iterator2.hasNext(); ) {
            int skillId = iterator2.next();
            if (BufferManager.getInstance().getSchemeContainsSkill(player.getObjectId(), schemeName, skillId))
                continue;
            if (count == 0)
                sb.append("<tr>");
            if (skillId < 100) {
                sb.append("<td width=180><font color=\"949490\"><a action=\"bypass _bbsbuffer;skillselect;" + groupType + ";" + schemeName + ";" + skillId + "\">" + SkillTable.getInstance().getInfo(skillId, 1).getName() + "</a></font></td>");
            } else if (skillId < 1000) {
                sb.append("<td width=180><font color=\"949490\"><a action=\"bypass _bbsbuffer;skillselect;" + groupType + ";" + schemeName + ";" + skillId + "\">" + SkillTable.getInstance().getInfo(skillId, 1).getName() + "</a></font></td>");
            } else {
                sb.append("<td width=180><font color=\"949490\"><a action=\"bypass _bbsbuffer;skillselect;" + groupType + ";" + schemeName + ";" + skillId + "\">" + SkillTable.getInstance().getInfo(skillId, 1).getName() + "</a></font></td>");
            }
            count++;
            if (count == 2) {
                sb.append("</tr><tr><td></td></tr>");
                count = 0;
            }
        }
        if (!sb.toString().endsWith("</tr>"))
            sb.append("</tr>");
        sb.append("</table>");
        return sb.toString();
    }

    private static String getPlayerSchemeSkillList(Player player, String groupType, String schemeName) {
        List<Integer> skills = BufferManager.getInstance().getScheme(player.getObjectId(), schemeName);
        if (skills.isEmpty())
            return "That scheme is empty.";
        StringBuilder sb = new StringBuilder(500);
        sb.append("<table>");
        int count = 0;
        for (Iterator<Integer> iterator = skills.iterator(); iterator.hasNext(); ) {
            int sk = iterator.next();
            if (count == 0)
                sb.append("<tr>");
            if (sk < 100) {
                sb.append("<td width=180><font color=\"6e6e6a\"><a action=\"bypass _bbsbuffer;skillunselect;" + groupType + ";" + schemeName + ";" + sk + "\">" + SkillTable.getInstance().getInfo(sk, 1).getName() + "</a></font></td>");
            } else if (sk < 1000) {
                sb.append("<td width=180><font color=\"6e6e6a\"><a action=\"bypass _bbsbuffer;skillunselect;" + groupType + ";" + schemeName + ";" + sk + "\">" + SkillTable.getInstance().getInfo(sk, 1).getName() + "</a></font></td>");
            } else {
                sb.append("<td width=180><font color=\"6e6e6a\"><a action=\"bypass _bbsbuffer;skillunselect;" + groupType + ";" + schemeName + ";" + sk + "\">" + SkillTable.getInstance().getInfo(sk, 1).getName() + "</a></font></td>");
            }
            count++;
            if (count == 2) {
                sb.append("</tr><tr><td></td></tr>");
                count = 0;
            }
        }
        if (!sb.toString().endsWith("<tr>"))
            sb.append("<tr>");
        sb.append("</table>");
        return sb.toString();
    }

    private static String getTypesFrame(String groupType, String schemeName) {
        StringBuilder sb = new StringBuilder(500);
        sb.append("<table>");
        int count = 0;
        for (String s : BufferManager.getInstance().getSkillTypes()) {
            if (count == 0)
                sb.append("<tr>");
            if (groupType.equalsIgnoreCase(s)) {
                StringUtil.append(sb, "<td width=65>", s, "</td>");
            } else {
                StringUtil.append(sb, "<td width=65><a action=\"bypass _bbsbuffer;editschemes;", s, ";", schemeName, "\">", s, "</a></td>");
            }
            count++;
            if (count == 4) {
                sb.append("</tr>");
                count = 0;
            }
        }
        if (!sb.toString().endsWith("</tr>"))
            sb.append("</tr>");
        sb.append("</table>");
        return sb.toString();
    }

    private static int getFee(ArrayList<Integer> list) {
        if (Config.BUFFER_STATIC_BUFF_COST > 0)
            return list.size() * Config.BUFFER_STATIC_BUFF_COST;
        int fee = 0;
        for (Iterator<Integer> iterator = list.iterator(); iterator.hasNext(); ) {
            int sk = iterator.next();
            fee += BufferManager.getInstance().getAvailableBuff(sk).getValue();
        }
        return fee;
    }

    public static BufferBBSManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    private void showMainWindow(Player activeChar) {
        String content = HtmCache.getInstance().getHtm("data/html/CommunityBoard/" + getFolder() + "index.htm");
        content = content.replaceAll("%name%", activeChar.getName());
        content = content.replaceAll("%buffcount%", "You have " + activeChar.getBuffCount() + "/" + activeChar.getMaxBuffCount() + " buffs.");
        content = content.replaceAll("%buffing%", (activeChar.getBuff() == 0) ? "Yourself" : "Your pet");
        separateAndSend(content, activeChar);
    }

    public void parseCmd(String command, Player player) {
        if (player.getPvpFlag() > 0) {
            separateAndSend("<html><body><br><br><center>You can't use Community Board when you are pvp flagged.</center></body></html>", player);
            player.sendMessage("You can't use buffer when you are pvp flagged.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (player.isInCombat()) {
            separateAndSend("<html><body><br><br><center>You can't use Community Board when you are in combat.</center></body></html>", player);
            player.sendMessage("You can't use buffer when you are in combat.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (player.isDead()) {
            separateAndSend("<html><body><br><br><center>You're dead. You can't use Community Board.</center></body></html>", player);
            player.sendMessage("You're dead. You can't use buffer.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (!player.isInsideZone(ZoneId.PEACE)) {
            separateAndSend("<html><body><br><br><center>You're not in Peace Zone. You can't use Community Board.</center></body></html>", player);
            player.sendMessage("You're not in Peace Zone. You can't use buffer.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (command.equals("_bbsbuffer")) {
            showMainWindow(player);
        } else if (command.startsWith("_bbsbuffer;")) {
            StringTokenizer st = new StringTokenizer(command, ";");
            st.nextToken();
            String actualCommand = st.nextToken();
            if (actualCommand.equalsIgnoreCase("restore")) {
                String noble = st.nextToken();
                if (player.getBuff() == 0) {
                    player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
                    player.setCurrentCp(player.getMaxCp());
                    if (noble.equals("true")) {
                        SkillTable.getInstance().getInfo(1323, 1).getEffects(player, player);
                        player.broadcastPacket(new MagicSkillUse(player, player, 1323, 1, 850, 0));
                    }
                } else if (player.getSummon() != null) {
                    Summon summon = player.getSummon();
                    summon.setCurrentHpMp(summon.getMaxHp(), summon.getMaxMp());
                }
                showMainWindow(player);
            } else if (actualCommand.equalsIgnoreCase("cancellation")) {
                L2Skill buff = SkillTable.getInstance().getInfo(1056, 1);
                if (player.getBuff() == 0) {
                    buff.getEffects(player, player);
                    player.stopAllEffectsExceptThoseThatLastThroughDeath();
                    player.broadcastPacket(new MagicSkillUse(player, player, 1056, 1, 850, 0));
                    player.stopAllEffects();
                } else if (player.getSummon() != null) {
                    Summon summon = player.getSummon();
                    summon.stopAllEffects();
                }
                showMainWindow(player);
            } else if (actualCommand.equals("changebuff")) {
                player.setBuff((player.getBuff() == 0) ? 1 : 0);
                showMainWindow(player);
            } else if (actualCommand.equalsIgnoreCase("openlist")) {
                String html, category = st.nextToken();
                String htmfile = st.nextToken();
                if (category.equalsIgnoreCase("null")) {
                    html = HtmCache.getInstance().getHtm("data/html/CommunityBoard/" + getFolder() + htmfile + ".htm");
                    if (htmfile.equals("index")) {
                        html = html.replaceAll("%name%", player.getName());
                        html = html.replaceAll("%buffcount%", "You have " + player.getBuffCount() + "/" + player.getMaxBuffCount() + " buffs.");
                    }
                } else {
                    html = HtmCache.getInstance().getHtm("data/html/CommunityBoard/" + getFolder() + category + "/" + htmfile + ".htm");
                }
                separateAndSend(html, player);
            } else if (actualCommand.equalsIgnoreCase("dobuff")) {
                int buffid = Integer.parseInt(st.nextToken());
                int bufflevel = Integer.parseInt(st.nextToken());
                String category = st.nextToken();
                String windowhtml = st.nextToken();
                if (player.getBuff() == 0) {
                    MagicSkillUse mgc = new MagicSkillUse(player, player, buffid, bufflevel, 1150, 0);
                    player.sendPacket(mgc);
                    player.broadcastPacket(mgc);
                } else if (player.getSummon() != null) {
                    MagicSkillUse mgc = new MagicSkillUse(player, player.getSummon(), buffid, bufflevel, 1150, 0);
                    player.sendPacket(mgc);
                    player.broadcastPacket(mgc);
                }
                if (player.getBuff() == 0) {
                    SkillTable.getInstance().getInfo(buffid, bufflevel).getEffects(player, player);
                } else if (player.getSummon() != null) {
                    SkillTable.getInstance().getInfo(buffid, bufflevel).getEffects(player.getSummon(), player.getSummon());
                }
                String html = HtmCache.getInstance().getHtm("data/html/CommunityBoard/" + getFolder() + category + "/" + windowhtml + ".htm");
                html = html.replaceAll("%name%", player.getName());
                html.replace("%name%", player.getName());
                separateAndSend(html, player);
            } else if (actualCommand.equalsIgnoreCase("getbuff")) {
                int buffid = Integer.parseInt(st.nextToken());
                int bufflevel = Integer.parseInt(st.nextToken());
                if (buffid != 0) {
                    if (player.getBuff() == 0) {
                        MagicSkillUse mgc = new MagicSkillUse(player, player, buffid, bufflevel, 450, 0);
                        player.sendPacket(mgc);
                        player.broadcastPacket(mgc);
                    } else if (player.getSummon() != null) {
                        MagicSkillUse mgc = new MagicSkillUse(player, player.getSummon(), buffid, bufflevel, 450, 0);
                        player.sendPacket(mgc);
                        player.broadcastPacket(mgc);
                    }
                    if (player.getBuff() == 0) {
                        SkillTable.getInstance().getInfo(buffid, bufflevel).getEffects(player, player);
                    } else if (player.getSummon() != null) {
                        SkillTable.getInstance().getInfo(buffid, bufflevel).getEffects(player.getSummon(), player.getSummon());
                    }
                    showMainWindow(player);
                }
            } else if (actualCommand.startsWith("support")) {
                showGiveBuffsWindow(player, st.nextToken());
            } else if (actualCommand.startsWith("givebuffs")) {
                String targetType = st.nextToken();
                String schemeName = st.nextToken();
                int cost = Integer.parseInt(st.nextToken());
                if (player.getBuff() == 0) {
                    if (cost == 0 || player.reduceAdena("NPC Buffer", cost, player, true))
                        for (Iterator<Integer> iterator = BufferManager.getInstance().getScheme(player.getObjectId(), schemeName).iterator(); iterator.hasNext(); ) {
                            int skillId = iterator.next();
                            SkillTable.getInstance().getInfo(skillId, SkillTable.getInstance().getMaxLevel(skillId)).getEffects(player, player);
                        }
                } else if (player.getSummon() != null) {
                    Summon summon = player.getSummon();
                    if (cost == 0 || player.reduceAdena("NPC Buffer", cost, player, true))
                        for (Iterator<Integer> iterator = BufferManager.getInstance().getScheme(player.getObjectId(), schemeName).iterator(); iterator.hasNext(); ) {
                            int skillId = iterator.next();
                            SkillTable.getInstance().getInfo(skillId, SkillTable.getInstance().getMaxLevel(skillId)).getEffects(summon, summon);
                        }
                }
                showGiveBuffsWindow(player, targetType);
            } else if (actualCommand.startsWith("editschemes")) {
                if (st.countTokens() == 2) {
                    showEditSchemeWindow(player, st.nextToken(), st.nextToken());
                } else {
                    player.sendMessage("Something wrong with your scheme. Please contact with Admin");
                }
            } else if (actualCommand.startsWith("skill")) {
                String groupType = st.nextToken();
                String schemeName = st.nextToken();
                int skillId = Integer.parseInt(st.nextToken());
                List<Integer> skills = BufferManager.getInstance().getScheme(player.getObjectId(), schemeName);
                if (actualCommand.startsWith("skillselect") && !schemeName.equalsIgnoreCase("none")) {
                    if (skills.size() < Config.MAX_BUFFS_AMOUNT) {
                        skills.add(skillId);
                    } else {
                        player.sendMessage("This scheme has reached the maximum amount of buffs.");
                    }
                } else if (actualCommand.startsWith("skillunselect")) {
                    skills.remove(Integer.valueOf(skillId));
                }
                showEditSchemeWindow(player, groupType, schemeName);
            } else if (actualCommand.startsWith("manageschemes")) {
                showManageSchemeWindow(player);
            } else if (actualCommand.startsWith("createscheme")) {
                try {
                    String schemeName = st.nextToken();
                    if (schemeName.length() > 14) {
                        player.sendMessage("Scheme's name must contain up to 14 chars. Spaces are trimmed.");
                        showManageSchemeWindow(player);
                        return;
                    }
                    Map<String, ArrayList<Integer>> schemes = BufferManager.getInstance().getPlayerSchemes(player.getObjectId());
                    if (schemes != null) {
                        if (schemes.size() == Config.BUFFER_MAX_SCHEMES) {
                            player.sendMessage("Maximum schemes amount is already reached.");
                            showManageSchemeWindow(player);
                            return;
                        }
                        if (schemes.containsKey(schemeName)) {
                            player.sendMessage("The scheme name already exists.");
                            showManageSchemeWindow(player);
                            return;
                        }
                    }
                    BufferManager.getInstance().setScheme(player.getObjectId(), schemeName.trim(), new ArrayList<>());
                    showManageSchemeWindow(player);
                } catch (Exception e) {
                    player.sendMessage("Scheme's name must contain up to 14 chars. Spaces are trimmed.");
                    showManageSchemeWindow(player);
                }
            } else if (actualCommand.startsWith("deletescheme")) {
                try {
                    String schemeName = st.nextToken();
                    Map<String, ArrayList<Integer>> schemes = BufferManager.getInstance().getPlayerSchemes(player.getObjectId());
                    if (schemes != null)
                        schemes.remove(schemeName);
                } catch (Exception e) {
                    player.sendMessage("This scheme name is invalid.");
                }
                showManageSchemeWindow(player);
            } else if (actualCommand.startsWith("clearscheme")) {
                try {
                    String schemeName = st.nextToken();
                    Map<String, ArrayList<Integer>> schemes = BufferManager.getInstance().getPlayerSchemes(player.getObjectId());
                    if (schemes != null && schemes.containsKey(schemeName))
                        schemes.get(schemeName).clear();
                } catch (Exception e) {
                    player.sendMessage("This scheme name is invalid.");
                }
                showManageSchemeWindow(player);
            } else if (actualCommand.equalsIgnoreCase("fightersetbers")) {
                if (player.getBuff() == 0) {
                    for (int id : this.fighterbuffs)
                        SkillTable.getInstance().getInfo(id, SkillTable.getInstance().getMaxLevel(id)).getEffects(player, player);
                } else if (player.getSummon() != null) {
                    Summon summon = player.getSummon();
                    for (int id : this.fighterbuffs)
                        SkillTable.getInstance().getInfo(id, SkillTable.getInstance().getMaxLevel(id)).getEffects(summon, summon);
                }
                showMainWindow(player);
            } else if (actualCommand.equalsIgnoreCase("magesetbers")) {
                if (player.getBuff() == 0) {
                    for (int id : this.magebuffs)
                        SkillTable.getInstance().getInfo(id, SkillTable.getInstance().getMaxLevel(id)).getEffects(player, player);
                } else if (player.getSummon() != null) {
                    Summon summon = player.getSummon();
                    for (int id : this.magebuffs)
                        SkillTable.getInstance().getInfo(id, SkillTable.getInstance().getMaxLevel(id)).getEffects(summon, summon);
                }
                showMainWindow(player);
            } else if (actualCommand.equalsIgnoreCase("fighterset")) {
                if (player.getBuff() == 0) {
                    for (int id : this.fighterbuffs)
                        SkillTable.getInstance().getInfo(id, SkillTable.getInstance().getMaxLevel(id)).getEffects(player, player);
                } else if (player.getSummon() != null) {
                    Summon summon = player.getSummon();
                    for (int id : this.fighterbuffs)
                        SkillTable.getInstance().getInfo(id, SkillTable.getInstance().getMaxLevel(id)).getEffects(summon, summon);
                }
                showMainWindow(player);
            } else if (actualCommand.equalsIgnoreCase("mageset")) {
                if (player.getBuff() == 0) {
                    for (int id : this.magebuffs)
                        SkillTable.getInstance().getInfo(id, SkillTable.getInstance().getMaxLevel(id)).getEffects(player, player);
                } else if (player.getSummon() != null) {
                    Summon summon = player.getSummon();
                    for (int id : this.magebuffs)
                        SkillTable.getInstance().getInfo(id, SkillTable.getInstance().getMaxLevel(id)).getEffects(summon, summon);
                }
                showMainWindow(player);
            }
        } else {
            super.parseCmd(command, player);
        }
    }

    private void showGiveBuffsWindow(Player player, String targetType) {
        StringBuilder sb = new StringBuilder(200);
        Map<String, ArrayList<Integer>> schemes = BufferManager.getInstance().getPlayerSchemes(player.getObjectId());
        if (schemes == null || schemes.isEmpty()) {
            sb.append("<font color=\"LEVEL\">You haven't defined any scheme, please go to 'Manage my schemes' and create at least one valid scheme.</font>");
        } else {
            for (Map.Entry<String, ArrayList<Integer>> scheme : schemes.entrySet()) {
                int cost = getFee(scheme.getValue());
                StringUtil.append(sb, "<font color=\"LEVEL\"><a action=\"bypass _bbsbuffer;givebuffs;", targetType, ";", scheme.getKey(), ";", cost, "\">", scheme.getKey(), " (", scheme.getValue().size(),
                        " skill(s))</a>", (cost > 0) ? (" - Adena cost: " + cost) : "", "</font><br1>");
            }
        }
        String html = HtmCache.getInstance().getHtm("data/html/CommunityBoard/" + getFolder() + "schememanager/index-1.htm");
        html = html.replaceAll("%schemes%", sb.toString());
        html = html.replaceAll("%targettype%", targetType.equalsIgnoreCase("pet") ? "&nbsp;<a action=\"bypass _bbsbuffer;support;player\">yourself</a>&nbsp;|&nbsp;your pet" : "yourself&nbsp;|&nbsp;<a action=\"bypass _bbsbuffer;support;pet\">your pet</a>");
        separateAndSend(html, player);
    }

    private void showManageSchemeWindow(Player player) {
        StringBuilder sb = new StringBuilder(200);
        Map<String, ArrayList<Integer>> schemes = BufferManager.getInstance().getPlayerSchemes(player.getObjectId());
        if (schemes == null || schemes.isEmpty()) {
            sb.append("<font color=\"LEVEL\">You haven't created any scheme.</font>");
        } else {
            sb.append("<table>");
            for (Map.Entry<String, ArrayList<Integer>> scheme : schemes.entrySet()) {
                StringUtil.append(sb, "<tr><td width=140>", scheme.getKey(), " (", scheme.getValue().size(), " skill(s))</td><td width=60><button value=\"Clear\" action=\"bypass _bbsbuffer;clearscheme;", scheme.getKey(), "\" width=55 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td width=60><button value=\"Drop\" action=\"bypass _bbsbuffer;deletescheme;", scheme.getKey(), "\" width=55 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>");
            }
            sb.append("</table>");
        }
        String html = HtmCache.getInstance().getHtm("data/html/CommunityBoard/" + getFolder() + "schememanager/index-2.htm");
        html = html.replaceAll("%schemes%", sb.toString());
        html = html.replaceAll("%max_schemes%", Integer.toString(Config.BUFFER_MAX_SCHEMES));
        separateAndSend(html, player);
    }

    private void showEditSchemeWindow(Player player, String groupType, String schemeName) {
        String html;
        if (schemeName.equalsIgnoreCase("none")) {
            html = HtmCache.getInstance().getHtm("data/html/CommunityBoard/" + getFolder() + "schememanager/index-3.htm");
        } else {
            if (groupType.equalsIgnoreCase("none")) {
                html = HtmCache.getInstance().getHtm("data/html/CommunityBoard/" + getFolder() + "schememanager/index-4.htm");
            } else {
                html = HtmCache.getInstance().getHtm("data/html/CommunityBoard/" + getFolder() + "schememanager/index-5.htm");
                html = html.replaceAll("%skilllistframe%", getGroupSkillList(player, groupType, schemeName));
            }
            html = html.replaceAll("%schemename%", schemeName);
            html = html.replaceAll("%myschemeframe%", getPlayerSchemeSkillList(player, groupType, schemeName));
            html = html.replaceAll("%typesframe%", getTypesFrame(groupType, schemeName));
        }
        html = html.replaceAll("%schemes%", getPlayerSchemes(player, schemeName));
        separateAndSend(html, player);
    }

    protected String getFolder() {
        return "top/buffer/";
    }

    private static class SingletonHolder {
        protected static final BufferBBSManager INSTANCE = new BufferBBSManager();
    }
}
