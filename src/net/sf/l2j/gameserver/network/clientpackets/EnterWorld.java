package net.sf.l2j.gameserver.network.clientpackets;

import enginemods.main.EngineModsManager;
import mods.dungeon.DungeonManager;
import mods.pvpZone.RandomZoneManager;
import net.sf.l2j.Config;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.communitybbs.Manager.MailBBSManager;
import net.sf.l2j.gameserver.data.DollsData;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.manager.*;
import net.sf.l2j.gameserver.data.xml.AdminData;
import net.sf.l2j.gameserver.data.xml.AnnouncementData;
import net.sf.l2j.gameserver.data.xml.MapRegionData;
import net.sf.l2j.gameserver.data.xml.ScriptData;
import net.sf.l2j.gameserver.enums.CabalType;
import net.sf.l2j.gameserver.enums.SealType;
import net.sf.l2j.gameserver.enums.SiegeSide;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.events.eventengine.NextEventsInfo;
import net.sf.l2j.gameserver.events.partyfarm.PartyFarm;
import net.sf.l2j.gameserver.events.soloboss.SoloBossManager;
import net.sf.l2j.gameserver.events.tournament.ArenaTask;
import net.sf.l2j.gameserver.hwid.Hwid;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.ClassMaster;
import net.sf.l2j.gameserver.model.clanhall.ClanHall;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.pledge.SubPledge;
import net.sf.l2j.gameserver.network.GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.*;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;
import net.sf.l2j.gameserver.scripting.scripts.feature.TutorialQuest;
import net.sf.l2j.gameserver.taskmanager.GameTimeTaskManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

public class EnterWorld extends L2GameClientPacket {
    private static final String LOAD_PLAYER_QUESTS = "SELECT name,var,value FROM character_quests WHERE charId=?";

    protected void readImpl() {
    }

