package net.sf.l2j.gameserver.model.clanhall;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

public class Auction {
    private static final CLogger LOGGER = new CLogger(Auction.class.getName());
    private static final String LOAD_BIDDERS = "SELECT bidderId, bidderName, maxBid, clan_name, time_bid FROM auction_bid WHERE auctionId = ? ORDER BY maxBid DESC";
    private static final String UPDATE_DATE = "UPDATE clanhall SET endDate=? WHERE id=?";
    private static final String INSERT_OR_UPDATE_BIDDER = "INSERT INTO auction_bid (id, auctionId, bidderId, bidderName, maxBid, clan_name, time_bid) VALUES (?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE bidderId=VALUES(bidderId), bidderName=VALUES(bidderName), maxBid=VALUES(maxBid), time_bid=VALUES(time_bid)";
    private static final String DELETE_BIDDERS = "DELETE FROM auction_bid WHERE auctionId=?";
    private static final String DELETE_BIDDER = "DELETE FROM auction_bid WHERE auctionId=? AND bidderId=?";
    private static final String UPDATE_SELLER = "UPDATE clanhall SET sellerBid=?, sellerName=?, sellerClanName=?, endDate=? WHERE id=?";
    private final Map<Integer, Bidder> _bidders = new HashMap<>();
    private final ClanHall _ch;
    private long _endDate;
    private Bidder _highestBidder;
    private Seller _seller;
    private Future<?> _task;

    public Auction(ClanHall ch, int sellerBid, String sellerName, String sellerClanName, long endDate) {
        this._ch = ch;
        this._endDate = endDate;
        if (!StringUtil.isEmpty(sellerName, sellerClanName)) {
            this._seller = new Seller(sellerName, sellerClanName, sellerBid);
        }

        try (
                Connection con = ConnectionPool.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT bidderId, bidderName, maxBid, clan_name, time_bid FROM auction_bid WHERE auctionId = ? ORDER BY maxBid DESC")
        ) {
            ps.setInt(1, ch.getId());

            Bidder bidder;
            try (ResultSet rs = ps.executeQuery()) {
                for (; rs.next(); this._bidders.put(rs.getInt("bidderId"), bidder)) {
                    bidder = new Bidder(rs.getString("bidderName"), rs.getString("clan_name"), rs.getInt("maxBid"), rs.getLong("time_bid"));
                    if (rs.isFirst()) {
                        this._highestBidder = bidder;
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Couldn't load Auction bid.", e);
        }

        this.startAutoTask();
    }

    private static void returnItem(Clan clan, int quantity, boolean penalty) {
        if (clan != null) {
            if (penalty) {
                quantity = (int) ((double) quantity * 0.9);
            }

            int limit = Integer.MAX_VALUE - clan.getWarehouse().getAdena();
            quantity = Math.min(quantity, limit);
            clan.getWarehouse().addItem("Outbidded", 57, quantity, null, null);
        }
    }

    public final long getEndDate() {
        return this._endDate;
    }

    public final void setEndDate(long endDate) {
        this._endDate = System.currentTimeMillis() + endDate;
    }

    public final Bidder getHighestBidder() {
        return this._highestBidder;
    }

    public final Seller getSeller() {
        return this._seller;
    }

    public final void setSeller(Clan clan, int bid) {
        if (clan != null) {
            this._seller = new Seller(clan.getLeaderName(), clan.getName(), bid);
        }
    }

    public final Map<Integer, Bidder> getBidders() {
        return this._bidders;
    }

    public void startAutoTask() {
        long currentTime = System.currentTimeMillis();
        long taskDelay = 0L;
        if (this._endDate <= currentTime) {
            this._endDate = currentTime + 604800000L;

            try (
                    Connection con = ConnectionPool.getConnection();
                    PreparedStatement ps = con.prepareStatement("UPDATE clanhall SET endDate=? WHERE id=?")
            ) {
                ps.setLong(1, this._endDate);
                ps.setInt(2, this._ch.getId());
                ps.execute();
            } catch (Exception e) {
                LOGGER.error("Couldn't save Auction date.", e);
            }
        } else {
            taskDelay = this._endDate - currentTime;
        }

        this._task = ThreadPool.schedule(this::endAuction, taskDelay);
    }

    public synchronized void setBid(Player player, int bid) {
        Clan clan = player.getClan();
        if (clan != null) {
            if (bid <= this.getMinimumBid()) {
                player.sendPacket(SystemMessageId.BID_PRICE_MUST_BE_HIGHER);
            } else {
                int requiredAdena = bid;
                Bidder bidder = this._bidders.get(player.getClanId());
                if (bidder != null) {
                    if (bid <= bidder.getBid()) {
                        player.sendPacket(SystemMessageId.BID_PRICE_MUST_BE_HIGHER);
                        return;
                    }

                    requiredAdena = bid - bidder.getBid();
                }

                if (this.takeItem(player, requiredAdena)) {
                    long time = System.currentTimeMillis();
                    if (bidder == null) {
                        bidder = new Bidder(clan.getLeaderName(), clan.getName(), bid, time);
                        this._bidders.put(player.getClanId(), bidder);
                    } else {
                        bidder.setBid(bid);
                        bidder.setTime(time);
                    }

                    this.recalculateHighestBidder();
                    player.sendPacket(SystemMessageId.BID_IN_CLANHALL_AUCTION);
                    clan.setAuctionBiddedAt(this._ch.getId());

                    try (
                            Connection con = ConnectionPool.getConnection();
                            PreparedStatement ps = con.prepareStatement("INSERT INTO auction_bid (id, auctionId, bidderId, bidderName, maxBid, clan_name, time_bid) VALUES (?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE bidderId=VALUES(bidderId), bidderName=VALUES(bidderName), maxBid=VALUES(maxBid), time_bid=VALUES(time_bid)")
                    ) {
                        ps.setInt(1, player.getClanId());
                        ps.setInt(2, this._ch.getId());
                        ps.setInt(3, player.getClanId());
                        ps.setString(4, player.getName());
                        ps.setInt(5, bid);
                        ps.setString(6, clan.getName());
                        ps.setLong(7, time);
                        ps.execute();
                    } catch (Exception e) {
                        LOGGER.error("Couldn't update Auction.", e);
                    }

                }
            }
        }
    }

    public boolean takeItem(Player bidder, int quantity) {
        Clan clan = bidder.getClan();
        if (clan == null) {
            return false;
        } else if (clan.getWarehouse().getAdena() < quantity) {
            bidder.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA_IN_CWH);
            return false;
        } else {
            clan.getWarehouse().destroyItemByItemId("Buy", 57, quantity, bidder, bidder);
            return true;
        }
    }

    public void removeBids(Clan newOwner) {
        try (
                Connection con = ConnectionPool.getConnection();
                PreparedStatement ps = con.prepareStatement("DELETE FROM auction_bid WHERE auctionId=?")
        ) {
            ps.setInt(1, this._ch.getId());
            ps.execute();
        } catch (Exception e) {
            LOGGER.error("Couldn't remove Auction bids.", e);
        }

        for (Bidder bidder : this._bidders.values()) {
            Clan clan = bidder.getClan();
            if (clan != null) {
                clan.setAuctionBiddedAt(0);
                if (clan != newOwner) {
                    returnItem(clan, bidder.getBid(), true);
                }

                if (newOwner != null) {
                    clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLANHALL_AWARDED_TO_CLAN_S1).addString(newOwner.getName()));
                }
            }
        }

        this._bidders.clear();
    }

