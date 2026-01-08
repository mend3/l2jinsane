package net.sf.l2j.gameserver.model.pledge;

import net.sf.l2j.Config;
import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.communitybbs.bb.Forum;
import net.sf.l2j.gameserver.communitybbs.manager.ForumsBBSManager;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.cache.CrestCache;
import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.enums.SiegeSide;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.itemcontainer.ClanWarehouse;
import net.sf.l2j.gameserver.model.itemcontainer.ItemContainer;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Clan {
    public static final int PENALTY_TYPE_CLAN_LEAVED = 1;
    public static final int PENALTY_TYPE_CLAN_DISMISSED = 2;
    public static final int PENALTY_TYPE_DISMISS_CLAN = 3;
    public static final int PENALTY_TYPE_DISSOLVE_ALLY = 4;
    public static final int CP_NOTHING = 0;
    public static final int CP_CL_JOIN_CLAN = 2;
    public static final int CP_CL_GIVE_TITLE = 4;
    public static final int CP_CL_VIEW_WAREHOUSE = 8;
    public static final int CP_CL_MANAGE_RANKS = 16;
    public static final int CP_CL_PLEDGE_WAR = 32;
    public static final int CP_CL_DISMISS = 64;
    public static final int CP_CL_REGISTER_CREST = 128;
    public static final int CP_CL_MASTER_RIGHTS = 256;
    public static final int CP_CL_MANAGE_LEVELS = 512;
    public static final int CP_CH_OPEN_DOOR = 1024;
    public static final int CP_CH_OTHER_RIGHTS = 2048;
    public static final int CP_CH_AUCTION = 4096;
    public static final int CP_CH_DISMISS = 8192;
    public static final int CP_CH_SET_FUNCTIONS = 16384;
    public static final int CP_CS_OPEN_DOOR = 32768;
    public static final int CP_CS_MANOR_ADMIN = 65536;
    public static final int CP_CS_MANAGE_SIEGE = 131072;
    public static final int CP_CS_USE_FUNCTIONS = 262144;
    public static final int CP_CS_DISMISS = 524288;
    public static final int CP_CS_TAXES = 1048576;
    public static final int CP_CS_MERCENARIES = 2097152;
    public static final int CP_CS_SET_FUNCTIONS = 4194304;
    public static final int CP_ALL = 8388606;
    public static final int SUBUNIT_ACADEMY = -1;
    public static final int SUBUNIT_ROYAL1 = 100;
    public static final int SUBUNIT_ROYAL2 = 200;
    public static final int SUBUNIT_KNIGHT1 = 1001;
    public static final int SUBUNIT_KNIGHT2 = 1002;
    public static final int SUBUNIT_KNIGHT3 = 2001;
    public static final int SUBUNIT_KNIGHT4 = 2002;
    private static final CLogger LOGGER = new CLogger(Clan.class.getName());
    private static final String LOAD_MEMBERS = "SELECT char_name,level,classid,obj_Id,title,power_grade,subpledge,apprentice,sponsor,sex,race FROM characters WHERE clanid=?";
    private static final String LOAD_SUBPLEDGES = "SELECT sub_pledge_id,name,leader_id FROM clan_subpledges WHERE clan_id=?";
    private static final String LOAD_PRIVILEDGES = "SELECT privs,`rank` FROM clan_privs WHERE clan_id=?";
    private static final String LOAD_SKILLS = "SELECT skill_id,skill_level FROM clan_skills WHERE clan_id=?";
    private static final String RESET_MEMBER_PRIVS = "UPDATE characters SET clan_privs=0 WHERE obj_Id=?";
    private static final String REMOVE_MEMBER = "UPDATE characters SET clanid=0, title=?, clan_join_expiry_time=?, clan_create_expiry_time=?, clan_privs=0, wantspeace=0, subpledge=0, lvl_joined_academy=0, apprentice=0, sponsor=0 WHERE obj_Id=?";
    private static final String REMOVE_APPRENTICE = "UPDATE characters SET apprentice=0 WHERE apprentice=?";
    private static final String REMOVE_SPONSOR = "UPDATE characters SET sponsor=0 WHERE sponsor=?";
    private static final String UPDATE_CLAN = "UPDATE clan_data SET leader_id=?,new_leader_id=?,ally_id=?,ally_name=?,reputation_score=?,ally_penalty_expiry_time=?,ally_penalty_type=?,char_penalty_expiry_time=?,dissolving_expiry_time=? WHERE clan_id=?";
    private static final String STORE_CLAN = "INSERT INTO clan_data (clan_id,clan_name,clan_level,hasCastle,ally_id,ally_name,leader_id,new_leader_id,crest_id,crest_large_id,ally_crest_id) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
    private static final String UPDATE_NOTICE = "UPDATE clan_data SET enabled=?,notice=? WHERE clan_id=?";
    private static final String UPDATE_INTRODUCTION = "UPDATE clan_data SET introduction=? WHERE clan_id=?";
    private static final String ADD_OR_UPDATE_SKILL = "INSERT INTO clan_skills (clan_id,skill_id,skill_level) VALUES (?,?,?) ON DUPLICATE KEY UPDATE skill_level=VALUES(skill_level)";
    private static final String INSERT_SUBPLEDGE = "INSERT INTO clan_subpledges (clan_id,sub_pledge_id,name,leader_id) values (?,?,?,?)";
    private static final String UPDATE_SUBPLEDGE = "UPDATE clan_subpledges SET leader_id=?, name=? WHERE clan_id=? AND sub_pledge_id=?";
    private static final String ADD_OR_UPDATE_PRIVILEDGE = "INSERT INTO clan_privs (clan_id,rank,privs) VALUES (?,?,?) ON DUPLICATE KEY UPDATE privs=VALUES(privs)";
    private static final String UPDATE_CRP = "UPDATE clan_data SET reputation_score=? WHERE clan_id=?";
    private static final String UPDATE_AUCTION = "UPDATE clan_data SET auction_bid_at=? WHERE clan_id=?";
    private static final String UPDATE_CLAN_LEVEL = "UPDATE clan_data SET clan_level = ? WHERE clan_id = ?";
    private static final String UPDATE_CLAN_CREST = "UPDATE clan_data SET crest_id = ? WHERE clan_id = ?";
    private static final String UPDATE_LARGE_CREST = "UPDATE clan_data SET crest_large_id = ? WHERE clan_id = ?";
    private static final int MAX_NOTICE_LENGTH = 8192;

    private static final int MAX_INTRODUCTION_LENGTH = 300;

    private final Map<Integer, ClanMember> _members = new ConcurrentHashMap<>();

    private final Map<Integer, Long> _warPenaltyExpiryTime = new ConcurrentHashMap<>();

    private final Map<Integer, L2Skill> _skills = new ConcurrentHashMap<>();

    private final Map<Integer, Integer> _priviledges = new ConcurrentHashMap<>();

    private final Map<Integer, SubPledge> _subPledges = new ConcurrentHashMap<>();

    private final Set<Integer> _atWarWith = ConcurrentHashMap.newKeySet();

    private final Set<Integer> _atWarAttackers = ConcurrentHashMap.newKeySet();

    private final ItemContainer _warehouse = new ClanWarehouse(this);

    private String _name;

    private int _clanId;

    private ClanMember _leader;

    private int _newLeaderId;

    private String _allyName;

    private int _allyId;

    private int _level;

    private int _castleId;

    private int _chId;

    private int _crestId;

    private int _crestLargeId;

    private int _allyCrestId;

    private int _auctionBiddedAt;

    private long _allyPenaltyExpiryTime;

    private int _allyPenaltyType;

    private long _charPenaltyExpiryTime;

    private long _dissolvingExpiryTime;

    private Forum _forum;

    private int _reputationScore;

    private int _rank;

    private String _notice;

    private boolean _noticeEnabled;

    private String _introduction;

    private int _siegeKills;

    private int _siegeDeaths;

    private Npc _flag;

    public Clan(int clanId, int leaderId) {
        this._clanId = clanId;
        for (int i = 1; i < 10; i++)
            this._priviledges.put(i, 0);
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("SELECT char_name,level,classid,obj_Id,title,power_grade,subpledge,apprentice,sponsor,sex,race FROM characters WHERE clanid=?");
                try {
                    ps.setInt(1, this._clanId);
                    ResultSet rs = ps.executeQuery();
                    try {
                        while (rs.next()) {
                            ClanMember member = new ClanMember(this, rs);
                            if (member.getObjectId() == leaderId) {
                                setLeader(member);
                            } else {
                                this._members.put(member.getObjectId(), member);
                            }
                            member.setApprenticeAndSponsor(rs.getInt("apprentice"), rs.getInt("sponsor"));
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
                ps = con.prepareStatement("SELECT sub_pledge_id,name,leader_id FROM clan_subpledges WHERE clan_id=?");
                try {
                    ps.setInt(1, this._clanId);
                    ResultSet rs = ps.executeQuery();
                    try {
                        while (rs.next()) {
                            int id = rs.getInt("sub_pledge_id");
                            this._subPledges.put(id, new SubPledge(id, rs.getString("name"), rs.getInt("leader_id")));
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
                ps = con.prepareStatement("SELECT privs,`rank` FROM clan_privs WHERE clan_id=?");
                try {
                    ps.setInt(1, this._clanId);
                    ResultSet rs = ps.executeQuery();
                    try {
                        while (rs.next())
                            this._priviledges.put(rs.getInt("rank"), rs.getInt("privs"));
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
                ps = con.prepareStatement("SELECT skill_id,skill_level FROM clan_skills WHERE clan_id=?");
                try {
                    ps.setInt(1, this._clanId);
                    ResultSet rs = ps.executeQuery();
                    try {
                        while (rs.next()) {
                            int id = rs.getInt("skill_id");
                            int level = rs.getInt("skill_level");
                            L2Skill skill = SkillTable.getInstance().getInfo(id, level);
                            if (skill == null)
                                continue;
                            this._skills.put(id, skill);
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
            LOGGER.error("Error while restoring clan.", e);
        }
        if (this._crestId != 0 && CrestCache.getInstance().getCrest(CrestCache.CrestType.PLEDGE, this._crestId) == null) {
            LOGGER.warn("Removing non-existent crest for clan {}, crestId: {}.", toString(), this._crestId);
            changeClanCrest(0);
        }
        if (this._crestLargeId != 0 && CrestCache.getInstance().getCrest(CrestCache.CrestType.PLEDGE_LARGE, this._crestLargeId) == null) {
            LOGGER.warn("Removing non-existent large crest for clan {}, crestLargeId: {}.", toString(), this._crestLargeId);
            changeLargeCrest(0);
        }
        if (this._allyCrestId != 0 && CrestCache.getInstance().getCrest(CrestCache.CrestType.ALLY, this._allyCrestId) == null) {
            LOGGER.warn("Removing non-existent ally crest for clan {}, allyCrestId: {}.", toString(), this._allyCrestId);
            changeAllyCrest(0, true);
        }
        this._warehouse.restore();
    }

    public Clan(int clanId, String clanName) {
        this._clanId = clanId;
        this._name = clanName;
        for (int i = 1; i < 10; i++)
            this._priviledges.put(i, 0);
    }

    public static boolean checkAllyJoinCondition(Player player, Player target) {
        if (player == null)
            return false;
        if (player.getAllyId() == 0 || !player.isClanLeader() || player.getClanId() != player.getAllyId()) {
            player.sendPacket(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER);
            return false;
        }
        Clan leaderClan = player.getClan();
        if (leaderClan.getAllyPenaltyType() == 3 && leaderClan.getAllyPenaltyExpiryTime() > System.currentTimeMillis()) {
            player.sendPacket(SystemMessageId.CANT_INVITE_CLAN_WITHIN_1_DAY);
            return false;
        }
        if (target == null) {
            player.sendPacket(SystemMessageId.SELECT_USER_TO_INVITE);
            return false;
        }
        if (player.getObjectId() == target.getObjectId()) {
            player.sendPacket(SystemMessageId.CANNOT_INVITE_YOURSELF);
            return false;
        }
        if (target.getClan() == null) {
            player.sendPacket(SystemMessageId.TARGET_MUST_BE_IN_CLAN);
            return false;
        }
        if (!target.isClanLeader()) {
            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IS_NOT_A_CLAN_LEADER).addCharName(target));
            return false;
        }
        Clan targetClan = target.getClan();
        if (target.getAllyId() != 0) {
            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CLAN_ALREADY_MEMBER_OF_S2_ALLIANCE).addString(targetClan.getName()).addString(targetClan.getAllyName()));
            return false;
        }
        if (targetClan.getAllyPenaltyExpiryTime() > System.currentTimeMillis()) {
            if (targetClan.getAllyPenaltyType() == 1) {
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANT_ENTER_ALLIANCE_WITHIN_1_DAY).addString(targetClan.getName()));
                return false;
            }
            if (targetClan.getAllyPenaltyType() == 2) {
                player.sendPacket(SystemMessageId.CANT_ENTER_ALLIANCE_WITHIN_1_DAY);
                return false;
            }
        }
        for (Castle castle : CastleManager.getInstance().getCastles()) {
            if (castle.getSiege().isOnOppositeSide(leaderClan, targetClan)) {
                player.sendPacket(SystemMessageId.OPPOSING_CLAN_IS_PARTICIPATING_IN_SIEGE);
                return false;
            }
        }
        if (leaderClan.isAtWarWith(targetClan.getClanId())) {
            player.sendPacket(SystemMessageId.MAY_NOT_ALLY_CLAN_BATTLE);
            return false;
        }
        if (ClanTable.getInstance().getClanAllies(player.getAllyId()).size() >= Config.ALT_MAX_NUM_OF_CLANS_IN_ALLY) {
            player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_THE_LIMIT);
            return false;
        }
        return true;
    }

    public String getName() {
        return this._name;
    }

    public void setName(String name) {
        this._name = name;
    }

    public int getClanId() {
        return this._clanId;
    }

    public void setClanId(int clanId) {
        this._clanId = clanId;
    }

    public ClanMember getLeader() {
        return this._leader;
    }

    public void setLeader(ClanMember leader) {
        this._leader = leader;
        this._members.put(leader.getObjectId(), leader);
    }

    public int getLeaderId() {
        return this._leader.getObjectId();
    }

    public String getLeaderName() {
        return (this._leader == null) ? "" : this._leader.getName();
    }

    public void setNewLeader(ClanMember member) {
        Player newLeader = member.getPlayerInstance();
        ClanMember exMember = getLeader();
        Player exLeader = exMember.getPlayerInstance();
        if (exLeader != null) {
            if (exLeader.isFlying())
                exLeader.dismount();
            if (this._level >= Config.MINIMUM_CLAN_LEVEL)
                exLeader.removeSiegeSkills();
            exLeader.setClanPrivileges(0);
            exLeader.broadcastUserInfo();
        } else {
            try {
                Connection con = ConnectionPool.getConnection();
                try {
                    PreparedStatement ps = con.prepareStatement("UPDATE characters SET clan_privs=0 WHERE obj_Id=?");
                    try {
                        ps.setInt(1, getLeaderId());
                        ps.executeUpdate();
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
                LOGGER.error("Couldn't update clan privs for old clan leader.", e);
            }
        }
        setLeader(member);
        if (getNewLeaderId() != 0)
            setNewLeaderId(0, true);
        updateClanInDB();
        if (exLeader != null) {
            exLeader.setPledgeClass(ClanMember.calculatePledgeClass(exLeader));
            exLeader.broadcastUserInfo();
            exLeader.checkItemRestriction();
        }
        if (newLeader != null) {
            newLeader.setPledgeClass(ClanMember.calculatePledgeClass(newLeader));
            newLeader.setClanPrivileges(8388606);
            if (this._level >= Config.MINIMUM_CLAN_LEVEL)
                newLeader.addSiegeSkills();
            newLeader.broadcastUserInfo();
        } else {
            try {
                Connection con = ConnectionPool.getConnection();
                try {
                    PreparedStatement ps = con.prepareStatement("UPDATE characters SET clan_privs=0 WHERE obj_Id=?");
                    try {
                        ps.setInt(1, getLeaderId());
                        ps.executeUpdate();
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
                LOGGER.error("Couldn't update clan privs for new clan leader.", e);
            }
        }
        broadcastClanStatus();
        broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_LEADER_PRIVILEGES_HAVE_BEEN_TRANSFERRED_TO_S1).addString(member.getName()));
    }

    public int getNewLeaderId() {
        return this._newLeaderId;
    }

    public void setNewLeaderId(int objectId, boolean storeInDb) {
        this._newLeaderId = objectId;
        if (storeInDb)
            updateClanInDB();
    }

    public String getAllyName() {
        return this._allyName;
    }

    public void setAllyName(String allyName) {
        this._allyName = allyName;
    }

    public int getAllyId() {
        return this._allyId;
    }

    public void setAllyId(int allyId) {
        this._allyId = allyId;
    }

    public int getAllyCrestId() {
        return this._allyCrestId;
    }

    public void setAllyCrestId(int allyCrestId) {
        this._allyCrestId = allyCrestId;
    }

    public int getLevel() {
        return this._level;
    }

    public void setLevel(int level) {
        this._level = level;
        if (Config.ENABLE_COMMUNITY_BOARD && this._level >= 2 && this._forum == null) {
            Forum forum = ForumsBBSManager.getInstance().getForumByName("ClanRoot");
            if (forum != null) {
                this._forum = forum.getChildByName(this._name);
                if (this._forum == null)
                    this._forum = ForumsBBSManager.getInstance().createNewForum(this._name, forum, 2, 2, this._clanId);
            }
        }
    }

    public int getCastleId() {
        return this._castleId;
    }

    public boolean hasCastle() {
        return (this._castleId > 0);
    }

    public void setCastle(int castle) {
        this._castleId = castle;
    }

    public int getClanHallId() {
        return this._chId;
    }

    public boolean hasClanHall() {
        return (this._chId > 0);
    }

    public void setClanHall(int id) {
        this._chId = id;
    }

    public int getCrestId() {
        return this._crestId;
    }

    public void setCrestId(int crestId) {
        this._crestId = crestId;
    }

    public int getCrestLargeId() {
        return this._crestLargeId;
    }

    public void setCrestLargeId(int crestLargeId) {
        this._crestLargeId = crestLargeId;
    }

    public int getSiegeKills() {
        return this._siegeKills;
    }

    public void setSiegeKills(int value) {
        this._siegeKills = value;
    }

    public int getSiegeDeaths() {
        return this._siegeDeaths;
    }

    public void setSiegeDeaths(int value) {
        this._siegeDeaths = value;
    }

    public Npc getFlag() {
        return this._flag;
    }

    public void setFlag(Npc flag) {
        if (flag == null && this._flag != null)
            this._flag.deleteMe();
        this._flag = flag;
    }

    public long getAllyPenaltyExpiryTime() {
        return this._allyPenaltyExpiryTime;
    }

    public int getAllyPenaltyType() {
        return this._allyPenaltyType;
    }

    public void setAllyPenaltyExpiryTime(long expiryTime, int penaltyType) {
        this._allyPenaltyExpiryTime = expiryTime;
        this._allyPenaltyType = penaltyType;
    }

    public long getCharPenaltyExpiryTime() {
        return this._charPenaltyExpiryTime;
    }

    public void setCharPenaltyExpiryTime(long time) {
        this._charPenaltyExpiryTime = time;
    }

    public long getDissolvingExpiryTime() {
        return this._dissolvingExpiryTime;
    }

    public void setDissolvingExpiryTime(long time) {
        this._dissolvingExpiryTime = time;
    }

    public ClanMember getClanMember(int objectId) {
        return this._members.get(objectId);
    }

    public ClanMember getClanMember(String name) {
        return this._members.values().stream().filter(m -> m.getName().equals(name)).findFirst().orElse(null);
    }

    public boolean isMember(int objectId) {
        return this._members.containsKey(objectId);
    }

    public void addClanMember(Player player) {
        ClanMember member = new ClanMember(this, player);
        this._members.put(member.getObjectId(), member);
        member.setPlayerInstance(player);
        player.setClan(this);
        player.setPledgeClass(ClanMember.calculatePledgeClass(player));
        for (Castle castle : CastleManager.getInstance().getCastles()) {
            Siege siege = castle.getSiege();
            if (!siege.isInProgress())
                continue;
            if (siege.checkSide(this, SiegeSide.ATTACKER)) {
                player.setSiegeState((byte) 1);
                continue;
            }
            if (siege.checkSides(this, SiegeSide.DEFENDER, SiegeSide.OWNER))
                player.setSiegeState((byte) 2);
        }
        player.sendPacket(new PledgeShowMemberListUpdate(player));
        player.sendPacket(new UserInfo(player));
    }

    public void removeClanMember(int objectId, long clanJoinExpiryTime) {
        ClanMember exMember = this._members.remove(objectId);
        if (exMember == null)
            return;
        int subPledgeId = getLeaderSubPledge(objectId);
        if (subPledgeId != 0) {
            SubPledge pledge = getSubPledge(subPledgeId);
            if (pledge != null) {
                pledge.setLeaderId(0);
                updateSubPledgeInDB(pledge);
            }
        }
        if (exMember.getApprentice() != 0) {
            ClanMember apprentice = getClanMember(exMember.getApprentice());
            if (apprentice != null) {
                if (apprentice.getPlayerInstance() != null) {
                    apprentice.getPlayerInstance().setSponsor(0);
                } else {
                    apprentice.setApprenticeAndSponsor(0, 0);
                }
                apprentice.saveApprenticeAndSponsor(0, 0);
            }
        }
        if (exMember.getSponsor() != 0) {
            ClanMember sponsor = getClanMember(exMember.getSponsor());
            if (sponsor != null) {
                if (sponsor.getPlayerInstance() != null) {
                    sponsor.getPlayerInstance().setApprentice(0);
                } else {
                    sponsor.setApprenticeAndSponsor(0, 0);
                }
                sponsor.saveApprenticeAndSponsor(0, 0);
            }
        }
        exMember.saveApprenticeAndSponsor(0, 0);
        if (hasCastle())
            CastleManager.getInstance().getCastleById(this._castleId).checkItemsForMember(exMember);
        if (exMember.isOnline()) {
            Player player = exMember.getPlayerInstance();
            if (!player.isNoble())
                player.setTitle("");
            if (player.getActiveWarehouse() != null)
                player.setActiveWarehouse(null);
            player.setApprentice(0);
            player.setSponsor(0);
            player.setSiegeState((byte) 0);
            if (player.isClanLeader()) {
                player.removeSiegeSkills();
                player.setClanCreateExpiryTime(System.currentTimeMillis() + Config.ALT_CLAN_CREATE_DAYS * 86400000L);
            }
            for (L2Skill skill : this._skills.values())
                player.removeSkill(skill.getId(), false);
            player.sendSkillList();
            player.setClan(null);
            if (exMember.getPledgeType() != -1)
                player.setClanJoinExpiryTime(clanJoinExpiryTime);
            player.setPledgeClass(ClanMember.calculatePledgeClass(player));
            player.broadcastUserInfo();
            player.sendPacket(PledgeShowMemberListDeleteAll.STATIC_PACKET);
        } else {
            try {
                Connection con = ConnectionPool.getConnection();
                try {
                    PreparedStatement ps = con.prepareStatement("UPDATE characters SET clanid=0, title=?, clan_join_expiry_time=?, clan_create_expiry_time=?, clan_privs=0, wantspeace=0, subpledge=0, lvl_joined_academy=0, apprentice=0, sponsor=0 WHERE obj_Id=?");
                    try {
                        ps.setString(1, "");
                        ps.setLong(2, clanJoinExpiryTime);
                        ps.setLong(3, (this._leader.getObjectId() == objectId) ? (System.currentTimeMillis() + Config.ALT_CLAN_CREATE_DAYS * 86400000L) : 0L);
                        ps.setInt(4, exMember.getObjectId());
                        ps.executeUpdate();
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
                    ps = con.prepareStatement("UPDATE characters SET apprentice=0 WHERE apprentice=?");
                    try {
                        ps.setInt(1, exMember.getObjectId());
                        ps.executeUpdate();
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
                    ps = con.prepareStatement("UPDATE characters SET sponsor=0 WHERE sponsor=?");
                    try {
                        ps.setInt(1, exMember.getObjectId());
                        ps.executeUpdate();
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
                LOGGER.error("Error while removing clan member.", e);
            }
        }
    }

    public Collection<ClanMember> getMembers() {
        return this._members.values();
    }

    public int getMembersCount() {
        return this._members.size();
    }

    public int getSubPledgeMembersCount(int pledgeType) {
        return (int) this._members.values().stream().filter(m -> (m.getPledgeType() == pledgeType)).count();
    }

    public String getSubPledgeLeaderName(int pledgeType) {
        if (pledgeType == 0)
            return this._leader.getName();
        SubPledge subPledge = this._subPledges.get(pledgeType);
        int leaderId = subPledge.getLeaderId();
        if (subPledge.getId() == -1 || leaderId == 0)
            return "";
        if (!this._members.containsKey(leaderId)) {
            LOGGER.warn("SubPledge leader {} is missing from clan: {}.", leaderId, toString());
            return "";
        }
        return this._members.get(leaderId).getName();
    }

    public int getMaxNrOfMembers(int pledgeType) {
        return switch (pledgeType) {
            case 0 -> switch (this._level) {
                case 0 -> 10;
                case 1 -> 15;
                case 2 -> 20;
                case 3 -> 30;
                default -> 40;
            };
            case -1, 100, 200 -> 20;
            case 1001, 1002, 2001, 2002 -> 10;
            default -> 0;
        };
    }

    public Player[] getOnlineMembers() {
        List<Player> list = new ArrayList<>();
        for (ClanMember temp : this._members.values()) {
            if (temp != null && temp.isOnline())
                list.add(temp.getPlayerInstance());
        }
        return list.toArray(new Player[0]);
    }

    public int getOnlineMembersCount() {
        return (int) this._members.values().stream().filter(ClanMember::isOnline).count();
    }

    public void updateClanInDB() {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("UPDATE clan_data SET leader_id=?,new_leader_id=?,ally_id=?,ally_name=?,reputation_score=?,ally_penalty_expiry_time=?,ally_penalty_type=?,char_penalty_expiry_time=?,dissolving_expiry_time=? WHERE clan_id=?");
                try {
                    ps.setInt(1, this._leader.getObjectId());
                    ps.setInt(2, this._newLeaderId);
                    ps.setInt(3, this._allyId);
                    ps.setString(4, this._allyName);
                    ps.setInt(5, this._reputationScore);
                    ps.setLong(6, this._allyPenaltyExpiryTime);
                    ps.setInt(7, this._allyPenaltyType);
                    ps.setLong(8, this._charPenaltyExpiryTime);
                    ps.setLong(9, this._dissolvingExpiryTime);
                    ps.setInt(10, this._clanId);
                    ps.executeUpdate();
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
            LOGGER.error("Error while updating clan.", e);
        }
    }

    public void store() {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("INSERT INTO clan_data (clan_id,clan_name,clan_level,hasCastle,ally_id,ally_name,leader_id,new_leader_id,crest_id,crest_large_id,ally_crest_id) VALUES (?,?,?,?,?,?,?,?,?,?,?)");
                try {
                    ps.setInt(1, this._clanId);
                    ps.setString(2, this._name);
                    ps.setInt(3, this._level);
                    ps.setInt(4, this._castleId);
                    ps.setInt(5, this._allyId);
                    ps.setString(6, this._allyName);
                    ps.setInt(7, this._leader.getObjectId());
                    ps.setInt(8, this._newLeaderId);
                    ps.setInt(9, this._crestId);
                    ps.setInt(10, this._crestLargeId);
                    ps.setInt(11, this._allyCrestId);
                    ps.executeUpdate();
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
            LOGGER.error("Error while storing clan.", e);
        }
    }

    private void storeNotice(String notice, boolean enabled) {
        if (notice == null)
            notice = "";
        if (notice.length() > 8192)
            notice = notice.substring(0, 8191);
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("UPDATE clan_data SET enabled=?,notice=? WHERE clan_id=?");
                try {
                    ps.setBoolean(1, enabled);
                    ps.setString(2, notice);
                    ps.setInt(3, this._clanId);
                    ps.executeUpdate();
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
            LOGGER.error("Error while storing notice.", e);
        }
        this._notice = notice;
        this._noticeEnabled = enabled;
    }

    public void setNoticeEnabledAndStore(boolean enabled) {
        storeNotice(this._notice, enabled);
    }

    public void setNoticeAndStore(String notice) {
        storeNotice(notice, this._noticeEnabled);
    }

    public boolean isNoticeEnabled() {
        return this._noticeEnabled;
    }

    public void setNoticeEnabled(boolean enabled) {
        this._noticeEnabled = enabled;
    }

    public String getNotice() {
        return (this._notice == null) ? "" : this._notice;
    }

    public void setNotice(String notice) {
        this._notice = notice;
    }

    public String getIntroduction() {
        return (this._introduction == null) ? "" : this._introduction;
    }

    public void setIntroduction(String intro, boolean saveOnDb) {
        if (saveOnDb) {
            if (intro == null)
                intro = "";
            if (intro.length() > 300)
                intro = intro.substring(0, 299);
            try {
                Connection con = ConnectionPool.getConnection();
                try {
                    PreparedStatement ps = con.prepareStatement("UPDATE clan_data SET introduction=? WHERE clan_id=?");
                    try {
                        ps.setString(1, intro);
                        ps.setInt(2, this._clanId);
                        ps.executeUpdate();
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
                LOGGER.error("Error while storing introduction.", e);
            }
        }
        this._introduction = intro;
    }

    public final Map<Integer, L2Skill> getClanSkills() {
        return this._skills;
    }

    public void addNewSkill(L2Skill newSkill) {
        if (newSkill == null)
            return;
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("INSERT INTO clan_skills (clan_id,skill_id,skill_level) VALUES (?,?,?) ON DUPLICATE KEY UPDATE skill_level=VALUES(skill_level)");
                try {
                    ps.setInt(1, this._clanId);
                    ps.setInt(2, newSkill.getId());
                    ps.setInt(3, newSkill.getLevel());
                    ps.executeUpdate();
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
            LOGGER.error("Error while storing a clan skill.", e);
            return;
        }
        this._skills.put(newSkill.getId(), newSkill);
        PledgeSkillListAdd pledgeListAdd = new PledgeSkillListAdd(newSkill.getId(), newSkill.getLevel());
        PledgeSkillList pledgeList = new PledgeSkillList(this);
        SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_SKILL_S1_ADDED).addSkillName(newSkill.getId());
        for (Player temp : getOnlineMembers()) {
            if (newSkill.getMinPledgeClass() <= temp.getPledgeClass()) {
                temp.addSkill(newSkill, false);
                temp.sendPacket(pledgeListAdd);
                temp.sendSkillList();
            }
            temp.sendPacket(pledgeList);
            temp.sendPacket(sm);
        }
    }

    public void addSkillEffects(Player player) {
        if (player == null || this._reputationScore <= 0 || player.isInOlympiadMode())
            return;
        for (L2Skill skill : this._skills.values()) {
            if (skill.getMinPledgeClass() <= player.getPledgeClass())
                player.addSkill(skill, false);
        }
    }

    public void broadcastToOnlineAllyMembers(L2GameServerPacket packet) {
        for (Clan clan : ClanTable.getInstance().getClanAllies(this._allyId)) {
            clan.broadcastToOnlineMembers(packet);
        }
    }

    public void broadcastToOnlineMembers(L2GameServerPacket... packets) {
        for (ClanMember member : this._members.values()) {
            if (member != null && member.isOnline())
                for (L2GameServerPacket packet : packets)
                    member.getPlayerInstance().sendPacket(packet);
        }
    }

    public void broadcastToOtherOnlineMembers(L2GameServerPacket packet, Player player) {
        for (ClanMember member : this._members.values()) {
            if (member != null && member.isOnline() && member.getPlayerInstance() != player)
                member.getPlayerInstance().sendPacket(packet);
        }
    }

    public String toString() {
        return this._name + "[" + this._name + "]";
    }

    public ItemContainer getWarehouse() {
        return this._warehouse;
    }

    public boolean isAtWarWith(int id) {
        return this._atWarWith.contains(id);
    }

    public boolean isAtWarAttacker(int id) {
        return this._atWarAttackers.contains(id);
    }

    public void setEnemyClan(int clanId) {
        this._atWarWith.add(clanId);
    }

    public void setAttackerClan(int clanId) {
        this._atWarAttackers.add(clanId);
    }

    public void deleteEnemyClan(int clanId) {
        this._atWarWith.remove(clanId);
    }

    public void deleteAttackerClan(int clanId) {
        this._atWarAttackers.remove(clanId);
    }

    public void addWarPenaltyTime(int clanId, long expiryTime) {
        this._warPenaltyExpiryTime.put(clanId, expiryTime);
    }

    public boolean hasWarPenaltyWith(int clanId) {
        if (!this._warPenaltyExpiryTime.containsKey(clanId))
            return false;
        return (this._warPenaltyExpiryTime.get(clanId) > System.currentTimeMillis());
    }

    public Map<Integer, Long> getWarPenalty() {
        return this._warPenaltyExpiryTime;
    }

    public boolean isAtWar() {
        return !this._atWarWith.isEmpty();
    }

    public Set<Integer> getWarList() {
        return this._atWarWith;
    }

    public Set<Integer> getAttackerList() {
        return this._atWarAttackers;
    }

    public void broadcastClanStatus() {
        for (Player member : getOnlineMembers()) {
            member.sendPacket(PledgeShowMemberListDeleteAll.STATIC_PACKET);
            member.sendPacket(new PledgeShowMemberListAll(this, 0));
            for (SubPledge sp : getAllSubPledges())
                member.sendPacket(new PledgeShowMemberListAll(this, sp.getId()));
            member.sendPacket(new UserInfo(member));
        }
    }

    public boolean isSubPledgeLeader(int objectId) {
        for (SubPledge sp : getAllSubPledges()) {
            if (sp.getLeaderId() == objectId)
                return true;
        }
        return false;
    }

    public final SubPledge getSubPledge(int pledgeType) {
        return this._subPledges.get(pledgeType);
    }

    public final SubPledge getSubPledge(String pledgeName) {
        return this._subPledges.values().stream().filter(s -> s.getName().equalsIgnoreCase(pledgeName)).findFirst().orElse(null);
    }

    public final SubPledge[] getAllSubPledges() {
        return (SubPledge[]) this._subPledges.values().toArray((Object[]) new SubPledge[this._subPledges.size()]);
    }

    public SubPledge createSubPledge(Player player, int type, int leaderId, String subPledgeName) {
        int pledgeType = getAvailablePledgeTypes(type);
        if (pledgeType == 0) {
            player.sendPacket((type == -1) ? SystemMessageId.CLAN_HAS_ALREADY_ESTABLISHED_A_CLAN_ACADEMY : SystemMessageId.YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_MILITARY_UNIT);
            return null;
        }
        if (this._leader.getObjectId() == leaderId) {
            player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_MILITARY_UNIT);
            return null;
        }
        if (pledgeType != -1 && ((this._reputationScore < 5000 && pledgeType < 1001) || (this._reputationScore < 10000 && pledgeType > 200))) {
            player.sendPacket(SystemMessageId.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
            return null;
        }
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("INSERT INTO clan_subpledges (clan_id,sub_pledge_id,name,leader_id) values (?,?,?,?)");
                try {
                    ps.setInt(1, this._clanId);
                    ps.setInt(2, pledgeType);
                    ps.setString(3, subPledgeName);
                    ps.setInt(4, (pledgeType != -1) ? leaderId : 0);
                    ps.executeUpdate();
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
            LOGGER.error("Error creating subpledge.", e);
            return null;
        }
        SubPledge subPledge = new SubPledge(pledgeType, subPledgeName, leaderId);
        this._subPledges.put(pledgeType, subPledge);
        if (pledgeType != -1)
            if (pledgeType < 1001) {
                takeReputationScore(5000);
            } else if (pledgeType > 200) {
                takeReputationScore(10000);
            }
        broadcastToOnlineMembers(new PledgeShowInfoUpdate(this), new PledgeReceiveSubPledgeCreated(subPledge, this));
        return subPledge;
    }

    public int getAvailablePledgeTypes(int pledgeType) {
        if (this._subPledges.get(pledgeType) != null)
            switch (pledgeType) {
                case -1:
                    return 0;
                case 100:
                    pledgeType = getAvailablePledgeTypes(200);
                    break;
                case 200:
                    return 0;
                case 1001:
                    pledgeType = getAvailablePledgeTypes(1002);
                    break;
                case 1002:
                    pledgeType = getAvailablePledgeTypes(2001);
                    break;
                case 2001:
                    pledgeType = getAvailablePledgeTypes(2002);
                    break;
                case 2002:
                    return 0;
            }
        return pledgeType;
    }

    public void updateSubPledgeInDB(SubPledge pledge) {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("UPDATE clan_subpledges SET leader_id=?, name=? WHERE clan_id=? AND sub_pledge_id=?");
                try {
                    ps.setInt(1, pledge.getLeaderId());
                    ps.setString(2, pledge.getName());
                    ps.setInt(3, this._clanId);
                    ps.setInt(4, pledge.getId());
                    ps.executeUpdate();
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
            LOGGER.error("Error updating subpledge.", e);
        }
    }

    public final Map<Integer, Integer> getPriviledges() {
        return this._priviledges;
    }

    public int getPriviledgesByRank(int rank) {
        return this._priviledges.getOrDefault(rank, 0);
    }

    public void setPriviledgesForRank(int rank, int privs) {
        if (!this._priviledges.containsKey(rank))
            return;
        this._priviledges.put(rank, privs);
        for (Player member : getOnlineMembers()) {
            if (member.getPowerGrade() == rank)
                member.setClanPrivileges(privs);
        }
        broadcastClanStatus();
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("INSERT INTO clan_privs (clan_id,rank,privs) VALUES (?,?,?) ON DUPLICATE KEY UPDATE privs=VALUES(privs)");
                try {
                    ps.setInt(1, this._clanId);
                    ps.setInt(2, rank);
                    ps.setInt(3, privs);
                    ps.executeUpdate();
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
            LOGGER.error("Error while storing rank.", e);
        }
    }

    public int getLeaderSubPledge(int leaderId) {
        int id = 0;
        for (SubPledge sp : this._subPledges.values()) {
            if (sp.getLeaderId() == 0)
                continue;
            if (sp.getLeaderId() == leaderId)
                id = sp.getId();
        }
        return id;
    }

    public int getReputationScore() {
        return this._reputationScore;
    }

    private void setReputationScore(int value) {
        if (this._level < 5)
            return;
        boolean needRefresh = ((this._reputationScore > 0 && value <= 0) || (value > 0 && this._reputationScore <= 0));
        Player[] members = getOnlineMembers();
        this._reputationScore = MathUtil.limit(value, -100000000, 100000000);
        if (needRefresh) {
            Collection<L2Skill> skills = getClanSkills().values();
            if (this._reputationScore <= 0) {
                for (Player member : members) {
                    member.sendPacket(SystemMessageId.REPUTATION_POINTS_0_OR_LOWER_CLAN_SKILLS_DEACTIVATED);
                    for (L2Skill sk : skills)
                        member.removeSkill(sk.getId(), false);
                    member.sendSkillList();
                }
            } else {
                for (Player member : members) {
                    member.sendPacket(SystemMessageId.CLAN_SKILLS_WILL_BE_ACTIVATED_SINCE_REPUTATION_IS_0_OR_HIGHER);
                    for (L2Skill sk : skills) {
                        if (sk.getMinPledgeClass() <= member.getPledgeClass())
                            member.addSkill(sk, false);
                    }
                    member.sendSkillList();
                }
            }
        }
        PledgeShowInfoUpdate infoRefresh = new PledgeShowInfoUpdate(this);
        for (Player member : members)
            member.sendPacket(infoRefresh);
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("UPDATE clan_data SET reputation_score=? WHERE clan_id=?");
                try {
                    ps.setInt(1, this._reputationScore);
                    ps.setInt(2, this._clanId);
                    ps.executeUpdate();
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
            LOGGER.error("Error while updating clan reputation points.", e);
        }
    }

    public synchronized void addReputationScore(int value) {
        setReputationScore(this._reputationScore + value);
    }

    public synchronized void takeReputationScore(int value) {
        setReputationScore(this._reputationScore - value);
    }

    public int getRank() {
        return this._rank;
    }

    public void setRank(int rank) {
        this._rank = rank;
    }

    public int getAuctionBiddedAt() {
        return this._auctionBiddedAt;
    }

    public void setAuctionBiddedAt(int id) {
        this._auctionBiddedAt = id;
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("UPDATE clan_data SET auction_bid_at=? WHERE clan_id=?");
                try {
                    ps.setInt(1, id);
                    ps.setInt(2, this._clanId);
                    ps.executeUpdate();
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
            LOGGER.error("Error while updating clan auction.", e);
        }
    }

    public boolean checkClanJoinCondition(Player activeChar, Player target, int pledgeType) {
        if (activeChar == null)
            return false;
        if ((activeChar.getClanPrivileges() & 0x2) != 2) {
            activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
            return false;
        }
        if (target == null) {
            activeChar.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET);
            return false;
        }
        if (activeChar.getObjectId() == target.getObjectId()) {
            activeChar.sendPacket(SystemMessageId.CANNOT_INVITE_YOURSELF);
            return false;
        }
        if (this._charPenaltyExpiryTime > System.currentTimeMillis()) {
            activeChar.sendPacket(SystemMessageId.YOU_MUST_WAIT_BEFORE_ACCEPTING_A_NEW_MEMBER);
            return false;
        }
        if (target.getClanId() != 0) {
            activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_WORKING_WITH_ANOTHER_CLAN).addCharName(target));
            return false;
        }
        if (target.getClanJoinExpiryTime() > System.currentTimeMillis()) {
            activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_MUST_WAIT_BEFORE_JOINING_ANOTHER_CLAN).addCharName(target));
            return false;
        }
        if ((target.getLevel() > 40 || target.getClassId().level() >= 2) && pledgeType == -1) {
            activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DOESNOT_MEET_REQUIREMENTS_TO_JOIN_ACADEMY).addCharName(target));
            activeChar.sendPacket(SystemMessageId.ACADEMY_REQUIREMENTS);
            return false;
        }
        if (getSubPledgeMembersCount(pledgeType) >= getMaxNrOfMembers(pledgeType)) {
            if (pledgeType == 0) {
                activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CLAN_IS_FULL).addString(getName()));
            } else {
                activeChar.sendPacket(SystemMessageId.SUBCLAN_IS_FULL);
            }
            return false;
        }
        return true;
    }

    public void createAlly(Player player, String allyName) {
        if (player == null)
            return;
        if (!player.isClanLeader()) {
            player.sendPacket(SystemMessageId.ONLY_CLAN_LEADER_CREATE_ALLIANCE);
            return;
        }
        if (this._allyId != 0) {
            player.sendPacket(SystemMessageId.ALREADY_JOINED_ALLIANCE);
            return;
        }
        if (this._level < 5) {
            player.sendPacket(SystemMessageId.TO_CREATE_AN_ALLY_YOU_CLAN_MUST_BE_LEVEL_5_OR_HIGHER);
            return;
        }
        if (this._allyPenaltyType == 4 && this._allyPenaltyExpiryTime > System.currentTimeMillis()) {
            player.sendPacket(SystemMessageId.CANT_CREATE_ALLIANCE_10_DAYS_DISOLUTION);
            return;
        }
        if (this._dissolvingExpiryTime > System.currentTimeMillis()) {
            player.sendPacket(SystemMessageId.YOU_MAY_NOT_CREATE_ALLY_WHILE_DISSOLVING);
            return;
        }
        if (!StringUtil.isAlphaNumeric(allyName)) {
            player.sendPacket(SystemMessageId.INCORRECT_ALLIANCE_NAME);
            return;
        }
        if (allyName.length() > 16 || allyName.length() < 2) {
            player.sendPacket(SystemMessageId.INCORRECT_ALLIANCE_NAME_LENGTH);
            return;
        }
        if (ClanTable.getInstance().isAllyExists(allyName)) {
            player.sendPacket(SystemMessageId.ALLIANCE_ALREADY_EXISTS);
            return;
        }
        for (Castle castle : CastleManager.getInstance().getCastles()) {
            if (castle.getSiege().isInProgress() && castle.getSiege().checkSides(this)) {
                player.sendPacket(SystemMessageId.NO_ALLY_CREATION_WHILE_SIEGE);
                return;
            }
        }
        this._allyId = this._clanId;
        this._allyName = allyName;
        setAllyPenaltyExpiryTime(0L, 0);
        updateClanInDB();
        player.sendPacket(new UserInfo(player));
    }

    public void dissolveAlly(Player player) {
        if (this._allyId == 0) {
            player.sendPacket(SystemMessageId.NO_CURRENT_ALLIANCES);
            return;
        }
        if (!player.isClanLeader() || this._clanId != this._allyId) {
            player.sendPacket(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER);
            return;
        }
        for (Clan clan : ClanTable.getInstance().getClanAllies(this._allyId)) {
            for (Castle castle : CastleManager.getInstance().getCastles()) {
                if (castle.getSiege().isInProgress() && castle.getSiege().checkSides(clan)) {
                    player.sendPacket(SystemMessageId.CANNOT_DISSOLVE_ALLY_WHILE_IN_SIEGE);
                    return;
                }
            }
        }
        broadcastToOnlineAllyMembers(SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_DISOLVED));
        long currentTime = System.currentTimeMillis();
        for (Clan clan : ClanTable.getInstance().getClans()) {
            if (clan.getAllyId() == this._allyId && clan.getClanId() != this._clanId) {
                clan.setAllyId(0);
                clan.setAllyName(null);
                clan.setAllyPenaltyExpiryTime(0L, 0);
                clan.updateClanInDB();
            }
        }
        this._allyId = 0;
        this._allyName = null;
        changeAllyCrest(0, false);
        setAllyPenaltyExpiryTime(currentTime + Config.ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED * 86400000L, 4);
        updateClanInDB();
        player.deathPenalty(false, false, false);
    }

    public boolean levelUpClan(Player player) {
        if (!player.isClanLeader()) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
            return false;
        }
        if (System.currentTimeMillis() < this._dissolvingExpiryTime) {
            player.sendPacket(SystemMessageId.CANNOT_RISE_LEVEL_WHILE_DISSOLUTION_IN_PROGRESS);
            return false;
        }
        boolean increaseClanLevel = false;
        switch (this._level) {
            case 0:
                if (player.getSp() >= 30000 && player.reduceAdena("ClanLvl", 650000, player.getTarget(), true)) {
                    player.removeExpAndSp(0L, 30000);
                    increaseClanLevel = true;
                }
                break;
            case 1:
                if (player.getSp() >= 150000 && player.reduceAdena("ClanLvl", 2500000, player.getTarget(), true)) {
                    player.removeExpAndSp(0L, 150000);
                    increaseClanLevel = true;
                }
                break;
            case 2:
                if (player.getSp() >= 500000 && player.destroyItemByItemId("ClanLvl", 1419, 1, player.getTarget(), true)) {
                    player.removeExpAndSp(0L, 500000);
                    increaseClanLevel = true;
                }
                break;
            case 3:
                if (player.getSp() >= 1400000 && player.destroyItemByItemId("ClanLvl", 3874, 1, player.getTarget(), true)) {
                    player.removeExpAndSp(0L, 1400000);
                    increaseClanLevel = true;
                }
                break;
            case 4:
                if (player.getSp() >= 3500000 && player.destroyItemByItemId("ClanLvl", 3870, 1, player.getTarget(), true)) {
                    player.removeExpAndSp(0L, 3500000);
                    increaseClanLevel = true;
                }
                break;
            case 5:
                if (this._reputationScore >= 10000 && getMembersCount() >= 30) {
                    takeReputationScore(10000);
                    player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP).addNumber(10000));
                    increaseClanLevel = true;
                }
                break;
            case 6:
                if (this._reputationScore >= 20000 && getMembersCount() >= 80) {
                    takeReputationScore(20000);
                    player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP).addNumber(20000));
                    increaseClanLevel = true;
                }
                break;
            case 7:
                if (this._reputationScore >= 40000 && getMembersCount() >= 120) {
                    takeReputationScore(40000);
                    player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP).addNumber(40000));
                    increaseClanLevel = true;
                }
                break;
        }
        if (!increaseClanLevel) {
            player.sendPacket(SystemMessageId.FAILED_TO_INCREASE_CLAN_LEVEL);
            return false;
        }
        player.sendPacket(new ItemList(player, false));
        changeLevel(this._level + 1);
        return true;
    }

    public void changeLevel(int level) {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("UPDATE clan_data SET clan_level = ? WHERE clan_id = ?");
                try {
                    ps.setInt(1, level);
                    ps.setInt(2, this._clanId);
                    ps.executeUpdate();
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
            LOGGER.error("Error while updating clan level.", e);
        }
        setLevel(level);
        if (this._leader.isOnline()) {
            Player leader = this._leader.getPlayerInstance();
            if (3 < level) {
                leader.addSiegeSkills();
            } else if (4 > level) {
                leader.removeSiegeSkills();
            }
            if (4 < level)
                leader.sendPacket(SystemMessageId.CLAN_CAN_ACCUMULATE_CLAN_REPUTATION_POINTS);
        }
        broadcastToOnlineMembers(new PledgeShowInfoUpdate(this), SystemMessage.getSystemMessage(SystemMessageId.CLAN_LEVEL_INCREASED));
    }

    public void changeClanCrest(int crestId) {
        if (this._crestId != 0)
            CrestCache.getInstance().removeCrest(CrestCache.CrestType.PLEDGE, this._crestId);
        this._crestId = crestId;
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("UPDATE clan_data SET crest_id = ? WHERE clan_id = ?");
                try {
                    ps.setInt(1, crestId);
                    ps.setInt(2, this._clanId);
                    ps.executeUpdate();
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
            LOGGER.error("Error while updating clan crest.", e);
        }
        for (Player member : getOnlineMembers())
            member.broadcastUserInfo();
    }

    public void changeAllyCrest(int crestId, boolean onlyThisClan) {
        String query = "UPDATE clan_data SET ally_crest_id = ? WHERE clan_id = ?";
        int allyId = this._clanId;
        if (!onlyThisClan) {
            if (this._allyCrestId != 0)
                CrestCache.getInstance().removeCrest(CrestCache.CrestType.ALLY, this._allyCrestId);
            query = "UPDATE clan_data SET ally_crest_id = ? WHERE ally_id = ?";
            allyId = this._allyId;
        }
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement(query);
                try {
                    ps.setInt(1, crestId);
                    ps.setInt(2, allyId);
                    ps.executeUpdate();
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
            LOGGER.error("Error while updating ally crest.", e);
        }
        if (onlyThisClan) {
            this._allyCrestId = crestId;
            for (Player member : getOnlineMembers())
                member.broadcastUserInfo();
        } else {
            for (Clan clan : ClanTable.getInstance().getClans()) {
                if (clan.getAllyId() == this._allyId) {
                    clan.setAllyCrestId(crestId);
                    for (Player member : clan.getOnlineMembers())
                        member.broadcastUserInfo();
                }
            }
        }
    }

    public void changeLargeCrest(int crestId) {
        if (this._crestLargeId != 0)
            CrestCache.getInstance().removeCrest(CrestCache.CrestType.PLEDGE_LARGE, this._crestLargeId);
        this._crestLargeId = crestId;
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("UPDATE clan_data SET crest_large_id = ? WHERE clan_id = ?");
                try {
                    ps.setInt(1, crestId);
                    ps.setInt(2, this._clanId);
                    ps.executeUpdate();
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
            LOGGER.error("Error while updating large crest.", e);
        }
        for (Player member : getOnlineMembers())
            member.broadcastUserInfo();
    }

    public boolean isRegisteredOnSiege() {
        for (Castle castle : CastleManager.getInstance().getCastles()) {
            if (castle.getSiege().checkSides(this))
                return true;
        }
        return false;
    }
}
