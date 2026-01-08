/**/
package net.sf.l2j.gameserver.data.manager;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.HistoryInfo;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.zone.type.DerbyTrackZone;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.DeleteObject;
import net.sf.l2j.gameserver.network.serverpackets.MonRaceInfo;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class DerbyTrackManager {
    protected static final CLogger LOGGER = new CLogger(DerbyTrackManager.class.getName());
    protected static final PlaySound SOUND_1 = new PlaySound(1, "S_Race");
    protected static final PlaySound SOUND_2 = new PlaySound("ItemSound2.race_start");
    protected static final int[][] CODES = new int[][]{{-1, 0}, {0, 15322}, {13765, -1}};
    private static final String SAVE_HISTORY = "INSERT INTO mdt_history (race_id, first, second, odd_rate) VALUES (?,?,?,?)";
    private static final String LOAD_HISTORY = "SELECT * FROM mdt_history";
    private static final String LOAD_BETS = "SELECT * FROM mdt_bets";
    private static final String SAVE_BETS = "REPLACE INTO mdt_bets (lane_id, bet) VALUES (?,?)";
    private static final String CLEAR_BETS = "UPDATE mdt_bets SET bet = 0";
    protected final List<Npc> _runners = new ArrayList<>();
    protected final TreeMap<Integer, HistoryInfo> _history = new TreeMap<>();
    protected final Map<Integer, Long> _betsPerLane = new ConcurrentHashMap<>();
    protected final List<Double> _odds = new ArrayList<>();
    protected int _raceNumber = 1;
    protected int _finalCountdown = 0;
    protected DerbyTrackManager.RaceState _state;
    protected MonRaceInfo _packet;
    private List<Npc> _chosenRunners;
    private int[][] _speeds;
    private int _firstIndex;
    private int _secondIndex;

    public static DerbyTrackManager getInstance() {
        return DerbyTrackManager.SingletonHolder.INSTANCE;
    }

    public void load() {
        this._state = DerbyTrackManager.RaceState.RACE_END;
        this.loadHistory();
        this.loadBets();

        try {
            for (int i = 31003; i < 31027; ++i) {
                NpcTemplate template = NpcData.getInstance().getTemplate(i);
                if (template != null) {
                    Constructor<?> _constructor = Class.forName("net.sf.l2j.gameserver.model.actor.instance." + template.getType()).getConstructors()[0];
                    this._runners.add((Npc) _constructor.newInstance(IdFactory.getInstance().getNextId(), template));
                }
            }
        } catch (Exception var4) {
            LOGGER.error("Couldn't initialize runners.", var4);
        }

        this._speeds = new int[8][20];
//        ThreadPool.scheduleAtFixedRate(new DerbyTrackManager.Announcement(), 0L, 1000L);
    }

    public List<Npc> getRunners() {
        return this._chosenRunners;
    }

    public String getRunnerName(int index) {
        Npc npc = this._chosenRunners.get(index);
        return npc == null ? "" : npc.getName();
    }

    public int[][] getSpeeds() {
        return this._speeds;
    }

    public int getFirst() {
        return this._firstIndex;
    }

    public int getSecond() {
        return this._secondIndex;
    }

    public MonRaceInfo getRacePacket() {
        return this._packet;
    }

    public DerbyTrackManager.RaceState getCurrentRaceState() {
        return this._state;
    }

    public int getRaceNumber() {
        return this._raceNumber;
    }

    public List<HistoryInfo> getLastHistoryEntries() {
        return this._history.descendingMap().values().stream().limit(8L).collect(Collectors.toList());
    }

    public HistoryInfo getHistoryInfo(int raceNumber) {
        return this._history.get(raceNumber);
    }

    public List<Double> getOdds() {
        return this._odds;
    }

    public void newRace() {
        this._history.put(this._raceNumber, new HistoryInfo(this._raceNumber, 0, 0, 0.0D));
        Collections.shuffle(this._runners);
        this._chosenRunners = this._runners.subList(0, 8);
    }

    public void newSpeeds() {
        this._speeds = new int[8][20];
        int winnerDistance = 0;
        int secondDistance = 0;

        for (int i = 0; i < 8; ++i) {
            int total = 0;

            for (int j = 0; j < 20; ++j) {
                if (j == 19) {
                    this._speeds[i][j] = 100;
                } else {
                    this._speeds[i][j] = Rnd.get(60) + 65;
                }

                total += this._speeds[i][j];
            }

            if (total >= winnerDistance) {
                this._secondIndex = this._firstIndex;
                secondDistance = winnerDistance;
                this._firstIndex = i;
                winnerDistance = total;
            } else if (total >= secondDistance) {
                this._secondIndex = i;
                secondDistance = total;
            }
        }

    }

    protected void loadHistory() {
        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM mdt_history");

                try {
                    ResultSet rs = ps.executeQuery();

                    try {
                        while (rs.next()) {
                            int savedRaceNumber = rs.getInt("race_id");
                            this._history.put(savedRaceNumber, new HistoryInfo(savedRaceNumber, rs.getInt("first"), rs.getInt("second"), rs.getDouble("odd_rate")));
                            if (this._raceNumber <= savedRaceNumber) {
                                this._raceNumber = savedRaceNumber + 1;
                            }
                        }
                    } catch (Throwable var9) {
                        if (rs != null) {
                            try {
                                rs.close();
                            } catch (Throwable var8) {
                                var9.addSuppressed(var8);
                            }
                        }

                        throw var9;
                    }

                    if (rs != null) {
                        rs.close();
                    }
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

                if (ps != null) {
                    ps.close();
                }
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

            if (con != null) {
                con.close();
            }
        } catch (Exception var12) {
            LOGGER.error("Can't load Derby Track history.", var12);
        }

        LOGGER.info("Loaded {} Derby Track records, currently on race #{}.", this._history.size(), this._raceNumber);
    }

    protected void saveHistory(HistoryInfo history) {
        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("INSERT INTO mdt_history (race_id, first, second, odd_rate) VALUES (?,?,?,?)");

                try {
                    ps.setInt(1, history.getRaceId());
                    ps.setInt(2, history.getFirst());
                    ps.setInt(3, history.getSecond());
                    ps.setDouble(4, history.getOddRate());
                    ps.execute();
                } catch (Throwable var8) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var7) {
                            var8.addSuppressed(var7);
                        }
                    }

                    throw var8;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (Throwable var9) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var6) {
                        var9.addSuppressed(var6);
                    }
                }

                throw var9;
            }

            if (con != null) {
                con.close();
            }
        } catch (Exception var10) {
            LOGGER.error("Can't save Derby Track history.", var10);
        }

    }

    protected void loadBets() {
        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM mdt_bets");

                try {
                    ResultSet rs = ps.executeQuery();

                    try {
                        while (rs.next()) {
                            this.setBetOnLane(rs.getInt("lane_id"), rs.getLong("bet"), false);
                        }
                    } catch (Throwable var9) {
                        if (rs != null) {
                            try {
                                rs.close();
                            } catch (Throwable var8) {
                                var9.addSuppressed(var8);
                            }
                        }

                        throw var9;
                    }

                    if (rs != null) {
                        rs.close();
                    }
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

                if (ps != null) {
                    ps.close();
                }
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

            if (con != null) {
                con.close();
            }
        } catch (Exception var12) {
            LOGGER.error("Can't load Derby Track bets.", var12);
        }

    }

    protected void saveBet(int lane, long sum) {
        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("REPLACE INTO mdt_bets (lane_id, bet) VALUES (?,?)");

                try {
                    ps.setInt(1, lane);
                    ps.setLong(2, sum);
                    ps.execute();
                } catch (Throwable var10) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var9) {
                            var10.addSuppressed(var9);
                        }
                    }

                    throw var10;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (Throwable var11) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var8) {
                        var11.addSuppressed(var8);
                    }
                }

                throw var11;
            }

            if (con != null) {
                con.close();
            }
        } catch (Exception var12) {
            LOGGER.error("Can't save Derby Track bet.", var12);
        }

    }

    protected void clearBets() {

        this._betsPerLane.replaceAll((k, v) -> 0L);

        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("UPDATE mdt_bets SET bet = 0");

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
            LOGGER.error("Can't clear Derby Track bets.", var9);
        }

    }

    public void setBetOnLane(int lane, long amount, boolean saveOnDb) {
        long sum = this._betsPerLane.getOrDefault(lane, 0L) + amount;
        this._betsPerLane.put(lane, sum);
        if (saveOnDb) {
            this.saveBet(lane, sum);
        }

    }

    protected void calculateOdds() {
        this._odds.clear();
        Map<Integer, Long> sortedLanes = new TreeMap<>(this._betsPerLane);
        long sumOfAllLanes = 0L;

        Iterator var4;
        long amount;
        for (var4 = sortedLanes.values().iterator(); var4.hasNext(); sumOfAllLanes += amount) {
            amount = (Long) var4.next();
        }

        var4 = sortedLanes.values().iterator();

        while (var4.hasNext()) {
            amount = (Long) var4.next();
            this._odds.add(amount == 0L ? 0.0D : Math.max(1.25D, (double) sumOfAllLanes * 0.7D / (double) amount));
        }

    }

    public enum RaceState {
        ACCEPTING_BETS,
        WAITING,
        STARTING_RACE,
        RACE_END;
    }

    private static class SingletonHolder {
        protected static final DerbyTrackManager INSTANCE = new DerbyTrackManager();
    }

    private class Announcement implements Runnable {
        public Announcement() {
        }

        public void run() {
            if (DerbyTrackManager.this._finalCountdown > 1200) {
                DerbyTrackManager.this._finalCountdown = 0;
            }

            switch (DerbyTrackManager.this._finalCountdown) {
                case 0:
                    DerbyTrackManager.this.newRace();
                    DerbyTrackManager.this.newSpeeds();
                    DerbyTrackManager.this._state = DerbyTrackManager.RaceState.ACCEPTING_BETS;
                    DerbyTrackManager.this._packet = new MonRaceInfo(DerbyTrackManager.CODES[0][0], DerbyTrackManager.CODES[0][1], DerbyTrackManager.this.getRunners(), DerbyTrackManager.this.getSpeeds());
                    ZoneManager.toAllPlayersInZoneType(DerbyTrackZone.class, DerbyTrackManager.this._packet, SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_AVAILABLE_FOR_S1_RACE).addNumber(DerbyTrackManager.this._raceNumber));
                    break;
                case 30:
                case 60:
                case 90:
                case 120:
                case 150:
                case 180:
                case 210:
                case 240:
                case 270:
                case 330:
                case 360:
                case 390:
                case 420:
                case 450:
                case 480:
                case 510:
                case 540:
                case 570:
                case 630:
                case 660:
                case 690:
                case 720:
                case 750:
                case 780:
                case 810:
                case 870:
                    ZoneManager.toAllPlayersInZoneType(DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_NOW_AVAILABLE_FOR_S1_RACE).addNumber(DerbyTrackManager.this._raceNumber));
                    break;
                case 300:
                    ZoneManager.toAllPlayersInZoneType(DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_NOW_AVAILABLE_FOR_S1_RACE).addNumber(DerbyTrackManager.this._raceNumber), SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_STOP_IN_S1_MINUTES).addNumber(10));
                    break;
                case 600:
                    ZoneManager.toAllPlayersInZoneType(DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_NOW_AVAILABLE_FOR_S1_RACE).addNumber(DerbyTrackManager.this._raceNumber), SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_STOP_IN_S1_MINUTES).addNumber(5));
                    break;
                case 840:
                    ZoneManager.toAllPlayersInZoneType(DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_NOW_AVAILABLE_FOR_S1_RACE).addNumber(DerbyTrackManager.this._raceNumber), SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_STOP_IN_S1_MINUTES).addNumber(1));
                    break;
                case 900:
                    DerbyTrackManager.this._state = DerbyTrackManager.RaceState.WAITING;
                    DerbyTrackManager.this.calculateOdds();
                    ZoneManager.toAllPlayersInZoneType(DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_TICKETS_NOW_AVAILABLE_FOR_S1_RACE).addNumber(DerbyTrackManager.this._raceNumber), SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_S1_TICKET_SALES_CLOSED));
                    break;
                case 960:
                case 1020:
                    int minutes = DerbyTrackManager.this._finalCountdown == 960 ? 2 : 1;
                    ZoneManager.toAllPlayersInZoneType(DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_S2_BEGINS_IN_S1_MINUTES).addNumber(minutes));
                    break;
                case 1050:
                    ZoneManager.toAllPlayersInZoneType(DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_S1_BEGINS_IN_30_SECONDS));
                    break;
                case 1070:
                    ZoneManager.toAllPlayersInZoneType(DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_S1_COUNTDOWN_IN_FIVE_SECONDS));
                    break;
                case 1075:
                case 1076:
                case 1077:
                case 1078:
                case 1079:
                    int seconds = 1080 - DerbyTrackManager.this._finalCountdown;
                    ZoneManager.toAllPlayersInZoneType(DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_BEGINS_IN_S1_SECONDS).addNumber(seconds));
                    break;
                case 1080:
                    DerbyTrackManager.this._state = DerbyTrackManager.RaceState.STARTING_RACE;
                    DerbyTrackManager.this._packet = new MonRaceInfo(DerbyTrackManager.CODES[1][0], DerbyTrackManager.CODES[1][1], DerbyTrackManager.this.getRunners(), DerbyTrackManager.this.getSpeeds());
                    ZoneManager.toAllPlayersInZoneType(DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_RACE_START), DerbyTrackManager.SOUND_1, DerbyTrackManager.SOUND_2, DerbyTrackManager.this._packet);
                    break;
                case 1085:
                    DerbyTrackManager.this._packet = new MonRaceInfo(DerbyTrackManager.CODES[2][0], DerbyTrackManager.CODES[2][1], DerbyTrackManager.this.getRunners(), DerbyTrackManager.this.getSpeeds());
                    ZoneManager.toAllPlayersInZoneType(DerbyTrackZone.class, DerbyTrackManager.this._packet);
                    break;
                case 1115:
                    DerbyTrackManager.this._state = DerbyTrackManager.RaceState.RACE_END;
                    HistoryInfo info = DerbyTrackManager.this.getHistoryInfo(DerbyTrackManager.this._raceNumber);
                    if (info != null) {
                        info.setFirst(DerbyTrackManager.this.getFirst());
                        info.setSecond(DerbyTrackManager.this.getSecond());
                        info.setOddRate(DerbyTrackManager.this._odds.get(DerbyTrackManager.this.getFirst()));
                        DerbyTrackManager.this.saveHistory(info);
                    }

                    DerbyTrackManager.this.clearBets();
                    ZoneManager.toAllPlayersInZoneType(DerbyTrackZone.class, SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_FIRST_PLACE_S1_SECOND_S2).addNumber(DerbyTrackManager.this.getFirst() + 1).addNumber(DerbyTrackManager.this.getSecond() + 1), SystemMessage.getSystemMessage(SystemMessageId.MONSRACE_S1_RACE_END).addNumber(DerbyTrackManager.this._raceNumber));
                    ++DerbyTrackManager.this._raceNumber;
                    break;
                case 1140:
                    ZoneManager.toAllPlayersInZoneType(DerbyTrackZone.class, new DeleteObject(DerbyTrackManager.this.getRunners().get(0)), new DeleteObject(DerbyTrackManager.this.getRunners().get(1)), new DeleteObject(DerbyTrackManager.this.getRunners().get(2)), new DeleteObject(DerbyTrackManager.this.getRunners().get(3)), new DeleteObject(DerbyTrackManager.this.getRunners().get(4)), new DeleteObject(DerbyTrackManager.this.getRunners().get(5)), new DeleteObject(DerbyTrackManager.this.getRunners().get(6)), new DeleteObject(DerbyTrackManager.this.getRunners().get(7)));
            }

            ++DerbyTrackManager.this._finalCountdown;
        }
    }
}