    public void endAuction() {
        if (this._task != null) {
            this._task.cancel(false);
            this._task = null;
        }

        if (this._highestBidder == null) {
            if (this._seller == null) {
                this.startAutoTask();
            } else {
                Clan owner = this._seller.getClan();
                if (owner == null) {
                    return;
                }

                owner.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLANHALL_NOT_SOLD));
            }

        } else {
            if (this._seller != null) {
                Clan clan = this._seller.getClan();
                returnItem(clan, this._highestBidder.getBid(), true);
                returnItem(clan, this._ch.getLease(), false);
            }

            this._ch.setOwner(this._highestBidder.getClan());
        }
    }

    public synchronized void cancelBid(int objectId) {
        try (
                Connection con = ConnectionPool.getConnection();
                PreparedStatement ps = con.prepareStatement("DELETE FROM auction_bid WHERE auctionId=? AND bidderId=?")
        ) {
            ps.setInt(1, this._ch.getId());
            ps.setInt(2, objectId);
            ps.execute();
        } catch (Exception e) {
            LOGGER.error("Couldn't cancel Auction bid.", e);
        }

        Bidder bidder = this._bidders.remove(objectId);
        if (bidder != null) {
            Clan clan = bidder.getClan();
            if (clan != null) {
                returnItem(clan, bidder.getBid(), true);
                clan.setAuctionBiddedAt(0);
            }
        }

        if (bidder == this._highestBidder) {
            this.recalculateHighestBidder();
        }

    }

    public void cancelAuction() {
        if (this._seller != null) {
            this.removeBids(this._seller.getClan());
            this.reset(false);
            this._ch.updateDb();
        }
    }

    public void confirmAuction() {
        if (this._seller != null) {
            try (
                    Connection con = ConnectionPool.getConnection();
                    PreparedStatement ps = con.prepareStatement("UPDATE clanhall SET sellerBid=?, sellerName=?, sellerClanName=?, endDate=? WHERE id=?")
            ) {
                ps.setInt(1, this._seller.getBid());
                ps.setString(2, this._seller.getName());
                ps.setString(3, this._seller.getClanName());
                ps.setLong(4, this._endDate);
                ps.setInt(5, this._ch.getId());
                ps.execute();
            } catch (Exception e) {
                LOGGER.error("Couldn't confirm Auction.", e);
            }

        }
    }

    public void recalculateHighestBidder() {
        Bidder highestBidder = null;
        int highestBid = 0;

        for (Bidder bidder : this._bidders.values()) {
            if (bidder.getBid() > highestBid) {
                highestBidder = bidder;
                highestBid = bidder.getBid();
            }
        }

        this._highestBidder = highestBidder;
    }

    public void reset(boolean runTask) {
        this._highestBidder = null;
        this._seller = null;
        this._endDate = 0L;
        if (this._task != null) {
            this._task.cancel(false);
            this._task = null;
        }

        if (runTask) {
            this.startAutoTask();
        }

    }

    public int getMinimumBid() {
        return this._seller == null ? this._ch.getDefaultBid() : Math.max(this._ch.getDefaultBid(), this._seller.getBid());
    }
}
