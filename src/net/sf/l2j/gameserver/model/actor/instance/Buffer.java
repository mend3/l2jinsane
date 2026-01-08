package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.manager.BufferManager;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class Buffer extends Npc {
    int[] magebuffs = new int[]{1204, 1048, 1045, 1040, 1035, 1085, 1303, 1304, 1243, 1036, 1087, 1059, 1078, 1062, 1363, 273, 276, 277, 365, 264, 265, 266, 267, 268, 270, 304, 349, 364, 1393, 1392, 1352, 1353, 1354, 311, 307, 309, 306, 308, 1259, 1182, 1189, 1191, 4703, 1389, 1416, 1323};
    int[] fighterbuffs = new int[]{1204, 1048, 1045, 1068, 1040, 1035, 1086, 1242, 1036, 1240, 1268, 1077, 1087, 1062, 1363, 271, 272, 274, 275, 277, 310, 264, 265, 266, 267, 268, 269, 270, 304, 305, 349, 364, 1393, 1392, 1352, 1353, 1354, 311, 307, 309, 306, 308, 1259, 1182, 1189, 1191, 4703, 4699, 1388, 1416, 1323};

    public Buffer(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    private static String getPlayerSchemes(Player player, String schemeName) {
        Map<String, ArrayList<Integer>> schemes = BufferManager.getInstance().getPlayerSchemes(player.getObjectId());
        if (schemes != null && !schemes.isEmpty()) {
            StringBuilder sb = new StringBuilder(200);
            sb.append("<table>");

            for (Map.Entry<String, ArrayList<Integer>> scheme : schemes.entrySet()) {
                if (schemeName.equalsIgnoreCase(scheme.getKey())) {
                    StringUtil.append(sb, new Object[]{"<tr><td width=200>", scheme.getKey(), " (<font color=\"LEVEL\">", scheme.getValue().size(), "</font> / ", player.getMaxBuffCount(), " skill(s))</td></tr>"});
                } else {
                    StringUtil.append(sb, new Object[]{"<tr><td width=200><a action=\"bypass -h npc_%objectId%_editschemes none ", scheme.getKey(), "\">", scheme.getKey(), " (", scheme.getValue().size(), " / ", player.getMaxBuffCount(), " skill(s))</a></td></tr>"});
                }
            }

            sb.append("</table>");
            return sb.toString();
        } else {
            return "Please create at least one scheme.";
        }
    }

    private static String getGroupSkillList(Player player, String groupType, String schemeName) {
        List<Integer> skills = new ArrayList<>();

        for (int skillId : BufferManager.getInstance().getSkillsIdsByType(groupType)) {
            if (!BufferManager.getInstance().getSchemeContainsSkill(player.getObjectId(), schemeName, skillId)) {
                skills.add(skillId);
            }
        }

        if (skills.isEmpty()) {
            return "That group doesn't contain any skills.";
        } else {
            StringBuilder sb = new StringBuilder(500);
            sb.append("<table>");
            int count = 0;

            for (int skillId : skills) {
                if (!BufferManager.getInstance().getSchemeContainsSkill(player.getObjectId(), schemeName, skillId)) {
                    if (count == 0) {
                        sb.append("<tr>");
                    }

                    if (skillId < 100) {
                        sb.append("<td width=180><font color=\"949490\"><a action=\"bypass -h npc_%objectId%_skillselect " + groupType + " " + schemeName + " " + skillId + "\">" + SkillTable.getInstance().getInfo(skillId, 1).getName() + "</a></font></td>");
                    } else if (skillId < 1000) {
                        sb.append("<td width=180><font color=\"949490\"><a action=\"bypass -h npc_%objectId%_skillselect " + groupType + " " + schemeName + " " + skillId + "\">" + SkillTable.getInstance().getInfo(skillId, 1).getName() + "</a></font></td>");
                    } else {
                        sb.append("<td width=180><font color=\"949490\"><a action=\"bypass -h npc_%objectId%_skillselect " + groupType + " " + schemeName + " " + skillId + "\">" + SkillTable.getInstance().getInfo(skillId, 1).getName() + "</a></font></td>");
                    }

                    ++count;
                    if (count == 2) {
                        sb.append("</tr><tr><td></td></tr>");
                        count = 0;
                    }
                }
            }

            if (!sb.toString().endsWith("</tr>")) {
                sb.append("</tr>");
            }

            sb.append("</table>");
            return sb.toString();
        }
    }

    private static String getPlayerSchemeSkillList(Player player, String groupType, String schemeName) {
        List<Integer> skills = BufferManager.getInstance().getScheme(player.getObjectId(), schemeName);
        if (skills.isEmpty()) {
            return "That scheme is empty.";
        } else {
            StringBuilder sb = new StringBuilder(500);
            sb.append("<table>");
            int count = 0;

            for (int sk : skills) {
                if (count == 0) {
                    sb.append("<tr>");
                }

                sb.append("<td width=180><font color=\"6e6e6a\"><a action=\"bypass -h npc_%objectId%_skillunselect ").append(groupType).append(" ").append(schemeName).append(" ").append(sk).append("\">").append(SkillTable.getInstance().getInfo(sk, 1).getName()).append("</a></font></td>");

                ++count;
                if (count == 2) {
                    sb.append("</tr><tr><td></td></tr>");
                    count = 0;
                }
            }

            if (!sb.toString().endsWith("<tr>")) {
                sb.append("<tr>");
            }

            sb.append("</table>");
            return sb.toString();
        }
    }

    private static String getTypesFrame(String groupType, String schemeName) {
        StringBuilder sb = new StringBuilder(500);
        sb.append("<table>");
        int count = 0;

        for (String s : BufferManager.getInstance().getSkillTypes()) {
            if (count == 0) {
                sb.append("<tr>");
            }

            if (groupType.equalsIgnoreCase(s)) {
                StringUtil.append(sb, new Object[]{"<td width=65>", s, "</td>"});
            } else {
                StringUtil.append(sb, new Object[]{"<td width=65><a action=\"bypass -h npc_%objectId%_editschemes ", s, " ", schemeName, "\">", s, "</a></td>"});
            }

            ++count;
            if (count == 4) {
                sb.append("</tr>");
                count = 0;
            }
        }

        if (!sb.toString().endsWith("</tr>")) {
            sb.append("</tr>");
        }

        sb.append("</table>");
        return sb.toString();
    }

    private static int getFee(ArrayList<Integer> list) {
        if (Config.BUFFER_STATIC_BUFF_COST > 0) {
            return list.size() * Config.BUFFER_STATIC_BUFF_COST;
        } else {
            int fee = 0;

            for (int sk : list) {
                fee += BufferManager.getInstance().getAvailableBuff(sk).getValue();
            }

            return fee;
        }
    }

    private void showMainWindow(Player activeChar) {
        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
        html.setFile("data/html/mods/bufferNew/index.htm");
        html.replace("%objectId%", String.valueOf(this.getObjectId()));
        html.replace("%name%", activeChar.getName());
        int var10002 = activeChar.getBuffCount();
        html.replace("%buffcount%", "You have " + var10002 + "/" + activeChar.getMaxBuffCount() + " buffs.");
        html.replace("%buffing%", activeChar.getBuff() == 0 ? "Yourself" : "Your pet");
        activeChar.sendPacket(html);
    }

    public void onAction(Player player) {
        if (this != player.getTarget()) {
            player.setTarget(this);
            player.sendPacket(new MyTargetSelected(this.getObjectId(), 0));
            player.sendPacket(new ValidateLocation(this));
        } else if (!this.canInteract(player)) {
            player.getAI().setIntention(IntentionType.INTERACT, this);
        } else {
            player.sendPacket(new MoveToPawn(player, this, 150));
            if (this.hasRandomAnimation()) {
                this.onRandomAnimation(Rnd.get(8));
            }

            this.showMainWindow(player);
            player.sendPacket(ActionFailed.STATIC_PACKET);
        }

    }

    public void onBypassFeedback(Player player, String command) {
        if (player.getPvpFlag() > 0) {
            player.sendMessage("You can't use buffer when you are pvp flagged.");
        } else if (player.isInCombat()) {
            player.sendMessage("You can't use buffer when you are in combat.");
        } else if (!player.isDead()) {
            StringTokenizer st = new StringTokenizer(command, " ");
            String actualCommand = st.nextToken();
            if (actualCommand.equalsIgnoreCase("restore")) {
                String noble = st.nextToken();
                if (player.getBuff() == 0) {
                    player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
                    player.setCurrentCp(player.getMaxCp());
                    if (noble.equals("true")) {
                        SkillTable.getInstance().getInfo(1323, 1).getEffects(player, player);
                        player.broadcastPacket(new MagicSkillUse(this, player, 1323, 1, 850, 0));
                    }
                } else if (player.getSummon() != null) {
                    Summon summon = player.getSummon();
                    summon.setCurrentHpMp(summon.getMaxHp(), summon.getMaxMp());
                }

                this.showMainWindow(player);
            } else if (actualCommand.equalsIgnoreCase("cancellation")) {
                L2Skill buff = SkillTable.getInstance().getInfo(1056, 1);
                if (player.getBuff() == 0) {
                    buff.getEffects(this, player);
                    player.stopAllEffectsExceptThoseThatLastThroughDeath();
                    player.broadcastPacket(new MagicSkillUse(this, player, 1056, 1, 850, 0));
                    player.stopAllEffects();
                } else if (player.getSummon() != null) {
                    Summon summon = player.getSummon();
                    summon.stopAllEffects();
                }

                this.showMainWindow(player);
            } else if (command.equals("changebuff")) {
                player.setBuff(player.getBuff() == 0 ? 1 : 0);
                this.showMainWindow(player);
            } else if (actualCommand.equalsIgnoreCase("openlist")) {
                String category = st.nextToken();
                String htmfile = st.nextToken();
                NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                if (category.equalsIgnoreCase("null")) {
                    html.setFile("data/html/mods/bufferNew/" + htmfile + ".htm");
                    if (htmfile.equals("index")) {
                        html.replace("%name%", player.getName());
                        int var10002 = player.getBuffCount();
                        html.replace("%buffcount%", "You have " + var10002 + "/" + player.getMaxBuffCount() + " buffs.");
                    }
                } else {
                    html.setFile("data/html/mods/bufferNew/" + category + "/" + htmfile + ".htm");
                }

                html.replace("%objectId%", String.valueOf(this.getObjectId()));
                player.sendPacket(html);
            } else if (actualCommand.equalsIgnoreCase("dobuff")) {
                int buffid = Integer.parseInt(st.nextToken());
                int bufflevel = Integer.parseInt(st.nextToken());
                String category = st.nextToken();
                String windowhtml = st.nextToken();
                if (player.getBuff() == 0) {
                    MagicSkillUse mgc = new MagicSkillUse(this, player, buffid, bufflevel, 1150, 0);
                    player.sendPacket(mgc);
                    player.broadcastPacket(mgc);
                } else if (player.getSummon() != null) {
                    MagicSkillUse mgc = new MagicSkillUse(this, player.getSummon(), buffid, bufflevel, 1150, 0);
                    player.sendPacket(mgc);
                    player.broadcastPacket(mgc);
                }

                if (player.getBuff() == 0) {
                    SkillTable.getInstance().getInfo(buffid, bufflevel).getEffects(player, player);
                } else if (player.getSummon() != null) {
                    SkillTable.getInstance().getInfo(buffid, bufflevel).getEffects(player.getSummon(), player.getSummon());
                }

                NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                html.setFile("data/html/mods/bufferNew/" + category + "/" + windowhtml + ".htm");
                html.replace("%objectId%", String.valueOf(this.getObjectId()));
                html.replace("%name%", player.getName());
                player.sendPacket(html);
            } else if (actualCommand.equalsIgnoreCase("getbuff")) {
                int buffid = Integer.parseInt(st.nextToken());
                int bufflevel = Integer.parseInt(st.nextToken());
                if (buffid != 0) {
                    if (player.getBuff() == 0) {
                        MagicSkillUse mgc = new MagicSkillUse(this, player, buffid, bufflevel, 450, 0);
                        player.sendPacket(mgc);
                        player.broadcastPacket(mgc);
                    } else if (player.getSummon() != null) {
                        MagicSkillUse mgc = new MagicSkillUse(this, player.getSummon(), buffid, bufflevel, 450, 0);
                        player.sendPacket(mgc);
                        player.broadcastPacket(mgc);
                    }

                    if (player.getBuff() == 0) {
                        SkillTable.getInstance().getInfo(buffid, bufflevel).getEffects(player, player);
                    } else if (player.getSummon() != null) {
                        SkillTable.getInstance().getInfo(buffid, bufflevel).getEffects(player.getSummon(), player.getSummon());
                    }

                    this.showMainWindow(player);
                }
            } else if (actualCommand.startsWith("support")) {
                this.showGiveBuffsWindow(player, st.nextToken());
            } else if (actualCommand.startsWith("givebuffs")) {
                String targetType = st.nextToken();
                String schemeName = st.nextToken();
                int cost = Integer.parseInt(st.nextToken());
                if (player.getBuff() == 0) {
                    if (cost == 0 || player.reduceAdena("NPC Buffer", cost, this, true)) {
                        for (int skillId : BufferManager.getInstance().getScheme(player.getObjectId(), schemeName)) {
                            SkillTable.getInstance().getInfo(skillId, SkillTable.getInstance().getMaxLevel(skillId)).getEffects(player, player);
                        }
                    }
                } else if (player.getSummon() != null) {
                    Summon summon = player.getSummon();
                    if (cost == 0 || player.reduceAdena("NPC Buffer", cost, this, true)) {
                        for (int skillId : BufferManager.getInstance().getScheme(player.getObjectId(), schemeName)) {
                            SkillTable.getInstance().getInfo(skillId, SkillTable.getInstance().getMaxLevel(skillId)).getEffects(summon, summon);
                        }
                    }
                }

                this.showGiveBuffsWindow(player, targetType);
            } else if (actualCommand.startsWith("editschemes")) {
                if (st.countTokens() == 2) {
                    this.showEditSchemeWindow(player, st.nextToken(), st.nextToken());
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
                    skills.remove(skillId);
                }

                this.showEditSchemeWindow(player, groupType, schemeName);
            } else if (actualCommand.startsWith("manageschemes")) {
                this.showManageSchemeWindow(player);
            } else if (actualCommand.startsWith("createscheme")) {
                try {
                    String schemeName = st.nextToken();
                    if (schemeName.length() > 14) {
                        player.sendMessage("Scheme's name must contain up to 14 chars. Spaces are trimmed.");
                        this.showManageSchemeWindow(player);
                        return;
                    }

                    Map<String, ArrayList<Integer>> schemes = BufferManager.getInstance().getPlayerSchemes(player.getObjectId());
                    if (schemes != null) {
                        if (schemes.size() == Config.BUFFER_MAX_SCHEMES) {
                            player.sendMessage("Maximum schemes amount is already reached.");
                            this.showManageSchemeWindow(player);
                            return;
                        }

                        if (schemes.containsKey(schemeName)) {
                            player.sendMessage("The scheme name already exists.");
                            this.showManageSchemeWindow(player);
                            return;
                        }
                    }

                    BufferManager.getInstance().setScheme(player.getObjectId(), schemeName.trim(), new ArrayList<>());
                    this.showManageSchemeWindow(player);
                } catch (Exception var13) {
                    player.sendMessage("Scheme's name must contain up to 14 chars. Spaces are trimmed.");
                    this.showManageSchemeWindow(player);
                }
            } else if (actualCommand.startsWith("deletescheme")) {
                try {
                    String schemeName = st.nextToken();
                    Map<String, ArrayList<Integer>> schemes = BufferManager.getInstance().getPlayerSchemes(player.getObjectId());
                    if (schemes != null) {
                        schemes.remove(schemeName);
                    }
                } catch (Exception var12) {
                    player.sendMessage("This scheme name is invalid.");
                }

                this.showManageSchemeWindow(player);
            } else if (actualCommand.startsWith("clearscheme")) {
                try {
                    String schemeName = st.nextToken();
                    Map<String, ArrayList<Integer>> schemes = BufferManager.getInstance().getPlayerSchemes(player.getObjectId());
                    if (schemes != null) {
                        schemes.get(schemeName).clear();
                    }
                } catch (Exception var11) {
                    player.sendMessage("This scheme name is invalid.");
                }

                this.showManageSchemeWindow(player);
            } else if (actualCommand.equalsIgnoreCase("fightersetbers")) {
                if (player.getBuff() == 0) {
                    for (int id : this.fighterbuffs) {
                        SkillTable.getInstance().getInfo(id, SkillTable.getInstance().getMaxLevel(id)).getEffects(this, player);
                    }
                } else if (player.getSummon() != null) {
                    Summon summon = player.getSummon();

                    for (int id : this.fighterbuffs) {
                        SkillTable.getInstance().getInfo(id, SkillTable.getInstance().getMaxLevel(id)).getEffects(summon, summon);
                    }
                }

                this.showMainWindow(player);
            } else if (actualCommand.equalsIgnoreCase("magesetbers")) {
                if (player.getBuff() == 0) {
                    for (int id : this.magebuffs) {
                        SkillTable.getInstance().getInfo(id, SkillTable.getInstance().getMaxLevel(id)).getEffects(this, player);
                    }
                } else if (player.getSummon() != null) {
                    Summon summon = player.getSummon();

                    for (int id : this.magebuffs) {
                        SkillTable.getInstance().getInfo(id, SkillTable.getInstance().getMaxLevel(id)).getEffects(summon, summon);
                    }
                }

                this.showMainWindow(player);
            } else if (actualCommand.equalsIgnoreCase("fighterset")) {
                if (player.getBuff() == 0) {
                    for (int id : this.fighterbuffs) {
                        SkillTable.getInstance().getInfo(id, SkillTable.getInstance().getMaxLevel(id)).getEffects(this, player);
                    }
                } else if (player.getSummon() != null) {
                    Summon summon = player.getSummon();

                    for (int id : this.fighterbuffs) {
                        SkillTable.getInstance().getInfo(id, SkillTable.getInstance().getMaxLevel(id)).getEffects(summon, summon);
                    }
                }

                this.showMainWindow(player);
            } else if (actualCommand.equalsIgnoreCase("mageset")) {
                if (player.getBuff() == 0) {
                    for (int id : this.magebuffs) {
                        SkillTable.getInstance().getInfo(id, SkillTable.getInstance().getMaxLevel(id)).getEffects(this, player);
                    }
                } else if (player.getSummon() != null) {
                    Summon summon = player.getSummon();

                    for (int id : this.magebuffs) {
                        SkillTable.getInstance().getInfo(id, SkillTable.getInstance().getMaxLevel(id)).getEffects(summon, summon);
                    }
                }

                this.showMainWindow(player);
            } else {
                super.onBypassFeedback(player, command);
            }

        }
    }

    private void showGiveBuffsWindow(Player player, String targetType) {
        StringBuilder sb = new StringBuilder(200);
        Map<String, ArrayList<Integer>> schemes = BufferManager.getInstance().getPlayerSchemes(player.getObjectId());
        if (schemes != null && !schemes.isEmpty()) {
            for (Map.Entry<String, ArrayList<Integer>> scheme : schemes.entrySet()) {
                int cost = getFee(scheme.getValue());
                StringUtil.append(sb, new Object[]{"<font color=\"LEVEL\"><a action=\"bypass -h npc_%objectId%_givebuffs ", targetType, " ", scheme.getKey(), " ", cost, "\">", scheme.getKey(), " (", scheme.getValue().size(), " skill(s))</a>", cost > 0 ? " - Adena cost: " + cost : "", "</font><br1>"});
            }
        } else {
            sb.append("<font color=\"LEVEL\">You haven't defined any scheme, please go to 'Manage my schemes' and create at least one valid scheme.</font>");
        }

        NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setFile("data/html/mods/bufferNew/schememanager/index-1.htm");
        html.replace("%schemes%", sb.toString());
        html.replace("%targettype%", targetType.equalsIgnoreCase("pet") ? "&nbsp;<a action=\"bypass -h npc_%objectId%_support player\">yourself</a>&nbsp;|&nbsp;your pet" : "yourself&nbsp;|&nbsp;<a action=\"bypass -h npc_%objectId%_support pet\">your pet</a>");
        html.replace("%objectId%", this.getObjectId());
        player.sendPacket(html);
    }

    private void showManageSchemeWindow(Player player) {
        StringBuilder sb = new StringBuilder(200);
        Map<String, ArrayList<Integer>> schemes = BufferManager.getInstance().getPlayerSchemes(player.getObjectId());
        if (schemes != null && !schemes.isEmpty()) {
            sb.append("<table>");

            for (Map.Entry<String, ArrayList<Integer>> scheme : schemes.entrySet()) {
                StringUtil.append(sb, new Object[]{"<tr><td width=140>", scheme.getKey(), " (", scheme.getValue().size(), " skill(s))</td><td width=60><button value=\"Clear\" action=\"bypass -h npc_%objectId%_clearscheme ", scheme.getKey(), "\" width=55 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td><td width=60><button value=\"Drop\" action=\"bypass -h npc_%objectId%_deletescheme ", scheme.getKey(), "\" width=55 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></td></tr>"});
            }

            sb.append("</table>");
        } else {
            sb.append("<font color=\"LEVEL\">You haven't created any scheme.</font>");
        }

        NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setFile("data/html/mods/bufferNew/schememanager/index-2.htm");
        html.replace("%schemes%", sb.toString());
        html.replace("%max_schemes%", Config.BUFFER_MAX_SCHEMES);
        html.replace("%objectId%", this.getObjectId());
        player.sendPacket(html);
    }

    private void showEditSchemeWindow(Player player, String groupType, String schemeName) {
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        if (schemeName.equalsIgnoreCase("none")) {
            html.setFile("data/html/mods/bufferNew/schememanager/index-3.htm");
        } else {
            if (groupType.equalsIgnoreCase("none")) {
                html.setFile("data/html/mods/bufferNew/schememanager/index-4.htm");
            } else {
                html.setFile("data/html/mods/bufferNew/schememanager/index-5.htm");
                html.replace("%skilllistframe%", getGroupSkillList(player, groupType, schemeName));
            }

            html.replace("%schemename%", schemeName);
            html.replace("%myschemeframe%", getPlayerSchemeSkillList(player, groupType, schemeName));
            html.replace("%typesframe%", getTypesFrame(groupType, schemeName));
        }

        html.replace("%schemes%", getPlayerSchemes(player, schemeName));
        html.replace("%objectId%", this.getObjectId());
        player.sendPacket(html);
    }
}
