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
        } else if (actualCommand.equalsIgnoreCase("list")) {
            this.showAuctionsList(val, player);
        } else if (actualCommand.equalsIgnoreCase("bidding")) {
            if (!val.isEmpty()) {
                try {
                    ClanHall ch = ClanHallManager.getInstance().getClanHall(Integer.parseInt(val));
                    if (ch != null) {
                        Auction auction = ch.getAuction();
                        if (auction != null) {
                            long remainingTime = auction.getEndDate() - System.currentTimeMillis();
                            NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                            html.setFile("data/html/auction/AgitAuctionInfo.htm");
                            html.replace("%AGIT_NAME%", ch.getName());
                            html.replace("%AGIT_SIZE%", ch.getGrade() * 10);
                            html.replace("%AGIT_LEASE%", ch.getLease());
                            html.replace("%AGIT_LOCATION%", ch.getLocation());
                            html.replace("%AGIT_AUCTION_END%", (new SimpleDateFormat("dd-MM-yyyy HH:mm")).format(auction.getEndDate()));
                            html.replace("%AGIT_AUCTION_REMAIN%", remainingTime / 3600000L + " hours " + remainingTime / 60000L % 60L + " minutes");
                            html.replace("%AGIT_AUCTION_MINBID%", auction.getMinimumBid());
                            html.replace("%AGIT_AUCTION_COUNT%", auction.getBidders().size());
                            html.replace("%AGIT_AUCTION_DESC%", ch.getDesc());
                            html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + this.getObjectId() + "_list");
                            int var60 = this.getObjectId();
                            html.replace("%AGIT_LINK_BIDLIST%", "bypass -h npc_" + var60 + "_bidlist " + ch.getId());
                            var60 = this.getObjectId();
                            html.replace("%AGIT_LINK_RE%", "bypass -h npc_" + var60 + "_bid1 " + ch.getId());
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
                } catch (Exception ignored) {
                }

            }
        } else if (actualCommand.equalsIgnoreCase("location")) {
            NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
            html.setFile("data/html/auction/location.htm");
            html.replace("%location%", MapRegionData.getInstance().getClosestTownName(player.getX(), player.getY()));
            html.replace("%LOCATION%", MapRegionData.getInstance().getPictureName(player.getX(), player.getY()));
            html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + this.getObjectId() + "_start");
            player.sendPacket(html);
        } else if (actualCommand.equalsIgnoreCase("start")) {
            this.showChatWindow(player);
        } else {
            Clan clan = player.getClan();
            if (clan != null && (player.getClanPrivileges() & 4096) == 4096) {
                if (actualCommand.equalsIgnoreCase("bid")) {
                    if (!val.isEmpty()) {
                        try {
                            int bid = st.hasMoreTokens() ? Math.min(Integer.parseInt(st.nextToken()), Integer.MAX_VALUE) : 0;
                            Auction auction = ClanHallManager.getInstance().getAuction(Integer.parseInt(val));
                            if (auction != null) {
                                auction.setBid(player, bid);
                            }
                        } catch (Exception ignored) {
                        }

                    }
                } else if (actualCommand.equalsIgnoreCase("bid1")) {
                    if (!val.isEmpty()) {
                        try {
                            if (clan.getLevel() < 2) {
                                this.showAuctionsList("1", player);
                                player.sendPacket(SystemMessageId.AUCTION_ONLY_CLAN_LEVEL_2_HIGHER);
                                return;
                            }

                            if (clan.hasClanHall()) {
                                this.showAuctionsList("1", player);
                                player.sendPacket(SystemMessageId.CANNOT_PARTICIPATE_IN_AUCTION);
                                return;
                            }

                            if (clan.getAuctionBiddedAt() > 0 && clan.getAuctionBiddedAt() != Integer.parseInt(val)) {
                                this.showAuctionsList("1", player);
                                player.sendPacket(SystemMessageId.ALREADY_SUBMITTED_BID);
                                return;
                            }

                            ClanHall ch = ClanHallManager.getInstance().getClanHall(Integer.parseInt(val));
                            if (ch == null) {
                                return;
                            }

                            Auction auction = ch.getAuction();
                            if (auction == null) {
                                return;
                            }

                            NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                            html.setFile("data/html/auction/AgitBid1.htm");
                            int var58 = this.getObjectId();
                            html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + var58 + "_bidding " + val);
                            html.replace("%PLEDGE_ADENA%", clan.getWarehouse().getAdena());
                            html.replace("%AGIT_AUCTION_MINBID%", auction.getMinimumBid());
                            var58 = this.getObjectId();
                            html.replace("npc_%objectId%_bid", "npc_" + var58 + "_bid " + val);
                            player.sendPacket(html);
                        } catch (Exception ignored) {
                        }

                    }
                } else if (actualCommand.equalsIgnoreCase("bidlist")) {
                    try {
                        int auctionId = val.isEmpty() ? clan.getAuctionBiddedAt() : Integer.parseInt(val);
                        Auction auction = ClanHallManager.getInstance().getAuction(auctionId);
                        if (auction == null) {
                            return;
                        }

                        boolean isSeller = false;
                        Seller seller = auction.getSeller();
                        if (seller != null) {
                            isSeller = seller.getClanName().equalsIgnoreCase(clan.getName());
                        }

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        Collection<Bidder> bidders = auction.getBidders().values();
                        StringBuilder sb = new StringBuilder(bidders.size() * 150);

                        for (Bidder bidder : bidders) {
                            StringUtil.append(sb, "<tr><td width=90 align=center>", bidder.getClanName(), "</td><td width=90 align=center>", bidder.getName(), "</td><td width=90 align=center>", sdf.format(bidder.getTime()), "</td></tr>");
                        }

                        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                        html.setFile("data/html/auction/AgitBidderList.htm");
                        html.replace("%AGIT_LIST%", sb.toString());
                        int var57 = this.getObjectId();
                        html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + var57 + (isSeller ? "_selectedItems" : "_bidding " + auctionId));
                        html.replace("%objectId%", this.getObjectId());
                        player.sendPacket(html);
                    } catch (Exception ignored) {
                    }

                } else if (actualCommand.equalsIgnoreCase("selectedItems")) {
                    this.showSelectedItems(player);
                } else if (actualCommand.equalsIgnoreCase("cancelBid")) {
                    try {
                        Auction auction = ClanHallManager.getInstance().getAuction(clan.getAuctionBiddedAt());
                        if (auction == null) {
                            return;
                        }

                        Bidder bidder = auction.getBidders().get(player.getClanId());
                        if (bidder == null) {
                            return;
                        }

                        int bid = bidder.getBid();
                        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                        html.setFile("data/html/auction/AgitBidCancel.htm");
                        html.replace("%AGIT_BID%", bid);
                        html.replace("%AGIT_BID_REMAIN%", (int) ((double) bid * 0.9));
                        html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + this.getObjectId() + "_selectedItems");
                        html.replace("%objectId%", this.getObjectId());
                        player.sendPacket(html);
                    } catch (Exception ignored) {
                    }

                } else if (actualCommand.equalsIgnoreCase("doCancelBid")) {
                    Auction auction = ClanHallManager.getInstance().getAuction(clan.getAuctionBiddedAt());
                    if (auction != null) {
                        auction.cancelBid(player.getClanId());
                        player.sendPacket(SystemMessageId.CANCELED_BID);
                    }

                } else if (actualCommand.equalsIgnoreCase("cancelAuction")) {
                    ClanHall ch = ClanHallManager.getInstance().getClanHallByOwner(clan);
                    if (ch != null) {
                        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                        html.setFile("data/html/auction/AgitSaleCancel.htm");
                        html.replace("%AGIT_DEPOSIT%", ch.getLease());
                        html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + this.getObjectId() + "_selectedItems");
                        html.replace("%objectId%", this.getObjectId());
                        player.sendPacket(html);
                    }
                } else if (actualCommand.equalsIgnoreCase("doCancelAuction")) {
                    Auction auction = ClanHallManager.getInstance().getAuction(clan.getClanHallId());
                    if (auction != null) {
                        auction.cancelAuction();
                        player.sendPacket(SystemMessageId.CANCELED_BID);
                    }

                    this.showChatWindow(player);
                } else if (actualCommand.equalsIgnoreCase("sale")) {
                    ClanHall ch = ClanHallManager.getInstance().getClanHallByOwner(clan);
                    if (ch != null) {
                        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                        html.setFile("data/html/auction/AgitSale1.htm");
                        html.replace("%AGIT_DEPOSIT%", ch.getLease());
                        html.replace("%AGIT_PLEDGE_ADENA%", clan.getWarehouse().getAdena());
                        html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + this.getObjectId() + "_selectedItems");
                        html.replace("%objectId%", this.getObjectId());
                        player.sendPacket(html);
                    }
                } else if (actualCommand.equalsIgnoreCase("rebid")) {
                    ClanHall ch = ClanHallManager.getInstance().getClanHall(clan.getAuctionBiddedAt());
                    if (ch != null) {
                        Auction auction = ch.getAuction();
                        if (auction != null) {
                            Bidder bidder = auction.getBidders().get(player.getClanId());
                            if (bidder != null) {
                                NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                                html.setFile("data/html/auction/AgitBid2.htm");
                                html.replace("%AGIT_AUCTION_BID%", bidder.getBid());
                                html.replace("%AGIT_AUCTION_MINBID%", ch.getDefaultBid());
                                html.replace("%AGIT_AUCTION_END%", (new SimpleDateFormat("dd-MM-yyyy HH:mm")).format(auction.getEndDate()));
                                html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + this.getObjectId() + "_selectedItems");
                                int var10002 = this.getObjectId();
                                html.replace("npc_%objectId%_bid1", "npc_" + var10002 + "_bid1 " + ch.getId());
                                player.sendPacket(html);
                            }
                        }
                    }
                } else if (clan.getWarehouse().getAdena() < ClanHallManager.getInstance().getClanHallByOwner(clan).getLease()) {
                    this.showSelectedItems(player);
                    player.sendPacket(SystemMessageId.NOT_ENOUGH_ADENA_IN_CWH);
                } else if (actualCommand.equalsIgnoreCase("auction")) {
                    if (!val.isEmpty()) {
                        try {
                            int days = Integer.parseInt(val);
                            int bid = st.hasMoreTokens() ? Math.min(Integer.parseInt(st.nextToken()), Integer.MAX_VALUE) : 0;
                            ClanHall ch = ClanHallManager.getInstance().getClanHallByOwner(clan);
                            if (ch == null) {
                                return;
                            }

                            Auction auction = ch.getAuction();
                            if (auction == null) {
                                return;
                            }

                            auction.setSeller(clan, bid);
                            auction.setEndDate((long) days * 86400000L);
                            NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                            html.setFile("data/html/auction/AgitSale3.htm");
                            html.replace("%x%", val);
                            html.replace("%AGIT_AUCTION_END%", (new SimpleDateFormat("dd-MM-yyyy HH:mm")).format(auction.getEndDate()));
                            html.replace("%AGIT_AUCTION_MINBID%", ch.getDefaultBid());
                            html.replace("%AGIT_AUCTION_MIN%", bid);
                            html.replace("%AGIT_AUCTION_DESC%", ch.getDesc());
                            html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + this.getObjectId() + "_sale2");
                            html.replace("%objectId%", this.getObjectId());
                            player.sendPacket(html);
                        } catch (Exception ignored) {
                        }

                    }
                } else if (actualCommand.equalsIgnoreCase("confirmAuction")) {
                    ClanHall ch = ClanHallManager.getInstance().getClanHall(clan.getClanHallId());
                    if (ch != null) {
                        Auction auction = ch.getAuction();
                        if (auction != null && auction.getSeller() != null) {
                            if (auction.takeItem(player, ch.getLease())) {
                                auction.confirmAuction();
                                this.showSelectedItems(player);
                                player.sendPacket(SystemMessageId.REGISTERED_FOR_CLANHALL);
                            }

                        }
                    }
                } else if (actualCommand.equalsIgnoreCase("sale2")) {
                    ClanHall ch = ClanHallManager.getInstance().getClanHallByOwner(clan);
                    if (ch != null) {
                        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                        html.setFile("data/html/auction/AgitSale2.htm");
                        html.replace("%AGIT_LAST_PRICE%", ch.getLease());
                        html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + this.getObjectId() + "_sale");
                        html.replace("%objectId%", this.getObjectId());
                        player.sendPacket(html);
                    }
                } else {
                    super.onBypassFeedback(player, command);
                }
            } else {
                this.showAuctionsList("1", player);
                player.sendPacket(SystemMessageId.CANNOT_PARTICIPATE_IN_AUCTION);
            }
        }
    }

    public void showChatWindow(Player player) {
        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
        html.setFile("data/html/auction/auction.htm");
        html.replace("%objectId%", this.getObjectId());
        html.replace("%npcId%", this.getNpcId());
        html.replace("%npcname%", this.getName());
        player.sendPacket(html);
    }

    public void showChatWindow(Player player, int val) {
        if (val != 0) {
            super.showChatWindow(player, val);
        }
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
            StringUtil.append(sb, "<tr><td><font color=\"aaaaff\">", ch.getLocation(), "</font></td><td><font color=\"ffffaa\"><a action=\"bypass -h npc_", this.getObjectId(), "_bidding ", ch.getId(), "\">", ch.getName(), " [", auction.getBidders().size(), "]</a></font></td><td>", sdf.format(auction.getEndDate()), "</td><td><font color=\"aaffff\">", auction.getMinimumBid(), "</font></td></tr>");
        }

        sb.append("</table><table width=280><tr>");

        for (int j = 1; j <= max; ++j) {
            StringUtil.append(sb, "<td><center><a action=\"bypass -h npc_", this.getObjectId(), "_list ", j, "\"> Page ", j, " </a></center></td>");
        }

        sb.append("</tr></table>");
        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
        html.setFile("data/html/auction/AgitAuctionList.htm");
        html.replace("%AGIT_LIST%", sb.toString());
        html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + this.getObjectId() + "_start");
        player.sendPacket(html);
    }

    private void showSelectedItems(Player player) {
        Clan clan = player.getClan();
        if (clan != null) {
            if (!clan.hasClanHall() && clan.getAuctionBiddedAt() > 0) {
                ClanHall ch = ClanHallManager.getInstance().getClanHall(clan.getAuctionBiddedAt());
                if (ch != null) {
                    Auction auction = ch.getAuction();
                    if (auction != null) {
                        long remainingTime = auction.getEndDate() - System.currentTimeMillis();
                        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
                        html.setFile("data/html/auction/AgitBidInfo.htm");
                        html.replace("%AGIT_NAME%", ch.getName());
                        html.replace("%AGIT_SIZE%", ch.getGrade() * 10);
                        html.replace("%AGIT_LEASE%", ch.getLease());
                        html.replace("%AGIT_LOCATION%", ch.getLocation());
                        html.replace("%AGIT_AUCTION_END%", (new SimpleDateFormat("dd-MM-yyyy HH:mm")).format(auction.getEndDate()));
                        html.replace("%AGIT_AUCTION_REMAIN%", remainingTime / 3600000L + " hours " + remainingTime / 60000L % 60L + " minutes");
                        html.replace("%AGIT_AUCTION_MYBID%", auction.getBidders().get(player.getClanId()).getBid());
                        html.replace("%AGIT_AUCTION_DESC%", ch.getDesc());
                        html.replace("%objectId%", this.getObjectId());
                        html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + this.getObjectId() + "_start");
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
                    }
                }
            } else if (clan.hasClanHall()) {
                ClanHall ch = ClanHallManager.getInstance().getClanHall(clan.getClanHallId());
                if (ch != null) {
                    Auction auction = ch.getAuction();
                    if (auction != null) {
                        NpcHtmlMessage html = new NpcHtmlMessage(this.getObjectId());
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
                            html.replace("%AGIT_AUCTION_REMAIN%", remainingTime / 3600000L + " hours " + remainingTime / 60000L % 60L + " minutes");
                            html.replace("%AGIT_AUCTION_MINBID%", seller.getBid());
                            html.replace("%AGIT_AUCTION_BIDCOUNT%", auction.getBidders().size());
                            html.replace("%AGIT_AUCTION_DESC%", ch.getDesc());
                            html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + this.getObjectId() + "_start");
                            html.replace("%id%", ch.getId());
                            html.replace("%objectId%", this.getObjectId());
                        } else {
                            html.setFile("data/html/auction/AgitInfo.htm");
                            html.replace("%AGIT_NAME%", ch.getName());
                            html.replace("%AGIT_OWNER_PLEDGE_NAME%", clan.getName());
                            html.replace("%OWNER_PLEDGE_MASTER%", clan.getLeaderName());
                            html.replace("%AGIT_SIZE%", ch.getGrade() * 10);
                            html.replace("%AGIT_LEASE%", ch.getLease());
                            html.replace("%AGIT_LOCATION%", ch.getLocation());
                            html.replace("%AGIT_LINK_BACK%", "bypass -h npc_" + this.getObjectId() + "_start");
                            html.replace("%objectId%", this.getObjectId());
                        }

                        player.sendPacket(html);
                    }
                }
            } else {
                this.showAuctionsList("1", player);
                player.sendPacket(SystemMessageId.NO_OFFERINGS_OWN_OR_MADE_BID_FOR);
            }
        }
    }
}