    protected void runImpl() {
        if (getClient() == null)
            return;
        Player player = getClient().getPlayer();
        if (player == null) {
            getClient().closeNow();
            return;
        }
        getClient().setState(GameClient.GameClientState.IN_GAME);
        int objectId = player.getObjectId();
        if (player.isGM()) {
            if (Config.GM_STARTUP_INVULNERABLE && AdminData.getInstance().hasAccess("admin_invul", player.getAccessLevel()))
                player.setIsInvul(true);
            if (Config.GM_STARTUP_INVISIBLE && AdminData.getInstance().hasAccess("admin_hide", player.getAccessLevel()))
                player.getAppearance().setInvisible();
            if (Config.GM_STARTUP_SILENCE && AdminData.getInstance().hasAccess("admin_silence", player.getAccessLevel()))
                player.setInRefusalMode(true);
            AdminData.getInstance().addGm(player, !Config.GM_STARTUP_AUTO_LIST || !AdminData.getInstance().hasAccess("admin_gmlist", player.getAccessLevel()));
            if (Config.GM_SUPER_HASTE)
                SkillTable.getInstance().getInfo(7029, 4).getEffects(player, player);
        }
        if (player.getCurrentHp() < 0.5D && player.isMortal()) {
            player.setIsDead(true);
            if (Config.ENABLE_EFFECT_ON_DIE) {
                ExRedSky packet = new ExRedSky(7);
                sendPacket(packet);
            }
        }
        Clan clan = player.getClan();
        if (clan != null) {
            player.sendPacket(new PledgeSkillList(clan));
            clan.getClanMember(objectId).setPlayerInstance(player);
            SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_LOGGED_IN).addCharName(player);
            PledgeShowMemberListUpdate update = new PledgeShowMemberListUpdate(player);
            for (Player member : clan.getOnlineMembers()) {
                if (member != player) {
                    member.sendPacket(msg);
                    member.sendPacket(update);
                }
            }
            if (player.getSponsor() != 0) {
                Player sponsor = World.getInstance().getPlayer(player.getSponsor());
                if (sponsor != null)
                    sponsor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_APPRENTICE_S1_HAS_LOGGED_IN).addCharName(player));
            } else if (player.getApprentice() != 0) {
                Player apprentice = World.getInstance().getPlayer(player.getApprentice());
                if (apprentice != null)
                    apprentice.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_SPONSOR_S1_HAS_LOGGED_IN).addCharName(player));
            }
            ClanHall clanHall = ClanHallManager.getInstance().getClanHallByOwner(clan);
            if (clanHall != null && !clanHall.getPaid())
                player.sendPacket(SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW);
            for (Castle castle : CastleManager.getInstance().getCastles()) {
                Siege siege = castle.getSiege();
                if (!siege.isInProgress())
                    continue;
                SiegeSide type = siege.getSide(clan);
                if (type == SiegeSide.ATTACKER) {
                    player.setSiegeState((byte) 1);
                    continue;
                }
                if (type == SiegeSide.DEFENDER || type == SiegeSide.OWNER)
                    player.setSiegeState((byte) 2);
            }
            player.sendPacket(new PledgeShowMemberListAll(clan, 0));
            for (SubPledge sp : clan.getAllSubPledges())
                player.sendPacket(new PledgeShowMemberListAll(clan, sp.getId()));
            player.sendPacket(new UserInfo(player));
            player.sendPacket(new PledgeStatusChanged(clan));
        }
        if (SevenSignsManager.getInstance().isSealValidationPeriod() && SevenSignsManager.getInstance().getSealOwner(SealType.STRIFE) != CabalType.NORMAL) {
            CabalType cabal = SevenSignsManager.getInstance().getPlayerCabal(objectId);
            if (cabal != CabalType.NORMAL)
                if (cabal == SevenSignsManager.getInstance().getSealOwner(SealType.STRIFE)) {
                    player.addSkill(SkillTable.FrequentSkill.THE_VICTOR_OF_WAR.getSkill(), false);
                } else {
                    player.addSkill(SkillTable.FrequentSkill.THE_VANQUISHED_OF_WAR.getSkill(), false);
                }
        } else {
            player.removeSkill(SkillTable.FrequentSkill.THE_VICTOR_OF_WAR.getSkill().getId(), false);
            player.removeSkill(SkillTable.FrequentSkill.THE_VANQUISHED_OF_WAR.getSkill().getId(), false);
        }
        if (Config.PLAYER_SPAWN_PROTECTION > 0)
            player.setSpawnProtection(true);
        if (Config.ALLOW_DAILY_REWARD)
            DailyLoginRewardManager.claimDailyReward(player);
        player.spawnMe();
        if (Config.ALLOW_WEDDING)
            for (Map.Entry<Integer, IntIntHolder> coupleEntry : CoupleManager.getInstance().getCouples().entrySet()) {
                IntIntHolder couple = coupleEntry.getValue();
                if (couple.getId() == objectId || couple.getValue() == objectId) {
                    player.setCoupleId(coupleEntry.getKey());
                    break;
                }
            }
        if (player.isSubClassActive())
            if (player.getSubClasses().get(Integer.valueOf(player.getClassIndex())).getLevel() > Config.SUBCLASS_MAX_LEVEL - 1)
                player.getSubClasses().get(Integer.valueOf(player.getClassIndex())).setLevel((byte) (Config.SUBCLASS_MAX_LEVEL - 1));
        player.checkEquipmentXPvps();
        Hwid.enterlog(player, getClient());
        if (DungeonManager.getInstance().getDungeonParticipants().contains(Integer.valueOf(player.getObjectId()))) {
            DungeonManager.getInstance().getDungeonParticipants().remove(Integer.valueOf(player.getObjectId()));
            player.teleportTo(82635, 148798, -3464, 25);
        }
        player.sendPacket(SystemMessageId.WELCOME_TO_LINEAGE);
        player.sendPacket(SevenSignsManager.getInstance().getCurrentPeriod().getMessageId());
        AnnouncementData.getInstance().showAnnouncements(player, false);
        player.loadAutoFarmSettings();
        if (Config.PCB_ENABLE)
            player.showPcBangWindow();
        if (player.getRace() == ClassRace.DARK_ELF && player.hasSkill(294))
            player.sendPacket(SystemMessage.getSystemMessage(GameTimeTaskManager.getInstance().isNight() ? SystemMessageId.NIGHT_S1_EFFECT_APPLIES : SystemMessageId.DAY_S1_EFFECT_DISAPPEARS).addSkillName(294));
        player.removeSpoilSkillinZone();
        DollsData.refreshAllDollSkills(player);
        player.getMacroList().sendUpdate();
        player.sendPacket(new UserInfo(player));
        player.sendPacket(new HennaInfo(player));
        player.sendPacket(new FriendList(player));
        player.sendPacket(new ItemList(player, false));
        player.sendPacket(new ShortCutInit(player));
        player.sendPacket(new ExStorageMaxCount(player));
        if (player.isAlikeDead())
            player.sendPacket(new Die(player));
        player.updateEffectIcons();
        player.sendPacket(new EtcStatusUpdate(player));
        player.sendSkillList();
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("SELECT name,var,value FROM character_quests WHERE charId=?");
                try {
                    ps.setInt(1, objectId);
                    ResultSet rs = ps.executeQuery();
                    try {
                        while (rs.next()) {
                            String questName = rs.getString("name");
                            Quest quest = ScriptData.getInstance().getQuest(questName);
                            if (quest == null) {
                                LOGGER.warn("Unknown quest {} for player {}.", questName, player.getName());
                                continue;
                            }
                            String var = rs.getString("var");
                            if (var.equals("<state>")) {
                                new QuestState(player, quest, rs.getByte("value"));
                                if (quest.getOnEnterWorld())
                                    quest.notifyEnterWorld(player);
                                continue;
                            }
                            QuestState qs = player.getQuestState(questName);
                            if (qs == null) {
                                LOGGER.warn("Unknown quest state {} for player {}.", questName, player.getName());
                                continue;
                            }
                            qs.setInternal(var, rs.getString("value"));
                        }
                        if (rs != null)
                            rs.close();
                    } catch (Throwable throwable) {
                        if (rs != null)
                            try {
                                rs.close();
                            } catch (Throwable throwable1) {
                                throwable.addSuppressed(throwable1);
                            }
                        throw throwable;
                    }
                    if (ps != null)
                        ps.close();
                } catch (Throwable throwable) {
                    if (ps != null)
                        try {
                            ps.close();
                        } catch (Throwable throwable1) {
                            throwable.addSuppressed(throwable1);
                        }
                    throw throwable;
                }
                if (con != null)
                    con.close();
            } catch (Throwable throwable) {
                if (con != null)
                    try {
                        con.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                throw throwable;
            }
        } catch (Exception e) {
            LOGGER.error("Couldn't load quests for player {}.", e, player.getName());
        }
        player.sendPacket(new QuestList(player));
        if (Config.ENABLE_COMMUNITY_BOARD && MailBBSManager.getInstance().checkUnreadMail(player) > 0) {
            player.sendPacket(SystemMessageId.NEW_MAIL);
            player.sendPacket(new PlaySound("systemmsg_e.1233"));
            player.sendPacket(ExMailArrived.STATIC_PACKET);
        }
        if (Config.ENABLE_COMMUNITY_BOARD && clan != null && clan.isNoticeEnabled()) {
            NpcHtmlMessage html = new NpcHtmlMessage(0);
            html.setFile("data/html/clan_notice.htm");
            html.replace("%clan_name%", clan.getName());
            html.replace("%notice_text%", clan.getNotice().replaceAll("\r\n", "<br>").replaceAll("action", "").replaceAll("bypass", ""));
            sendPacket(html);
        } else if (Config.SERVER_NEWS) {
            NpcHtmlMessage html = new NpcHtmlMessage(0);
            html.setFile("data/html/servnews.htm");
            sendPacket(html);
        }
        PetitionManager.getInstance().checkPetitionMessages(player);
        TutorialQuest.onCreate(player);
        player.onPlayerEnter();
        sendPacket(new SkillCoolTime(player));
        if (Olympiad.getInstance().playerInStadia(player))
            player.teleportTo(MapRegionData.TeleportType.TOWN);
        if (DimensionalRiftManager.getInstance().checkIfInRiftZone(player.getX(), player.getY(), player.getZ(), false))
            DimensionalRiftManager.getInstance().teleportToWaitingRoom(player);
        if (player.getClanJoinExpiryTime() > System.currentTimeMillis())
            player.sendPacket(SystemMessageId.CLAN_MEMBERSHIP_TERMINATED);
        if (!player.isGM() && (!player.isInSiege() || player.getSiegeState() < 2) && player.isInsideZone(ZoneId.SIEGE))
            player.teleportTo(MapRegionData.TeleportType.TOWN);
        EngineModsManager.onEnterWorld(player);
        ClassMaster.showQuestionMark(player);
        if (Config.ALLOW_DM_EVENT) {
            player.sendPacket(new CreatureSay(0, 1, "[DeathMatch] Next event: ", NextEventsInfo.getInstance().NextDMEvent()));
            player.sendPacket(new CreatureSay(0, 2, "[DeathMatch] Next event: ", NextEventsInfo.getInstance().NextDMEvent()));
        }
        if (Config.ALLOW_TVT_EVENT) {
            player.sendPacket(new CreatureSay(0, 1, "[Team vs Team] Next event: ", NextEventsInfo.getInstance().NextTvtEvent()));
            player.sendPacket(new CreatureSay(0, 2, "[Team vs Team] Next event: ", NextEventsInfo.getInstance().NextTvtEvent()));
        }
        if (Config.ALLOW_CTF_EVENT) {
            player.sendPacket(new CreatureSay(0, 1, "[Capture the Flag] Next event: ", NextEventsInfo.getInstance().NextCtfEvent()));
            player.sendPacket(new CreatureSay(0, 2, "[Capture the Flag] Next event: ", NextEventsInfo.getInstance().NextCtfEvent()));
        }
        if (ArenaTask.is_started() && Config.ARENA_MESSAGE_ENABLED) {
            player.sendPacket(new ExShowScreenMessage(Config.ARENA_MESSAGE_TEXT, Config.ARENA_MESSAGE_TIME, 2, true));
            player.sendPacket(new CreatureSay(0, 1, "[Party Farm] : ", Config.ARENA_MESSAGE_TEXT));
        }
        if (PartyFarm.is_started() && Config.PARTY_FARM_BY_TIME_OF_DAY) {
            player.sendPacket(new CreatureSay(0, 17, "[Party Farm] : ", Config.PARTY_FARM_MESSAGE_TEXT));
            player.sendPacket(new ExShowScreenMessage(Config.PARTY_FARM_MESSAGE_TEXT, 3));
        }
        if (Config.ENABLE_AUTO_PVP_ZONE) {
            player.sendPacket(new CreatureSay(0, 1, "[PvP Zone]", "Current zone: " + RandomZoneManager.getInstance().getCurrentZone().getName()));
            player.sendPacket(new CreatureSay(0, 1, "[PvP Zone]", "will be changed in " + RandomZoneManager.getInstance().getLeftTime()));
        }
        if (Config.SOLOBOSS_EVENT_ENABLE)
            if (SoloBossManager.isActive())
                player.sendPacket(new CreatureSay(0, 1, "[Solo Boss]", " Event is active: " + SoloBossManager.getInstance().getEventMessage()));
        if (Config.ENABLE_EFFECT_ON_LOGIN) {
            player.sendPacket(new Earthquake(player.getX(), player.getY(), player.getZ(), 65, 12));
            player.sendPacket(new PlaySound("skillsound7.sound_crystal_smelting"));
        }
        if (player.isNewChar()) {
            L2Skill skill = SkillTable.getInstance().getInfo(2025, 1);
            if (skill != null) {
                MagicSkillUse MSU = new MagicSkillUse(player, player, 2025, 1, 1, 0);
                player.sendPacket(MSU);
                player.broadcastPacket(MSU);
                player.useMagic(skill, false, false);
            }
        }
        if (Config.WELLCOME_MESSAGE_ACTIVE) {
            player.sendPacket(new CreatureSay(0, 2, "Have Fun and Nice Stay on ", Config.WELLCOME_SERVER_NAME));
            player.sendPacket(new CreatureSay(0, 2, player.getName(), Config.WELLCOME_SERVER_SECOND_MESSAGE));
        }
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }

    protected boolean triggersOnActionRequest() {
        return false;
    }
}
