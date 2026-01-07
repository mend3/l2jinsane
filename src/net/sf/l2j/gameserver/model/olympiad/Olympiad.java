/**/
package net.sf.l2j.gameserver.model.olympiad;

import net.sf.l2j.Config;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.manager.HeroManager;
import net.sf.l2j.gameserver.data.manager.ZoneManager;
import net.sf.l2j.gameserver.enums.OlympiadState;
import net.sf.l2j.gameserver.enums.OlympiadType;
import net.sf.l2j.gameserver.enums.actors.ClassId;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.OlympiadManagerNpc;
import net.sf.l2j.gameserver.model.zone.type.OlympiadStadiumZone;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.NpcSay;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;

public class Olympiad {
    public static final String OLYMPIAD_HTML_PATH = "data/html/olympiad/";
    public static final String CHAR_ID = "char_id";
    public static final String CLASS_ID = "class_id";
    public static final String CHAR_NAME = "char_name";
    public static final String POINTS = "olympiad_points";
    public static final String COMP_DONE = "competitions_done";
    public static final String COMP_WON = "competitions_won";
    public static final String COMP_LOST = "competitions_lost";
    public static final String COMP_DRAWN = "competitions_drawn";
    protected static final CLogger LOGGER = new CLogger(Olympiad.class.getName());
    private static final String OLYMPIAD_LOAD_DATA = "SELECT current_cycle, period, olympiad_end, validation_end, next_weekly_change FROM olympiad_data WHERE id = 0";
    private static final String OLYMPIAD_SAVE_DATA = "INSERT INTO olympiad_data (id, current_cycle, period, olympiad_end, validation_end, next_weekly_change) VALUES (0,?,?,?,?,?) ON DUPLICATE KEY UPDATE current_cycle=?, period=?, olympiad_end=?, validation_end=?, next_weekly_change=?";
    private static final String OLYMPIAD_LOAD_NOBLES = "SELECT olympiad_nobles.char_id, olympiad_nobles.class_id, characters.char_name, olympiad_nobles.olympiad_points, olympiad_nobles.competitions_done, olympiad_nobles.competitions_won, olympiad_nobles.competitions_lost, olympiad_nobles.competitions_drawn FROM olympiad_nobles, characters WHERE characters.obj_Id = olympiad_nobles.char_id";
    private static final String OLYMPIAD_SAVE_NOBLES = "INSERT INTO olympiad_nobles (`char_id`,`class_id`,`olympiad_points`,`competitions_done`,`competitions_won`,`competitions_lost`, `competitions_drawn`) VALUES (?,?,?,?,?,?,?)";
    private static final String OLYMPIAD_UPDATE_NOBLES = "UPDATE olympiad_nobles SET olympiad_points = ?, competitions_done = ?, competitions_won = ?, competitions_lost = ?, competitions_drawn = ? WHERE char_id = ?";
    private static final String OLYMPIAD_GET_HEROS;
    private static final String GET_ALL_CLASSIFIED_NOBLESS;
    private static final String GET_EACH_CLASS_LEADER;
    private static final String OLYMPIAD_LOAD_POINTS = "SELECT olympiad_points FROM olympiad_nobles_eom WHERE char_id = ?";
    private static final String OLYMPIAD_DELETE_ALL = "TRUNCATE olympiad_nobles";
    private static final String OLYMPIAD_MONTH_CLEAR = "TRUNCATE olympiad_nobles_eom";
    private static final String OLYMPIAD_MONTH_CREATE = "INSERT INTO olympiad_nobles_eom SELECT char_id, class_id, olympiad_points, competitions_done, competitions_won, competitions_lost, competitions_drawn FROM olympiad_nobles";

    static {
        OLYMPIAD_GET_HEROS = "SELECT olympiad_nobles.char_id, characters.char_name FROM olympiad_nobles, characters WHERE characters.obj_Id = olympiad_nobles.char_id AND olympiad_nobles.class_id = ? AND olympiad_nobles.competitions_done >= " + Config.ALT_OLY_MIN_MATCHES + " AND olympiad_nobles.competitions_won > 0 ORDER BY olympiad_nobles.olympiad_points DESC, olympiad_nobles.competitions_done DESC, olympiad_nobles.competitions_won DESC";
        GET_ALL_CLASSIFIED_NOBLESS = "SELECT char_id from olympiad_nobles_eom WHERE competitions_done >= " + Config.ALT_OLY_MIN_MATCHES + " ORDER BY olympiad_points DESC, competitions_done DESC, competitions_won DESC";
        GET_EACH_CLASS_LEADER = "SELECT characters.char_name from olympiad_nobles_eom, characters WHERE characters.obj_Id = olympiad_nobles_eom.char_id AND olympiad_nobles_eom.class_id = ? AND olympiad_nobles_eom.competitions_done >= " + Config.ALT_OLY_MIN_MATCHES + " ORDER BY olympiad_nobles_eom.olympiad_points DESC, olympiad_nobles_eom.competitions_done DESC, olympiad_nobles_eom.competitions_won DESC LIMIT 10";
    }

