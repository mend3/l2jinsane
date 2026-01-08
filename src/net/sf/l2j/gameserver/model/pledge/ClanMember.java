package net.sf.l2j.gameserver.model.pledge;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.enums.actors.Sex;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClanMember {
    private static final CLogger LOGGER = new CLogger(ClanMember.class.getName());

    private static final String UPDATE_PLEDGE = "UPDATE characters SET subpledge=? WHERE obj_id=?";

    private static final String UPDATE_POWER_GRADE = "UPDATE characters SET power_grade=? WHERE obj_id=?";

    private static final String UPDATE_SPONSOR = "UPDATE characters SET apprentice=?,sponsor=? WHERE obj_Id=?";

    private final Clan _clan;

    private int _objectId;

    private String _name;

    private String _title;

    private int _powerGrade;

    private int _level;

    private int _classId;

    private Sex _sex;

    private int _raceOrdinal;

    private Player _player;

    private int _pledgeType;

    private int _apprentice;

    private int _sponsor;

    public ClanMember(Clan clan, ResultSet rs) throws SQLException {
        if (clan == null)
            throw new IllegalArgumentException("Cannot create a clan member with a null clan.");
        this._clan = clan;
        this._name = rs.getString("char_name");
        this._level = rs.getInt("level");
        this._classId = rs.getInt("classid");
        this._objectId = rs.getInt("obj_Id");
        this._pledgeType = rs.getInt("subpledge");
        this._title = rs.getString("title");
        this._powerGrade = rs.getInt("power_grade");
        this._apprentice = rs.getInt("apprentice");
        this._sponsor = rs.getInt("sponsor");
        this._sex = Sex.values()[rs.getInt("sex")];
        this._raceOrdinal = rs.getInt("race");
    }

    public ClanMember(Clan clan, Player player) {
        if (clan == null)
            throw new IllegalArgumentException("Cannot create a clan member with a null clan.");
        this._player = player;
        this._clan = clan;
        this._name = player.getName();
        this._level = player.getLevel();
        this._classId = player.getClassId().getId();
        this._objectId = player.getObjectId();
        this._pledgeType = player.getPledgeType();
        this._powerGrade = player.getPowerGrade();
        this._title = player.getTitle();
        this._sponsor = 0;
        this._apprentice = 0;
        this._sex = player.getAppearance().getSex();
        this._raceOrdinal = player.getRace().ordinal();
    }

    public static int calculatePledgeClass(Player player) {
        int pledgeClass = 0;
        Clan clan = player.getClan();
        if (clan != null)
            switch (clan.getLevel()) {
                case 4:
                    if (player.isClanLeader())
                        pledgeClass = 3;
                    break;
                case 5:
                    if (player.isClanLeader()) {
                        pledgeClass = 4;
                        break;
                    }
                    pledgeClass = 2;
                    break;
                case 6:
                    switch (player.getPledgeType()) {
                        case -1:
                            pledgeClass = 1;
                            break;
                        case 100:
                        case 200:
                            pledgeClass = 2;
                            break;
                        case 0:
                            if (player.isClanLeader()) {
                                pledgeClass = 5;
                                break;
                            }
                            pledgeClass = switch (clan.getLeaderSubPledge(player.getObjectId())) {
                                case 100, 200 -> 4;
                                default -> pledgeClass;
                            };
                            pledgeClass = 3;
                            break;
                    }
                    break;
                case 7:
                    switch (player.getPledgeType()) {
                        case -1:
                            pledgeClass = 1;
                            break;
                        case 100:
                        case 200:
                            pledgeClass = 3;
                            break;
                        case 1001:
                        case 1002:
                        case 2001:
                        case 2002:
                            pledgeClass = 2;
                            break;
                        case 0:
                            if (player.isClanLeader()) {
                                pledgeClass = 7;
                                break;
                            }
                            pledgeClass = switch (clan.getLeaderSubPledge(player.getObjectId())) {
                                case 100, 200 -> 6;
                                case 1001, 1002, 2001, 2002 -> 5;
                                default -> pledgeClass;
                            };
                            pledgeClass = 4;
                            break;
                    }
                    break;
                case 8:
                    switch (player.getPledgeType()) {
                        case -1:
                            pledgeClass = 1;
                            break;
                        case 100:
                        case 200:
                            pledgeClass = 4;
                            break;
                        case 1001:
                        case 1002:
                        case 2001:
                        case 2002:
                            pledgeClass = 3;
                            break;
                        case 0:
                            if (player.isClanLeader()) {
                                pledgeClass = 8;
                                break;
                            }
                            pledgeClass = switch (clan.getLeaderSubPledge(player.getObjectId())) {
                                case 100, 200 -> 7;
                                case 1001, 1002, 2001, 2002 -> 6;
                                default -> pledgeClass;
                            };
                            pledgeClass = 5;
                            break;
                    }
                    break;
                default:
                    pledgeClass = 1;
                    break;
            }
        if (player.isHero() && pledgeClass < 8) {
            pledgeClass = 8;
        } else if (player.isNoble() && pledgeClass < 5) {
            pledgeClass = 5;
        }
        return pledgeClass;
    }

    public Player getPlayerInstance() {
        return this._player;
    }

    public void setPlayerInstance(Player player) {
        if (player == null && this._player != null) {
            this._name = this._player.getName();
            this._level = this._player.getLevel();
            this._classId = this._player.getClassId().getId();
            this._objectId = this._player.getObjectId();
            this._powerGrade = this._player.getPowerGrade();
            this._pledgeType = this._player.getPledgeType();
            this._title = this._player.getTitle();
            this._apprentice = this._player.getApprentice();
            this._sponsor = this._player.getSponsor();
            this._sex = this._player.getAppearance().getSex();
            this._raceOrdinal = this._player.getRace().ordinal();
        }
        if (player != null) {
            if (this._clan.getLevel() > 3 && player.isClanLeader())
                player.addSiegeSkills();
            if (this._clan.getReputationScore() >= 0)
                for (L2Skill sk : this._clan.getClanSkills().values()) {
                    if (sk.getMinPledgeClass() <= player.getPledgeClass())
                        player.addSkill(sk, false);
                }
        }
        this._player = player;
    }

    public boolean isOnline() {
        return (this._player != null && this._player.getClient() != null && !this._player.getClient().isDetached());
    }

    public int getClassId() {
        return (this._player != null) ? this._player.getClassId().getId() : this._classId;
    }

    public int getLevel() {
        return (this._player != null) ? this._player.getLevel() : this._level;
    }

    public void refreshLevel() {
        if (this._player != null)
            this._level = this._player.getLevel();
    }

    public String getName() {
        return (this._player != null) ? this._player.getName() : this._name;
    }

    public int getObjectId() {
        return (this._player != null) ? this._player.getObjectId() : this._objectId;
    }

    public String getTitle() {
        return (this._player != null) ? this._player.getTitle() : this._title;
    }

    public int getPledgeType() {
        return (this._player != null) ? this._player.getPledgeType() : this._pledgeType;
    }

    public void setPledgeType(int pledgeType) {
        this._pledgeType = pledgeType;
        if (this._player != null) {
            this._player.setPledgeType(pledgeType);
        } else {
            updatePledgeType();
        }
    }

    public void updatePledgeType() {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("UPDATE characters SET subpledge=? WHERE obj_id=?");
                try {
                    ps.setInt(1, this._pledgeType);
                    ps.setInt(2, getObjectId());
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
            LOGGER.error("Couldn't update ClanMember pledge.", e);
        }
    }

    public int getPowerGrade() {
        return (this._player != null) ? this._player.getPowerGrade() : this._powerGrade;
    }

    public void setPowerGrade(int powerGrade) {
        this._powerGrade = powerGrade;
        if (this._player != null) {
            this._player.setPowerGrade(powerGrade);
        } else {
            updatePowerGrade();
        }
    }

    public void updatePowerGrade() {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("UPDATE characters SET power_grade=? WHERE obj_id=?");
                try {
                    ps.setInt(1, this._powerGrade);
                    ps.setInt(2, getObjectId());
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
            LOGGER.error("Couldn't update ClanMember power grade.", e);
        }
    }

    public void setApprenticeAndSponsor(int apprenticeId, int sponsorId) {
        this._apprentice = apprenticeId;
        this._sponsor = sponsorId;
    }

    public int getRaceOrdinal() {
        return (this._player != null) ? this._player.getRace().ordinal() : this._raceOrdinal;
    }

    public Sex getSex() {
        return (this._player != null) ? this._player.getAppearance().getSex() : this._sex;
    }

    public int getSponsor() {
        return (this._player != null) ? this._player.getSponsor() : this._sponsor;
    }

    public int getApprentice() {
        return (this._player != null) ? this._player.getApprentice() : this._apprentice;
    }

    public String getApprenticeOrSponsorName() {
        if (this._player != null) {
            this._apprentice = this._player.getApprentice();
            this._sponsor = this._player.getSponsor();
        }
        if (this._apprentice != 0) {
            ClanMember apprentice = this._clan.getClanMember(this._apprentice);
            if (apprentice != null)
                return apprentice.getName();
            return "Error";
        }
        if (this._sponsor != 0) {
            ClanMember sponsor = this._clan.getClanMember(this._sponsor);
            if (sponsor != null)
                return sponsor.getName();
            return "Error";
        }
        return "";
    }

    public Clan getClan() {
        return this._clan;
    }

    public void saveApprenticeAndSponsor(int apprentice, int sponsor) {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("UPDATE characters SET apprentice=?,sponsor=? WHERE obj_Id=?");
                try {
                    ps.setInt(1, apprentice);
                    ps.setInt(2, sponsor);
                    ps.setInt(3, getObjectId());
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
            LOGGER.error("Couldn't update ClanMember sponsor/apprentice.", e);
        }
    }
}
