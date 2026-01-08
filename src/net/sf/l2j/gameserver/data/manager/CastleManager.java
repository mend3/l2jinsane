package net.sf.l2j.gameserver.data.manager;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.enums.CabalType;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.item.MercenaryTicket;
import net.sf.l2j.gameserver.model.location.SpawnLocation;
import net.sf.l2j.gameserver.model.location.TowerSpawnLocation;
import net.sf.l2j.gameserver.model.pledge.Clan;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class CastleManager implements IXmlReader {
    private static final String LOAD_CASTLES = "SELECT * FROM castle ORDER BY id";
    private static final String LOAD_OWNER = "SELECT clan_id FROM clan_data WHERE hasCastle=?";
    private static final String RESET_CERTIFICATES = "UPDATE castle SET certificates=300";
    private final Map<Integer, Castle> _castles = new HashMap<>();

    private CastleManager() {
    }

    public static final CastleManager getInstance() {
        return CastleManager.SingletonHolder.INSTANCE;
    }

    public void load() {
        try (
                Connection con = ConnectionPool.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT * FROM castle ORDER BY id");
                ResultSet rs = ps.executeQuery();
        ) {
            while (rs.next()) {
                int id = rs.getInt("id");
                Castle castle = new Castle(id, rs.getString("name"));
                castle.setSiegeDate(Calendar.getInstance());
                castle.getSiegeDate().setTimeInMillis(rs.getLong("siegeDate"));
                castle.setTimeRegistrationOver(rs.getBoolean("regTimeOver"));
                castle.setTaxPercent(rs.getInt("taxPercent"), false);
                castle.setTreasury(rs.getLong("treasury"));
                castle.setLeftCertificates(rs.getInt("certificates"), false);

                try (PreparedStatement ps1 = con.prepareStatement("SELECT clan_id FROM clan_data WHERE hasCastle=?")) {
                    ps1.setInt(1, id);

                    try (ResultSet rs1 = ps1.executeQuery()) {
                        while (rs1.next()) {
                            int ownerId = rs1.getInt("clan_id");
                            if (ownerId > 0) {
                                Clan clan = ClanTable.getInstance().getClan(ownerId);
                                if (clan != null) {
                                    castle.setOwnerId(ownerId);
                                }
                            }
                        }
                    }
                }
                this._castles.put(id, castle);
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load castles.", e);
        }

        this.parseFile("./data/xml/castles.xml");
        LOGGER.info("Loaded {} castles.", new Object[]{this._castles.size()});

        for (Castle castle : this._castles.values()) {
            castle.loadTrapUpgrade();
            castle.setSiege(new Siege(castle));
        }
    }

    public void parseDocument(Document doc, Path path) {
        this.forEach(doc, "list", (listNode) -> this.forEach(listNode, "castle", (castleNode) -> {
            NamedNodeMap attrs = castleNode.getAttributes();
            Castle castle = this._castles.get(this.parseInteger(attrs, "id"));
            if (castle != null) {
                castle.setCircletId(this.parseInteger(attrs, "circletId"));
                this.forEach(castleNode, "artifact", (artifactNode) -> castle.setArtifacts(this.parseString(artifactNode.getAttributes(), "val")));
                this.forEach(castleNode, "controlTowers", (controlTowersNode) -> this.forEach(controlTowersNode, "tower", (towerNode) -> {
                    String[] location = this.parseString(towerNode.getAttributes(), "loc").split(",");
                    castle.getControlTowers().add(new TowerSpawnLocation(13002, new SpawnLocation(Integer.parseInt(location[0]), Integer.parseInt(location[1]), Integer.parseInt(location[2]), -1)));
                }));
                this.forEach(castleNode, "flameTowers", (flameTowersNode) -> this.forEach(flameTowersNode, "tower", (towerNode) -> {
                    NamedNodeMap towerAttrs = towerNode.getAttributes();
                    String[] location = this.parseString(towerAttrs, "loc").split(",");
                    castle.getFlameTowers().add(new TowerSpawnLocation(13004, new SpawnLocation(Integer.parseInt(location[0]), Integer.parseInt(location[1]), Integer.parseInt(location[2]), -1), this.parseString(towerAttrs, "zones").split(",")));
                }));
                this.forEach(castleNode, "relatedNpcIds", (relatedNpcIdsNode) -> castle.setRelatedNpcIds(this.parseString(relatedNpcIdsNode.getAttributes(), "val")));
                this.forEach(castleNode, "tickets", (ticketsNode) -> this.forEach(ticketsNode, "ticket", (ticketNode) -> castle.getTickets().add(new MercenaryTicket(this.parseAttributes(ticketNode)))));
            }

        }));
    }

    public Castle getCastleById(int castleId) {
        return this._castles.get(castleId);
    }

    public Castle getCastleByOwner(Clan clan) {
        return this._castles.values().stream().filter((c) -> c.getOwnerId() == clan.getClanId()).findFirst().orElse(null);
    }

    public Castle getCastleByName(String name) {
        return this._castles.values().stream().filter((c) -> c.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Castle getCastle(int x, int y, int z) {
        return this._castles.values().stream().filter((c) -> c.checkIfInZone(x, y, z)).findFirst().orElse(null);
    }

    public Castle getCastle(WorldObject object) {
        return this.getCastle(object.getX(), object.getY(), object.getZ());
    }

    public Collection<Castle> getCastles() {
        return this._castles.values();
    }

    public void validateTaxes(CabalType sealStrifeOwner) {
        int maxTax;
        switch (sealStrifeOwner) {
            case DAWN -> maxTax = 25;
            case DUSK -> maxTax = 5;
            default -> maxTax = 15;
        }

        this._castles.values().stream().filter((c) -> c.getTaxPercent() > maxTax).forEach((c) -> c.setTaxPercent(maxTax, true));
    }

    public Siege getActiveSiege(WorldObject object) {
        return this.getActiveSiege(object.getX(), object.getY(), object.getZ());
    }

    public Siege getActiveSiege(int x, int y, int z) {
        for (Castle castle : this._castles.values()) {
            if (castle.getSiege().checkIfInZone(x, y, z)) {
                return castle.getSiege();
            }
        }

        return null;
    }

    public void resetCertificates() {
        for (Castle castle : this._castles.values()) {
            castle.setLeftCertificates(300, false);
        }

        try (
                Connection con = ConnectionPool.getConnection();
                PreparedStatement ps = con.prepareStatement("UPDATE castle SET certificates=300");
        ) {
            ps.executeUpdate();
        } catch (Exception e) {
            LOGGER.error("Failed to reset certificates.", e);
        }

    }

    private static final class SingletonHolder {
        protected static final CastleManager INSTANCE = new CastleManager();
    }
}
