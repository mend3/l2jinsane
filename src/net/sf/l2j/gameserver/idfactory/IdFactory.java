package net.sf.l2j.gameserver.idfactory;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.math.PrimeFinder;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.pool.ThreadPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class IdFactory {
    public static final int FIRST_OID = 268435456;
    public static final int LAST_OID = 2147483647;
    public static final int FREE_OBJECT_ID_SIZE = 1879048191;
    private static final CLogger LOGGER = new CLogger(IdFactory.class.getName());
    private BitSet _freeIds;
    protected static final IdFactory INSTANCE = new IdFactory();

    private AtomicInteger _freeIdCount;

    private AtomicInteger _nextFreeId;

    protected IdFactory() {
        setAllCharacterOffline();
        cleanUpDB();
        cleanUpTimeStamps();
        initialize();
        ThreadPool.scheduleAtFixedRate(() -> {
            if (reachingBitSetCapacity())
                increaseBitSetCapacity();
        }, 30000L, 30000L);
    }

    private static void setAllCharacterOffline() {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("UPDATE characters SET online = 0");
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
            LOGGER.warn("Couldn't set characters offline.", e);
        }
        LOGGER.info("Updated characters online status.");
    }

    private static void cleanUpDB() {
        int cleanCount = 0;
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                Statement stmt = con.createStatement();
                try {
                    cleanCount += stmt.executeUpdate("DELETE FROM augmentations WHERE augmentations.item_id NOT IN (SELECT object_id FROM items);");
                    cleanCount += stmt.executeUpdate("DELETE FROM character_friends WHERE character_friends.char_id NOT IN (SELECT obj_Id FROM characters);");
                    cleanCount += stmt.executeUpdate("DELETE FROM character_friends WHERE character_friends.friend_id NOT IN (SELECT obj_Id FROM characters);");
                    cleanCount += stmt.executeUpdate("DELETE FROM character_hennas WHERE character_hennas.char_obj_id NOT IN (SELECT obj_Id FROM characters);");
                    cleanCount += stmt.executeUpdate("DELETE FROM character_macroses WHERE character_macroses.char_obj_id NOT IN (SELECT obj_Id FROM characters);");
                    cleanCount += stmt.executeUpdate("DELETE FROM character_memo WHERE character_memo.charId NOT IN (SELECT obj_Id FROM characters);");
                    cleanCount += stmt.executeUpdate("DELETE FROM character_quests WHERE character_quests.charId NOT IN (SELECT obj_Id FROM characters);");
                    cleanCount += stmt.executeUpdate("DELETE FROM character_raid_points WHERE character_raid_points.char_id NOT IN (SELECT obj_Id FROM characters);");
                    cleanCount += stmt.executeUpdate("DELETE FROM character_recipebook WHERE character_recipebook.charId NOT IN (SELECT obj_Id FROM characters);");
                    cleanCount += stmt.executeUpdate("DELETE FROM character_shortcuts WHERE character_shortcuts.char_obj_id NOT IN (SELECT obj_Id FROM characters);");
                    cleanCount += stmt.executeUpdate("DELETE FROM character_skills WHERE character_skills.char_obj_id NOT IN (SELECT obj_Id FROM characters);");
                    cleanCount += stmt.executeUpdate("DELETE FROM character_skills_save WHERE character_skills_save.char_obj_id NOT IN (SELECT obj_Id FROM characters);");
                    cleanCount += stmt.executeUpdate("DELETE FROM character_subclasses WHERE character_subclasses.char_obj_id NOT IN (SELECT obj_Id FROM characters);");
                    cleanCount += stmt.executeUpdate("DELETE FROM cursed_weapons WHERE cursed_weapons.playerId NOT IN (SELECT obj_Id FROM characters);");
                    cleanCount += stmt.executeUpdate("DELETE FROM pets WHERE pets.item_obj_id NOT IN (SELECT object_id FROM items);");
                    cleanCount += stmt.executeUpdate("DELETE FROM seven_signs WHERE seven_signs.char_obj_id NOT IN (SELECT obj_Id FROM characters);");
                    cleanCount += stmt.executeUpdate("DELETE FROM heroes WHERE heroes.char_id NOT IN (SELECT obj_Id FROM characters);");
                    cleanCount += stmt.executeUpdate("DELETE FROM olympiad_nobles WHERE olympiad_nobles.char_id NOT IN (SELECT obj_Id FROM characters);");
                    cleanCount += stmt.executeUpdate("DELETE FROM olympiad_nobles_eom WHERE olympiad_nobles_eom.char_id NOT IN (SELECT obj_Id FROM characters);");
                    cleanCount += stmt.executeUpdate("DELETE FROM olympiad_fights WHERE olympiad_fights.charOneId NOT IN (SELECT obj_Id FROM characters);");
                    cleanCount += stmt.executeUpdate("DELETE FROM olympiad_fights WHERE olympiad_fights.charTwoId NOT IN (SELECT obj_Id FROM characters);");
                    cleanCount += stmt.executeUpdate("DELETE FROM heroes_diary WHERE heroes_diary.char_id NOT IN (SELECT obj_Id FROM characters);");
                    cleanCount += stmt.executeUpdate("DELETE FROM auction_bid WHERE auctionId IN (SELECT id FROM clanhall WHERE ownerId <> 0 AND sellerClanName='');");
                    cleanCount += stmt.executeUpdate("DELETE FROM clan_data WHERE clan_data.leader_id NOT IN (SELECT obj_Id FROM characters);");
                    cleanCount += stmt.executeUpdate("DELETE FROM auction_bid WHERE auction_bid.bidderId NOT IN (SELECT clan_id FROM clan_data);");
                    cleanCount += stmt.executeUpdate("DELETE FROM clanhall_functions WHERE clanhall_functions.hall_id NOT IN (SELECT id FROM clanhall WHERE ownerId <> 0);");
                    cleanCount += stmt.executeUpdate("DELETE FROM clan_privs WHERE clan_privs.clan_id NOT IN (SELECT clan_id FROM clan_data);");
                    cleanCount += stmt.executeUpdate("DELETE FROM clan_skills WHERE clan_skills.clan_id NOT IN (SELECT clan_id FROM clan_data);");
                    cleanCount += stmt.executeUpdate("DELETE FROM clan_subpledges WHERE clan_subpledges.clan_id NOT IN (SELECT clan_id FROM clan_data);");
                    cleanCount += stmt.executeUpdate("DELETE FROM clan_wars WHERE clan_wars.clan1 NOT IN (SELECT clan_id FROM clan_data);");
                    cleanCount += stmt.executeUpdate("DELETE FROM clan_wars WHERE clan_wars.clan2 NOT IN (SELECT clan_id FROM clan_data);");
                    cleanCount += stmt.executeUpdate("DELETE FROM siege_clans WHERE siege_clans.clan_id NOT IN (SELECT clan_id FROM clan_data);");
                    cleanCount += stmt.executeUpdate("DELETE FROM items WHERE items.owner_id NOT IN (SELECT obj_Id FROM characters) AND items.owner_id NOT IN (SELECT clan_id FROM clan_data);");
                    cleanCount += stmt.executeUpdate("DELETE FROM forums WHERE forums.forum_owner_id NOT IN (SELECT clan_id FROM clan_data) AND forums.forum_parent=2;");
                    cleanCount += stmt.executeUpdate("DELETE FROM topic WHERE topic.topic_forum_id NOT IN (SELECT forum_id FROM forums);");
                    cleanCount += stmt.executeUpdate("DELETE FROM posts WHERE posts.post_forum_id NOT IN (SELECT forum_id FROM forums);");
                    stmt.executeUpdate("UPDATE clan_data SET auction_bid_at = 0 WHERE auction_bid_at NOT IN (SELECT auctionId FROM auction_bid);");
                    stmt.executeUpdate("UPDATE clan_data SET new_leader_id = 0 WHERE new_leader_id NOT IN (SELECT obj_Id FROM characters);");
                    stmt.executeUpdate("UPDATE clan_subpledges SET leader_id=0 WHERE clan_subpledges.leader_id NOT IN (SELECT obj_Id FROM characters) AND leader_id > 0;");
                    stmt.executeUpdate("UPDATE castle SET taxpercent=0 WHERE castle.id NOT IN (SELECT hasCastle FROM clan_data);");
                    stmt.executeUpdate("UPDATE characters SET clanid=0 WHERE characters.clanid NOT IN (SELECT clan_id FROM clan_data);");
                    stmt.executeUpdate("UPDATE clanhall SET ownerId=0, paidUntil=0, paid=0 WHERE clanhall.ownerId NOT IN (SELECT clan_id FROM clan_data);");
                    if (stmt != null)
                        stmt.close();
                } catch (Throwable throwable) {
                    if (stmt != null)
                        try {
                            stmt.close();
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
            LOGGER.warn("Couldn't cleanup database.", e);
        }
        LOGGER.info("Cleaned {} elements from database.", Integer.valueOf(cleanCount));
    }

    private static void cleanUpTimeStamps() {
        int cleanCount = 0;
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("DELETE FROM character_skills_save WHERE restore_type = 1 AND systime <= ?");
                try {
                    ps.setLong(1, System.currentTimeMillis());
                    cleanCount += ps.executeUpdate();
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
            LOGGER.warn("Couldn't cleanup timestamps.", e);
        }
        LOGGER.info("Cleaned {} expired timestamps from database.", Integer.valueOf(cleanCount));
    }

    public static IdFactory getInstance() {
        return INSTANCE;
    }

    private void initialize() {
        this._freeIds = new BitSet(PrimeFinder.nextPrime(100000));
        this._freeIdCount = new AtomicInteger(1879048191);
        List<Integer> usedObjectIds = new ArrayList<>();
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                Statement st = con.createStatement();
                try {
                    ResultSet rs = st.executeQuery("SELECT obj_Id FROM characters");
                    try {
                        while (rs.next())
                            usedObjectIds.add(Integer.valueOf(rs.getInt(1)));
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
                    rs = st.executeQuery("SELECT object_id FROM items");
                    try {
                        while (rs.next())
                            usedObjectIds.add(Integer.valueOf(rs.getInt(1)));
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
                    rs = st.executeQuery("SELECT clan_id FROM clan_data");
                    try {
                        while (rs.next())
                            usedObjectIds.add(Integer.valueOf(rs.getInt(1)));
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
                    rs = st.executeQuery("SELECT object_id FROM items_on_ground");
                    try {
                        while (rs.next())
                            usedObjectIds.add(Integer.valueOf(rs.getInt(1)));
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
                    rs = st.executeQuery("SELECT id FROM mods_wedding");
                    try {
                        while (rs.next())
                            usedObjectIds.add(Integer.valueOf(rs.getInt(1)));
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
                    if (st != null)
                        st.close();
                } catch (Throwable throwable) {
                    if (st != null)
                        try {
                            st.close();
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
            LOGGER.error("Couldn't properly initialize objectIds.", e);
        }
        for (Iterator<Integer> iterator = usedObjectIds.iterator(); iterator.hasNext(); ) {
            int usedObjectId = iterator.next();
            int objectId = usedObjectId - 268435456;
            if (objectId < 0) {
                LOGGER.warn("Found invalid objectId {}. It is less than minimum of {}.", Integer.valueOf(usedObjectId), Integer.valueOf(268435456));
                continue;
            }
            this._freeIds.set(objectId);
            this._freeIdCount.decrementAndGet();
        }
        this._nextFreeId = new AtomicInteger(this._freeIds.nextClearBit(0));
        LOGGER.info("Initializing {} objectIds pool, with {} used ids.", Integer.valueOf(this._freeIds.size()), Integer.valueOf(usedObjectIds.size()));
    }

    public synchronized void releaseId(int objectID) {
        if (objectID - 268435456 > -1) {
            this._freeIds.clear(objectID - 268435456);
            this._freeIdCount.incrementAndGet();
        } else {
            LOGGER.warn("Release objectId {} failed (< {}).", Integer.valueOf(objectID), Integer.valueOf(268435456));
        }
    }

    public synchronized int getNextId() {
        int newId = this._nextFreeId.get();
        this._freeIds.set(newId);
        this._freeIdCount.decrementAndGet();
        int nextFree = this._freeIds.nextClearBit(newId);
        if (nextFree < 0)
            nextFree = this._freeIds.nextClearBit(0);
        if (nextFree < 0)
            if (this._freeIds.size() < 1879048191) {
                increaseBitSetCapacity();
            } else {
                throw new NullPointerException("Ran out of valid Id's.");
            }
        this._nextFreeId.set(nextFree);
        return newId + 268435456;
    }

    protected int usedIdCount() {
        return this._freeIdCount.get() - 268435456;
    }

    protected synchronized boolean reachingBitSetCapacity() {
        return (PrimeFinder.nextPrime(usedIdCount() * 11 / 10) > this._freeIds.size());
    }

    protected synchronized void increaseBitSetCapacity() {
        BitSet newBitSet = new BitSet(PrimeFinder.nextPrime(usedIdCount() * 11 / 10));
        newBitSet.or(this._freeIds);
        this._freeIds = newBitSet;
    }

}
