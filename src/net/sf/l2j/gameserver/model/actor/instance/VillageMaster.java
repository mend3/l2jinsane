package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.gameserver.data.DollsData;
import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.data.xml.PlayerClassData;
import net.sf.l2j.gameserver.data.xml.SkillTreeData;
import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.enums.skills.AcquireSkillType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.player.SubClass;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.holder.skillnode.ClanSkillNode;
import net.sf.l2j.gameserver.model.olympiad.OlympiadManager;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.pledge.ClanMember;
import net.sf.l2j.gameserver.model.pledge.SubPledge;
import net.sf.l2j.gameserver.network.FloodProtectors;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.*;
import net.sf.l2j.gameserver.scripting.QuestState;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class VillageMaster extends Folk {
    public VillageMaster(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    private static void createSubPledge(Player player, String clanName, String leaderName, int pledgeType, int minClanLvl) {
        SystemMessage sm;
        if (!player.isClanLeader()) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
            return;
        }
        Clan clan = player.getClan();
        if (clan.getLevel() < minClanLvl) {
            if (pledgeType == -1) {
                player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_CLAN_ACADEMY);
            } else {
                player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_MILITARY_UNIT);
            }
            return;
        }
        if (!StringUtil.isAlphaNumeric(clanName)) {
            player.sendPacket(SystemMessageId.CLAN_NAME_INVALID);
            return;
        }
        if (clanName.length() < 2 || clanName.length() > 16) {
            player.sendPacket(SystemMessageId.CLAN_NAME_LENGTH_INCORRECT);
            return;
        }
        for (Clan tempClan : ClanTable.getInstance().getClans()) {
            if (tempClan.getSubPledge(clanName) != null) {
                if (pledgeType == -1) {
                    player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_EXISTS).addString(clanName));
                } else {
                    player.sendPacket(SystemMessageId.ANOTHER_MILITARY_UNIT_IS_ALREADY_USING_THAT_NAME);
                }
                return;
            }
        }
        if (pledgeType != -1)
            if (clan.getClanMember(leaderName) == null || clan.getClanMember(leaderName).getPledgeType() != 0) {
                if (pledgeType >= 1001) {
                    player.sendPacket(SystemMessageId.CAPTAIN_OF_ORDER_OF_KNIGHTS_CANNOT_BE_APPOINTED);
                } else if (pledgeType >= 100) {
                    player.sendPacket(SystemMessageId.CAPTAIN_OF_ROYAL_GUARD_CANNOT_BE_APPOINTED);
                }
                return;
            }
        int leaderId = (pledgeType != -1) ? clan.getClanMember(leaderName).getObjectId() : 0;
        if (clan.createSubPledge(player, pledgeType, leaderId, clanName) == null)
            return;
        if (pledgeType == -1) {
            sm = SystemMessage.getSystemMessage(SystemMessageId.THE_S1S_CLAN_ACADEMY_HAS_BEEN_CREATED);
            sm.addString(player.getClan().getName());
        } else if (pledgeType >= 1001) {
            sm = SystemMessage.getSystemMessage(SystemMessageId.THE_KNIGHTS_OF_S1_HAVE_BEEN_CREATED);
            sm.addString(player.getClan().getName());
        } else if (pledgeType >= 100) {
            sm = SystemMessage.getSystemMessage(SystemMessageId.THE_ROYAL_GUARD_OF_S1_HAVE_BEEN_CREATED);
            sm.addString(player.getClan().getName());
        } else {
            sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_CREATED);
        }
        player.sendPacket(sm);
        if (pledgeType != -1) {
            ClanMember leaderSubPledge = clan.getClanMember(leaderName);
            Player leaderPlayer = leaderSubPledge.getPlayerInstance();
            if (leaderPlayer != null) {
                leaderPlayer.setPledgeClass(ClanMember.calculatePledgeClass(leaderPlayer));
                leaderPlayer.sendPacket(new UserInfo(leaderPlayer));
            }
        }
    }

    private static void assignSubPledgeLeader(Player player, String clanName, String leaderName) {
        if (!player.isClanLeader()) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
            return;
        }
        if (leaderName.length() > 16) {
            player.sendPacket(SystemMessageId.NAMING_CHARNAME_UP_TO_16CHARS);
            return;
        }
        if (player.getName().equals(leaderName)) {
            player.sendPacket(SystemMessageId.CAPTAIN_OF_ROYAL_GUARD_CANNOT_BE_APPOINTED);
            return;
        }
        Clan clan = player.getClan();
        SubPledge subPledge = player.getClan().getSubPledge(clanName);
        if (null == subPledge || subPledge.getId() == -1) {
            player.sendPacket(SystemMessageId.CLAN_NAME_INVALID);
            return;
        }
        ClanMember leaderSubPledge = clan.getClanMember(leaderName);
        if (leaderSubPledge == null || leaderSubPledge.getPledgeType() != 0) {
            if (subPledge.getId() >= 1001) {
                player.sendPacket(SystemMessageId.CAPTAIN_OF_ORDER_OF_KNIGHTS_CANNOT_BE_APPOINTED);
            } else if (subPledge.getId() >= 100) {
                player.sendPacket(SystemMessageId.CAPTAIN_OF_ROYAL_GUARD_CANNOT_BE_APPOINTED);
            }
            return;
        }
        if (clan.isSubPledgeLeader(leaderSubPledge.getObjectId())) {
            if (subPledge.getId() >= 1001) {
                player.sendPacket(SystemMessageId.CAPTAIN_OF_ORDER_OF_KNIGHTS_CANNOT_BE_APPOINTED);
            } else if (subPledge.getId() >= 100) {
                player.sendPacket(SystemMessageId.CAPTAIN_OF_ROYAL_GUARD_CANNOT_BE_APPOINTED);
            }
            return;
        }
        subPledge.setLeaderId(leaderSubPledge.getObjectId());
        clan.updateSubPledgeInDB(subPledge);
        Player leaderPlayer = leaderSubPledge.getPlayerInstance();
        if (leaderPlayer != null) {
            leaderPlayer.setPledgeClass(ClanMember.calculatePledgeClass(leaderPlayer));
            leaderPlayer.sendPacket(new UserInfo(leaderPlayer));
        }
        clan.broadcastToOnlineMembers(new PledgeShowMemberListAll(clan, subPledge.getId()), SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_SELECTED_AS_CAPTAIN_OF_S2).addString(leaderName).addString(clanName));
    }

    public static void showPledgeSkillList(Player player) {
        if (!player.isClanLeader()) {
            NpcHtmlMessage html = new NpcHtmlMessage(0);
            html.setFile("data/html/scripts/village_master/Clan/9000-09-no.htm");
            player.sendPacket(html);
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        List<ClanSkillNode> skills = SkillTreeData.getInstance().getClanSkillsFor(player);
        if (skills.isEmpty()) {
            NpcHtmlMessage html = new NpcHtmlMessage(0);
            html.setFile("data/html/scripts/village_master/Clan/9000-09-no.htm");
            player.sendPacket(html);
        } else {
            player.sendPacket(new AcquireSkillList(AcquireSkillType.CLAN, skills));
        }
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }

    public String getHtmlPath(int npcId, int val) {
        String filename = "";
        if (val == 0) {
            filename = "" + npcId;
        } else {
            filename = npcId + "-" + npcId;
        }
        return "data/html/villagemaster/" + filename + ".htm";
    }

    public void onBypassFeedback(Player player, String command) {
        String[] commandStr = command.split(" ");
        String actualCommand = commandStr[0];
        String cmdParams = "";
        String cmdParams2 = "";
        if (commandStr.length >= 2)
            cmdParams = commandStr[1];
        if (commandStr.length >= 3)
            cmdParams2 = commandStr[2];
        if (actualCommand.equalsIgnoreCase("create_clan")) {
            if (cmdParams.isEmpty())
                return;
            ClanTable.getInstance().createClan(player, cmdParams);
        } else if (actualCommand.equalsIgnoreCase("create_academy")) {
            if (cmdParams.isEmpty())
                return;
            createSubPledge(player, cmdParams, null, -1, 5);
        } else if (actualCommand.equalsIgnoreCase("rename_pledge")) {
            if (cmdParams.isEmpty() || cmdParams2.isEmpty())
                return;
            if (!player.isClanLeader()) {
                player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
                return;
            }
            Clan clan = player.getClan();
            SubPledge subPledge = player.getClan().getSubPledge(Integer.parseInt(cmdParams));
            if (subPledge == null) {
                player.sendMessage("Pledge doesn't exist.");
                return;
            }
            if (!StringUtil.isAlphaNumeric(cmdParams2)) {
                player.sendPacket(SystemMessageId.CLAN_NAME_INVALID);
                return;
            }
            if (cmdParams2.length() < 2 || cmdParams2.length() > 16) {
                player.sendPacket(SystemMessageId.CLAN_NAME_LENGTH_INCORRECT);
                return;
            }
            subPledge.setName(cmdParams2);
            clan.updateSubPledgeInDB(subPledge);
            clan.broadcastToOnlineMembers(new PledgeShowMemberListAll(clan, subPledge.getId()));
            player.sendMessage("Pledge name have been changed to: " + cmdParams2);
        } else if (actualCommand.equalsIgnoreCase("create_royal")) {
            if (cmdParams.isEmpty())
                return;
            createSubPledge(player, cmdParams, cmdParams2, 100, 6);
        } else if (actualCommand.equalsIgnoreCase("create_knight")) {
            if (cmdParams.isEmpty())
                return;
            createSubPledge(player, cmdParams, cmdParams2, 1001, 7);
        } else if (actualCommand.equalsIgnoreCase("assign_subpl_leader")) {
            if (cmdParams.isEmpty())
                return;
            assignSubPledgeLeader(player, cmdParams, cmdParams2);
        } else if (actualCommand.equalsIgnoreCase("create_ally")) {
            if (cmdParams.isEmpty())
                return;
            if (player.getClan() == null) {
                player.sendPacket(SystemMessageId.ONLY_CLAN_LEADER_CREATE_ALLIANCE);
            } else {
                player.getClan().createAlly(player, cmdParams);
            }
        } else if (actualCommand.equalsIgnoreCase("dissolve_ally")) {
            player.getClan().dissolveAlly(player);
        } else if (actualCommand.equalsIgnoreCase("dissolve_clan")) {
            if (!player.isClanLeader()) {
                player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
                return;
            }
            Clan clan = player.getClan();
            if (clan.getAllyId() != 0) {
                player.sendPacket(SystemMessageId.CANNOT_DISPERSE_THE_CLANS_IN_ALLY);
                return;
            }
            if (clan.isAtWar()) {
                player.sendPacket(SystemMessageId.CANNOT_DISSOLVE_WHILE_IN_WAR);
                return;
            }
            if (clan.hasCastle() || clan.hasClanHall()) {
                player.sendPacket(SystemMessageId.CANNOT_DISSOLVE_WHILE_OWNING_CLAN_HALL_OR_CASTLE);
                return;
            }
            for (Castle castle : CastleManager.getInstance().getCastles()) {
                if (castle.getSiege().checkSides(clan)) {
                    player.sendPacket(castle.getSiege().isInProgress() ? SystemMessageId.CANNOT_DISSOLVE_WHILE_IN_SIEGE : SystemMessageId.CANNOT_DISSOLVE_CAUSE_CLAN_WILL_PARTICIPATE_IN_CASTLE_SIEGE);
                    return;
                }
            }
            if (clan.getDissolvingExpiryTime() > System.currentTimeMillis()) {
                player.sendPacket(SystemMessageId.DISSOLUTION_IN_PROGRESS);
                return;
            }
            if (Config.ALT_CLAN_DISSOLVE_DAYS > 0) {
                clan.setDissolvingExpiryTime(System.currentTimeMillis() + Config.ALT_CLAN_DISSOLVE_DAYS * 86400000L);
                clan.updateClanInDB();
                ClanTable.getInstance().scheduleRemoveClan(clan);
            } else {
                ClanTable.getInstance().destroyClan(clan);
            }
            player.deathPenalty(false, false, false);
        } else if (actualCommand.equalsIgnoreCase("change_clan_leader")) {
            if (cmdParams.isEmpty())
                return;
            if (!player.isClanLeader()) {
                player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
                return;
            }
            if (player.getName().equalsIgnoreCase(cmdParams))
                return;
            Clan clan = player.getClan();
            ClanMember member = clan.getClanMember(cmdParams);
            if (member == null) {
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DOES_NOT_EXIST).addString(cmdParams));
                return;
            }
            if (!member.isOnline()) {
                player.sendPacket(SystemMessageId.INVITED_USER_NOT_ONLINE);
                return;
            }
            if (member.getPledgeType() != 0) {
                player.sendMessage("Selected member cannot be found in main clan.");
                return;
            }
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            if (clan.getNewLeaderId() == 0) {
                clan.setNewLeaderId(member.getObjectId(), true);
                html.setFile("data/html/scripts/village_master/Clan/9000-07-success.htm");
            } else {
                html.setFile("data/html/scripts/village_master/Clan/9000-07-in-progress.htm");
            }
            player.sendPacket(html);
        } else if (actualCommand.equalsIgnoreCase("cancel_clan_leader_change")) {
            if (!player.isClanLeader()) {
                player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
                return;
            }
            Clan clan = player.getClan();
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            if (clan.getNewLeaderId() != 0) {
                clan.setNewLeaderId(0, true);
                html.setFile("data/html/scripts/village_master/Clan/9000-08-success.htm");
            } else {
                html.setFile("data/html/scripts/village_master/Clan/9000-08-no.htm");
            }
            player.sendPacket(html);
        } else if (actualCommand.equalsIgnoreCase("recover_clan")) {
            if (!player.isClanLeader()) {
                player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
                return;
            }
            Clan clan = player.getClan();
            clan.setDissolvingExpiryTime(0L);
            clan.updateClanInDB();
        } else if (actualCommand.equalsIgnoreCase("increase_clan_level")) {
            if (player.getClan().levelUpClan(player))
                player.broadcastPacket(new MagicSkillUse(player, player, 5103, 1, 0, 0));
        } else if (actualCommand.equalsIgnoreCase("learn_clan_skills")) {
            showPledgeSkillList(player);
        } else if (command.startsWith("Subclass")) {
            StringBuilder sb;
            Set<ClassId> subsAvailable;
            boolean allowAddition;
            if (player.isCastingNow() || player.isAllSkillsDisabled()) {
                player.sendPacket(SystemMessageId.SUBCLASS_NO_CHANGE_OR_CREATE_WHILE_SKILL_IN_USE);
                return;
            }
            if (OlympiadManager.getInstance().isRegisteredInComp(player))
                OlympiadManager.getInstance().unRegisterNoble(player);
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            int cmdChoice = 0;
            int paramOne = 0;
            int paramTwo = 0;
            try {
                cmdChoice = Integer.parseInt(command.substring(9, 10).trim());
                int endIndex = command.indexOf(' ', 11);
                if (endIndex == -1)
                    endIndex = command.length();
                paramOne = Integer.parseInt(command.substring(11, endIndex).trim());
                if (command.length() > endIndex)
                    paramTwo = Integer.parseInt(command.substring(endIndex).trim());
            } catch (Exception exception) {
            }
            switch (cmdChoice) {
                case 0:
                    html.setFile("data/html/villagemaster/SubClass.htm");
                    break;
                case 1:
                    if (player.getSummon() != null) {
                        player.sendPacket(SystemMessageId.CANT_SUBCLASS_WITH_SUMMONED_SERVITOR);
                        return;
                    }
                    if (player.getInventoryLimit() * 0.8D <= player.getInventory().getSize() || player.getWeightPenalty() > 2) {
                        player.sendPacket(SystemMessageId.NOT_SUBCLASS_WHILE_OVERWEIGHT);
                        return;
                    }
                    if (player.getSubClasses().size() >= Config.ALLOWED_SUBCLASS) {
                        html.setFile("data/html/villagemaster/SubClass_Fail.htm");
                        break;
                    }
                    subsAvailable = getAvailableSubClasses(player);
                    if (subsAvailable == null || subsAvailable.isEmpty()) {
                        player.sendMessage("There are no sub classes available at this time.");
                        return;
                    }
                    sb = new StringBuilder(300);
                    for (ClassId subClass : subsAvailable) {
                        StringUtil.append(sb, "<a action=\"bypass -h npc_%objectId%_Subclass 4 ", subClass.getId(), "\" msg=\"1268;", subClass, "\">", subClass, "</a><br>");
                    }
                    html.setFile("data/html/villagemaster/SubClass_Add.htm");
                    html.replace("%list%", sb.toString());
                    break;
                case 2:
                    if (player.getSummon() != null) {
                        player.sendPacket(SystemMessageId.CANT_SUBCLASS_WITH_SUMMONED_SERVITOR);
                        return;
                    }
                    if (player.getInventoryLimit() * 0.8D <= player.getInventory().getSize() || player.getWeightPenalty() > 2) {
                        player.sendPacket(SystemMessageId.NOT_SUBCLASS_WHILE_OVERWEIGHT);
                        return;
                    }
                    if (player.getSubClasses().isEmpty()) {
                        html.setFile("data/html/villagemaster/SubClass_ChangeNo.htm");
                        break;
                    }
                    sb = new StringBuilder(300);
                    if (checkVillageMaster(player.getBaseClass()))
                        StringUtil.append(sb, "<a action=\"bypass -h npc_%objectId%_Subclass 5 0\">", PlayerClassData.getInstance().getClassNameById(player.getBaseClass()), "</a><br>");
                    for (SubClass subclass : player.getSubClasses().values()) {
                        if (checkVillageMaster(subclass.getClassDefinition()))
                            StringUtil.append(sb, "<a action=\"bypass -h npc_%objectId%_Subclass 5 ", subclass.getClassIndex(), "\">", subclass.getClassDefinition(), "</a><br>");
                    }
                    if (sb.length() > 0) {
                        html.setFile("data/html/villagemaster/SubClass_Change.htm");
                        html.replace("%list%", sb.toString());
                        break;
                    }
                    html.setFile("data/html/villagemaster/SubClass_ChangeNotFound.htm");
                    break;
                case 3:
                    if (player.getSubClasses().isEmpty()) {
                        html.setFile("data/html/villagemaster/SubClass_ModifyEmpty.htm");
                        break;
                    }
                    html.setFile("data/html/villagemaster/SubClass_Modify.htm");
                    if (player.getSubClasses().containsKey(1)) {
                        html.replace("%sub1%", PlayerClassData.getInstance().getClassNameById(player.getSubClasses().get(1).getClassId()));
                    } else {
                        html.replace("<a action=\"bypass -h npc_%objectId%_Subclass 6 1\">%sub1%</a><br>", "");
                    }
                    if (player.getSubClasses().containsKey(2)) {
                        html.replace("%sub2%", PlayerClassData.getInstance().getClassNameById(player.getSubClasses().get(2).getClassId()));
                    } else {
                        html.replace("<a action=\"bypass -h npc_%objectId%_Subclass 6 2\">%sub2%</a><br>", "");
                    }
                    if (player.getSubClasses().containsKey(3)) {
                        html.replace("%sub3%", PlayerClassData.getInstance().getClassNameById(player.getSubClasses().get(3).getClassId()));
                        break;
                    }
                    html.replace("<a action=\"bypass -h npc_%objectId%_Subclass 6 3\">%sub3%</a><br>", "");
                    break;
                case 4:
                    if (!FloodProtectors.performAction(player.getClient(), FloodProtectors.Action.SUBCLASS))
                        return;
                    allowAddition = player.getSubClasses().size() < Config.ALLOWED_SUBCLASS;
                    if (player.getLevel() < 75)
                        allowAddition = false;
                    if (allowAddition)
                        for (SubClass subclass : player.getSubClasses().values()) {
                            if (subclass.getLevel() < 75) {
                                allowAddition = false;
                                break;
                            }
                        }
                    if (allowAddition && !Config.SUBCLASS_WITHOUT_QUESTS)
                        allowAddition = checkQuests(player);
                    if (allowAddition && isValidNewSubClass(player, paramOne)) {
                        if (!player.addSubClass(paramOne, player.getSubClasses().size() + 1))
                            return;
                        player.setActiveClass(player.getSubClasses().size());
                        html.setFile("data/html/villagemaster/SubClass_AddOk.htm");
                        player.sendPacket(SystemMessageId.ADD_NEW_SUBCLASS);
                        DollsData.refreshAllDollSkills(player);
                        break;
                    }
                    html.setFile("data/html/villagemaster/SubClass_Fail.htm");
                    break;
                case 5:
                    if (!FloodProtectors.performAction(player.getClient(), FloodProtectors.Action.SUBCLASS))
                        return;
                    if (player.getClassIndex() == paramOne) {
                        html.setFile("data/html/villagemaster/SubClass_Current.htm");
                        break;
                    }
                    if (paramOne == 0) {
                        if (!checkVillageMaster(player.getBaseClass()))
                            return;
                    } else {
                        try {
                            if (!checkVillageMaster(player.getSubClasses().get(paramOne).getClassDefinition()))
                                return;
                        } catch (NullPointerException e) {
                            return;
                        }
                    }
                    player.setActiveClass(paramOne);
                    player.sendPacket(SystemMessageId.SUBCLASS_TRANSFER_COMPLETED);
                    DollsData.refreshAllDollSkills(player);
                    return;
                case 6:
                    if (paramOne < 1 || paramOne > 3)
                        return;
                    subsAvailable = getAvailableSubClasses(player);
                    if (subsAvailable == null || subsAvailable.isEmpty()) {
                        player.sendMessage("There are no sub classes available at this time.");
                        return;
                    }
                    sb = new StringBuilder(300);
                    for (ClassId subClass : subsAvailable) {
                        StringUtil.append(sb, "<a action=\"bypass -h npc_%objectId%_Subclass 7 ", paramOne, " ", subClass.getId(), "\" msg=\"1445;", "\">", subClass, "</a><br>");
                    }
                    switch (paramOne) {
                        case 1:
                            html.setFile("data/html/villagemaster/SubClass_ModifyChoice1.htm");
                            break;
                        case 2:
                            html.setFile("data/html/villagemaster/SubClass_ModifyChoice2.htm");
                            break;
                        case 3:
                            html.setFile("data/html/villagemaster/SubClass_ModifyChoice3.htm");
                            break;
                        default:
                            html.setFile("data/html/villagemaster/SubClass_ModifyChoice.htm");
                            break;
                    }
                    html.replace("%list%", sb.toString());
                    break;
                case 7:
                    if (!FloodProtectors.performAction(player.getClient(), FloodProtectors.Action.SUBCLASS))
                        return;
                    if (!isValidNewSubClass(player, paramTwo))
                        return;
                    if (player.modifySubClass(paramOne, paramTwo)) {
                        player.abortCast();
                        player.stopAllEffectsExceptThoseThatLastThroughDeath();
                        player.stopCubics();
                        player.setActiveClass(paramOne);
                        html.setFile("data/html/villagemaster/SubClass_ModifyOk.htm");
                        player.sendPacket(SystemMessageId.ADD_NEW_SUBCLASS);
                        DollsData.refreshAllDollSkills(player);
                        break;
                    }
                    player.setActiveClass(0);
                    player.sendMessage("The sub class could not be added, you have been reverted to your base class.");
                    return;
            }
            html.replace("%objectId%", getObjectId());
            player.sendPacket(html);
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    protected boolean checkQuests(Player player) {
        if (player.isNoble())
            return true;
        QuestState qs = player.getQuestState("Q234_FatesWhisper");
        if (qs == null || !qs.isCompleted())
            return false;
        qs = player.getQuestState("Q235_MimirsElixir");
        return qs != null && qs.isCompleted();
    }

    private final Set<ClassId> getAvailableSubClasses(Player player) {
        Set<ClassId> availSubs = ClassId.getAvailableSubclasses(player);
        if (availSubs != null && !availSubs.isEmpty())
            for (Iterator<ClassId> availSub = availSubs.iterator(); availSub.hasNext(); ) {
                ClassId classId = availSub.next();
                if (!checkVillageMaster(classId)) {
                    availSub.remove();
                    continue;
                }
                for (SubClass subclass : player.getSubClasses().values()) {
                    if (subclass.getClassDefinition().equalsOrChildOf(classId))
                        availSub.remove();
                }
            }
        return availSubs;
    }

    private final boolean isValidNewSubClass(Player player, int classId) {
        if (!checkVillageMaster(classId))
            return false;
        ClassId cid = ClassId.VALUES[classId];
        for (SubClass subclass : player.getSubClasses().values()) {
            if (subclass.getClassDefinition().equalsOrChildOf(cid))
                return false;
        }
        Set<ClassId> availSubs = ClassId.getAvailableSubclasses(player);
        if (availSubs == null || availSubs.isEmpty())
            return false;
        boolean found = false;
        for (ClassId pclass : availSubs) {
            if (pclass.getId() == classId) {
                found = true;
                break;
            }
        }
        return found;
    }

    protected boolean checkVillageMasterRace(ClassId pclass) {
        return true;
    }

    protected boolean checkVillageMasterTeachType(ClassId pclass) {
        return true;
    }

    public final boolean checkVillageMaster(int classId) {
        return checkVillageMaster(ClassId.VALUES[classId]);
    }

    public final boolean checkVillageMaster(ClassId pclass) {
        if (Config.ALT_GAME_SUBCLASS_EVERYWHERE)
            return true;
        return (checkVillageMasterRace(pclass) && checkVillageMasterTeachType(pclass));
    }
}
