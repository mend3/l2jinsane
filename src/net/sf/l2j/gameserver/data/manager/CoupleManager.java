package net.sf.l2j.gameserver.data.manager;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CoupleManager {
    private static final CLogger LOGGER = new CLogger(CoupleManager.class.getName());

    private static final String LOAD_COUPLES = "SELECT * FROM mods_wedding";

    private static final String DELETE_COUPLES = "DELETE FROM mods_wedding";

    private static final String ADD_COUPLE = "INSERT INTO mods_wedding (id, requesterId, partnerId) VALUES (?,?,?)";

    private final Map<Integer, IntIntHolder> _couples = new ConcurrentHashMap<>();

    public static CoupleManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void load() {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("SELECT * FROM mods_wedding");
                try {
                    ResultSet rs = ps.executeQuery();
                    try {
                        while (rs.next())
                            this._couples.put(rs.getInt("id"), new IntIntHolder(rs.getInt("requesterId"), rs.getInt("partnerId")));
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
            LOGGER.error("Couldn't load couples.", e);
        }
        LOGGER.info("Loaded {} couples.", this._couples.size());
    }

    public final Map<Integer, IntIntHolder> getCouples() {
        return this._couples;
    }

    public final IntIntHolder getCouple(int coupleId) {
        return this._couples.get(coupleId);
    }

    public void addCouple(Player requester, Player partner) {
        if (requester == null || partner == null)
            return;
        int coupleId = IdFactory.getInstance().getNextId();
        this._couples.put(coupleId, new IntIntHolder(requester.getObjectId(), partner.getObjectId()));
        requester.setCoupleId(coupleId);
        partner.setCoupleId(coupleId);
    }

    public void deleteCouple(int coupleId) {
        IntIntHolder couple = this._couples.remove(coupleId);
        if (couple == null)
            return;
        Player requester = World.getInstance().getPlayer(couple.getId());
        if (requester != null) {
            requester.setCoupleId(0);
            requester.sendMessage("You are now divorced.");
        }
        Player partner = World.getInstance().getPlayer(couple.getValue());
        if (partner != null) {
            partner.setCoupleId(0);
            partner.sendMessage("You are now divorced.");
        }
        IdFactory.getInstance().releaseId(coupleId);
    }

    public void save() {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("DELETE FROM mods_wedding");
                try {
                    ps.execute();
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
                ps = con.prepareStatement("INSERT INTO mods_wedding (id, requesterId, partnerId) VALUES (?,?,?)");
                try {
                    for (Map.Entry<Integer, IntIntHolder> coupleEntry : this._couples.entrySet()) {
                        IntIntHolder couple = coupleEntry.getValue();
                        ps.setInt(1, (Integer) coupleEntry.getKey());
                        ps.setInt(2, couple.getId());
                        ps.setInt(3, couple.getValue());
                        ps.addBatch();
                    }
                    ps.executeBatch();
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
            LOGGER.error("Couldn't add a couple.", e);
        }
    }

    public final int getPartnerId(int coupleId, int objectId) {
        IntIntHolder couple = this._couples.get(coupleId);
        if (couple == null)
            return 0;
        return (couple.getId() == objectId) ? couple.getValue() : couple.getId();
    }

    private static class SingletonHolder {
        protected static final CoupleManager INSTANCE = new CoupleManager();
    }
}
