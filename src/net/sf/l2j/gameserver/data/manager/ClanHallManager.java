package net.sf.l2j.gameserver.data.manager;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.model.clanhall.Auction;
import net.sf.l2j.gameserver.model.clanhall.ClanHall;
import net.sf.l2j.gameserver.model.clanhall.ClanHallFunction;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.zone.type.ClanHallZone;
import org.w3c.dom.Document;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

public class ClanHallManager implements IXmlReader {
    private static final String LOAD_CLANHALLS = "SELECT * FROM clanhall";
    private static final String LOAD_FUNCTIONS = "SELECT * FROM clanhall_functions WHERE hall_id = ?";
    private final Map<Integer, ClanHall> _clanHalls = new HashMap<>();

    private ClanHallManager() {
    }

    public static ClanHallManager getInstance() {
        return ClanHallManager.SingletonHolder.INSTANCE;
    }

    public void load() {
        this.parseFile("./data/xml/clanHalls.xml");
        LOGGER.info("Loaded {} clan halls.", this._clanHalls.size());
        Collection<ClanHallZone> zones = ZoneManager.getInstance().getAllZones(ClanHallZone.class);

        try (
                Connection con = ConnectionPool.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT * FROM clanhall");
                PreparedStatement ps2 = con.prepareStatement("SELECT * FROM clanhall_functions WHERE hall_id = ?");
                ResultSet rs = ps.executeQuery()
        ) {
            while (rs.next()) {
                int id = rs.getInt("id");
                ClanHall ch = this._clanHalls.get(id);
                if (ch != null) {
                    ClanHallZone zone = zones.stream().filter((z) -> z.getClanHallId() == id).findFirst().orElse(null);
                    if (zone == null) {
                        LOGGER.warn("No existing ClanHallZone for ClanHall {}.", id);
                    }

                    ch.setZone(zone);
                    if (ch.getDefaultBid() > 0) {
                        ch.setAuction(new Auction(ch, rs.getInt("sellerBid"), rs.getString("sellerName"), rs.getString("sellerClanName"), rs.getLong("endDate")));
                    }

                    int ownerId = rs.getInt("ownerId");
                    if (ownerId > 0) {
                        Clan clan = ClanTable.getInstance().getClan(ownerId);
                        if (clan == null) {
                            ch.free();
                        } else {
                            clan.setClanHall(id);
                            ch.setOwnerId(ownerId);
                            ch.setPaidUntil(rs.getLong("paidUntil"));
                            ch.setPaid(rs.getBoolean("paid"));
                            ch.initializeFeeTask();
                            ps2.setInt(1, id);

                            try (ResultSet rs2 = ps2.executeQuery()) {
                                while (rs2.next()) {
                                    ch.getFunctions().put(rs2.getInt("type"), new ClanHallFunction(ch, rs2.getInt("type"), rs2.getInt("lvl"), rs2.getInt("lease"), rs2.getLong("rate"), rs2.getLong("endTime")));
                                }
                            }

                            ps2.clearParameters();
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Couldn't load clan hall data.", e);
        }
    }

    public void parseDocument(Document doc, Path path) {
        this.forEach(doc, "list", (listNode) -> this.forEach(listNode, "clanhall", (armorsetNode) -> {
            StatSet set = this.parseAttributes(armorsetNode);
            this._clanHalls.put(set.getInteger("id"), new ClanHall(set));
        }));
    }

    public final ClanHall getClanHall(int id) {
        return this._clanHalls.get(id);
    }

    public final Map<Integer, ClanHall> getClanHalls() {
        return this._clanHalls;
    }

    public final List<ClanHall> getAuctionableClanHalls() {
        List<ClanHall> list = new ArrayList<>();

        for (ClanHall ch : this._clanHalls.values()) {
            Auction auction = ch.getAuction();
            if (auction != null && (ch.getOwnerId() <= 0 || auction.getSeller() != null)) {
                list.add(ch);
            }
        }

        return list;
    }

    public final List<ClanHall> getClanHallsByLocation(String location) {
        return this._clanHalls.values().stream().filter((ch) -> ch.getLocation().equalsIgnoreCase(location)).collect(Collectors.toList());
    }

    public final ClanHall getClanHallByOwner(Clan clan) {
        return this._clanHalls.values().stream().filter((ch) -> ch.getOwnerId() == clan.getClanId()).findFirst().orElse(null);
    }

    public final ClanHall getNearestClanHall(int x, int y, int maxDist) {
        for (ClanHall ch : this._clanHalls.values()) {
            if (ch.getZone() != null && ch.getZone().getDistanceToZone(x, y) < (double) maxDist) {
                return ch;
            }
        }

        return null;
    }

    public final Auction getAuction(int id) {
        ClanHall ch = this._clanHalls.get(id);
        return ch == null ? null : ch.getAuction();
    }

    private static class SingletonHolder {
        protected static final ClanHallManager INSTANCE = new ClanHallManager();
    }
}
