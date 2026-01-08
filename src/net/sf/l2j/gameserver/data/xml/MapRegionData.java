/**/
package net.sf.l2j.gameserver.data.xml;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.data.manager.ClanHallManager;
import net.sf.l2j.gameserver.data.manager.ZoneManager;
import net.sf.l2j.gameserver.enums.SiegeSide;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.enums.actors.ClassRace;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.clanhall.ClanHall;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.zone.type.ArenaZone;
import net.sf.l2j.gameserver.model.zone.type.ClanHallZone;
import net.sf.l2j.gameserver.model.zone.type.TownZone;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

import java.nio.file.Path;

public class MapRegionData implements IXmlReader {
    protected static final MapRegionData INSTANCE = new MapRegionData();
    private static final int REGIONS_X = 11;
    private static final int REGIONS_Y = 16;
    private static final Location MDT_LOCATION = new Location(12661, 181687, -3560);
    private final int[][] _regions = new int[REGIONS_X][REGIONS_Y];

    protected MapRegionData() {
    }

    public static int getMapRegionX(int posX) {
        return (posX >> 15) + 4;
    }

    public static int getMapRegionY(int posY) {
        return (posY >> 15) + 8;
    }

    public static TownZone getTown(int townId) {
        return ZoneManager.getInstance().getAllZones(TownZone.class).stream().filter((t) -> {
            return t.getTownId() == townId;
        }).findFirst().orElse(null);
    }

    public static TownZone getTown(int x, int y, int z) {
        return ZoneManager.getInstance().getZone(x, y, z, TownZone.class);
    }

    public static MapRegionData getInstance() {
        return MapRegionData.INSTANCE;
    }

    public void load() {
        this.parseFile("./data/xml/mapRegions.xml");
        LOGGER.info("Loaded regions.");
    }

    public void parseDocument(Document doc, Path path) {
        this.forEach(doc, "list", (listNode) -> {
            this.forEach(listNode, "map", (mapNode) -> {
                NamedNodeMap attrs = mapNode.getAttributes();
                int rY = this.parseInteger(attrs, "geoY") - 10;

                for (int rX = 0; rX < 11; ++rX) {
                    this._regions[rX][rY] = this.parseInteger(attrs, "geoX_" + (rX + 16));
                }

            });
        });
    }

    public final int getMapRegion(int posX, int posY) {
        return this._regions[getMapRegionX(posX)][getMapRegionY(posY)];
    }

    public final int getAreaCastle(int x, int y) {
        return switch (this.getMapRegion(x, y)) {
            case 0, 5, 6 -> 1;
            case 1, 2, 9, 17 -> 4;
            case 3, 4, 16 -> 9;
            case 7 -> 2;
            case 8, 12 -> 3;
            case 13 -> 6;
            case 14, 18 -> 8;
            case 15 -> 7;
            default -> 5;
        };
    }

    public String getClosestTownName(int x, int y) {
        return switch (this.getMapRegion(x, y)) {
            case 0 -> "Talking Island Village";
            case 1 -> "Elven Village";
            case 2 -> "Dark Elven Village";
            case 3 -> "Orc Village";
            case 4 -> "Dwarven Village";
            case 5 -> "Town of Gludio";
            case 6 -> "Gludin Village";
            case 7 -> "Town of Dion";
            case 8 -> "Town of Giran";
            case 9 -> "Town of Oren";
            case 10 -> "Town of Aden";
            case 11 -> "Hunters Village";
            case 12 -> "Giran Harbor";
            case 13 -> "Heine";
            case 14 -> "Rune Township";
            case 15 -> "Town of Goddard";
            case 16 -> "Town of Schuttgart";
            case 17 -> "Floran Village";
            case 18 -> "Primeval Isle";
            default -> "Town of Aden";
        };
    }

