/**/
package net.sf.l2j.gameserver.model.clanhall;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Door;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.zone.type.ClanHallZone;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

public class ClanHall {
    public static final int FUNC_TELEPORT = 1;
    public static final int FUNC_ITEM_CREATE = 2;
    public static final int FUNC_RESTORE_HP = 3;
    public static final int FUNC_RESTORE_MP = 4;
    public static final int FUNC_RESTORE_EXP = 5;
    public static final int FUNC_SUPPORT = 6;
    public static final int FUNC_DECO_FRONTPLATEFORM = 7;
    public static final int FUNC_DECO_CURTAINS = 8;
    private static final CLogger LOGGER = new CLogger(ClanHall.class.getName());
    private static final String DELETE_FUNCTIONS = "DELETE FROM clanhall_functions WHERE hall_id=?";
    private static final String UPDATE_CH = "UPDATE clanhall SET ownerId=?, paidUntil=?, paid=?, sellerBid=?, sellerName=?, sellerClanName=?, endDate=? WHERE id=?";
    private static final int ONE_WEEK = 604800000;
    private final Map<Integer, ClanHallFunction> _functions = new ConcurrentHashMap<>();
    private final List<Door> _doors = new ArrayList<>();
    private final int _id;
    private final String _name;
    private final String _desc;
    private final String _location;
    private final int _grade;
    private final int _lease;
    private final int _defaultBid;
    private ScheduledFuture<?> _feeTask;
    private Auction _auction;
    private int _ownerId;
    private ClanHallZone _zone;
    private long _paidUntil;
    private boolean _isPaid;

    public ClanHall(StatSet set) {
        this._id = set.getInteger("id");
        this._name = set.getString("name");
        this._desc = set.getString("desc");
        this._location = set.getString("loc");
        this._grade = set.getInteger("grade");
        this._lease = set.getInteger("lease");
        this._defaultBid = set.getInteger("defaultBid");
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

    public final int getId() {
        return this._id;
    }

    public final String getName() {
        return this._name;
    }

    public final String getDesc() {
        return this._desc;
    }

    public final String getLocation() {
        return this._location;
    }

    public final int getGrade() {
        return this._grade;
    }

    public final int getLease() {
        return this._lease;
    }

    public final int getDefaultBid() {
        return this._defaultBid;
    }

    public final Auction getAuction() {
        return this._auction;
    }

    public final void setAuction(Auction auction) {
        this._auction = auction;
    }

    public final int getOwnerId() {
        return this._ownerId;
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

    public final List<Door> getDoors() {
        return this._doors;
    }

    public final Door getDoor(int doorId) {
        return this._doors.stream().filter((d) -> d.getDoorId() == doorId).findFirst().orElse(null);
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
        for (Door door : this._doors) {
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

        try (
                Connection con = ConnectionPool.getConnection();
                PreparedStatement ps = con.prepareStatement("DELETE FROM clanhall_functions WHERE hall_id=?")
        ) {
            ps.setInt(1, this.getId());
            ps.execute();
        } catch (Exception e) {
            LOGGER.error("Couldn't delete all clan hall functions.", e);
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
        try (
                Connection con = ConnectionPool.getConnection();
                PreparedStatement ps = con.prepareStatement("UPDATE clanhall SET ownerId=?, paidUntil=?, paid=?, sellerBid=?, sellerName=?, sellerClanName=?, endDate=? WHERE id=?")
        ) {
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
        } catch (Exception e) {
            LOGGER.error("Couldn't update clan hall.", e);
        }

    }

    public void initializeFeeTask() {
        if (this._feeTask != null) {
            this._feeTask.cancel(false);
        }

        long time = System.currentTimeMillis();
        time = this._paidUntil > time ? this._paidUntil - time : 0L;
        this._feeTask = ThreadPool.schedule(new FeeTask(), time);
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
                    if (clan.getWarehouse().getAdena() >= ClanHall.this.getLease()) {
                        clan.getWarehouse().destroyItemByItemId("CH_rental_fee", 57, ClanHall.this.getLease(), null, null);
                        ClanHall.this._feeTask = ThreadPool.schedule(ClanHall.this.new FeeTask(), 604800000L);
                        ClanHall var10000 = ClanHall.this;
                        var10000._paidUntil += 604800000L;
                        ClanHall.this._isPaid = true;
                        ClanHall.this.updateDb();
                    } else if (!ClanHall.this._isPaid) {
                        ClanHall.this.free();
                        clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.THE_CLAN_HALL_FEE_IS_ONE_WEEK_OVERDUE_THEREFORE_THE_CLAN_HALL_OWNERSHIP_HAS_BEEN_REVOKED));
                    } else {
                        ClanHall.this._feeTask = ThreadPool.schedule(ClanHall.this.new FeeTask(), 604800000L);
                        ClanHall var2 = ClanHall.this;
                        var2._paidUntil += 604800000L;
                        ClanHall.this._isPaid = false;
                        ClanHall.this.updateDb();
                        clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW).addNumber(ClanHall.this.getLease()));
                    }

                }
            }
        }
    }
}
