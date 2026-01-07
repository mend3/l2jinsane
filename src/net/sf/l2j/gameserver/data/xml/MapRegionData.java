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
    private final int[][] _regions = new int[11][16];

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
        switch (this.getMapRegion(x, y)) {
            case 0:
            case 5:
            case 6:
                return 1;
            case 1:
            case 2:
            case 9:
            case 17:
                return 4;
            case 3:
            case 4:
            case 16:
                return 9;
            case 7:
                return 2;
            case 8:
            case 12:
                return 3;
            case 10:
            case 11:
            default:
                return 5;
            case 13:
                return 6;
            case 14:
            case 18:
                return 8;
            case 15:
                return 7;
        }
    }

    public String getClosestTownName(int x, int y) {
        switch (this.getMapRegion(x, y)) {
            case 0:
                return "Talking Island Village";
            case 1:
                return "Elven Village";
            case 2:
                return "Dark Elven Village";
            case 3:
                return "Orc Village";
            case 4:
                return "Dwarven Village";
            case 5:
                return "Town of Gludio";
            case 6:
                return "Gludin Village";
            case 7:
                return "Town of Dion";
            case 8:
                return "Town of Giran";
            case 9:
                return "Town of Oren";
            case 10:
                return "Town of Aden";
            case 11:
                return "Hunters Village";
            case 12:
                return "Giran Harbor";
            case 13:
                return "Heine";
            case 14:
                return "Rune Township";
            case 15:
                return "Town of Goddard";
            case 16:
                return "Town of Schuttgart";
            case 17:
                return "Floran Village";
            case 18:
                return "Primeval Isle";
            default:
                return "Town of Aden";
        }
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

    private final TownZone getClosestTown(Creature creature) {
        switch (this.getMapRegion(creature.getX(), creature.getY())) {
            case 0:
                return getTown(2);
            case 1:
                return getTown(creature instanceof Player && ((Player) creature).getTemplate().getRace() == ClassRace.DARK_ELF ? 1 : 3);
            case 2:
                return getTown(creature instanceof Player && ((Player) creature).getTemplate().getRace() == ClassRace.ELF ? 3 : 1);
            case 3:
                return getTown(4);
            case 4:
                return getTown(6);
            case 5:
                return getTown(7);
            case 6:
                return getTown(5);
            case 7:
                return getTown(8);
            case 8:
            case 12:
                return getTown(9);
            case 9:
                return getTown(10);
            case 10:
                return getTown(12);
            case 11:
                return getTown(11);
            case 13:
                return getTown(15);
            case 14:
                return getTown(14);
            case 15:
                return getTown(13);
            case 16:
                return getTown(17);
            case 17:
                return getTown(16);
            case 18:
                return getTown(19);
            default:
                return getTown(16);
        }
    }

    public final int getClosestLocation(int x, int y) {
        switch (this.getMapRegion(x, y)) {
            case 0:
                return 1;
            case 1:
                return 4;
            case 2:
                return 3;
            case 3:
            case 4:
            case 16:
                return 9;
            case 5:
            case 6:
                return 2;
            case 7:
                return 5;
            case 8:
            case 12:
                return 6;
            case 9:
                return 10;
            case 10:
                return 13;
            case 11:
                return 11;
            case 13:
                return 12;
            case 14:
                return 14;
            case 15:
                return 15;
            default:
                return 0;
        }
    }

    public final String getPictureName(int x, int y) {
        switch (this.getMapRegion(x, y)) {
            case 5:
                return "GLUDIO";
            case 6:
                return "GLUDIN";
            case 7:
                return "DION";
            case 8:
                return "GIRAN";
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            default:
                return "ADEN";
            case 14:
                return "RUNE";
            case 15:
                return "GODARD";
            case 16:
                return "SCHUTTGART";
        }
    }

    public enum TeleportType {
        CASTLE,
        CLAN_HALL,
        SIEGE_FLAG,
        TOWN;

        // $FF: synthetic method
        private static MapRegionData.TeleportType[] $values() {
            return new MapRegionData.TeleportType[]{CASTLE, CLAN_HALL, SIEGE_FLAG, TOWN};
        }
    }

}