    private final Map<Integer, StatSet> _nobles = new HashMap<>();
    private final Map<Integer, Integer> _noblesRank = new HashMap<>();
    private final List<StatSet> _heroesToBe = new ArrayList<>();
    protected long _olympiadEnd;
    protected long _validationEnd;
    protected OlympiadState _period;
    protected long _nextWeeklyChange;
    protected int _currentCycle;
    protected boolean _isInCompPeriod;
    protected boolean _compStarted = false;
    protected ScheduledFuture<?> _scheduledCompStart;
    protected ScheduledFuture<?> _scheduledCompEnd;
    protected ScheduledFuture<?> _scheduledOlympiadEnd;
    protected ScheduledFuture<?> _scheduledWeeklyTask;
    protected ScheduledFuture<?> _scheduledValdationTask;
    protected ScheduledFuture<?> _gameManager;
    protected ScheduledFuture<?> _gameAnnouncer;
    private long _compEnd;
    private Calendar _compStart;

    public static Olympiad getInstance() {
        return Olympiad.SingletonHolder.INSTANCE;
    }

    public void load() {
        Connection con;
        PreparedStatement ps;
        ResultSet rset;
        try {
            con = ConnectionPool.getConnection();

            try {
                ps = con.prepareStatement("SELECT current_cycle, period, olympiad_end, validation_end, next_weekly_change FROM olympiad_data WHERE id = 0");

                try {
                    rset = ps.executeQuery();

                    try {
                        if (rset.next()) {
                            this._currentCycle = rset.getInt("current_cycle");
                            this._period = Enum.valueOf(OlympiadState.class, rset.getString("period"));
                            this._olympiadEnd = rset.getLong("olympiad_end");
                            this._validationEnd = rset.getLong("validation_end");
                            this._nextWeeklyChange = rset.getLong("next_weekly_change");
                        } else {
                            this._currentCycle = 1;
                            this._period = OlympiadState.COMPETITION;
                            this._olympiadEnd = 0L;
                            this._validationEnd = 0L;
                            this._nextWeeklyChange = 0L;
                            LOGGER.info("Couldn't load Olympiad data, default values are used.");
                        }
                    } catch (Throwable var18) {
                        if (rset != null) {
                            try {
                                rset.close();
                            } catch (Throwable var12) {
                                var18.addSuppressed(var12);
                            }
                        }

                        throw var18;
                    }

                    rset.close();
                } catch (Throwable var19) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var11) {
                            var19.addSuppressed(var11);
                        }
                    }

                    throw var19;
                }

