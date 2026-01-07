/**/
package net.sf.l2j.gameserver.model.clanhall;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.enums.SpawnType;
import net.sf.l2j.gameserver.model.Residence;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Door;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.zone.type.ClanHallZone;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

public class ClanHall extends Residence {
    public static final int FUNC_TELEPORT = 1;
    public static final int FUNC_ITEM_CREATE = 2;
    public static final int FUNC_RESTORE_HP = 3;
    public static final int FUNC_RESTORE_MP = 4;
    public static final int FUNC_RESTORE_EXP = 5;
    public static final int FUNC_SUPPORT = 6;
    public static final int FUNC_DECO_FRONTPLATEFORM = 7;
    public static final int FUNC_DECO_CURTAINS = 8;
    public static final int FUNC_SUPPORT_MAGIC = 9;
    public static final int FUNC_DECO_FIXTURES = 11;
    public static final int FUNC_CREATE_ITEM = 12;
    private static final CLogger LOGGER = new CLogger(ClanHall.class.getName());
    private static final String DELETE_FUNCTIONS = "DELETE FROM clanhall_functions WHERE hall_id=?";
    private static final String UPDATE_CH = "UPDATE clanhall SET ownerId=?, paidUntil=?, paid=?, sellerBid=?, sellerName=?, sellerClanName=?, endDate=? WHERE id=?";
    private static final int ONE_DAY = 86400000; // One day
    private static final int ONE_WEEK = 604800000; // One week
    protected final List<Integer> _npcs = new ArrayList<>();
    protected final Map<SpawnType, List<Location>> _spawns = new EnumMap<>(SpawnType.class);
    private final Map<Integer, ClanHallFunction> _functions = new ConcurrentHashMap();
    private final List<Door> _doors = new ArrayList();

    private final String _desc;

    private final int _auctionMin;
    private final int _deposit;
    private final int _lease;
    private final int _size;
    private final int _grade;

    private ScheduledFuture<?> _feeTask;
    private Auction _auction;
    private ClanHallZone _zone;
    private long _paidUntil;
    private boolean _isPaid;

    public ClanHall(StatSet set) {
        super(set);
        _desc = set.getString("desc");
        _townName = set.getString("loc");
        _auctionMin = set.getInteger("auctionMin", 0);
        _deposit = set.getInteger("deposit", 0);
        _lease = set.getInteger("lease", 0);
        _size = set.getInteger("size", 0);
        _grade = set.getInteger("grade", 0);
    }

    public static void openCloseDoor(Door door, boolean open) {
        if (door != null) {
            if (open) {
                door.openMe();
            } else {
                door.closeMe();
            }
        }

    }


    public final String getDesc() {
        return this._desc;
    }


    public final int getAuctionMin() {
        return _auctionMin;
    }

    public final int getGrade() {
        return this._grade;
    }

    public final int getLease() {
        return this._lease;
    }


    public final Auction getAuction() {
        return this._auction;
    }

    public final void setAuction(Auction auction) {
        this._auction = auction;
    }

    public void setOwnerId(int ownerId) {
        this._ownerId = ownerId;
    }

    public final long getPaidUntil() {
        return this._paidUntil;
    }

    public void setPaidUntil(long paidUntil) {
        this._paidUntil = paidUntil;
    }

    public final boolean getPaid() {
        return this._isPaid;
    }

    public void setPaid(boolean isPaid) {
        this._isPaid = isPaid;
    }

    public ClanHallZone getZone() {
        return this._zone;
    }

    public void setZone(ClanHallZone zone) {
        this._zone = zone;
    }

    public boolean isFree() {
        return this._ownerId == 0;
    }

    public final Map<Integer, ClanHallFunction> getFunctions() {
        return this._functions;
    }

    public ClanHallFunction getFunction(int type) {
        return this._functions.get(type);
    }

    public void free() {
        if (this._feeTask != null) {
            this._feeTask.cancel(false);
            this._feeTask = null;
        }

        Clan clan = ClanTable.getInstance().getClan(this._ownerId);
        if (clan != null) {
            clan.setClanHall(0);
            clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
        }

        this._ownerId = 0;
        this._paidUntil = 0L;
        this._isPaid = false;
        this.removeAllFunctions();
        this.openCloseDoors(false);
        if (this._auction != null) {
            this._auction.removeBids(null);
            this._auction.reset(true);
            this._auction.startAutoTask();
        }

        this.updateDb();
    }

    public void setOwner(Clan clan) {
        if (this._auction != null) {
            this._auction.removeBids(clan);
            this._auction.reset(false);
        }

        if (clan == null) {
            if (this._auction != null) {
                this._auction.startAutoTask();
            }

        } else {
            Clan owner = ClanTable.getInstance().getClan(this._ownerId);
            if (owner != null) {
                owner.setClanHall(0);
                owner.broadcastToOnlineMembers(new PledgeShowInfoUpdate(owner));
            }

            this.removeAllFunctions();
            this.openCloseDoors(false);
            clan.setClanHall(this._id);
            this._ownerId = clan.getClanId();
            this._paidUntil = System.currentTimeMillis() + 604800000L;
            this._isPaid = true;
            this.initializeFeeTask();
            clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
            this.banishForeigners();
            this.updateDb();
        }
    }

