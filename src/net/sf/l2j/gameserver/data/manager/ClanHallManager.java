package net.sf.l2j.gameserver.data.manager;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.enums.SpawnType;
import net.sf.l2j.gameserver.model.clanhall.Auction;
import net.sf.l2j.gameserver.model.clanhall.ClanHall;
import net.sf.l2j.gameserver.model.clanhall.ClanHallFunction;
import net.sf.l2j.gameserver.model.clanhall.SiegableHall;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.zone.type.ClanHallZone;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class ClanHallManager implements IXmlReader {
    private static final String LOAD_CLANHALLS = "SELECT * FROM clanhall";

    private static final String LOAD_FUNCTIONS = "SELECT * FROM clanhall_functions WHERE hall_id = ?";

    private final Map<Integer, ClanHall> _clanHalls = new HashMap<>();

    protected ClanHallManager() {
    }

    public static ClanHallManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void load() {
        parseFile("./data/xml/clanHalls.xml");
        LOGGER.info("Loaded {} clan halls.", this._clanHalls.size());

        // Add dynamic data.
        try (Connection con = ConnectionPool.getConnection();
             PreparedStatement ps = con.prepareStatement(LOAD_CLANHALLS);
             PreparedStatement ps2 = con.prepareStatement(LOAD_FUNCTIONS);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                final int id = rs.getInt("id");

                final ClanHall ch = _clanHalls.get(id);
                if (ch == null)
                    continue;

                // Find the related zone, and associate it with the Clan Hall.
                final ClanHallZone zone = ZoneManager.getInstance().getAllZones(ClanHallZone.class).stream().filter(z -> z.getClanHallId() == id).findFirst().orElse(null);
                if (zone == null)
                    LOGGER.warn("No existing ClanHallZone for ClanHall {}.", id);

                // A default bid exists, it means it's a regular Clan Hall. Generate an Auction.
                if (ch.getAuctionMin() > 0)
                    ch.setAuction(new Auction(ch, rs.getInt("sellerBid"), rs.getString("sellerName"), rs.getString("sellerClanName"), rs.getLong("endDate")));
                    // No default bid ; it's actually a Siegable Hall.
                else {
                    // Test siege date, registered as end date.
                    long nextSiege = rs.getLong("endDate");
                    if (nextSiege - System.currentTimeMillis() < 0)
                        ((SiegableHall) ch).updateNextSiege();
                    else {
                        final Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(nextSiege);

                        ((SiegableHall) ch).setNextSiegeDate(cal);
                    }
                }

                // Feed the zone.
                ch.setZone(zone);

                final int ownerId = rs.getInt("ownerId");
                if (ownerId > 0) {
                    final Clan clan = ClanTable.getInstance().getClan(ownerId);
                    if (clan == null) {
                        ch.free();
                        continue;
                    }

                    // Set Clan variable.
                    clan.setClanHall(id);

                    // Set ClanHall variables.
                    ch.setOwnerId(ownerId);
                    ch.setPaidUntil(rs.getLong("paidUntil"));
                    ch.setPaid(rs.getBoolean("paid"));

                    // Initialize the fee task.
                    ch.initializeFeeTask();

                    // Load related ClanHallFunctions.
                    ps2.setInt(1, id);

                    try (ResultSet rs2 = ps2.executeQuery()) {
                        while (rs2.next())
                            ch.getFunctions().put(rs2.getInt("type"), new ClanHallFunction(ch, rs2.getInt("type"), rs2.getInt("lvl"), rs2.getInt("lease"), rs2.getLong("rate"), rs2.getLong("endTime")));
                    }
                    ps2.clearParameters();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Couldn't load clan hall data.", e);
        }
    }

    public void parseDocument(Document doc, Path path) {
        Node root = doc.getFirstChild();
        for (Node chNode = root.getFirstChild(); chNode != null; chNode = chNode.getNextSibling()) {
            if ("clanHall".equalsIgnoreCase(chNode.getNodeName())) {
                final StatSet set = parseAttributes(chNode);
                forEach(chNode, "agit", agitNode -> addAttributes(set, agitNode.getAttributes()));
                forEach(chNode, "tax", taxNode -> addAttributes(set, taxNode.getAttributes()));

                final ClanHall ch = (set.containsKey("siegeLength")) ? new SiegableHall(set) : new ClanHall(set);

                forEach(chNode, "gates", gatesNode -> ch.setDoors(parseString(gatesNode.getAttributes(), "val")));
                forEach(chNode, "npcs", npcsNode -> ch.setNpcs(parseString(npcsNode.getAttributes(), "val")));
                forEach(chNode, "spawns", spawnsNode -> forEach(spawnsNode, "spawn", spawnNode -> ch.addSpawn(parseEnum(spawnNode.getAttributes(), SpawnType.class, "type"), parseLocation(spawnNode))));

                _clanHalls.put(set.getInteger("id"), ch);
            }
        }
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
            if (auction == null)
                continue;
            if (ch.getOwnerId() > 0 && auction.getSeller() == null)
                continue;
            list.add(ch);
        }
        return list;
    }


    /**
     * @return a {@link List} with all {@link SiegableHall}s.
     */
    public List<SiegableHall> getSiegableHalls() {
        return _clanHalls.values().stream().filter(SiegableHall.class::isInstance).map(SiegableHall.class::cast).toList();
    }

    public final boolean isClanParticipating(Clan clan) {
        for (SiegableHall hall : getSiegableHalls()) {
            if (hall.getSiege() != null && hall.getSiege().getAttackerClans().contains(clan))
                return true;
        }
        return false;
    }

    public final ClanHall getClanHallByOwner(Clan clan) {
        return this._clanHalls.values().stream().filter(ch -> (ch.getOwnerId() == clan.getClanId())).findFirst().orElse(null);
    }

    public final List<ClanHall> getClanHallsByLocation(String loc) {
        return this._clanHalls.values().stream().filter(ch -> ch.getLocation().equalsIgnoreCase(loc)).toList();
    }

    public final ClanHall getNearestClanHall(int x, int y, int maxDist) {
        for (ClanHall ch : this._clanHalls.values()) {
            if (ch.getZone() != null && ch.getZone().getDistanceToZone(x, y) < maxDist)
                return ch;
        }
        return null;
    }

    public final Auction getAuction(int id) {
        ClanHall ch = this._clanHalls.get(id);
        return (ch == null) ? null : ch.getAuction();
    }

    private static class SingletonHolder {
        protected static final ClanHallManager INSTANCE = new ClanHallManager();
    }
}
