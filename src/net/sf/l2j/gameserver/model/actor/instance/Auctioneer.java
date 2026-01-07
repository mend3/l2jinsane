package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.gameserver.data.manager.ClanHallManager;
import net.sf.l2j.gameserver.data.xml.MapRegionData;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.clanhall.Auction;
import net.sf.l2j.gameserver.model.clanhall.Bidder;
import net.sf.l2j.gameserver.model.clanhall.ClanHall;
import net.sf.l2j.gameserver.model.clanhall.Seller;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

public final class Auctioneer extends Folk {
    private static final int PAGE_LIMIT = 15;

    public Auctioneer(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public void onBypassFeedback(Player player, String command) {
        StringTokenizer st = new StringTokenizer(command, " ");
        String actualCommand = st.nextToken();
        String val = st.hasMoreTokens() ? st.nextToken() : "";
        if (ClanHallManager.getInstance().getAuctionableClanHalls().isEmpty()) {
            player.sendPacket(SystemMessageId.NO_CLAN_HALLS_UP_FOR_AUCTION);
            return;
        }
        if (actualCommand.equalsIgnoreCase("list")) {
            showAuctionsList(val, player);
            return;
        }
        if (actualCommand.equalsIgnoreCase("bidding")) {
            if (val.isEmpty())
                return;
            try {
                ClanHall ch = ClanHallManager.getInstance().getClanHall(Integer.parseInt(val));
                if (ch != null) {
                    Auction auction = ch.getAuction();
                    if (auction != null) {
                        long remainingTime = auction.getEndDate() - System.currentTimeMillis();
                        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                        html.setFile("data/html/auction/AgitAuctionInfo.htm");
                        html.replace("%AGIT_NAME%", ch.getName());
                        html.replace("%AGIT_SIZE%", ch.getGrade() * 10);
                        html.replace("%AGIT_LEASE%", ch.getLease());
                        html.replace("%AGIT_LOCATION%", ch.getLocation());
                        html.replace("%AGIT_AUCTION_END%", (new SimpleDateFormat("dd-MM-yyyy HH:mm")).format(auction.getEndDate()));
                        html.replace("%AGIT_AUCTION_REMAIN%", remainingTime / 3600000L + " hours " + remainingTime / 3600000L + " minutes");
                        html.replace("%AGIT_AUCTION_MINBID%", auction.getMinimumBid());
                        html.replace("%AGIT_AUCTION_COUNT%", auction.getBidders().size());
                        html.replace("%AGIT_AUCTION_DESC%", ch.getDesc());
                        html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_list");
                        html.replace("%AGIT_LINK_BIDLIST%", "bypass -h npc_" + getObjectId() + "_bidlist " + ch.getId());
                        html.replace("%AGIT_LINK_RE%", "bypass -h npc_" + getObjectId() + "_bid1 " + ch.getId());
                        Seller seller = auction.getSeller();
                        if (seller == null) {
                            html.replace("%OWNER_PLEDGE_NAME%", "");
                            html.replace("%OWNER_PLEDGE_MASTER%", "");
                        } else {
                            html.replace("%OWNER_PLEDGE_NAME%", seller.getClanName());
                            html.replace("%OWNER_PLEDGE_MASTER%", seller.getName());
                        }
                        player.sendPacket(html);
                    }
                }
            } catch (Exception exception) {
            }
            return;
        }
        if (actualCommand.equalsIgnoreCase("location")) {
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            html.setFile("data/html/auction/location.htm");
            html.replace("%location%", MapRegionData.getInstance().getClosestTownName(player.getX(), player.getY()));
            html.replace("%LOCATION%", MapRegionData.getInstance().getPictureName(player.getX(), player.getY()));
            html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_start");
            player.sendPacket(html);
            return;
        }
        if (actualCommand.equalsIgnoreCase("start")) {
            showChatWindow(player);
            return;
        }
        Clan clan = player.getClan();
        if (clan == null || (player.getClanPrivileges() & 0x1000) != 4096) {
            showAuctionsList("1", player);
            player.sendPacket(SystemMessageId.CANNOT_PARTICIPATE_IN_AUCTION);
            return;
        }
        if (actualCommand.equalsIgnoreCase("bid")) {
            if (val.isEmpty())
                return;
            try {
                int bid = st.hasMoreTokens() ? Math.min(Integer.parseInt(st.nextToken()), 2147483647) : 0;
                Auction auction = ClanHallManager.getInstance().getAuction(Integer.parseInt(val));
                if (auction != null)
                    auction.setBid(player, bid);
            } catch (Exception exception) {
            }
            return;
        }
        if (actualCommand.equalsIgnoreCase("bid1")) {
            if (val.isEmpty())
                return;
            try {
                if (clan.getLevel() < 2) {
                    showAuctionsList("1", player);
                    player.sendPacket(SystemMessageId.AUCTION_ONLY_CLAN_LEVEL_2_HIGHER);
                    return;
                }
                if (clan.hasClanHall()) {
                    showAuctionsList("1", player);
                    player.sendPacket(SystemMessageId.CANNOT_PARTICIPATE_IN_AUCTION);
                    return;
                }
                if (clan.getAuctionBiddedAt() > 0 && clan.getAuctionBiddedAt() != Integer.parseInt(val)) {
                    showAuctionsList("1", player);
                    player.sendPacket(SystemMessageId.ALREADY_SUBMITTED_BID);
                    return;
                }
                ClanHall ch = ClanHallManager.getInstance().getClanHall(Integer.parseInt(val));
                if (ch == null)
                    return;
                Auction auction = ch.getAuction();
                if (auction == null)
                    return;
                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                html.setFile("data/html/auction/AgitBid1.htm");
                html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_bidding " + val);
                html.replace("%PLEDGE_ADENA%", clan.getWarehouse().getAdena());
                html.replace("%AGIT_AUCTION_MINBID%", auction.getMinimumBid());
                html.replace("npc_%objectId%_bid", "npc_" + getObjectId() + "_bid " + val);
                player.sendPacket(html);
            } catch (Exception exception) {
            }
            return;
        }
        if (actualCommand.equalsIgnoreCase("bidlist")) {
            try {
                int auctionId = val.isEmpty() ? clan.getAuctionBiddedAt() : Integer.parseInt(val);
                Auction auction = ClanHallManager.getInstance().getAuction(auctionId);
                if (auction == null)
                    return;
                boolean isSeller = false;
                Seller seller = auction.getSeller();
                if (seller != null)
                    isSeller = seller.getClanName().equalsIgnoreCase(clan.getName());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Collection<Bidder> bidders = auction.getBidders().values();
                StringBuilder sb = new StringBuilder(bidders.size() * 150);
                for (Bidder bidder : bidders) {
                    StringUtil.append(sb, "<tr><td width=90 align=center>", bidder.getClanName(), "</td><td width=90 align=center>", bidder.getName(), "</td><td width=90 align=center>", sdf.format(bidder.getTime()), "</td></tr>");
                }
                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                html.setFile("data/html/auction/AgitBidderList.htm");
                html.replace("%AGIT_LIST%", sb.toString());
                html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + (isSeller ? "_selectedItems" : ("_bidding " + auctionId)));
                html.replace("%objectId%", getObjectId());
                player.sendPacket(html);
            } catch (Exception exception) {
            }
            return;
        }
        if (actualCommand.equalsIgnoreCase("selectedItems")) {
            showSelectedItems(player);
            return;
        }
        if (actualCommand.equalsIgnoreCase("cancelBid")) {
            try {
                Auction auction = ClanHallManager.getInstance().getAuction(clan.getAuctionBiddedAt());
                if (auction == null)
                    return;
                Bidder bidder = auction.getBidders().get(player.getClanId());
                if (bidder == null)
                    return;
                int bid = bidder.getBid();
                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                html.setFile("data/html/auction/AgitBidCancel.htm");
                html.replace("%AGIT_BID%", bid);
                html.replace("%AGIT_BID_REMAIN%", (int) (bid * 0.9D));
                html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_selectedItems");
                html.replace("%objectId%", getObjectId());
                player.sendPacket(html);
            } catch (Exception exception) {
            }
            return;
        }
        if (actualCommand.equalsIgnoreCase("doCancelBid")) {
            Auction auction = ClanHallManager.getInstance().getAuction(clan.getAuctionBiddedAt());
            if (auction != null) {
                auction.cancelBid(player.getClanId());
                player.sendPacket(SystemMessageId.CANCELED_BID);
            }
            return;
        }
        if (actualCommand.equalsIgnoreCase("cancelAuction")) {
            ClanHall ch = ClanHallManager.getInstance().getClanHallByOwner(clan);
            if (ch == null)
                return;
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            html.setFile("data/html/auction/AgitSaleCancel.htm");
            html.replace("%AGIT_DEPOSIT%", ch.getLease());
            html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_selectedItems");
            html.replace("%objectId%", getObjectId());
            player.sendPacket(html);
            return;
        }
        if (actualCommand.equalsIgnoreCase("doCancelAuction")) {
            Auction auction = ClanHallManager.getInstance().getAuction(clan.getClanHallId());
            if (auction != null) {
                auction.cancelAuction();
                player.sendPacket(SystemMessageId.CANCELED_BID);
            }
            showChatWindow(player);
            return;
        }
        if (actualCommand.equalsIgnoreCase("sale")) {
            ClanHall ch = ClanHallManager.getInstance().getClanHallByOwner(clan);
            if (ch == null)
                return;
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            html.setFile("data/html/auction/AgitSale1.htm");
            html.replace("%AGIT_DEPOSIT%", ch.getLease());
            html.replace("%AGIT_PLEDGE_ADENA%", clan.getWarehouse().getAdena());
            html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_selectedItems");
            html.replace("%objectId%", getObjectId());
            player.sendPacket(html);
            return;
        }
        if (actualCommand.equalsIgnoreCase("rebid")) {
            ClanHall ch = ClanHallManager.getInstance().getClanHall(clan.getAuctionBiddedAt());
            if (ch == null)
                return;
            Auction auction = ch.getAuction();
            if (auction == null)
                return;
            Bidder bidder = auction.getBidders().get(player.getClanId());
            if (bidder == null)
                return;
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            html.setFile("data/html/auction/AgitBid2.htm");
            html.replace("%AGIT_AUCTION_BID%", bidder.getBid());
            html.replace("%AGIT_AUCTION_MINBID%", ch.getDefaultBid());
            html.replace("%AGIT_AUCTION_END%", (new SimpleDateFormat("dd-MM-yyyy HH:mm")).format(auction.getEndDate()));
            html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_selectedItems");
            html.replace("npc_%objectId%_bid1", "npc_" + getObjectId() + "_bid1 " + ch.getId());
            player.sendPacket(html);
            return;
        }
        if (clan.getWarehouse().getAdena() < ClanHallManager.getInstance().getClanHallByOwner(clan).getLease()) {
            showSelectedItems(player);
            player.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA_IN_CWH);
            return;
        }
        if (actualCommand.equalsIgnoreCase("auction")) {
            if (val.isEmpty())
                return;
            try {
                int days = Integer.parseInt(val);
                int bid = st.hasMoreTokens() ? Math.min(Integer.parseInt(st.nextToken()), 2147483647) : 0;
                ClanHall ch = ClanHallManager.getInstance().getClanHallByOwner(clan);
                if (ch == null)
                    return;
                Auction auction = ch.getAuction();
                if (auction == null)
                    return;
                auction.setSeller(clan, bid);
                auction.setEndDate(days * 86400000L);
                NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
                html.setFile("data/html/auction/AgitSale3.htm");
                html.replace("%x%", val);
                html.replace("%AGIT_AUCTION_END%", (new SimpleDateFormat("dd-MM-yyyy HH:mm")).format(auction.getEndDate()));
                html.replace("%AGIT_AUCTION_MINBID%", ch.getDefaultBid());
                html.replace("%AGIT_AUCTION_MIN%", bid);
                html.replace("%AGIT_AUCTION_DESC%", ch.getDesc());
                html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_sale2");
                html.replace("%objectId%", getObjectId());
                player.sendPacket(html);
            } catch (Exception exception) {
            }
            return;
        }
        if (actualCommand.equalsIgnoreCase("confirmAuction")) {
            ClanHall ch = ClanHallManager.getInstance().getClanHall(clan.getClanHallId());
            if (ch == null)
                return;
            Auction auction = ch.getAuction();
            if (auction == null || auction.getSeller() == null)
                return;
            if (auction.takeItem(player, ch.getLease())) {
                auction.confirmAuction();
                showSelectedItems(player);
                player.sendPacket(SystemMessageId.REGISTERED_FOR_CLANHALL);
            }
            return;
        }
        if (actualCommand.equalsIgnoreCase("sale2")) {
            ClanHall ch = ClanHallManager.getInstance().getClanHallByOwner(clan);
            if (ch == null)
                return;
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            html.setFile("data/html/auction/AgitSale2.htm");
            html.replace("%AGIT_LAST_PRICE%", ch.getLease());
            html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_sale");
            html.replace("%objectId%", getObjectId());
            player.sendPacket(html);
            return;
        }
        super.onBypassFeedback(player, command);
    }

    public void showChatWindow(Player player) {
        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
        html.setFile("data/html/auction/auction.htm");
        html.replace("%objectId%", getObjectId());
        html.replace("%npcId%", getNpcId());
        html.replace("%npcname%", getName());
        player.sendPacket(html);
    }

    public void showChatWindow(Player player, int val) {
        if (val == 0)
            return;
        super.showChatWindow(player, val);
    }

    private void showAuctionsList(String val, Player player) {
        List<ClanHall> chs = ClanHallManager.getInstance().getAuctionableClanHalls();
        int page = val.isEmpty() ? 1 : Integer.parseInt(val);
        int max = MathUtil.countPagesNumber(chs.size(), 15);
        chs = chs.subList((page - 1) * 15, Math.min(page * 15, chs.size()));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        StringBuilder sb = new StringBuilder(4000);
        sb.append("<table width=280>");
        for (ClanHall ch : chs) {
            Auction auction = ch.getAuction();
            StringUtil.append(sb, "<tr><td><font color=\"aaaaff\">", ch.getLocation(), "</font></td><td><font color=\"ffffaa\"><a action=\"bypass -h npc_", getObjectId(), "_bidding ", ch.getId(), "\">", ch.getName(), " [", auction.getBidders().size(),
                    "]</a></font></td><td>", sdf.format(auction.getEndDate()), "</td><td><font color=\"aaffff\">", auction.getMinimumBid(), "</font></td></tr>");
        }
        sb.append("</table><table width=280><tr>");
        for (int j = 1; j <= max; j++) {
            StringUtil.append(sb, "<td><center><a action=\"bypass -h npc_", getObjectId(), "_list ", j, "\"> Page ", j, " </a></center></td>");
        }
        sb.append("</tr></table>");
        NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
        html.setFile("data/html/auction/AgitAuctionList.htm");
        html.replace("%AGIT_LIST%", sb.toString());
        html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_start");
        player.sendPacket(html);
    }

    private void showSelectedItems(Player player) {
        Clan clan = player.getClan();
        if (clan == null)
            return;
        if (!clan.hasClanHall() && clan.getAuctionBiddedAt() > 0) {
            ClanHall ch = ClanHallManager.getInstance().getClanHall(clan.getAuctionBiddedAt());
            if (ch == null)
                return;
            Auction auction = ch.getAuction();
            if (auction == null)
                return;
            long remainingTime = auction.getEndDate() - System.currentTimeMillis();
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            html.setFile("data/html/auction/AgitBidInfo.htm");
            html.replace("%AGIT_NAME%", ch.getName());
            html.replace("%AGIT_SIZE%", ch.getGrade() * 10);
            html.replace("%AGIT_LEASE%", ch.getLease());
            html.replace("%AGIT_LOCATION%", ch.getLocation());
            html.replace("%AGIT_AUCTION_END%", (new SimpleDateFormat("dd-MM-yyyy HH:mm")).format(auction.getEndDate()));
            html.replace("%AGIT_AUCTION_REMAIN%", remainingTime / 3600000L + " hours " + remainingTime / 3600000L + " minutes");
            html.replace("%AGIT_AUCTION_MYBID%", auction.getBidders().get(player.getClanId()).getBid());
            html.replace("%AGIT_AUCTION_DESC%", ch.getDesc());
            html.replace("%objectId%", getObjectId());
            html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_start");
            Seller seller = auction.getSeller();
            if (seller == null) {
                html.replace("%OWNER_PLEDGE_NAME%", "");
                html.replace("%OWNER_PLEDGE_MASTER%", "");
                html.replace("%AGIT_AUCTION_MINBID%", ch.getDefaultBid());
            } else {
                html.replace("%OWNER_PLEDGE_NAME%", seller.getClanName());
                html.replace("%OWNER_PLEDGE_MASTER%", seller.getName());
                html.replace("%AGIT_AUCTION_MINBID%", seller.getBid());
            }
            player.sendPacket(html);
            return;
        }
        if (clan.hasClanHall()) {
            ClanHall ch = ClanHallManager.getInstance().getClanHall(clan.getClanHallId());
            if (ch == null)
                return;
            Auction auction = ch.getAuction();
            if (auction == null)
                return;
            NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
            Seller seller = auction.getSeller();
            if (seller != null) {
                long remainingTime = auction.getEndDate() - System.currentTimeMillis();
                html.setFile("data/html/auction/AgitSaleInfo.htm");
                html.replace("%AGIT_NAME%", ch.getName());
                html.replace("%AGIT_OWNER_PLEDGE_NAME%", seller.getClanName());
                html.replace("%OWNER_PLEDGE_MASTER%", seller.getName());
                html.replace("%AGIT_SIZE%", ch.getGrade() * 10);
                html.replace("%AGIT_LEASE%", ch.getLease());
                html.replace("%AGIT_LOCATION%", ch.getLocation());
                html.replace("%AGIT_AUCTION_END%", (new SimpleDateFormat("dd-MM-yyyy HH:mm")).format(auction.getEndDate()));
                html.replace("%AGIT_AUCTION_REMAIN%", remainingTime / 3600000L + " hours " + remainingTime / 3600000L + " minutes");
                html.replace("%AGIT_AUCTION_MINBID%", seller.getBid());
                html.replace("%AGIT_AUCTION_BIDCOUNT%", auction.getBidders().size());
                html.replace("%AGIT_AUCTION_DESC%", ch.getDesc());
                html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_start");
                html.replace("%id%", ch.getId());
                html.replace("%objectId%", getObjectId());
            } else {
                html.setFile("data/html/auction/AgitInfo.htm");
                html.replace("%AGIT_NAME%", ch.getName());
                html.replace("%AGIT_OWNER_PLEDGE_NAME%", clan.getName());
                html.replace("%OWNER_PLEDGE_MASTER%", clan.getLeaderName());
                html.replace("%AGIT_SIZE%", ch.getGrade() * 10);
                html.replace("%AGIT_LEASE%", ch.getLease());
                html.replace("%AGIT_LOCATION%", ch.getLocation());
                html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + getObjectId() + "_start");
                html.replace("%objectId%", getObjectId());
            }
            player.sendPacket(html);
            return;
        }
        showAuctionsList("1", player);
        player.sendPacket(SystemMessageId.NO_OFFERINGS_OWN_OR_MADE_BID_FOR);
    }
}
