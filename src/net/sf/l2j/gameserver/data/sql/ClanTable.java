/**/
package net.sf.l2j.gameserver.data.sql;

import net.sf.l2j.Config;
import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.pledge.ClanMember;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListAll;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ClanTable {
    private static final CLogger LOGGER = new CLogger(ClanTable.class.getName());
    private static final String LOAD_CLANS = "SELECT * FROM clan_data";
    private static final String DELETE_CLAN = "DELETE FROM clan_data WHERE clan_id=?";
    private static final String DELETE_CLAN_PRIVS = "DELETE FROM clan_privs WHERE clan_id=?";
    private static final String DELETE_CLAN_SKILLS = "DELETE FROM clan_skills WHERE clan_id=?";
    private static final String DELETE_CLAN_SUBPLEDGES = "DELETE FROM clan_subpledges WHERE clan_id=?";
    private static final String DELETE_CLAN_WARS = "DELETE FROM clan_wars WHERE clan1=? OR clan2=?";
    private static final String DELETE_CLAN_SIEGES = "DELETE FROM siege_clans WHERE clan_id=?";
    private static final String RESET_CASTLE_TAX = "UPDATE castle SET taxPercent = 0 WHERE id = ?";
    private static final String INSERT_WAR = "REPLACE INTO clan_wars (clan1, clan2) VALUES(?,?)";
    private static final String UPDATE_WAR_TIME = "UPDATE clan_wars SET expiry_time=? WHERE clan1=? AND clan2=?";
    private static final String DELETE_WAR = "DELETE FROM clan_wars WHERE clan1=? AND clan2=?";
    private static final String DELETE_OLD_WARS = "DELETE FROM clan_wars WHERE expiry_time > 0 AND expiry_time <= ?";
    private static final String LOAD_WARS = "SELECT * FROM clan_wars";
    private static final String LOAD_RANK = "SELECT clan_id FROM clan_data ORDER BY reputation_score DESC LIMIT 99";
    private final Map<Integer, Clan> _clans = new ConcurrentHashMap<>();

    public static ClanTable getInstance() {
        return ClanTable.SingletonHolder.INSTANCE;
    }

    public void load() {
        Connection con = null;
        try {
            con = ConnectionPool.getConnection();

            PreparedStatement ps = con.prepareStatement("SELECT * FROM clan_data");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int clanId = rs.getInt("clan_id");
                Clan clan = new Clan(clanId, rs.getInt("leader_id"));
                this._clans.put(clanId, clan);
                clan.setName(rs.getString("clan_name"));
                clan.setLevel(rs.getInt("clan_level"));
                clan.setCastle(rs.getInt("hasCastle"));
                clan.setAllyId(rs.getInt("ally_id"));
                clan.setAllyName(rs.getString("ally_name"));
                long allyExpireTime = rs.getLong("ally_penalty_expiry_time");
                if (allyExpireTime > System.currentTimeMillis()) {
                    clan.setAllyPenaltyExpiryTime(allyExpireTime, rs.getInt("ally_penalty_type"));
                }

                long charExpireTime = rs.getLong("char_penalty_expiry_time");
                if (charExpireTime + (long) Config.ALT_CLAN_JOIN_DAYS * 86400000L > System.currentTimeMillis()) {
                    clan.setCharPenaltyExpiryTime(charExpireTime);
                }

                clan.setDissolvingExpiryTime(rs.getLong("dissolving_expiry_time"));
                clan.setCrestId(rs.getInt("crest_id"));
                clan.setCrestLargeId(rs.getInt("crest_large_id"));
                clan.setAllyCrestId(rs.getInt("ally_crest_id"));
                clan.addReputationScore(rs.getInt("reputation_score"));
                clan.setAuctionBiddedAt(rs.getInt("auction_bid_at"));
                clan.setNewLeaderId(rs.getInt("new_leader_id"), false);
                if (clan.getDissolvingExpiryTime() != 0L) {
                    this.scheduleRemoveClan(clan);
                }

                clan.setNoticeEnabled(rs.getBoolean("enabled"));
                clan.setNotice(rs.getString("notice"));
                clan.setIntroduction(rs.getString("introduction"), false);
            }
        } catch (Exception var16) {
            LOGGER.error("Couldn't restore clans.", var16);
        } finally {
            try {

                if (con != null) {
                    con.close();
                }
            } catch (Exception ignored) {
            }
        }

        LOGGER.info("Loaded {} clans.", this._clans.size());
        this.allianceCheck();
        this.restoreWars();
        this.refreshClansLadder(false);
    }

    public Collection<Clan> getClans() {
        return this._clans.values();
    }

    public Clan getClan(int clanId) {
        return this._clans.get(clanId);
    }

    public Clan getClanByName(String clanName) {
        return this._clans.values().stream().filter((c) -> {
            return c.getName().equalsIgnoreCase(clanName);
        }).findAny().orElse(null);
    }

    public Clan createClan(Player player, String clanName) {
        if (player == null) {
            return null;
        } else if (player.getLevel() < 10) {
            player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_CLAN);
            return null;
        } else if (player.getClanId() != 0) {
            player.sendPacket(SystemMessageId.FAILED_TO_CREATE_CLAN);
            return null;
        } else if (System.currentTimeMillis() < player.getClanCreateExpiryTime()) {
            player.sendPacket(SystemMessageId.YOU_MUST_WAIT_XX_DAYS_BEFORE_CREATING_A_NEW_CLAN);
            return null;
        } else if (!StringUtil.isAlphaNumeric(clanName)) {
            player.sendPacket(SystemMessageId.CLAN_NAME_INVALID);
            return null;
        } else if (clanName.length() >= 2 && clanName.length() <= 16) {
            if (this.getClanByName(clanName) != null) {
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_EXISTS).addString(clanName));
                return null;
            } else {
                Clan clan = new Clan(IdFactory.getInstance().getNextId(), clanName);
                ClanMember leader = new ClanMember(clan, player);
                clan.setLeader(leader);
                leader.setPlayerInstance(player);
                clan.store();
                player.setClan(clan);
                player.setPledgeClass(ClanMember.calculatePledgeClass(player));
                player.setClanPrivileges(8388606);
                this._clans.put(clan.getClanId(), clan);
                player.sendPacket(new PledgeShowMemberListAll(clan, 0));
                player.sendPacket(new UserInfo(player));
                player.sendPacket(SystemMessageId.CLAN_CREATED);
                return clan;
            }
        } else {
            player.sendPacket(SystemMessageId.CLAN_NAME_LENGTH_INCORRECT);
            return null;
        }
    }

    public void destroyClan(Clan clan) {
        if (this._clans.containsKey(clan.getClanId())) {
            clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_HAS_DISPERSED));
            Iterator<?> var2 = CastleManager.getInstance().getCastles().iterator();

            while (var2.hasNext()) {
                Castle castle = (Castle) var2.next();
                castle.getSiege().getRegisteredClans().keySet().removeIf((c) -> {
                    return c.getClanId() == clan.getClanId();
                });
            }

            var2 = clan.getAttackerList().iterator();

            while (var2.hasNext()) {
                int clanId = (Integer) var2.next();
                Clan attackerClan = this._clans.get(clanId);
                attackerClan.deleteAttackerClan(clan.getClanId());
                attackerClan.deleteEnemyClan(clan.getClanId());
            }

            clan.getWarehouse().destroyAllItems("ClanRemove", clan.getLeader() == null ? null : clan.getLeader().getPlayerInstance(), null);
            var2 = clan.getMembers().iterator();

            while (var2.hasNext()) {
                ClanMember member = (ClanMember) var2.next();
                clan.removeClanMember(member.getObjectId(), 0L);
            }

            try {
                Connection con = ConnectionPool.getConnection();

                try {
                    PreparedStatement ps = con.prepareStatement("DELETE FROM clan_data WHERE clan_id=?");

                    try {
                        ps.setInt(1, clan.getClanId());
                        ps.executeUpdate();
                    } catch (Throwable var20) {
                        if (ps != null) {
                            try {
                                ps.close();
                            } catch (Throwable var13) {
                                var20.addSuppressed(var13);
                            }
                        }

                        throw var20;
                    }

                    if (ps != null) {
                        ps.close();
                    }

                    ps = con.prepareStatement("DELETE FROM clan_privs WHERE clan_id=?");

                    try {
                        ps.setInt(1, clan.getClanId());
                        ps.executeUpdate();
                    } catch (Throwable var19) {
                        if (ps != null) {
                            try {
                                ps.close();
                            } catch (Throwable var12) {
                                var19.addSuppressed(var12);
                            }
                        }

                        throw var19;
                    }

                    if (ps != null) {
                        ps.close();
                    }

                    ps = con.prepareStatement("DELETE FROM clan_skills WHERE clan_id=?");

                    try {
                        ps.setInt(1, clan.getClanId());
                        ps.executeUpdate();
                    } catch (Throwable var18) {
                        if (ps != null) {
                            try {
                                ps.close();
                            } catch (Throwable var11) {
                                var18.addSuppressed(var11);
                            }
                        }

                        throw var18;
                    }

                    if (ps != null) {
                        ps.close();
                    }

                    ps = con.prepareStatement("DELETE FROM clan_subpledges WHERE clan_id=?");

                    try {
                        ps.setInt(1, clan.getClanId());
                        ps.executeUpdate();
                    } catch (Throwable var17) {
                        if (ps != null) {
                            try {
                                ps.close();
                            } catch (Throwable var10) {
                                var17.addSuppressed(var10);
                            }
                        }

                        throw var17;
                    }

                    if (ps != null) {
                        ps.close();
                    }

                    ps = con.prepareStatement("DELETE FROM clan_wars WHERE clan1=? OR clan2=?");

                    try {
                        ps.setInt(1, clan.getClanId());
                        ps.setInt(2, clan.getClanId());
                        ps.executeUpdate();
                    } catch (Throwable var16) {
                        if (ps != null) {
                            try {
                                ps.close();
                            } catch (Throwable var9) {
                                var16.addSuppressed(var9);
                            }
                        }

                        throw var16;
                    }

                    if (ps != null) {
                        ps.close();
                    }

                    ps = con.prepareStatement("DELETE FROM siege_clans WHERE clan_id=?");

                    try {
                        ps.setInt(1, clan.getClanId());
                        ps.executeUpdate();
                    } catch (Throwable var15) {
                        if (ps != null) {
                            try {
                                ps.close();
                            } catch (Throwable var8) {
                                var15.addSuppressed(var8);
                            }
                        }

                        throw var15;
                    }

                    if (ps != null) {
                        ps.close();
                    }

                    if (clan.getCastleId() != 0) {
                        ps = con.prepareStatement("UPDATE castle SET taxPercent = 0 WHERE id = ?");

                        try {
                            ps.setInt(1, clan.getCastleId());
                            ps.executeUpdate();
                        } catch (Throwable var14) {
                            if (ps != null) {
                                try {
                                    ps.close();
                                } catch (Throwable var7) {
                                    var14.addSuppressed(var7);
                                }
                            }

                            throw var14;
                        }

                        if (ps != null) {
                            ps.close();
                        }
                    }
                } catch (Throwable var21) {
                    if (con != null) {
                        try {
                            con.close();
                        } catch (Throwable var6) {
                            var21.addSuppressed(var6);
                        }
                    }

                    throw var21;
                }

                if (con != null) {
                    con.close();
                }
            } catch (Exception var22) {
                LOGGER.error("Couldn't delete clan.", var22);
            }

            IdFactory.getInstance().releaseId(clan.getClanId());
            this._clans.remove(clan.getClanId());
        }
    }

    public void scheduleRemoveClan(Clan clan) {
        if (clan != null) {
            ThreadPool.schedule(() -> {
                if (clan.getDissolvingExpiryTime() != 0L) {
                    this.destroyClan(clan);
                }

            }, Math.max(clan.getDissolvingExpiryTime() - System.currentTimeMillis(), 60000L));
        }
    }

    public boolean isAllyExists(String allyName) {
        Iterator<Clan> var2 = this._clans.values().iterator();

        Clan clan;
        do {
            if (!var2.hasNext()) {
                return false;
            }

            clan = var2.next();
        } while (clan.getAllyName() == null || !clan.getAllyName().equalsIgnoreCase(allyName));

        return true;
    }

    public void storeClansWars(int clanId1, int clanId2) {
        Clan clan1 = this._clans.get(clanId1);
        Clan clan2 = this._clans.get(clanId2);
        clan1.setEnemyClan(clanId2);
        clan1.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan1), SystemMessage.getSystemMessage(SystemMessageId.CLAN_WAR_DECLARED_AGAINST_S1_IF_KILLED_LOSE_LOW_EXP).addString(clan2.getName()));
        clan2.setAttackerClan(clanId1);
        clan2.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan2), SystemMessage.getSystemMessage(SystemMessageId.CLAN_S1_DECLARED_WAR).addString(clan1.getName()));

        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("REPLACE INTO clan_wars (clan1, clan2) VALUES(?,?)");

                try {
                    ps.setInt(1, clanId1);
                    ps.setInt(2, clanId2);
                    ps.execute();
                } catch (Throwable var11) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var10) {
                            var11.addSuppressed(var10);
                        }
                    }

                    throw var11;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (Throwable var12) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var9) {
                        var12.addSuppressed(var9);
                    }
                }

                throw var12;
            }

            if (con != null) {
                con.close();
            }
        } catch (Exception var13) {
            LOGGER.error("Couldn't store clans wars.", var13);
        }

    }

    public void deleteClansWars(int clanId1, int clanId2) {
        Clan clan1 = this._clans.get(clanId1);
        Clan clan2 = this._clans.get(clanId2);
        clan1.deleteEnemyClan(clanId2);
        clan1.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan1), SystemMessage.getSystemMessage(SystemMessageId.WAR_AGAINST_S1_HAS_STOPPED).addString(clan2.getName()));
        clan2.deleteAttackerClan(clanId1);
        clan2.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan2), SystemMessage.getSystemMessage(SystemMessageId.CLAN_S1_HAS_DECIDED_TO_STOP).addString(clan1.getName()));

        try {
            Connection con = ConnectionPool.getConnection();

            try {
                if (Config.ALT_CLAN_WAR_PENALTY_WHEN_ENDED > 0) {
                    long penaltyExpiryTime = System.currentTimeMillis() + (long) Config.ALT_CLAN_WAR_PENALTY_WHEN_ENDED * 86400000L;
                    clan1.addWarPenaltyTime(clanId2, penaltyExpiryTime);
                    PreparedStatement ps = con.prepareStatement("UPDATE clan_wars SET expiry_time=? WHERE clan1=? AND clan2=?");

                    try {
                        ps.setLong(1, penaltyExpiryTime);
                        ps.setInt(2, clanId1);
                        ps.setInt(3, clanId2);
                        ps.executeUpdate();
                    } catch (Throwable var15) {
                        if (ps != null) {
                            try {
                                ps.close();
                            } catch (Throwable var13) {
                                var15.addSuppressed(var13);
                            }
                        }

                        throw var15;
                    }

                    if (ps != null) {
                        ps.close();
                    }
                } else {
                    PreparedStatement ps = con.prepareStatement("DELETE FROM clan_wars WHERE clan1=? AND clan2=?");

                    try {
                        ps.setInt(1, clanId1);
                        ps.setInt(2, clanId2);
                        ps.executeUpdate();
                    } catch (Throwable var14) {
                        if (ps != null) {
                            try {
                                ps.close();
                            } catch (Throwable var12) {
                                var14.addSuppressed(var12);
                            }
                        }

                        throw var14;
                    }

                    if (ps != null) {
                        ps.close();
                    }
                }
            } catch (Throwable var16) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var11) {
                        var16.addSuppressed(var11);
                    }
                }

                throw var16;
            }

            if (con != null) {
                con.close();
            }
        } catch (Exception var17) {
            LOGGER.error("Couldn't delete clans wars.", var17);
        }

    }

    public void checkSurrender(Clan clan1, Clan clan2) {
        int count = 0;

        for (ClanMember player : clan1.getMembers()) {
            if (player != null && player.getPlayerInstance().wantsPeace()) {
                ++count;
            }
        }

        if (count == clan1.getMembersCount() - 1) {
            clan1.deleteEnemyClan(clan2.getClanId());
            clan2.deleteEnemyClan(clan1.getClanId());
            this.deleteClansWars(clan1.getClanId(), clan2.getClanId());
        }

    }

    private void restoreWars() {
        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("DELETE FROM clan_wars WHERE expiry_time > 0 AND expiry_time <= ?");

                try {
                    ps.setLong(1, System.currentTimeMillis());
                    ps.executeUpdate();
                } catch (Throwable var12) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var11) {
                            var12.addSuppressed(var11);
                        }
                    }

                    throw var12;
                }

                if (ps != null) {
                    ps.close();
                }

                ps = con.prepareStatement("SELECT * FROM clan_wars");

                try {
                    ResultSet rs = ps.executeQuery();

                    try {
                        while (rs.next()) {
                            int clan1 = rs.getInt("clan1");
                            int clan2 = rs.getInt("clan2");
                            long expiryTime = rs.getLong("expiry_time");
                            if (expiryTime > 0L) {
                                this._clans.get(clan1).addWarPenaltyTime(clan2, expiryTime);
                            } else {
                                this._clans.get(clan1).setEnemyClan(clan2);
                                this._clans.get(clan2).setAttackerClan(clan1);
                            }
                        }
                    } catch (Throwable var13) {
                        if (rs != null) {
                            try {
                                rs.close();
                            } catch (Throwable var10) {
                                var13.addSuppressed(var10);
                            }
                        }

                        throw var13;
                    }

                    if (rs != null) {
                        rs.close();
                    }
                } catch (Throwable var14) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var9) {
                            var14.addSuppressed(var9);
                        }
                    }

                    throw var14;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (Throwable var15) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var8) {
                        var15.addSuppressed(var8);
                    }
                }

                throw var15;
            }

            if (con != null) {
                con.close();
            }
        } catch (Exception var16) {
            LOGGER.error("Couldn't restore clans wars.", var16);
        }

    }

    private void allianceCheck() {

        for (Clan clan : this._clans.values()) {
            int allyId = clan.getAllyId();
            if (allyId != 0 && clan.getClanId() != allyId && !this._clans.containsKey(allyId)) {
                clan.setAllyId(0);
                clan.setAllyName(null);
                clan.changeAllyCrest(0, true);
                clan.updateClanInDB();
            }
        }

    }

    public List<Clan> getClanAllies(int allianceId) {
        return allianceId == 0 ? Collections.emptyList() : this._clans.values().stream().filter((c) -> {
            return c.getAllyId() == allianceId;
        }).collect(Collectors.toList());
    }

    public void refreshClansLadder(boolean cleanupRank) {
        if (cleanupRank) {

            for (Clan clan : this._clans.values()) {
                if (clan != null && clan.getRank() != 0) {
                    clan.setRank(0);
                }
            }
        }

        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("SELECT clan_id FROM clan_data ORDER BY reputation_score DESC LIMIT 99");

                try {
                    ResultSet rs = ps.executeQuery();

                    try {
                        int var5 = 1;

                        while (rs.next()) {
                            Clan clan = this._clans.get(rs.getInt("clan_id"));
                            if (clan != null && clan.getReputationScore() > 0) {
                                clan.setRank(var5++);
                            }
                        }
                    } catch (Throwable var10) {
                        if (rs != null) {
                            try {
                                rs.close();
                            } catch (Throwable var9) {
                                var10.addSuppressed(var9);
                            }
                        }

                        throw var10;
                    }

                    if (rs != null) {
                        rs.close();
                    }
                } catch (Throwable var11) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var8) {
                            var11.addSuppressed(var8);
                        }
                    }

                    throw var11;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (Throwable var12) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var7) {
                        var12.addSuppressed(var7);
                    }
                }

                throw var12;
            }

            if (con != null) {
                con.close();
            }
        } catch (Exception var13) {
            LOGGER.error("Couldn't refresh clans ladder.", var13);
        }

    }

    public void addNewClan(Clan clan) {
        this._clans.put(clan.getClanId(), clan);
    }

    private static class SingletonHolder {
        protected static final ClanTable INSTANCE = new ClanTable();
    }
}