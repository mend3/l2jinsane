/**/
package net.sf.l2j.gameserver.data.manager;

import net.sf.l2j.Config;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.SkillTable.FrequentSkill;
import net.sf.l2j.gameserver.data.sql.AutoSpawnTable;
import net.sf.l2j.gameserver.data.xml.MapRegionData.TeleportType;
import net.sf.l2j.gameserver.enums.CabalType;
import net.sf.l2j.gameserver.enums.PeriodType;
import net.sf.l2j.gameserver.enums.SealType;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.spawn.AutoSpawn;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SSQInfo;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class SevenSignsManager {
    public static final String SEVEN_SIGNS_DATA_FILE = "config/signs.properties";
    public static final String SEVEN_SIGNS_HTML_PATH = "data/html/seven_signs/";
    public static final int PERIOD_START_HOUR = 18;
    public static final int PERIOD_START_MINS = 0;
    public static final int PERIOD_START_DAY = 2;
    public static final int PERIOD_MINOR_LENGTH = 900000;
    public static final int PERIOD_MAJOR_LENGTH = 603900000;
    public static final int RECORD_SEVEN_SIGNS_ID = 5707;
    public static final int CERTIFICATE_OF_APPROVAL_ID = 6388;
    public static final int RECORD_SEVEN_SIGNS_COST = 500;
    public static final int ADENA_JOIN_DAWN_COST = 50000;
    public static final int ORATOR_NPC_ID = 31094;
    public static final int PREACHER_NPC_ID = 31093;
    public static final int MAMMON_MERCHANT_ID = 31113;
    public static final int MAMMON_BLACKSMITH_ID = 31126;
    public static final int MAMMON_MARKETEER_ID = 31092;
    public static final int LILITH_NPC_ID = 25283;
    public static final int ANAKIM_NPC_ID = 25286;
    public static final int CREST_OF_DAWN_ID = 31170;
    public static final int CREST_OF_DUSK_ID = 31171;
    public static final int SEAL_STONE_BLUE_ID = 6360;
    public static final int SEAL_STONE_GREEN_ID = 6361;
    public static final int SEAL_STONE_RED_ID = 6362;
    public static final int SEAL_STONE_BLUE_VALUE = 3;
    public static final int SEAL_STONE_GREEN_VALUE = 5;
    public static final int SEAL_STONE_RED_VALUE = 10;
    private static final CLogger LOGGER = new CLogger(SevenSignsManager.class.getName());
    private static final String LOAD_DATA = "SELECT char_obj_id, cabal, seal, red_stones, green_stones, blue_stones, ancient_adena_amount, contribution_score FROM seven_signs";
    private static final String LOAD_STATUS = "SELECT * FROM seven_signs_status WHERE id=0";
    private static final String INSERT_PLAYER = "INSERT INTO seven_signs (char_obj_id, cabal, seal) VALUES (?,?,?)";
    private static final String UPDATE_PLAYER = "UPDATE seven_signs SET cabal=?, seal=?, red_stones=?, green_stones=?, blue_stones=?, ancient_adena_amount=?, contribution_score=? WHERE char_obj_id=?";
    private static final String UPDATE_STATUS = "UPDATE seven_signs_status SET current_cycle=?, active_period=?, previous_winner=?, dawn_stone_score=?, dawn_festival_score=?, dusk_stone_score=?, dusk_festival_score=?, avarice_owner=?, gnosis_owner=?, strife_owner=?, avarice_dawn_score=?, gnosis_dawn_score=?, strife_dawn_score=?, avarice_dusk_score=?, gnosis_dusk_score=?, strife_dusk_score=?, festival_cycle=?, accumulated_bonus0=?, accumulated_bonus1=?, accumulated_bonus2=?,accumulated_bonus3=?, accumulated_bonus4=?, date=? WHERE id=0";
    private static AutoSpawn _merchantSpawn;
    private static AutoSpawn _blacksmithSpawn;
    private static AutoSpawn _lilithSpawn;
    private static AutoSpawn _anakimSpawn;
    private static Map<Integer, AutoSpawn> _crestofdawnspawns;
    private static Map<Integer, AutoSpawn> _crestofduskspawns;
    private static Map<Integer, AutoSpawn> _oratorSpawns;
    private static Map<Integer, AutoSpawn> _preacherSpawns;
    private static Map<Integer, AutoSpawn> _marketeerSpawns;
    private final Calendar _nextPeriodChange = Calendar.getInstance();
    private final Map<Integer, StatSet> _playersData = new HashMap<>();
    private final Map<SealType, CabalType> _sealOwners = new HashMap<>();
    private final Map<SealType, Integer> _duskScores = new HashMap<>();
    private final Map<SealType, Integer> _dawnScores = new HashMap<>();
    protected PeriodType _activePeriod;
    protected int _currentCycle;
    protected double _dawnStoneScore;
    protected double _duskStoneScore;
    protected int _dawnFestivalScore;
    protected int _duskFestivalScore;
    protected CabalType _previousWinner;
    private Calendar _lastSave = Calendar.getInstance();

    protected SevenSignsManager() {
        this.restoreSevenSignsData();
        LOGGER.info("Currently on {} period.", this._activePeriod.getName());
        this.initializeSeals();
        CabalType winningCabal = this.getCabalHighestScore();
        if (this.isSealValidationPeriod()) {
            if (winningCabal == CabalType.NORMAL) {
                LOGGER.info("The Seven Signs competition ended with a tie last week.");
            } else {
                LOGGER.info("{} were victorious on Seven Signs competition last week.", winningCabal.getFullName());
            }
        } else if (winningCabal == CabalType.NORMAL) {
            LOGGER.info("The Seven Signs competition will end in a tie this week.");
        } else {
            LOGGER.info("{} are leading on Seven Signs competition this week.", winningCabal.getFullName());
        }

        long milliToChange = 0L;
        if (this.isNextPeriodChangeInPast()) {
            LOGGER.info("Next Seven Signs period is already computed.");
        } else {
            this.setCalendarForNextPeriodChange();
            milliToChange = this.getMilliToPeriodChange();
        }

        ThreadPool.schedule(new SevenSignsManager.SevenSignsPeriodChange(), milliToChange);
        double numSecs = (double) (milliToChange / 1000L % 60L);
        double countDown = ((double) (milliToChange / 1000L) - numSecs) / 60.0D;
        int numMins = (int) Math.floor(countDown % 60.0D);
        countDown = (countDown - (double) numMins) / 60.0D;
        int numHours = (int) Math.floor(countDown % 24.0D);
        int numDays = (int) Math.floor((countDown - (double) numHours) / 24.0D);
        LOGGER.info("Next Seven Signs period begins in {} days, {} hours and {} mins.", numDays, numHours, numMins);
    }

    public static int calcScore(int blueCount, int greenCount, int redCount) {
        return blueCount * 3 + greenCount * 5 + redCount * 10;
    }

    public static SevenSignsManager getInstance() {
        return SevenSignsManager.SingletonHolder.INSTANCE;
    }

    private boolean isNextPeriodChangeInPast() {
        Calendar lastPeriodChange = Calendar.getInstance();
        switch (this._activePeriod) {
            case SEAL_VALIDATION:
            case COMPETITION:
                lastPeriodChange.set(Calendar.DAY_OF_WEEK, 2);
                lastPeriodChange.set(Calendar.HOUR_OF_DAY, 18);
                lastPeriodChange.set(Calendar.MINUTE, 0);
                lastPeriodChange.set(Calendar.SECOND, 0);
                if (Calendar.getInstance().before(lastPeriodChange)) {
                    lastPeriodChange.add(Calendar.HOUR, -168);
                }
                break;
            case RECRUITING:
            case RESULTS:
                lastPeriodChange.setTimeInMillis(this._lastSave.getTimeInMillis() + 900000L);
        }

        return this._lastSave.getTimeInMillis() > 7L && this._lastSave.before(lastPeriodChange);
    }

    public void spawnSevenSignsNPC() {
        _merchantSpawn = AutoSpawnTable.getInstance().getAutoSpawnInstance(31113, false);
        _blacksmithSpawn = AutoSpawnTable.getInstance().getAutoSpawnInstance(31126, false);
        _marketeerSpawns = AutoSpawnTable.getInstance().getAutoSpawnInstances(31092);
        _lilithSpawn = AutoSpawnTable.getInstance().getAutoSpawnInstance(25283, false);
        _anakimSpawn = AutoSpawnTable.getInstance().getAutoSpawnInstance(25286, false);
        _crestofdawnspawns = AutoSpawnTable.getInstance().getAutoSpawnInstances(31170);
        _crestofduskspawns = AutoSpawnTable.getInstance().getAutoSpawnInstances(31171);
        _oratorSpawns = AutoSpawnTable.getInstance().getAutoSpawnInstances(31094);
        _preacherSpawns = AutoSpawnTable.getInstance().getAutoSpawnInstances(31093);
        Iterator var1;
        AutoSpawn spawnInst;
        if (!this.isSealValidationPeriod() && !this.isCompResultsPeriod()) {
            AutoSpawnTable.getInstance().setSpawnActive(_merchantSpawn, false);
            AutoSpawnTable.getInstance().setSpawnActive(_blacksmithSpawn, false);
            AutoSpawnTable.getInstance().setSpawnActive(_lilithSpawn, false);
            AutoSpawnTable.getInstance().setSpawnActive(_anakimSpawn, false);
            var1 = _crestofdawnspawns.values().iterator();

            while (var1.hasNext()) {
                spawnInst = (AutoSpawn) var1.next();
                AutoSpawnTable.getInstance().setSpawnActive(spawnInst, false);
            }

            var1 = _crestofduskspawns.values().iterator();

            while (var1.hasNext()) {
                spawnInst = (AutoSpawn) var1.next();
                AutoSpawnTable.getInstance().setSpawnActive(spawnInst, false);
            }

            var1 = _oratorSpawns.values().iterator();

            while (var1.hasNext()) {
                spawnInst = (AutoSpawn) var1.next();
                AutoSpawnTable.getInstance().setSpawnActive(spawnInst, false);
            }

            var1 = _preacherSpawns.values().iterator();

            while (var1.hasNext()) {
                spawnInst = (AutoSpawn) var1.next();
                AutoSpawnTable.getInstance().setSpawnActive(spawnInst, false);
            }

            var1 = _marketeerSpawns.values().iterator();

            while (var1.hasNext()) {
                spawnInst = (AutoSpawn) var1.next();
                AutoSpawnTable.getInstance().setSpawnActive(spawnInst, false);
            }
        } else {
            var1 = _marketeerSpawns.values().iterator();

            while (var1.hasNext()) {
                spawnInst = (AutoSpawn) var1.next();
                AutoSpawnTable.getInstance().setSpawnActive(spawnInst, true);
            }

            CabalType winningCabal = this.getCabalHighestScore();
            CabalType gnosisSealOwner = this.getSealOwner(SealType.GNOSIS);
            Iterator var3;
            if (gnosisSealOwner == winningCabal && gnosisSealOwner != CabalType.NORMAL) {
                if (!Config.ANNOUNCE_MAMMON_SPAWN) {
                    _blacksmithSpawn.setBroadcast(false);
                }

                if (!AutoSpawnTable.getInstance().getAutoSpawnInstance(_blacksmithSpawn.getObjectId(), true).isSpawnActive()) {
                    AutoSpawnTable.getInstance().setSpawnActive(_blacksmithSpawn, true);
                }

                var3 = _oratorSpawns.values().iterator();

                while (var3.hasNext()) {
                    spawnInst = (AutoSpawn) var3.next();
                    if (!AutoSpawnTable.getInstance().getAutoSpawnInstance(spawnInst.getObjectId(), true).isSpawnActive()) {
                        AutoSpawnTable.getInstance().setSpawnActive(spawnInst, true);
                    }
                }

                var3 = _preacherSpawns.values().iterator();

                while (var3.hasNext()) {
                    spawnInst = (AutoSpawn) var3.next();
                    if (!AutoSpawnTable.getInstance().getAutoSpawnInstance(spawnInst.getObjectId(), true).isSpawnActive()) {
                        AutoSpawnTable.getInstance().setSpawnActive(spawnInst, true);
                    }
                }
            } else {
                AutoSpawnTable.getInstance().setSpawnActive(_blacksmithSpawn, false);
                var3 = _oratorSpawns.values().iterator();

                while (var3.hasNext()) {
                    spawnInst = (AutoSpawn) var3.next();
                    AutoSpawnTable.getInstance().setSpawnActive(spawnInst, false);
                }

                var3 = _preacherSpawns.values().iterator();

                while (var3.hasNext()) {
                    spawnInst = (AutoSpawn) var3.next();
                    AutoSpawnTable.getInstance().setSpawnActive(spawnInst, false);
                }
            }

            CabalType avariceSealOwner = this.getSealOwner(SealType.AVARICE);
            AutoSpawn dawnCrest;
            Iterator var9;
            if (avariceSealOwner == winningCabal && avariceSealOwner != CabalType.NORMAL) {
                if (!Config.ANNOUNCE_MAMMON_SPAWN) {
                    _merchantSpawn.setBroadcast(false);
                }

                if (!AutoSpawnTable.getInstance().getAutoSpawnInstance(_merchantSpawn.getObjectId(), true).isSpawnActive()) {
                    AutoSpawnTable.getInstance().setSpawnActive(_merchantSpawn, true);
                }

                switch (winningCabal) {
                    case DAWN:
                        if (!AutoSpawnTable.getInstance().getAutoSpawnInstance(_lilithSpawn.getObjectId(), true).isSpawnActive()) {
                            AutoSpawnTable.getInstance().setSpawnActive(_lilithSpawn, true);
                        }

                        AutoSpawnTable.getInstance().setSpawnActive(_anakimSpawn, false);
                        var9 = _crestofdawnspawns.values().iterator();

                        while (var9.hasNext()) {
                            dawnCrest = (AutoSpawn) var9.next();
                            if (!AutoSpawnTable.getInstance().getAutoSpawnInstance(dawnCrest.getObjectId(), true).isSpawnActive()) {
                                AutoSpawnTable.getInstance().setSpawnActive(dawnCrest, true);
                            }
                        }

                        var9 = _crestofduskspawns.values().iterator();

                        while (var9.hasNext()) {
                            dawnCrest = (AutoSpawn) var9.next();
                            AutoSpawnTable.getInstance().setSpawnActive(dawnCrest, false);
                        }

                        return;
                    case DUSK:
                        if (!AutoSpawnTable.getInstance().getAutoSpawnInstance(_anakimSpawn.getObjectId(), true).isSpawnActive()) {
                            AutoSpawnTable.getInstance().setSpawnActive(_anakimSpawn, true);
                        }

                        AutoSpawnTable.getInstance().setSpawnActive(_lilithSpawn, false);
                        var9 = _crestofduskspawns.values().iterator();

                        while (var9.hasNext()) {
                            dawnCrest = (AutoSpawn) var9.next();
                            if (!AutoSpawnTable.getInstance().getAutoSpawnInstance(dawnCrest.getObjectId(), true).isSpawnActive()) {
                                AutoSpawnTable.getInstance().setSpawnActive(dawnCrest, true);
                            }
                        }

                        var9 = _crestofdawnspawns.values().iterator();

                        while (var9.hasNext()) {
                            dawnCrest = (AutoSpawn) var9.next();
                            AutoSpawnTable.getInstance().setSpawnActive(dawnCrest, false);
                        }
                }
            } else {
                AutoSpawnTable.getInstance().setSpawnActive(_merchantSpawn, false);
                AutoSpawnTable.getInstance().setSpawnActive(_lilithSpawn, false);
                AutoSpawnTable.getInstance().setSpawnActive(_anakimSpawn, false);
                var9 = _crestofdawnspawns.values().iterator();

                while (var9.hasNext()) {
                    dawnCrest = (AutoSpawn) var9.next();
                    AutoSpawnTable.getInstance().setSpawnActive(dawnCrest, false);
                }

                var9 = _crestofduskspawns.values().iterator();

                while (var9.hasNext()) {
                    dawnCrest = (AutoSpawn) var9.next();
                    AutoSpawnTable.getInstance().setSpawnActive(dawnCrest, false);
                }
            }
        }

    }

    public final int getCurrentCycle() {
        return this._currentCycle;
    }

    public final PeriodType getCurrentPeriod() {
        return this._activePeriod;
    }

    private final int getDaysToPeriodChange() {
        int numDays = this._nextPeriodChange.get(Calendar.DAY_OF_WEEK) - 2;
        return numDays < 0 ? -numDays : 7 - numDays;
    }

    public final long getMilliToPeriodChange() {
        return this._nextPeriodChange.getTimeInMillis() - System.currentTimeMillis();
    }

    protected void setCalendarForNextPeriodChange() {
        switch (this._activePeriod) {
            case SEAL_VALIDATION:
            case COMPETITION:
                int daysToChange = this.getDaysToPeriodChange();
                if (daysToChange == 7) {
                    if (this._nextPeriodChange.get(Calendar.HOUR_OF_DAY) < 18) {
                        daysToChange = 0;
                    } else if (this._nextPeriodChange.get(Calendar.HOUR_OF_DAY) == 18 && this._nextPeriodChange.get(Calendar.MINUTE) < 0) {
                        daysToChange = 0;
                    }
                }

                if (daysToChange > 0) {
                    this._nextPeriodChange.add(Calendar.DATE, daysToChange);
                }

                this._nextPeriodChange.set(Calendar.HOUR_OF_DAY, 18);
                this._nextPeriodChange.set(Calendar.MINUTE, 0);
                this._nextPeriodChange.set(Calendar.SECOND, 0);
                this._nextPeriodChange.set(Calendar.MILLISECOND, 0);
                break;
            case RECRUITING:
            case RESULTS:
                this._nextPeriodChange.add(Calendar.MILLISECOND, 900000);
        }

        LOGGER.info("Next Seven Signs period change set to {}.", this._nextPeriodChange.getTime());
    }

    public final boolean isRecruitingPeriod() {
        return this._activePeriod == PeriodType.RECRUITING;
    }

    public final boolean isSealValidationPeriod() {
        return this._activePeriod == PeriodType.SEAL_VALIDATION;
    }

    public final boolean isCompResultsPeriod() {
        return this._activePeriod == PeriodType.RESULTS;
    }

    public final int getCurrentScore(CabalType cabal) {
        double totalStoneScore = this._dawnStoneScore + this._duskStoneScore;
        switch (cabal) {
            case DAWN:
                return Math.round((float) (this._dawnStoneScore / ((float) totalStoneScore == 0.0F ? 1.0D : totalStoneScore)) * 500.0F) + this._dawnFestivalScore;
            case DUSK:
                return Math.round((float) (this._duskStoneScore / ((float) totalStoneScore == 0.0F ? 1.0D : totalStoneScore)) * 500.0F) + this._duskFestivalScore;
            default:
                return 0;
        }
    }

    public final double getCurrentStoneScore(CabalType cabal) {
        switch (cabal) {
            case DAWN:
                return this._dawnStoneScore;
            case DUSK:
                return this._duskStoneScore;
            default:
                return 0.0D;
        }
    }

    public final int getCurrentFestivalScore(CabalType cabal) {
        switch (cabal) {
            case DAWN:
                return this._dawnFestivalScore;
            case DUSK:
                return this._duskFestivalScore;
            default:
                return 0;
        }
    }

    public final CabalType getCabalHighestScore() {
        int duskScore = this.getCurrentScore(CabalType.DUSK);
        int dawnScore = this.getCurrentScore(CabalType.DAWN);
        if (duskScore == dawnScore) {
            return CabalType.NORMAL;
        } else {
            return duskScore > dawnScore ? CabalType.DUSK : CabalType.DAWN;
        }
    }

    public final CabalType getSealOwner(SealType seal) {
        return this._sealOwners.get(seal);
    }

    public final Map<SealType, CabalType> getSealOwners() {
        return this._sealOwners;
    }

    public final int getSealProportion(SealType seal, CabalType cabal) {
        switch (cabal) {
            case DAWN:
                return this._dawnScores.get(seal);
            case DUSK:
                return this._duskScores.get(seal);
            default:
                return 0;
        }
    }

    public final int getTotalMembers(CabalType cabal) {
        int cabalMembers = 0;

        for (StatSet set : this._playersData.values()) {
            if (set.getEnum("cabal", CabalType.class) == cabal) {
                ++cabalMembers;
            }
        }

        return cabalMembers;
    }

    public int getPlayerStoneContrib(int objectId) {
        StatSet set = this._playersData.get(objectId);
        return set == null ? 0 : set.getInteger("red_stones") + set.getInteger("green_stones") + set.getInteger("blue_stones");
    }

    public int getPlayerContribScore(int objectId) {
        StatSet set = this._playersData.get(objectId);
        return set == null ? 0 : set.getInteger("contribution_score");
    }

    public int getPlayerAdenaCollect(int objectId) {
        StatSet set = this._playersData.get(objectId);
        return set == null ? 0 : set.getInteger("ancient_adena_amount");
    }

    public SealType getPlayerSeal(int objectId) {
        StatSet set = this._playersData.get(objectId);
        return set == null ? SealType.NONE : set.getEnum("seal", SealType.class);
    }

    public CabalType getPlayerCabal(int objectId) {
        StatSet set = this._playersData.get(objectId);
        return set == null ? CabalType.NORMAL : set.getEnum("cabal", CabalType.class);
    }

    protected void restoreSevenSignsData() {
        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("SELECT char_obj_id, cabal, seal, red_stones, green_stones, blue_stones, ancient_adena_amount, contribution_score FROM seven_signs");

                ResultSet rs;
                try {
                    rs = ps.executeQuery();

                    try {
                        while (rs.next()) {
                            int objectId = rs.getInt("char_obj_id");
                            StatSet set = new StatSet();
                            set.set("char_obj_id", objectId);
                            set.set("cabal", Enum.valueOf(CabalType.class, rs.getString("cabal")));
                            set.set("seal", Enum.valueOf(SealType.class, rs.getString("seal")));
                            set.set("red_stones", rs.getInt("red_stones"));
                            set.set("green_stones", rs.getInt("green_stones"));
                            set.set("blue_stones", rs.getInt("blue_stones"));
                            set.set("ancient_adena_amount", rs.getDouble("ancient_adena_amount"));
                            set.set("contribution_score", rs.getDouble("contribution_score"));
                            this._playersData.put(objectId, set);
                        }
                    } catch (Throwable var11) {
                        if (rs != null) {
                            try {
                                rs.close();
                            } catch (Throwable var10) {
                                var11.addSuppressed(var10);
                            }
                        }

                        throw var11;
                    }

                    if (rs != null) {
                        rs.close();
                    }
                } catch (Throwable var12) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var9) {
                            var12.addSuppressed(var9);
                        }
                    }

                    throw var12;
                }

                if (ps != null) {
                    ps.close();
                }

                ps = con.prepareStatement("SELECT * FROM seven_signs_status WHERE id=0");

                try {
                    rs = ps.executeQuery();

                    try {
                        while (rs.next()) {
                            this._currentCycle = rs.getInt("current_cycle");
                            this._activePeriod = Enum.valueOf(PeriodType.class, rs.getString("active_period"));
                            this._previousWinner = Enum.valueOf(CabalType.class, rs.getString("previous_winner"));
                            this._dawnStoneScore = rs.getDouble("dawn_stone_score");
                            this._dawnFestivalScore = rs.getInt("dawn_festival_score");
                            this._duskStoneScore = rs.getDouble("dusk_stone_score");
                            this._duskFestivalScore = rs.getInt("dusk_festival_score");
                            this._sealOwners.put(SealType.AVARICE, Enum.valueOf(CabalType.class, rs.getString("avarice_owner")));
                            this._sealOwners.put(SealType.GNOSIS, Enum.valueOf(CabalType.class, rs.getString("gnosis_owner")));
                            this._sealOwners.put(SealType.STRIFE, Enum.valueOf(CabalType.class, rs.getString("strife_owner")));
                            this._dawnScores.put(SealType.AVARICE, rs.getInt("avarice_dawn_score"));
                            this._dawnScores.put(SealType.GNOSIS, rs.getInt("gnosis_dawn_score"));
                            this._dawnScores.put(SealType.STRIFE, rs.getInt("strife_dawn_score"));
                            this._duskScores.put(SealType.AVARICE, rs.getInt("avarice_dusk_score"));
                            this._duskScores.put(SealType.GNOSIS, rs.getInt("gnosis_dusk_score"));
                            this._duskScores.put(SealType.STRIFE, rs.getInt("strife_dusk_score"));
                            this._lastSave.setTimeInMillis(rs.getLong("date"));
                        }
                    } catch (Throwable var13) {
                        if (rs != null) {
                            try {
                                rs.close();
                            } catch (Throwable var8) {
                                var13.addSuppressed(var8);
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
                        } catch (Throwable var7) {
                            var14.addSuppressed(var7);
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
                    } catch (Throwable var6) {
                        var15.addSuppressed(var6);
                    }
                }

                throw var15;
            }

            if (con != null) {
                con.close();
            }
        } catch (Exception var16) {
            LOGGER.error("Couldn't load Seven Signs data.", var16);
        }

    }

    public void saveSevenSignsData() {
        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("UPDATE seven_signs SET cabal=?, seal=?, red_stones=?, green_stones=?, blue_stones=?, ancient_adena_amount=?, contribution_score=? WHERE char_obj_id=?");

                try {

                    for (StatSet set : this._playersData.values()) {
                        ps.setString(1, set.getString("cabal"));
                        ps.setString(2, set.getString("seal"));
                        ps.setInt(3, set.getInteger("red_stones"));
                        ps.setInt(4, set.getInteger("green_stones"));
                        ps.setInt(5, set.getInteger("blue_stones"));
                        ps.setDouble(6, set.getDouble("ancient_adena_amount"));
                        ps.setDouble(7, set.getDouble("contribution_score"));
                        ps.setInt(8, set.getInteger("char_obj_id"));
                        ps.addBatch();
                    }

                    ps.executeBatch();
                } catch (Throwable var7) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var6) {
                            var7.addSuppressed(var6);
                        }
                    }

                    throw var7;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (Throwable var8) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var5) {
                        var8.addSuppressed(var5);
                    }
                }

                throw var8;
            }

            if (con != null) {
                con.close();
            }
        } catch (Exception var9) {
            LOGGER.error("Couldn't save Seven Signs player data.", var9);
        }

    }

    public final void saveSevenSignsStatus() {
        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("UPDATE seven_signs_status SET current_cycle=?, active_period=?, previous_winner=?, dawn_stone_score=?, dawn_festival_score=?, dusk_stone_score=?, dusk_festival_score=?, avarice_owner=?, gnosis_owner=?, strife_owner=?, avarice_dawn_score=?, gnosis_dawn_score=?, strife_dawn_score=?, avarice_dusk_score=?, gnosis_dusk_score=?, strife_dusk_score=?, festival_cycle=?, accumulated_bonus0=?, accumulated_bonus1=?, accumulated_bonus2=?,accumulated_bonus3=?, accumulated_bonus4=?, date=? WHERE id=0");

                try {
                    ps.setInt(1, this._currentCycle);
                    ps.setString(2, this._activePeriod.toString());
                    ps.setString(3, this._previousWinner.toString());
                    ps.setDouble(4, this._dawnStoneScore);
                    ps.setInt(5, this._dawnFestivalScore);
                    ps.setDouble(6, this._duskStoneScore);
                    ps.setInt(7, this._duskFestivalScore);
                    ps.setString(8, this._sealOwners.get(SealType.AVARICE).toString());
                    ps.setString(9, this._sealOwners.get(SealType.GNOSIS).toString());
                    ps.setString(10, this._sealOwners.get(SealType.STRIFE).toString());
                    ps.setInt(11, (Integer) this._dawnScores.get(SealType.AVARICE));
                    ps.setInt(12, (Integer) this._dawnScores.get(SealType.GNOSIS));
                    ps.setInt(13, (Integer) this._dawnScores.get(SealType.STRIFE));
                    ps.setInt(14, (Integer) this._duskScores.get(SealType.AVARICE));
                    ps.setInt(15, (Integer) this._duskScores.get(SealType.GNOSIS));
                    ps.setInt(16, (Integer) this._duskScores.get(SealType.STRIFE));
                    ps.setInt(17, FestivalOfDarknessManager.getInstance().getCurrentFestivalCycle());

                    for (int i = 0; i < 5; ++i) {
                        ps.setInt(18 + i, FestivalOfDarknessManager.getInstance().getAccumulatedBonus(i));
                    }

                    this._lastSave = Calendar.getInstance();
                    ps.setLong(23, this._lastSave.getTimeInMillis());
                    ps.execute();
                } catch (Throwable var7) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var6) {
                            var7.addSuppressed(var6);
                        }
                    }

                    throw var7;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (Throwable var8) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var5) {
                        var8.addSuppressed(var5);
                    }
                }

                throw var8;
            }

            if (con != null) {
                con.close();
            }
        } catch (Exception var9) {
            LOGGER.error("Couldn't save Seven Signs status data.", var9);
        }

    }

    protected void resetPlayerData() {

        for (StatSet set : this._playersData.values()) {
            set.set("cabal", CabalType.NORMAL);
            set.set("seal", SealType.NONE);
            set.set("contribution_score", 0);
        }

    }

    public CabalType setPlayerInfo(int objectId, CabalType cabal, SealType seal) {
        StatSet set = this._playersData.get(objectId);
        if (set != null) {
            set.set("cabal", cabal);
            set.set("seal", seal);
        } else {
            set = new StatSet();
            set.set("char_obj_id", objectId);
            set.set("cabal", cabal);
            set.set("seal", seal);
            set.set("red_stones", 0);
            set.set("green_stones", 0);
            set.set("blue_stones", 0);
            set.set("ancient_adena_amount", 0);
            set.set("contribution_score", 0);
            this._playersData.put(objectId, set);

            try {
                Connection con = ConnectionPool.getConnection();

                try {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO seven_signs (char_obj_id, cabal, seal) VALUES (?,?,?)");

                    try {
                        ps.setInt(1, objectId);
                        ps.setString(2, cabal.toString());
                        ps.setString(3, seal.toString());
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
                LOGGER.error("Couldn't save Seven Signs player info data.", var13);
            }
        }

        if (cabal == CabalType.DAWN) {
            this._dawnScores.put(seal, this._dawnScores.get(seal) + 1);
        } else {
            this._duskScores.put(seal, this._duskScores.get(seal) + 1);
        }

        return cabal;
    }

    public int getAncientAdenaReward(int objectId) {
        StatSet set = this._playersData.get(objectId);
        int rewardAmount = set.getInteger("ancient_adena_amount");
        set.set("red_stones", 0);
        set.set("green_stones", 0);
        set.set("blue_stones", 0);
        set.set("ancient_adena_amount", 0);
        return rewardAmount;
    }

    public int addPlayerStoneContrib(int objectId, int blueCount, int greenCount, int redCount) {
        StatSet set = this._playersData.get(objectId);
        int contribScore = calcScore(blueCount, greenCount, redCount);
        int totalAncientAdena = set.getInteger("ancient_adena_amount") + contribScore;
        int totalContribScore = set.getInteger("contribution_score") + contribScore;
        if (totalContribScore > Config.ALT_MAXIMUM_PLAYER_CONTRIB) {
            return -1;
        } else {
            set.set("red_stones", set.getInteger("red_stones") + redCount);
            set.set("green_stones", set.getInteger("green_stones") + greenCount);
            set.set("blue_stones", set.getInteger("blue_stones") + blueCount);
            set.set("ancient_adena_amount", totalAncientAdena);
            set.set("contribution_score", totalContribScore);
            switch (this.getPlayerCabal(objectId)) {
                case DAWN:
                    this._dawnStoneScore += contribScore;
                    break;
                case DUSK:
                    this._duskStoneScore += contribScore;
            }

            return contribScore;
        }
    }

    public void addFestivalScore(CabalType cabal, int amount) {
        if (cabal == CabalType.DUSK) {
            this._duskFestivalScore += amount;
            if (this._dawnFestivalScore >= amount) {
                this._dawnFestivalScore -= amount;
            }
        } else {
            this._dawnFestivalScore += amount;
            if (this._duskFestivalScore >= amount) {
                this._duskFestivalScore -= amount;
            }
        }

    }

    protected void initializeSeals() {

        for (Entry<SealType, CabalType> sealEntry : this._sealOwners.entrySet()) {
            SealType currentSeal = sealEntry.getKey();
            CabalType sealOwner = sealEntry.getValue();
            if (sealOwner != CabalType.NORMAL) {
                if (this.isSealValidationPeriod()) {
                    LOGGER.info("The {} have won {}.", sealOwner.getFullName(), currentSeal.getFullName());
                } else {
                    LOGGER.info("The {} is currently owned by {}.", currentSeal.getFullName(), sealOwner.getFullName());
                }
            } else {
                LOGGER.info("The {} remains unclaimed.", currentSeal.getFullName());
            }
        }

    }

    protected void resetSeals() {
        this._dawnScores.put(SealType.AVARICE, 0);
        this._dawnScores.put(SealType.GNOSIS, 0);
        this._dawnScores.put(SealType.STRIFE, 0);
        this._duskScores.put(SealType.AVARICE, 0);
        this._duskScores.put(SealType.GNOSIS, 0);
        this._duskScores.put(SealType.STRIFE, 0);
    }

    protected void calcNewSealOwners() {

        for (SealType sealType : this._dawnScores.keySet()) {
            SealType seal;
            CabalType newSealOwner;
            seal = sealType;
            CabalType prevSealOwner = this._sealOwners.get(seal);
            int dawnProportion = this.getSealProportion(seal, CabalType.DAWN);
            int totalDawnMembers = Math.max(1, this.getTotalMembers(CabalType.DAWN));
            int dawnPercent = Math.round((float) dawnProportion / (float) totalDawnMembers * 100.0F);
            int duskProportion = this.getSealProportion(seal, CabalType.DUSK);
            int totalDuskMembers = Math.max(1, this.getTotalMembers(CabalType.DUSK));
            int duskPercent = Math.round((float) duskProportion / (float) totalDuskMembers * 100.0F);
            newSealOwner = CabalType.NORMAL;
            label78:
            switch (prevSealOwner) {
                case DAWN:
                    switch (this.getCabalHighestScore()) {
                        case DAWN:
                            if (dawnPercent >= 10) {
                                newSealOwner = CabalType.DAWN;
                            }
                            break label78;
                        case DUSK:
                            if (duskPercent >= 35) {
                                newSealOwner = CabalType.DUSK;
                            } else if (dawnPercent >= 10) {
                                newSealOwner = CabalType.DAWN;
                            }
                            break label78;
                        case NORMAL:
                            if (dawnPercent >= 10) {
                                newSealOwner = CabalType.DAWN;
                            }
                        default:
                            break label78;
                    }
                case DUSK:
                    switch (this.getCabalHighestScore()) {
                        case DAWN:
                            if (dawnPercent >= 35) {
                                newSealOwner = CabalType.DAWN;
                            } else if (duskPercent >= 10) {
                                newSealOwner = CabalType.DUSK;
                            }
                            break label78;
                        case DUSK:
                            if (duskPercent >= 10) {
                                newSealOwner = CabalType.DUSK;
                            }
                            break label78;
                        case NORMAL:
                            if (duskPercent >= 10) {
                                newSealOwner = CabalType.DUSK;
                            }
                        default:
                            break label78;
                    }
                case NORMAL:
                    switch (this.getCabalHighestScore()) {
                        case DAWN:
                            if (dawnPercent >= 35) {
                                newSealOwner = CabalType.DAWN;
                            }
                            break;
                        case DUSK:
                            if (duskPercent >= 35) {
                                newSealOwner = CabalType.DUSK;
                            }
                    }
            }

            this._sealOwners.put(seal, newSealOwner);
            switch (seal) {
                case AVARICE:
                    if (newSealOwner == CabalType.DAWN) {
                        World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.DAWN_OBTAINED_AVARICE));
                    } else if (newSealOwner == CabalType.DUSK) {
                        World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.DUSK_OBTAINED_AVARICE));
                    }
                    break;
                case GNOSIS:
                    if (newSealOwner == CabalType.DAWN) {
                        World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.DAWN_OBTAINED_GNOSIS));
                    } else if (newSealOwner == CabalType.DUSK) {
                        World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.DUSK_OBTAINED_GNOSIS));
                    }
                    break;
                case STRIFE:
                    if (newSealOwner == CabalType.DAWN) {
                        World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.DAWN_OBTAINED_STRIFE));
                    } else if (newSealOwner == CabalType.DUSK) {
                        World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.DUSK_OBTAINED_STRIFE));
                    }

                    CastleManager.getInstance().validateTaxes(newSealOwner);
            }
        }

    }

    protected void teleLosingCabalFromDungeons(CabalType winningCabal) {
        Iterator<Player> var2 = World.getInstance().getPlayers().iterator();

        while (true) {
            Player player = null;
            while (true) {
                do {
                    do {
                        if (!var2.hasNext()) {
                            return;
                        }

                        player = var2.next();
                    } while (player.isGM());
                } while (!player.isIn7sDungeon());

                StatSet set = this._playersData.get(player.getObjectId());
                if (set == null) {
                    break;
                }

                CabalType playerCabal = set.getEnum("cabal", CabalType.class);
                if (!this.isSealValidationPeriod() && !this.isCompResultsPeriod()) {
                    if (playerCabal == CabalType.NORMAL) {
                        continue;
                    }
                    break;
                } else if (playerCabal != winningCabal) {
                    break;
                }
            }

            player.teleportTo(TeleportType.TOWN);
            player.setIsIn7sDungeon(false);
        }
    }

    public void giveSosEffect(CabalType strifeOwner) {

        for (Player player : World.getInstance().getPlayers()) {
            CabalType cabal = this.getPlayerCabal(player.getObjectId());
            if (cabal != CabalType.NORMAL) {
                if (cabal == strifeOwner) {
                    player.addSkill(FrequentSkill.THE_VICTOR_OF_WAR.getSkill(), false);
                } else {
                    player.addSkill(FrequentSkill.THE_VANQUISHED_OF_WAR.getSkill(), false);
                }
            }
        }

    }

    public void removeSosEffect() {

        for (Player player : World.getInstance().getPlayers()) {
            player.removeSkill(FrequentSkill.THE_VICTOR_OF_WAR.getSkill().getId(), false);
            player.removeSkill(FrequentSkill.THE_VANQUISHED_OF_WAR.getSkill().getId(), false);
        }

    }

    private static class SingletonHolder {
        protected static final SevenSignsManager INSTANCE = new SevenSignsManager();
    }

    protected class SevenSignsPeriodChange implements Runnable {
        public void run() {
            PeriodType periodEnded = SevenSignsManager.this._activePeriod;
            SevenSignsManager.this._activePeriod = PeriodType.VALUES[(SevenSignsManager.this._activePeriod.ordinal() + 1) % PeriodType.VALUES.length];
            switch (periodEnded) {
                case SEAL_VALIDATION:
                    SevenSignsManager.this._activePeriod = PeriodType.RECRUITING;
                    World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.SEAL_VALIDATION_PERIOD_ENDED));
                    SevenSignsManager.this.removeSosEffect();
                    SevenSignsManager.this.resetPlayerData();
                    SevenSignsManager.this.resetSeals();
                    ++SevenSignsManager.this._currentCycle;
                    FestivalOfDarknessManager.getInstance().resetFestivalData(false);
                    SevenSignsManager.this._dawnStoneScore = 0.0D;
                    SevenSignsManager.this._duskStoneScore = 0.0D;
                    SevenSignsManager.this._dawnFestivalScore = 0;
                    SevenSignsManager.this._duskFestivalScore = 0;
                    break;
                case COMPETITION:
                    World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.QUEST_EVENT_PERIOD_ENDED));
                    CabalType winningCabal = SevenSignsManager.this.getCabalHighestScore();
                    FestivalOfDarknessManager.getInstance().getFestivalManagerSchedule().cancel(false);
                    FestivalOfDarknessManager.getInstance().rewardHighestRanked();
                    SevenSignsManager.this.calcNewSealOwners();
                    switch (winningCabal) {
                        case DAWN:
                            World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.DAWN_WON));
                            break;
                        case DUSK:
                            World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.DUSK_WON));
                    }

                    SevenSignsManager.this._previousWinner = winningCabal;
                    break;
                case RECRUITING:
                    FestivalOfDarknessManager.getInstance().startFestivalManager();
                    CastleManager.getInstance().resetCertificates();
                    World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.QUEST_EVENT_PERIOD_BEGUN));
                    break;
                case RESULTS:
                    SevenSignsManager.this.initializeSeals();
                    SevenSignsManager.this.giveSosEffect(SevenSignsManager.this.getSealOwner(SealType.STRIFE));
                    World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.SEAL_VALIDATION_PERIOD_BEGUN));
                    SevenSignsManager.LOGGER.info("The {} have won the competition with {} points.", SevenSignsManager.this._previousWinner.getFullName(), SevenSignsManager.this.getCurrentScore(SevenSignsManager.this._previousWinner));
            }

            SevenSignsManager.this.saveSevenSignsData();
            SevenSignsManager.this.saveSevenSignsStatus();
            SevenSignsManager.this.teleLosingCabalFromDungeons(SevenSignsManager.this.getCabalHighestScore());
            World.toAllOnlinePlayers(SSQInfo.sendSky());
            SevenSignsManager.this.spawnSevenSignsNPC();
            SevenSignsManager.LOGGER.info("The {} period of Seven Signs has begun.", SevenSignsManager.this._activePeriod.getName());
            SevenSignsManager.this.setCalendarForNextPeriodChange();
            ThreadPool.schedule(SevenSignsManager.this.new SevenSignsPeriodChange(), SevenSignsManager.this.getMilliToPeriodChange());
        }
    }
}