                ps.close();
            } catch (Throwable var20) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var10) {
                        var20.addSuppressed(var10);
                    }
                }

                throw var20;
            }

            con.close();
        } catch (Exception var21) {
            LOGGER.error("Couldn't load Olympiad data.", var21);
        }

        switch (this._period) {
            case COMPETITION:
                if (this._olympiadEnd != 0L && this._olympiadEnd >= Calendar.getInstance().getTimeInMillis()) {
                    this.scheduleWeeklyChange();
                } else {
                    this.setNewOlympiadEnd();
                }
                break;
            case VALIDATION:
                if (this._validationEnd > Calendar.getInstance().getTimeInMillis()) {
                    this.loadNoblesRank();
                    this._scheduledValdationTask = ThreadPool.schedule(new Olympiad.ValidationEndTask(), this.getMillisToValidationEnd());
                } else {
                    ++this._currentCycle;
                    this._period = OlympiadState.COMPETITION;
                    this.deleteNobles();
                    this.setNewOlympiadEnd();
                }
        }

        try {
            con = ConnectionPool.getConnection();

            try {
                ps = con.prepareStatement("SELECT olympiad_nobles.char_id, olympiad_nobles.class_id, characters.char_name, olympiad_nobles.olympiad_points, olympiad_nobles.competitions_done, olympiad_nobles.competitions_won, olympiad_nobles.competitions_lost, olympiad_nobles.competitions_drawn FROM olympiad_nobles, characters WHERE characters.obj_Id = olympiad_nobles.char_id");

                try {
                    rset = ps.executeQuery();

                    try {
                        while (rset.next()) {
                            StatSet statData = new StatSet();
                            statData.set("class_id", rset.getInt("class_id"));
                            statData.set("char_name", rset.getString("char_name"));
                            statData.set("olympiad_points", rset.getInt("olympiad_points"));
                            statData.set("competitions_done", rset.getInt("competitions_done"));
                            statData.set("competitions_won", rset.getInt("competitions_won"));
                            statData.set("competitions_lost", rset.getInt("competitions_lost"));
                            statData.set("competitions_drawn", rset.getInt("competitions_drawn"));
                            statData.set("to_save", false);
                            this.addNobleStats(rset.getInt("char_id"), statData);
                        }
                    } catch (Throwable var14) {
                        if (rset != null) {
                            try {
                                rset.close();
                            } catch (Throwable var9) {
                                var14.addSuppressed(var9);
                            }
                        }

                        throw var14;
                    }

                    rset.close();
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

                ps.close();
            } catch (Throwable var16) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var7) {
                        var16.addSuppressed(var7);
                    }
                }

                throw var16;
            }

            con.close();
        } catch (Exception var17) {
            LOGGER.error("Couldn't load noblesse data.", var17);
        }

        synchronized (this) {
            long milliToEnd;
            if (this._period == OlympiadState.COMPETITION) {
                milliToEnd = this.getMillisToOlympiadEnd();
            } else {
                milliToEnd = this.getMillisToValidationEnd();
            }

            LOGGER.info("{} minutes until Olympiad period ends.", Math.round((float) (milliToEnd / 60000L)));
            if (this._period == OlympiadState.COMPETITION) {
                milliToEnd = this.getMillisToWeekChange();
                LOGGER.info("Next weekly Olympiad change is in {} minutes.", Math.round((float) (milliToEnd / 60000L)));
            }
        }

        LOGGER.info("Loaded {} nobles.", this._nobles.size());
        if (this._period == OlympiadState.COMPETITION) {
            this.init();
        }
    }

    public void loadNoblesRank() {
        this._noblesRank.clear();
        HashMap<Integer, Integer> tmpPlace = new HashMap<>();

        int rank4;
        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement(GET_ALL_CLASSIFIED_NOBLESS);

                try {
                    ResultSet rs = ps.executeQuery();

                    try {
                        rank4 = 1;

                        while (rs.next()) {
                            tmpPlace.put(rs.getInt("char_id"), rank4++);
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

                    rs.close();
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

                ps.close();
            } catch (Throwable var13) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var8) {
                        var13.addSuppressed(var8);
                    }
                }

                throw var13;
            }

            con.close();
        } catch (Exception var14) {
            LOGGER.error("Couldn't load Olympiad ranks.", var14);
        }

        int rank1 = (int) Math.round((double) tmpPlace.size() * 0.01D);
        int rank2 = (int) Math.round((double) tmpPlace.size() * 0.1D);
        int rank3 = (int) Math.round((double) tmpPlace.size() * 0.25D);
        rank4 = (int) Math.round((double) tmpPlace.size() * 0.5D);
        if (rank1 == 0) {
            rank1 = 1;
            ++rank2;
            ++rank3;
            ++rank4;
        }

        for (Integer charId : tmpPlace.keySet()) {
            if (tmpPlace.get(charId) <= rank1) {
                this._noblesRank.put(charId, 1);
            } else if (tmpPlace.get(charId) <= rank2) {
                this._noblesRank.put(charId, 2);
            } else if (tmpPlace.get(charId) <= rank3) {
                this._noblesRank.put(charId, 3);
            } else if (tmpPlace.get(charId) <= rank4) {
                this._noblesRank.put(charId, 4);
            } else {
                this._noblesRank.put(charId, 5);
            }
        }

    }

    protected void init() {
        if (this._period != OlympiadState.VALIDATION) {
            this._compStart = Calendar.getInstance();
            this._compStart.set(Calendar.HOUR_OF_DAY, Config.ALT_OLY_START_TIME);
            this._compStart.set(Calendar.MINUTE, Config.ALT_OLY_MIN);
            this._compStart.set(Calendar.SECOND, 0);
            this._compEnd = this._compStart.getTimeInMillis() + Config.ALT_OLY_CPERIOD;
            if (this._scheduledOlympiadEnd != null) {
                this._scheduledOlympiadEnd.cancel(true);
            }

            this._scheduledOlympiadEnd = ThreadPool.schedule(new Olympiad.OlympiadEndTask(), this.getMillisToOlympiadEnd());
            this.updateCompStatus();
        }
    }

    protected int getNobleCount() {
        return this._nobles.size();
    }

    protected StatSet getNobleStats(int playerId) {
        return this._nobles.get(playerId);
    }

    @SuppressWarnings("BusyWait")
    private void updateCompStatus() {
        synchronized (this) {
            long milliToStart = this.getMillisToCompBegin();
            double numSecs = (double) (milliToStart / 1000L % 60L);
            double countDown = ((double) (milliToStart / 1000L) - numSecs) / 60.0D;
            int numMins = (int) Math.floor(countDown % 60.0D);
            countDown = (countDown - (double) numMins) / 60.0D;
            int numHours = (int) Math.floor(countDown % 24.0D);
            int numDays = (int) Math.floor((countDown - (double) numHours) / 24.0D);
            LOGGER.info("Olympiad competition period starts in {} days, {} hours and {} mins.", numDays, numHours, numMins);
            LOGGER.info("Olympiad event starts/started @ {}.", this._compStart.getTime());
        }

        this._scheduledCompStart = ThreadPool.schedule(() -> {
            if (!this.isOlympiadEnd()) {
                this._isInCompPeriod = true;
                World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.THE_OLYMPIAD_GAME_HAS_STARTED));
                LOGGER.info("Olympiad game started.");
                this._gameManager = ThreadPool.scheduleAtFixedRate(OlympiadGameManager.getInstance(), 30000L, 30000L);
                if (Config.ALT_OLY_ANNOUNCE_GAMES) {
                    this._gameAnnouncer = ThreadPool.scheduleAtFixedRate(new OlympiadAnnouncer(), 30000L, 500L);
                }

                long regEnd = this.getMillisToCompEnd() - 600000L;
                if (regEnd > 0L) {
                    ThreadPool.schedule(() -> {
                        World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.OLYMPIAD_REGISTRATION_PERIOD_ENDED));
                    }, regEnd);
                }

                this._scheduledCompEnd = ThreadPool.schedule(() -> {
                    if (!this.isOlympiadEnd()) {
                        this._isInCompPeriod = false;
                        World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.THE_OLYMPIAD_GAME_HAS_ENDED));
                        LOGGER.info("Olympiad game ended.");

                        while (OlympiadGameManager.getInstance().isBattleStarted()) {
                            try {
                                Thread.sleep(60000L);
                            } catch (InterruptedException ignored) {
                            }
                        }

                        if (this._gameManager != null) {
                            this._gameManager.cancel(false);
                            this._gameManager = null;
                        }

                        if (this._gameAnnouncer != null) {
                            this._gameAnnouncer.cancel(false);
                            this._gameAnnouncer = null;
                        }

                        this.saveOlympiadStatus();
                        this.init();
                    }
                }, this.getMillisToCompEnd());
            }
        }, this.getMillisToCompBegin());
    }

    private long getMillisToOlympiadEnd() {
        return this._olympiadEnd - Calendar.getInstance().getTimeInMillis();
    }

    public void manualSelectHeroes() {
        if (this._scheduledOlympiadEnd != null) {
            this._scheduledOlympiadEnd.cancel(true);
        }

        this._scheduledOlympiadEnd = ThreadPool.schedule(new Olympiad.OlympiadEndTask(), 0L);
    }

    protected long getMillisToValidationEnd() {
        return this._validationEnd > Calendar.getInstance().getTimeInMillis() ? this._validationEnd - Calendar.getInstance().getTimeInMillis() : 10L;
    }

    public boolean isOlympiadEnd() {
        return this._period == OlympiadState.VALIDATION;
    }

    protected void setNewOlympiadEnd() {
        World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.OLYMPIAD_PERIOD_S1_HAS_STARTED).addNumber(this._currentCycle));
        Calendar currentTime;
        Calendar nextChange;
        if (!Config.OLY_USE_CUSTOM_PERIOD_SETTINGS) {
            currentTime = Calendar.getInstance();
            currentTime.add(Calendar.MONTH, 1);
            currentTime.set(Calendar.DATE, 1);
            currentTime.set(Calendar.AM_PM, 0);
            currentTime.set(Calendar.HOUR, 12);
            currentTime.set(Calendar.MINUTE, 0);
            currentTime.set(Calendar.SECOND, 0);
            this._olympiadEnd = currentTime.getTimeInMillis();
            nextChange = Calendar.getInstance();
            this._nextWeeklyChange = nextChange.getTimeInMillis() + Config.ALT_OLY_WPERIOD;
            this.scheduleWeeklyChange();
        } else {
            currentTime = Calendar.getInstance();
            currentTime.set(Calendar.AM_PM, 0);
            currentTime.set(Calendar.HOUR, 12);
            currentTime.set(Calendar.MINUTE, 0);
            currentTime.set(Calendar.SECOND, 0);
            nextChange = Calendar.getInstance();
            switch (Config.OLY_PERIOD) {
                case DAY:
                    currentTime.add(Calendar.DATE, Config.OLY_PERIOD_MULTIPLIER);
                    currentTime.add(Calendar.DATE, -1);
                    if (Config.OLY_PERIOD_MULTIPLIER >= 14) {
                        this._nextWeeklyChange = nextChange.getTimeInMillis() + Config.ALT_OLY_WPERIOD;
                    } else if (Config.OLY_PERIOD_MULTIPLIER >= 7) {
                        this._nextWeeklyChange = nextChange.getTimeInMillis() + Config.ALT_OLY_WPERIOD / 2L;
                    }
                    break;
                case WEEK:
                    currentTime.add(Calendar.WEEK_OF_MONTH, Config.OLY_PERIOD_MULTIPLIER);
                    currentTime.add(Calendar.DATE, -1);
                    if (Config.OLY_PERIOD_MULTIPLIER > 1) {
                        this._nextWeeklyChange = nextChange.getTimeInMillis() + Config.ALT_OLY_WPERIOD;
                    } else {
                        this._nextWeeklyChange = nextChange.getTimeInMillis() + Config.ALT_OLY_WPERIOD / 2L;
                    }
                    break;
                case MONTH:
                    currentTime.add(Calendar.MONTH, Config.OLY_PERIOD_MULTIPLIER);
                    currentTime.add(Calendar.DATE, -1);
                    this._nextWeeklyChange = nextChange.getTimeInMillis() + Config.ALT_OLY_WPERIOD;
            }

            this._olympiadEnd = currentTime.getTimeInMillis();
        }

        this.scheduleWeeklyChange();
    }

    public boolean isInCompPeriod() {
        return this._isInCompPeriod;
    }

    private long getMillisToCompBegin() {
        if (this._compStart.getTimeInMillis() < Calendar.getInstance().getTimeInMillis() && this._compEnd > Calendar.getInstance().getTimeInMillis()) {
            return 10L;
        } else {
            return this._compStart.getTimeInMillis() > Calendar.getInstance().getTimeInMillis() ? this._compStart.getTimeInMillis() - Calendar.getInstance().getTimeInMillis() : this.setNewCompBegin();
        }
    }

    private long setNewCompBegin() {
        this._compStart = Calendar.getInstance();
        this._compStart.set(Calendar.HOUR_OF_DAY, Config.ALT_OLY_START_TIME);
        this._compStart.set(Calendar.MINUTE, Config.ALT_OLY_MIN);
        this._compStart.set(Calendar.SECOND, 0);
        this._compStart.add(Calendar.HOUR_OF_DAY, 24);
        this._compEnd = this._compStart.getTimeInMillis() + Config.ALT_OLY_CPERIOD;
        LOGGER.info("New Olympiad schedule @ {}.", this._compStart.getTime());
        return this._compStart.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
    }

    protected long getMillisToCompEnd() {
        return this._compEnd - Calendar.getInstance().getTimeInMillis();
    }

    private long getMillisToWeekChange() {
        return this._nextWeeklyChange > Calendar.getInstance().getTimeInMillis() ? this._nextWeeklyChange - Calendar.getInstance().getTimeInMillis() : 10L;
    }

    private void scheduleWeeklyChange() {
        this._scheduledWeeklyTask = ThreadPool.scheduleAtFixedRate(() -> {
            this.addWeeklyPoints();
            LOGGER.info("Added weekly Olympiad points to nobles.");
            Calendar nextChange = Calendar.getInstance();
            this._nextWeeklyChange = nextChange.getTimeInMillis() + Config.ALT_OLY_WPERIOD;
        }, this.getMillisToWeekChange(), Config.ALT_OLY_WPERIOD);
    }

    protected synchronized void addWeeklyPoints() {
        if (this._period != OlympiadState.VALIDATION) {

            for (StatSet nobleInfo : this._nobles.values()) {
                int currentPoints = nobleInfo.getInteger("olympiad_points");
                currentPoints += Config.ALT_OLY_WEEKLY_POINTS;
                nobleInfo.set("olympiad_points", currentPoints);
            }

        }
    }

    public int getCurrentCycle() {
        return this._currentCycle;
    }

    public boolean playerInStadia(Player player) {
        return ZoneManager.getInstance().getZone(player, OlympiadStadiumZone.class) != null;
    }

    protected synchronized void saveNobleData() {
        if (!this._nobles.isEmpty()) {
            try {
                Connection con = ConnectionPool.getConnection();

                try {

                    for (Entry<Integer, StatSet> noble : this._nobles.entrySet()) {
                        StatSet set = noble.getValue();
                        if (set != null) {
                            int charId = noble.getKey();
                            int classId = set.getInteger("class_id");
                            int points = set.getInteger("olympiad_points");
                            int compDone = set.getInteger("competitions_done");
                            int compWon = set.getInteger("competitions_won");
                            int compLost = set.getInteger("competitions_lost");
                            int compDrawn = set.getInteger("competitions_drawn");
                            boolean toSave = set.getBool("to_save");
                            PreparedStatement ps;
                            if (toSave) {
                                ps = con.prepareStatement("INSERT INTO olympiad_nobles (`char_id`,`class_id`,`olympiad_points`,`competitions_done`,`competitions_won`,`competitions_lost`, `competitions_drawn`) VALUES (?,?,?,?,?,?,?)");
                                ps.setInt(1, charId);
                                ps.setInt(2, classId);
                                ps.setInt(3, points);
                                ps.setInt(4, compDone);
                                ps.setInt(5, compWon);
                                ps.setInt(6, compLost);
                                ps.setInt(7, compDrawn);
                                set.set("to_save", false);
                            } else {
                                ps = con.prepareStatement("UPDATE olympiad_nobles SET olympiad_points = ?, competitions_done = ?, competitions_won = ?, competitions_lost = ?, competitions_drawn = ? WHERE char_id = ?");
                                ps.setInt(1, points);
                                ps.setInt(2, compDone);
                                ps.setInt(3, compWon);
                                ps.setInt(4, compLost);
                                ps.setInt(5, compDrawn);
                                ps.setInt(6, charId);
                            }

                            ps.execute();
                            ps.close();
                        }
                    }
                } catch (Throwable var15) {
                    if (con != null) {
                        try {
                            con.close();
                        } catch (Throwable var14) {
                            var15.addSuppressed(var14);
                        }
                    }

                    throw var15;
                }

                if (con != null) {
                    con.close();
                }
            } catch (Exception var16) {
                LOGGER.error("Couldn't save Olympiad nobles data.", var16);
            }

        }
    }

    public void saveOlympiadStatus() {
        this.saveNobleData();

        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("INSERT INTO olympiad_data (id, current_cycle, period, olympiad_end, validation_end, next_weekly_change) VALUES (0,?,?,?,?,?) ON DUPLICATE KEY UPDATE current_cycle=?, period=?, olympiad_end=?, validation_end=?, next_weekly_change=?");

                try {
                    ps.setInt(1, this._currentCycle);
                    ps.setString(2, this._period.toString());
                    ps.setLong(3, this._olympiadEnd);
                    ps.setLong(4, this._validationEnd);
                    ps.setLong(5, this._nextWeeklyChange);
                    ps.setInt(6, this._currentCycle);
                    ps.setString(7, this._period.toString());
                    ps.setLong(8, this._olympiadEnd);
                    ps.setLong(9, this._validationEnd);
                    ps.setLong(10, this._nextWeeklyChange);
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

                ps.close();
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

            con.close();
        } catch (Exception var9) {
            LOGGER.error("Couldn't save Olympiad status.", var9);
        }

    }

    protected void updateMonthlyData() {
        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("TRUNCATE olympiad_nobles_eom");

                try {
                    PreparedStatement ps2 = con.prepareStatement("INSERT INTO olympiad_nobles_eom SELECT char_id, class_id, olympiad_points, competitions_done, competitions_won, competitions_lost, competitions_drawn FROM olympiad_nobles");

                    try {
                        ps.execute();
                        ps2.execute();
                    } catch (Throwable var9) {
                        if (ps2 != null) {
                            try {
                                ps2.close();
                            } catch (Throwable var8) {
                                var9.addSuppressed(var8);
                            }
                        }

                        throw var9;
                    }

                    ps2.close();
                } catch (Throwable var10) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var7) {
                            var10.addSuppressed(var7);
                        }
                    }

                    throw var10;
                }

                ps.close();
            } catch (Throwable var11) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var6) {
                        var11.addSuppressed(var6);
                    }
                }

                throw var11;
            }

            con.close();
        } catch (Exception var12) {
            LOGGER.error("Couldn't update monthly Olympiad nobles.", var12);
        }

    }

    protected void sortHeroesToBe() {
        this._heroesToBe.clear();

        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement(OLYMPIAD_GET_HEROS);

                try {
                    for (ClassId id : ClassId.VALUES) {
                        if (id.level() == 3) {
                            ps.setInt(1, id.getId());
                            ResultSet rs = ps.executeQuery();

                            try {
                                if (rs.next()) {
                                    StatSet hero = new StatSet();
                                    hero.set("class_id", id.getId());
                                    hero.set("char_id", rs.getInt("char_id"));
                                    hero.set("char_name", rs.getString("char_name"));
                                    this._heroesToBe.add(hero);
                                }

                                ps.clearParameters();
                            } catch (Throwable var13) {
                                if (rs != null) {
                                    try {
                                        rs.close();
                                    } catch (Throwable var12) {
                                        var13.addSuppressed(var12);
                                    }
                                }

                                throw var13;
                            }

                            rs.close();
                        }
                    }
                } catch (Throwable var14) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var11) {
                            var14.addSuppressed(var11);
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
                    } catch (Throwable var10) {
                        var15.addSuppressed(var10);
                    }
                }

                throw var15;
            }

            con.close();
        } catch (Exception var16) {
            LOGGER.error("Couldn't load future Olympiad heroes.", var16);
        }

    }

    public List<String> getClassLeaderBoard(int classId) {
        ArrayList<String> names = new ArrayList<>();

        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement(GET_EACH_CLASS_LEADER);

                try {
                    ps.setInt(1, classId);
                    ResultSet rs = ps.executeQuery();

                    try {
                        while (rs.next()) {
                            names.add(rs.getString("char_name"));
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

                    rs.close();
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

                ps.close();
            } catch (Throwable var13) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var8) {
                        var13.addSuppressed(var8);
                    }
                }

                throw var13;
            }

            con.close();
        } catch (Exception var14) {
            LOGGER.error("Couldn't load Olympiad leaders.", var14);
        }

        return names;
    }

    public int getNoblessePasses(Player player, boolean clear) {
        if (player != null && this._period == OlympiadState.VALIDATION && !this._noblesRank.isEmpty()) {
            int objId = player.getObjectId();
            if (!this._noblesRank.containsKey(objId)) {
                return 0;
            } else {
                StatSet noble = this._nobles.get(objId);
                if (noble != null && noble.getInteger("olympiad_points") != 0) {
                    int rank = this._noblesRank.get(objId);
                    int points = !player.isHero() && !HeroManager.getInstance().isInactiveHero(player.getObjectId()) ? 0 : Config.ALT_OLY_HERO_POINTS;
                    switch (rank) {
                        case 1:
                            points += Config.ALT_OLY_RANK1_POINTS;
                            break;
                        case 2:
                            points += Config.ALT_OLY_RANK2_POINTS;
                            break;
                        case 3:
                            points += Config.ALT_OLY_RANK3_POINTS;
                            break;
                        case 4:
                            points += Config.ALT_OLY_RANK4_POINTS;
                            break;
                        default:
                            points += Config.ALT_OLY_RANK5_POINTS;
                    }

                    if (clear) {
                        noble.set("olympiad_points", 0);
                    }

                    points *= Config.ALT_OLY_GP_PER_POINT;
                    return points;
                } else {
                    return 0;
                }
            }
        } else {
            return 0;
        }
    }

    public int getNoblePoints(int objId) {
        return this._nobles.containsKey(objId) ? this._nobles.get(objId).getInteger("olympiad_points") : 0;
    }

    public int getLastNobleOlympiadPoints(int objId) {
        int result = 0;

        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("SELECT olympiad_points FROM olympiad_nobles_eom WHERE char_id = ?");

                try {
                    ps.setInt(1, objId);
                    ResultSet rs = ps.executeQuery();

                    try {
                        if (rs.first()) {
                            result = rs.getInt(1);
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

                    rs.close();
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

                ps.close();
            } catch (Throwable var13) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var8) {
                        var13.addSuppressed(var8);
                    }
                }

                throw var13;
            }

            con.close();
        } catch (Exception var14) {
            LOGGER.error("Couldn't load last Olympiad points.", var14);
        }

        return result;
    }

    public int getCompetitionDone(int objId) {
        return this._nobles.containsKey(objId) ? this._nobles.get(objId).getInteger("competitions_done") : 0;
    }

    public int getCompetitionWon(int objId) {
        return this._nobles.containsKey(objId) ? this._nobles.get(objId).getInteger("competitions_won") : 0;
    }

    public int getCompetitionLost(int objId) {
        return this._nobles.containsKey(objId) ? this._nobles.get(objId).getInteger("competitions_lost") : 0;
    }

    protected void deleteNobles() {
        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("TRUNCATE olympiad_nobles");

                try {
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

                ps.close();
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

            con.close();
        } catch (Exception var9) {
            LOGGER.error("Couldn't delete Olympiad nobles.", var9);
        }

        this._nobles.clear();
    }

    protected void addNobleStats(int charId, StatSet data) {
        this._nobles.put(charId, data);
    }

    private static class SingletonHolder {
        protected static final Olympiad INSTANCE = new Olympiad();
    }

    private static final class OlympiadAnnouncer implements Runnable {
        private final OlympiadGameTask[] _tasks = OlympiadGameManager.getInstance().getOlympiadTasks();

        public OlympiadAnnouncer() {
        }

        public void run() {
            for (OlympiadGameTask task : this._tasks) {
                if (task.needAnnounce()) {
                    AbstractOlympiadGame game = task.getGame();
                    if (game != null) {
                        int stadium;
                        String announcement;
                        if (game.getType() == OlympiadType.NON_CLASSED) {
                            stadium = game.getStadiumId();
                            announcement = "Olympiad class-free individual match is going to begin in Arena " + (stadium + 1) + " in a moment.";
                        } else {
                            stadium = game.getStadiumId();
                            announcement = "Olympiad class individual match is going to begin in Arena " + (stadium + 1) + " in a moment.";
                        }

                        for (OlympiadManagerNpc manager : OlympiadManagerNpc.getInstances()) {
                            manager.broadcastPacket(new NpcSay(manager.getObjectId(), 1, manager.getNpcId(), announcement));
                        }
                    }
                }
            }

        }
    }

    protected class ValidationEndTask implements Runnable {
        public void run() {
            Olympiad.this._period = OlympiadState.COMPETITION;
            ++Olympiad.this._currentCycle;
            Olympiad.this.deleteNobles();
            Olympiad.this.setNewOlympiadEnd();
            Olympiad.this.init();
        }
    }

    protected class OlympiadEndTask implements Runnable {
        public void run() {
            World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.OLYMPIAD_PERIOD_S1_HAS_ENDED).addNumber(Olympiad.this._currentCycle));
            if (Olympiad.this._scheduledWeeklyTask != null) {
                Olympiad.this._scheduledWeeklyTask.cancel(true);
            }

            Olympiad.this.saveNobleData();
            Olympiad.this._period = OlympiadState.VALIDATION;
            Olympiad.this.sortHeroesToBe();
            HeroManager.getInstance().resetData();
            HeroManager.getInstance().computeNewHeroes(Olympiad.this._heroesToBe);
            Olympiad.this.saveOlympiadStatus();
            Olympiad.this.updateMonthlyData();
            Calendar validationEnd = Calendar.getInstance();
            Olympiad.this._validationEnd = validationEnd.getTimeInMillis() + Config.ALT_OLY_VPERIOD;
            Olympiad.this.loadNoblesRank();
            Olympiad.this._scheduledValdationTask = ThreadPool.schedule(Olympiad.this.new ValidationEndTask(), Olympiad.this.getMillisToValidationEnd());
        }
    }
}