    public void openCloseDoor(Player player, int doorId, boolean open) {
        if (player != null && player.getClanId() == this.getOwnerId()) {
            this.openCloseDoor(doorId, open);
        }

    }

    public void openCloseDoor(int doorId, boolean open) {
        openCloseDoor(this.getDoor(doorId), open);
    }

    public void openCloseDoors(Player player, boolean open) {
        if (player != null && player.getClanId() == this.getOwnerId()) {
            this.openCloseDoors(open);
        }

    }

    public void openCloseDoors(boolean open) {
        Iterator var2 = this._doors.iterator();

        while (var2.hasNext()) {
            Door door = (Door) var2.next();
            if (open) {
                door.openMe();
            } else {
                door.closeMe();
            }
        }

    }

    public void banishForeigners() {
        if (this._zone != null) {
            this._zone.banishForeigners(this.getOwnerId());
        }

    }

    public void removeAllFunctions() {
        this._functions.clear();

        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("DELETE FROM clanhall_functions WHERE hall_id=?");

                try {
                    ps.setInt(1, this.getId());
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
            LOGGER.error("Couldn't delete all clan hall functions.", var9);
        }

    }

    public boolean updateFunctions(Player player, int type, int lvl, int lease, long rate) {
        if (player == null) {
            return false;
        } else if (lease > 0 && !player.destroyItemByItemId("Consume", 57, lease, null, true)) {
            return false;
        } else {
            ClanHallFunction chf = this._functions.get(type);
            if (chf == null) {
                this._functions.put(type, new ClanHallFunction(this, type, lvl, lease, rate, System.currentTimeMillis() + rate));
                return true;
            } else {
                if (lvl == 0 && lease == 0) {
                    chf.removeFunction();
                } else {
                    chf.refreshFunction(lease, lvl);
                }

                return true;
            }
        }
    }

    public void updateDb() {
        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("UPDATE clanhall SET ownerId=?, paidUntil=?, paid=?, sellerBid=?, sellerName=?, sellerClanName=?, endDate=? WHERE id=?");

                try {
                    ps.setInt(1, this._ownerId);
                    ps.setLong(2, this._paidUntil);
                    ps.setInt(3, this._isPaid ? 1 : 0);
                    if (this._auction != null) {
                        if (this._auction.getSeller() != null) {
                            ps.setInt(4, this._auction.getSeller().getBid());
                            ps.setString(5, this._auction.getSeller().getName());
                            ps.setString(6, this._auction.getSeller().getClanName());
                        } else {
                            ps.setInt(4, 0);
                            ps.setString(5, "");
                            ps.setString(6, "");
                        }

                        ps.setLong(7, this._auction.getEndDate());
                    } else {
                        ps.setInt(4, 0);
                        ps.setString(5, "");
                        ps.setString(6, "");
                        ps.setLong(7, 0L);
                    }

                    ps.setInt(8, this._id);
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
            LOGGER.error("Couldn't update clan hall.", var9);
        }

    }

    public void initializeFeeTask() {
        if (this._feeTask != null) {
            this._feeTask.cancel(false);
        }

        long time = System.currentTimeMillis();
        time = this._paidUntil > time ? this._paidUntil - time : 0L;
        this._feeTask = ThreadPool.schedule(new ClanHall.FeeTask(), time);
    }

    private class FeeTask implements Runnable {
        public FeeTask() {
        }

        public void run() {
            if (!ClanHall.this.isFree()) {
                Clan clan = ClanTable.getInstance().getClan(ClanHall.this.getOwnerId());
                if (clan == null) {
                    ClanHall.this.free();
                } else {
                    ClanHall var10000;
                    if (clan.getWarehouse().getAdena() >= ClanHall.this.getLease()) {
                        clan.getWarehouse().destroyItemByItemId("CH_rental_fee", 57, ClanHall.this.getLease(), null, null);
                        ClanHall.this._feeTask = ThreadPool.schedule(ClanHall.this.new FeeTask(), 604800000L);
                        var10000 = ClanHall.this;
                        var10000._paidUntil += 604800000L;
                        ClanHall.this._isPaid = true;
                        ClanHall.this.updateDb();
                    } else if (!ClanHall.this._isPaid) {
                        ClanHall.this.free();
                        clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.THE_CLAN_HALL_FEE_IS_ONE_WEEK_OVERDUE_THEREFORE_THE_CLAN_HALL_OWNERSHIP_HAS_BEEN_REVOKED));
                    } else {
                        ClanHall.this._feeTask = ThreadPool.schedule(ClanHall.this.new FeeTask(), 604800000L);
                        var10000 = ClanHall.this;
                        var10000._paidUntil += 604800000L;
                        ClanHall.this._isPaid = false;
                        ClanHall.this.updateDb();
                        clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW).addNumber(ClanHall.this.getLease()));
                    }

                }
            }
        }
    }
}