    public Location getLocationToTeleport(Creature creature, MapRegionData.TeleportType teleportType) {
        if (!(creature instanceof Player player)) {
            return this.getClosestTown(creature).getRandomLoc();
        } else {
            if (player.isInsideZone(ZoneId.MONSTER_TRACK)) {
                return MDT_LOCATION;
            } else {
                Castle castle;
                if (teleportType != MapRegionData.TeleportType.TOWN && player.getClan() != null) {
                    if (teleportType == MapRegionData.TeleportType.CLAN_HALL) {
                        ClanHall ch = ClanHallManager.getInstance().getClanHallByOwner(player.getClan());
                        if (ch != null) {
                            ClanHallZone zone = ch.getZone();
                            if (zone != null) {
                                return zone.getRandomLoc();
                            }
                        }
                    } else if (teleportType == MapRegionData.TeleportType.CASTLE) {
                        castle = CastleManager.getInstance().getCastleByOwner(player.getClan());
                        if (castle == null) {
                            castle = CastleManager.getInstance().getCastle(player);
                            if (castle == null || !castle.getSiege().isInProgress() || !castle.getSiege().checkSides(player.getClan(), SiegeSide.DEFENDER, SiegeSide.OWNER)) {
                                castle = null;
                            }
                        }

                        if (castle != null && castle.getCastleId() > 0) {
                            return castle.getCastleZone().getRandomLoc();
                        }
                    } else if (teleportType == MapRegionData.TeleportType.SIEGE_FLAG) {
                        Siege siege = CastleManager.getInstance().getActiveSiege(player);
                        if (siege != null) {
                            Npc flag = siege.getFlag(player.getClan());
                            if (flag != null) {
                                return flag.getPosition();
                            }
                        }
                    }
                }

                castle = CastleManager.getInstance().getCastle(player);
                if (castle != null && castle.getSiege().isInProgress()) {
                    return player.getKarma() > 0 ? castle.getSiegeZone().getRandomChaoticLoc() : castle.getSiegeZone().getRandomLoc();
                } else if (player.getKarma() > 0) {
                    return this.getClosestTown(player).getRandomChaoticLoc();
                } else {
                    ArenaZone arena = ZoneManager.getInstance().getZone(player, ArenaZone.class);
                    return arena != null ? arena.getRandomLoc() : this.getClosestTown(player).getRandomLoc();
                }
            }
        }
    }

    private TownZone getClosestTown(Creature creature) {
        return switch (this.getMapRegion(creature.getX(), creature.getY())) {
            case 0 -> getTown(2);
            case 1 ->
                    getTown(creature instanceof Player && ((Player) creature).getTemplate().getRace() == ClassRace.DARK_ELF ? 1 : 3);
            case 2 ->
                    getTown(creature instanceof Player && ((Player) creature).getTemplate().getRace() == ClassRace.ELF ? 3 : 1);
            case 3 -> getTown(4);
            case 4 -> getTown(6);
            case 5 -> getTown(7);
            case 6 -> getTown(5);
            case 7 -> getTown(8);
            case 8, 12 -> getTown(9);
            case 9 -> getTown(10);
            case 10 -> getTown(12);
            case 11 -> getTown(11);
            case 13 -> getTown(15);
            case 14 -> getTown(14);
            case 15 -> getTown(13);
            case 16 -> getTown(17);
            case 17 -> getTown(16);
            case 18 -> getTown(19);
            default -> getTown(16);
        };
    }

    public final int getClosestLocation(int x, int y) {
        return switch (this.getMapRegion(x, y)) {
            case 0 -> 1;
            case 1 -> 4;
            case 2 -> 3;
            case 3, 4, 16 -> 9;
            case 5, 6 -> 2;
            case 7 -> 5;
            case 8, 12 -> 6;
            case 9 -> 10;
            case 10 -> 13;
            case 11 -> 11;
            case 13 -> 12;
            case 14 -> 14;
            case 15 -> 15;
            default -> 0;
        };
    }

    public final String getPictureName(int x, int y) {
        return switch (this.getMapRegion(x, y)) {
            case 5 -> "GLUDIO";
            case 6 -> "GLUDIN";
            case 7 -> "DION";
            case 8 -> "GIRAN";
            case 14 -> "RUNE";
            case 15 -> "GODARD";
            case 16 -> "SCHUTTGART";
            default -> "ADEN";
        };
    }

    public enum TeleportType {
        CASTLE,
        CLAN_HALL,
        SIEGE_FLAG,
        TOWN;

    }

}