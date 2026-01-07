package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.manager.BufferManager;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

import java.util.*;

public class SchemeBuffer extends Folk {
    private static final int PAGE_LIMIT = 6;

    public SchemeBuffer(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    private static String getTypesFrame(String groupType, String schemeName) {
        StringBuilder sb = new StringBuilder(500);
        sb.append("<table>");
        int count = 0;
        for (String type : BufferManager.getInstance().getSkillTypes()) {
            if (count == 0)
                sb.append("<tr>");
            if (groupType.equalsIgnoreCase(type)) {
                StringUtil.append(sb, "<td width=65>", type, "</td>");
            } else {
                StringUtil.append(sb, "<td width=65><a action=\"bypass npc_%objectId%_editschemes ", type, " ", schemeName, " 1\">", type, "</a></td>");
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

    public void onBypassFeedback(Player player, String command) {
        StringTokenizer st = new StringTokenizer(command, " ");
        String currentCommand = st.nextToken();
        Summon summon;
        Player player1 = null;
        if (currentCommand.startsWith("menu")) {
            NpcHtmlMessage html = new NpcHtmlMessage(0);
            html.setFile(getHtmlPath(getNpcId(), 0));
            html.replace("%objectId%", getObjectId());
            player.sendPacket(html);
        } else if (currentCommand.startsWith("cleanup")) {
            player.stopAllEffectsExceptThoseThatLastThroughDeath();
            summon = player.getSummon();
            if (summon != null)
                summon.stopAllEffectsExceptThoseThatLastThroughDeath();
            NpcHtmlMessage html = new NpcHtmlMessage(0);
            html.setFile(getHtmlPath(getNpcId(), 0));
            html.replace("%objectId%", getObjectId());
            player.sendPacket(html);
        } else if (currentCommand.startsWith("heal")) {
            player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
            player.setCurrentCp(player.getMaxCp());
            summon = player.getSummon();
            if (summon != null)
                summon.setCurrentHpMp(summon.getMaxHp(), summon.getMaxMp());
            NpcHtmlMessage html = new NpcHtmlMessage(0);
            html.setFile(getHtmlPath(getNpcId(), 0));
            html.replace("%objectId%", getObjectId());
            player.sendPacket(html);
        } else if (currentCommand.startsWith("support")) {
            showGiveBuffsWindow(player);
        } else if (currentCommand.startsWith("givebuffs")) {
            String schemeName = st.nextToken();
            int cost = Integer.parseInt(st.nextToken());
            Creature target = null;
            if (st.hasMoreTokens()) {
                String targetType = st.nextToken();
                if (targetType != null && targetType.equalsIgnoreCase("pet"))
                    summon = player.getSummon();
            } else {
                player1 = player;
            }
            if (player1 == null) {
                player.sendMessage("You don't have a pet.");
            } else if (cost == 0 || player.reduceAdena("NPC Buffer", cost, this, true)) {
                for (Iterator<Integer> iterator = BufferManager.getInstance().getScheme(player.getObjectId(), schemeName).iterator(); iterator.hasNext(); ) {
                    int skillId = iterator.next();
                    SkillTable.getInstance().getInfo(skillId, SkillTable.getInstance().getMaxLevel(skillId)).getEffects(this, player1);
                }
            }
        } else if (currentCommand.startsWith("editschemes")) {
            showEditSchemeWindow(player, st.nextToken(), st.nextToken(), Integer.parseInt(st.nextToken()));
        } else if (currentCommand.startsWith("skill")) {
            String groupType = st.nextToken();
            String schemeName = st.nextToken();
            int skillId = Integer.parseInt(st.nextToken());
            int page = Integer.parseInt(st.nextToken());
            List<Integer> skills = BufferManager.getInstance().getScheme(player.getObjectId(), schemeName);
            if (currentCommand.startsWith("skillselect") && !schemeName.equalsIgnoreCase("none")) {
                if (skills.size() < player.getMaxBuffCount()) {
                    skills.add(skillId);
                } else {
                    player.sendMessage("This scheme has reached the maximum amount of buffs.");
                }
            } else if (currentCommand.startsWith("skillunselect")) {
                skills.remove(Integer.valueOf(skillId));
            }
            showEditSchemeWindow(player, groupType, schemeName, page);
        } else if (currentCommand.startsWith("createscheme")) {
            try {
                String schemeName = st.nextToken();
                if (schemeName.length() > 14) {
                    player.sendMessage("Scheme's name must contain up to 14 chars. Spaces are trimmed.");
                    return;
                }
                Map<String, ArrayList<Integer>> schemes = BufferManager.getInstance().getPlayerSchemes(player.getObjectId());
                if (schemes != null) {
                    if (schemes.size() == Config.BUFFER_MAX_SCHEMES) {
                        player.sendMessage("Maximum schemes amount is already reached.");
                        return;
                    }
                    if (schemes.containsKey(schemeName)) {
                        player.sendMessage("The scheme name already exists.");
                        return;
                    }
                }
                BufferManager.getInstance().setScheme(player.getObjectId(), schemeName.trim(), new ArrayList());
                showGiveBuffsWindow(player);
            } catch (Exception e) {
                player.sendMessage("Scheme's name must contain up to 14 chars. Spaces are trimmed.");
            }
        } else if (currentCommand.startsWith("deletescheme")) {
            try {
                String schemeName = st.nextToken();
                Map<String, ArrayList<Integer>> schemes = BufferManager.getInstance().getPlayerSchemes(player.getObjectId());
                if (schemes != null)
                    schemes.remove(schemeName);
            } catch (Exception e) {
                player.sendMessage("This scheme name is invalid.");
            }
            showGiveBuffsWindow(player);
        }
        super.onBypassFeedback(player, command);
    }

    public String getHtmlPath(int npcId, int val) {
        String filename = "";
        if (val == 0) {
            filename = "" + npcId;
        } else {
            filename = npcId + "-" + npcId;
        }
        return "data/html/mods/buffer/" + filename + ".htm";
    }

    private void showGiveBuffsWindow(Player player) {
        StringBuilder sb = new StringBuilder(200);
        Map<String, ArrayList<Integer>> schemes = BufferManager.getInstance().getPlayerSchemes(player.getObjectId());
        if (schemes == null || schemes.isEmpty()) {
            sb.append("<font color=\"LEVEL\">You haven't defined any scheme.</font>");
        } else {
            for (Map.Entry<String, ArrayList<Integer>> scheme : schemes.entrySet()) {
                int cost = getFee(scheme.getValue());
                StringUtil.append(sb, "<font color=\"LEVEL\">", scheme.getKey(), " [", scheme.getValue().size(), " / ", player.getMaxBuffCount(), "]", (cost > 0) ? (" - cost: " + StringUtil.formatNumber(cost)) : "", "</font><br1>");
                StringUtil.append(sb, "<a action=\"bypass npc_%objectId%_givebuffs ", scheme.getKey(), " ", cost, "\">Use on Me</a>&nbsp;|&nbsp;");
                StringUtil.append(sb, "<a action=\"bypass npc_%objectId%_givebuffs ", scheme.getKey(), " ", cost, " pet\">Use on Pet</a>&nbsp;|&nbsp;");
                StringUtil.append(sb, "<a action=\"bypass npc_%objectId%_editschemes Buffs ", scheme.getKey(), " 1\">Edit</a>&nbsp;|&nbsp;");
                StringUtil.append(sb, "<a action=\"bypass npc_%objectId%_deletescheme ", scheme.getKey(), "\">Delete</a><br>");
            }
        }
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setFile(getHtmlPath(getNpcId(), 1));
        html.replace("%schemes%", sb.toString());
        html.replace("%max_schemes%", Config.BUFFER_MAX_SCHEMES);
        html.replace("%objectId%", getObjectId());
        player.sendPacket(html);
    }

    private void showEditSchemeWindow(Player player, String groupType, String schemeName, int page) {
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        List<Integer> schemeSkills = BufferManager.getInstance().getScheme(player.getObjectId(), schemeName);
        html.setFile(getHtmlPath(getNpcId(), 2));
        html.replace("%schemename%", schemeName);
        html.replace("%count%", schemeSkills.size() + " / " + schemeSkills.size());
        html.replace("%typesframe%", getTypesFrame(groupType, schemeName));
        html.replace("%skilllistframe%", getGroupSkillList(player, groupType, schemeName, page));
        html.replace("%objectId%", getObjectId());
        player.sendPacket(html);
    }

    private String getGroupSkillList(Player player, String groupType, String schemeName, int page) {
        List<Integer> skills = BufferManager.getInstance().getSkillsIdsByType(groupType);
        if (skills.isEmpty())
            return "That group doesn't contain any skills.";
        int max = MathUtil.countPagesNumber(skills.size(), 6);
        if (page > max)
            page = max;
        skills = skills.subList((page - 1) * 6, Math.min(page * 6, skills.size()));
        List<Integer> schemeSkills = BufferManager.getInstance().getScheme(player.getObjectId(), schemeName);
        StringBuilder sb = new StringBuilder(skills.size() * 150);
        int row = 0;
        for (Iterator<Integer> iterator = skills.iterator(); iterator.hasNext(); ) {
            int skillId = iterator.next();
            sb.append((row % 2 == 0) ? "<table width=\"280\" bgcolor=\"000000\"><tr>" : "<table width=\"280\"><tr>");
            if (skillId < 100) {
                if (schemeSkills.contains(skillId)) {
                    StringUtil.append(sb, "<td height=40 width=40><img src=\"icon.skill00", skillId, "\" width=32 height=32></td><td width=190>", SkillTable.getInstance().getInfo(skillId, 1).getName(), "<br1><font color=\"B09878\">", BufferManager.getInstance().getAvailableBuff(skillId).getDescription(), "</font></td><td><button action=\"bypass npc_%objectId%_skillunselect ", groupType, " ", schemeName,
                            " ", skillId, " ", page, "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomout2\" fore=\"L2UI_CH3.mapbutton_zoomout1\"></td>");
                } else {
                    StringUtil.append(sb, "<td height=40 width=40><img src=\"icon.skill00", skillId, "\" width=32 height=32></td><td width=190>", SkillTable.getInstance().getInfo(skillId, 1).getName(), "<br1><font color=\"B09878\">", BufferManager.getInstance().getAvailableBuff(skillId).getDescription(), "</font></td><td><button action=\"bypass npc_%objectId%_skillselect ", groupType, " ", schemeName,
                            " ", skillId, " ", page, "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomin2\" fore=\"L2UI_CH3.mapbutton_zoomin1\"></td>");
                }
            } else if (skillId < 1000) {
                if (schemeSkills.contains(skillId)) {
                    StringUtil.append(sb, "<td height=40 width=40><img src=\"icon.skill0", skillId, "\" width=32 height=32></td><td width=190>", SkillTable.getInstance().getInfo(skillId, 1).getName(), "<br1><font color=\"B09878\">", BufferManager.getInstance().getAvailableBuff(skillId).getDescription(), "</font></td><td><button action=\"bypass npc_%objectId%_skillunselect ", groupType, " ", schemeName,
                            " ", skillId, " ", page, "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomout2\" fore=\"L2UI_CH3.mapbutton_zoomout1\"></td>");
                } else {
                    StringUtil.append(sb, "<td height=40 width=40><img src=\"icon.skill0", skillId, "\" width=32 height=32></td><td width=190>", SkillTable.getInstance().getInfo(skillId, 1).getName(), "<br1><font color=\"B09878\">", BufferManager.getInstance().getAvailableBuff(skillId).getDescription(), "</font></td><td><button action=\"bypass npc_%objectId%_skillselect ", groupType, " ", schemeName,
                            " ", skillId, " ", page, "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomin2\" fore=\"L2UI_CH3.mapbutton_zoomin1\"></td>");
                }
            } else if (schemeSkills.contains(skillId)) {
                StringUtil.append(sb, "<td height=40 width=40><img src=\"icon.skill", skillId, "\" width=32 height=32></td><td width=190>", SkillTable.getInstance().getInfo(skillId, 1).getName(), "<br1><font color=\"B09878\">", BufferManager.getInstance().getAvailableBuff(skillId).getDescription(), "</font></td><td><button action=\"bypass npc_%objectId%_skillunselect ", groupType, " ", schemeName,
                        " ", skillId, " ", page, "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomout2\" fore=\"L2UI_CH3.mapbutton_zoomout1\"></td>");
            } else {
                StringUtil.append(sb, "<td height=40 width=40><img src=\"icon.skill", skillId, "\" width=32 height=32></td><td width=190>", SkillTable.getInstance().getInfo(skillId, 1).getName(), "<br1><font color=\"B09878\">", BufferManager.getInstance().getAvailableBuff(skillId).getDescription(), "</font></td><td><button action=\"bypass npc_%objectId%_skillselect ", groupType, " ", schemeName,
                        " ", skillId, " ", page, "\" width=32 height=32 back=\"L2UI_CH3.mapbutton_zoomin2\" fore=\"L2UI_CH3.mapbutton_zoomin1\"></td>");
            }
            sb.append("</tr></table><img src=\"L2UI.SquareGray\" width=277 height=1>");
            row++;
        }
        sb.append("<br><img src=\"L2UI.SquareGray\" width=277 height=1><table width=\"100%\" bgcolor=000000><tr>");
        if (page > 1) {
            StringUtil.append(sb, "<td align=left width=70><a action=\"bypass npc_" + getObjectId() + "_editschemes ", groupType, " ", schemeName, " ", page - 1, "\">Previous</a></td>");
        } else {
            StringUtil.append(sb, "<td align=left width=70>Previous</td>");
        }
        StringUtil.append(sb, "<td align=center width=100>Page ", page, "</td>");
        if (page < max) {
            StringUtil.append(sb, "<td align=right width=70><a action=\"bypass npc_" + getObjectId() + "_editschemes ", groupType, " ", schemeName, " ", page + 1, "\">Next</a></td>");
        } else {
            StringUtil.append(sb, "<td align=right width=70>Next</td>");
        }
        sb.append("</tr></table><img src=\"L2UI.SquareGray\" width=277 height=1>");
        return sb.toString();
    }
}
