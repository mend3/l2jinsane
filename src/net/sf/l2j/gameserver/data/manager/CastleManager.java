package net.sf.l2j.gameserver.data.manager;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.enums.CabalType;
import net.sf.l2j.gameserver.enums.SpawnType;
import net.sf.l2j.gameserver.enums.TowerType;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.location.ArtifactSpawnLocation;
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

    @SuppressWarnings("SqlWithoutWhere")
    private static final String RESET_CERTIFICATES = "UPDATE castle SET certificates=300";

    private final Map<Integer, Castle> _castles = new HashMap<>();

    private CastleManager() {
    }

    public static CastleManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public static String getCastleName(int id) {
        return switch (id) {
            case 1 -> "Gludio";
            case 2 -> "Dion";
            case 3 -> "Giran";
            case 4 -> "Oren";
            case 5 -> "Aden";
            case 6 -> "Innadril";
            case 7 -> "Goddard";
            case 8 -> "Rune";
            case 9 -> "Schuttgart";
            default -> "Not found";
        };
    }

    public void load() {
        parseFile("./data/xml/castles.xml");
        LOGGER.info("Loaded {} castles.", this._castles.size());
        try {
            Connection con = ConnectionPool.getConnection();
            PreparedStatement ps = con.prepareStatement(LOAD_CASTLES);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                final Castle castle = _castles.get(rs.getInt("id"));

                castle.setSiegeDate(Calendar.getInstance());
                castle.getSiegeDate().setTimeInMillis(rs.getLong("siegeDate"));
                castle.setTimeRegistrationOver(rs.getBoolean("regTimeOver"));
                castle.setTaxPercent(rs.getInt("taxPercent"), false);
                castle.setTreasury(rs.getLong("treasury"));
                castle.setLeftCertificates(rs.getInt("certificates"), false);
                PreparedStatement ps1 = con.prepareStatement(LOAD_OWNER);
                ps1.setInt(1, id);
                ResultSet rs1 = ps1.executeQuery();
                while (rs1.next()) {
                    int ownerId = rs1.getInt("clan_id");
                    if (ownerId > 0) {
                        Clan clan = ClanTable.getInstance().getClan(ownerId);
                        if (clan != null)
                            castle.setOwnerId(ownerId);
                    }
                }
                rs1.close();
                ps1.close();
                this._castles.put(id, castle);
            }
            ps.close();
            con.close();
        } catch (Exception e) {
            LOGGER.error("Failed to load castles.", e);
        }
        for (Castle castle : this._castles.values()) {
            castle.loadTrapUpgrade();
            castle.setSiege(new Siege(castle));
        }
    }

    public void parseDocument(Document doc, Path path) {
        forEach(doc, "list", listNode -> forEach(listNode, "castle", castleNode ->
        {
            final StatSet set = parseAttributes(castleNode);
            forEach(castleNode, "tax", taxNode -> addAttributes(set, taxNode.getAttributes()));

            final Castle castle = new Castle(set);

            forEach(castleNode, "artifacts", artifactsNode -> forEach(artifactsNode, "artifact", artifactNode ->
            {
                final NamedNodeMap artifactAttrs = artifactNode.getAttributes();
                final int npcId = parseInteger(artifactAttrs, "id");
                final SpawnLocation pos = parseSpawnLocation(artifactAttrs, "pos");

                final ArtifactSpawnLocation asl = new ArtifactSpawnLocation(npcId, castle);
                asl.set(pos);

                castle.getArtifacts().add(asl);
            }));
            forEach(castleNode, "controlTowers", controlTowersNode -> forEach(controlTowersNode, "controlTower", towerNode ->
            {
                final NamedNodeMap towerAttrs = towerNode.getAttributes();
                final String alias = parseString(towerAttrs, "alias");
                final TowerType type = parseEnum(towerAttrs, TowerType.class, "type");

                final TowerSpawnLocation tsl = new TowerSpawnLocation(type, alias, castle);

                forEach(towerNode, "position", positionNode ->
                {
                    final NamedNodeMap attrs = positionNode.getAttributes();
                    tsl.set(parseInteger(attrs, "x"), parseInteger(attrs, "y"), parseInteger(attrs, "z"));
                });
                forEach(towerNode, "stats", statNode ->
                {
                    final NamedNodeMap attrs = statNode.getAttributes();
                    tsl.setStats(parseDouble(attrs, "hp"), parseDouble(attrs, "pDef"), parseDouble(attrs, "mDef"));
                });
                forEach(towerNode, "zones", zoneNode -> tsl.setZones(parseString(zoneNode.getAttributes(), "val").split(";")));

                castle.getControlTowers().add(tsl);
            }));
            forEach(castleNode, "gates", gatesNode -> castle.setDoors(parseString(gatesNode.getAttributes(), "val")));
            forEach(castleNode, "npcs", npcsNode -> castle.setNpcs(parseString(npcsNode.getAttributes(), "val")));
            forEach(castleNode, "spawns", spawnsNode -> forEach(spawnsNode, "spawn", spawnNode -> castle.addSpawn(parseEnum(spawnNode.getAttributes(), SpawnType.class, "type"), parseLocation(spawnNode))));
            forEach(castleNode, "tickets", ticketsNode -> forEach(ticketsNode, "ticket", ticketNode -> castle.addTicket(parseAttributes(ticketNode))));

            // Feed castles Map.
            _castles.put(castle.getId(), castle);
        }));
    }

    public Castle getCastleById(int castleId) {
        return this._castles.get(castleId);
    }

    public Castle getCastleByOwner(Clan clan) {
        return this._castles.values().stream().filter(c -> (c.getOwnerId() == clan.getClanId())).findFirst().orElse(null);
    }

    public Castle getCastleByName(String name) {
        return this._castles.values().stream().filter(c -> c.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public Castle getCastle(int x, int y, int z) {
        return this._castles.values().stream().filter(c -> c.checkIfInZone(x, y, z)).findFirst().orElse(null);
    }

    public Castle getCastle(WorldObject object) {
        return getCastle(object.getX(), object.getY(), object.getZ());
    }

    public Collection<Castle> getCastles() {
        return this._castles.values();
    }

    public void validateTaxes(CabalType sealStrifeOwner) {
        int maxTax = switch (sealStrifeOwner) {
            case DAWN -> 25;
            case DUSK -> 5;
            default -> 15;
        };
        this._castles.values().stream().filter(c -> (c.getTaxPercent() > maxTax)).forEach(c -> c.setTaxPercent(maxTax, true));
    }

    public Siege getActiveSiege(WorldObject object) {
        return getActiveSiege(object.getX(), object.getY(), object.getZ());
    }

    public Siege getActiveSiege(int x, int y, int z) {
        for (Castle castle : this._castles.values()) {
            if (castle.getSiege().checkIfInZone(x, y, z))
                return castle.getSiege();
        }
        return null;
    }

    public void resetCertificates() {
        for (Castle castle : this._castles.values())
            castle.setLeftCertificates(300, false);
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement(RESET_CERTIFICATES);
                try {
                    ps.executeUpdate();
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
            LOGGER.error("Failed to reset certificates.", e);
        }
    }

    private static final class SingletonHolder {
        private static final CastleManager INSTANCE = new CastleManager();
    }
}
