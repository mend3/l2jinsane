package net.sf.l2j.gameserver.data.manager;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.model.actor.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RaidPointManager {
    private static final CLogger LOGGER = new CLogger(RaidPointManager.class.getName());

    private static final String LOAD_DATA = "SELECT * FROM character_raid_points";

    private static final String INSERT_DATA = "REPLACE INTO character_raid_points (char_id,boss_id,points) VALUES (?,?,?)";

    private static final String DELETE_DATA = "TRUNCATE TABLE character_raid_points";

    private final Map<Integer, Map<Integer, Integer>> _entries = new ConcurrentHashMap<>();

    public static RaidPointManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void load() {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM character_raid_points");
                try {
                    ResultSet rs = ps.executeQuery();
                    try {
                        while (rs.next()) {
                            int objectId = rs.getInt("char_id");
                            int bossId = rs.getInt("boss_id");
                            int points = rs.getInt("points");
                            Map<Integer, Integer> playerData = this._entries.get(objectId);
                            if (playerData == null)
                                playerData = new HashMap<>();
                            playerData.put(bossId, points);
                            this._entries.put(objectId, playerData);
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
            LOGGER.error("Couldn't load RaidPoints entries.", e);
        }
        LOGGER.info("Loaded {} RaidPoints entries.", this._entries.size());
    }

    public final Map<Integer, Integer> getList(Player player) {
        return this._entries.get(player.getObjectId());
    }

    public final void addPoints(Player player, int bossId, int points) {
        int objectId = player.getObjectId();
        Map<Integer, Integer> playerData = this._entries.computeIfAbsent(objectId, k -> new HashMap<>());
        playerData.merge(bossId, points, Integer::sum);
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("REPLACE INTO character_raid_points (char_id,boss_id,points) VALUES (?,?,?)");
                try {
                    ps.setInt(1, objectId);
                    ps.setInt(2, bossId);
                    ps.setInt(3, points);
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
            LOGGER.error("Couldn't update RaidPoints entries.", e);
        }
    }

    public final int getPointsByOwnerId(int objectId) {
        Map<Integer, Integer> playerData = this._entries.get(objectId);
        if (playerData == null || playerData.isEmpty())
            return 0;
        return playerData.values().stream().mapToInt(Number::intValue).sum();
    }

    public final void cleanUp() {
        this._entries.clear();
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("TRUNCATE TABLE character_raid_points");
                try {
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
            LOGGER.error("Couldn't delete RaidPoints entries.", e);
        }
    }

    public final int calculateRanking(int objectId) {
        Map<Integer, Integer> playersData = new HashMap<>();
        for (Iterator<Integer> iterator = this._entries.keySet().iterator(); iterator.hasNext(); ) {
            int ownerId = iterator.next();
            int points = getPointsByOwnerId(ownerId);
            if (points > 0)
                playersData.put(ownerId, points);
        }
        AtomicInteger counter = new AtomicInteger(1);
        Map<Integer, Integer> rankMap = new LinkedHashMap<>();
        playersData.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).forEachOrdered(e -> rankMap.put(e.getKey(), counter.getAndIncrement()));
        Integer rank = rankMap.get(objectId);
        return (rank == null) ? 0 : rank;
    }

    public Map<Integer, Integer> getWinners() {
        Map<Integer, Integer> playersData = new HashMap<>();
        for (Iterator<Integer> iterator = this._entries.keySet().iterator(); iterator.hasNext(); ) {
            int objectId = iterator.next();
            int points = getPointsByOwnerId(objectId);
            if (points > 0)
                playersData.put(objectId, points);
        }
        AtomicInteger counter = new AtomicInteger(1);
        Map<Integer, Integer> rankMap = new LinkedHashMap<>();
        playersData.entrySet().stream().sorted(Collections.reverseOrder(Map.Entry.comparingByValue())).limit(100L).forEachOrdered(e -> rankMap.put(e.getKey(), counter.getAndIncrement()));
        return rankMap;
    }

    private static class SingletonHolder {
        protected static final RaidPointManager INSTANCE = new RaidPointManager();
    }
}
