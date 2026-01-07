/**/
package net.sf.l2j.gameserver.data.xml;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.commons.geometry.Polygon;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.enums.DoorType;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.geoengine.geodata.ABlock;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.actor.instance.Door;
import net.sf.l2j.gameserver.model.actor.template.DoorTemplate;
import net.sf.l2j.gameserver.model.entity.Castle;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

import java.nio.file.Path;
import java.util.*;

public class DoorData implements IXmlReader {
    private final Map<Integer, Door> _doors = new HashMap();

    protected DoorData() {
        this.load();
    }

    public static DoorData getInstance() {
        return DoorData.SingletonHolder.INSTANCE;
    }

    public void load() {
        this.parseFile("./data/xml/doors.xml");
        LOGGER.info("Loaded {} doors templates.", this._doors.size());
    }

    public void parseDocument(Document doc, Path path) {
        this.forEach(doc, "list", (listNode) -> {
            this.forEach(listNode, "door", (doorNode) -> {
                StatSet set = this.parseAttributes(doorNode);
                int id = set.getInteger("id");
                this.forEach(doorNode, "castle", (castleNode) -> {
                    set.set("castle", this.parseString(castleNode.getAttributes(), "id"));
                });
                this.forEach(doorNode, "position", (positionNode) -> {
                    NamedNodeMap attrs = positionNode.getAttributes();
                    set.set("posX", this.parseInteger(attrs, "x"));
                    set.set("posY", this.parseInteger(attrs, "y"));
                    set.set("posZ", this.parseInteger(attrs, "z"));
                });
                List<int[]> coords = new ArrayList();
                this.forEach(doorNode, "coordinates", (coordinatesNode) -> {
                    this.forEach(coordinatesNode, "loc", (locNode) -> {
                        NamedNodeMap attrs = locNode.getAttributes();
                        coords.add(new int[]{this.parseInteger(attrs, "x"), this.parseInteger(attrs, "y")});
                    });
                });
                int minX = Integer.MAX_VALUE;
                int maxX = Integer.MIN_VALUE;
                int minY = Integer.MAX_VALUE;
                int maxY = Integer.MIN_VALUE;

                int[] coord;
                for (Iterator var9 = coords.iterator(); var9.hasNext(); maxY = Math.max(maxY, coord[1])) {
                    coord = (int[]) var9.next();
                    minX = Math.min(minX, coord[0]);
                    maxX = Math.max(maxX, coord[0]);
                    minY = Math.min(minY, coord[1]);
                }

                this.forEach(doorNode, "stats|function", (node) -> {
                    set.putAll(this.parseAttributes(node));
                });
                int posX = set.getInteger("posX");
                int posY = set.getInteger("posY");
                int posZ = set.getInteger("posZ");
                int x = GeoEngine.getGeoX(minX) - 1;
                int y = GeoEngine.getGeoY(minY) - 1;
                int sizeX = GeoEngine.getGeoX(maxX) + 1 - x + 1;
                int sizeY = GeoEngine.getGeoY(maxY) + 1 - y + 1;
                int geoX = GeoEngine.getGeoX(posX);
                int geoY = GeoEngine.getGeoY(posY);
                int geoZ = GeoEngine.getInstance().getHeightNearest(geoX, geoY, posZ);
                ABlock block = GeoEngine.getInstance().getBlock(geoX, geoY);
                int i = block.getIndexAbove(geoX, geoY, geoZ);
                int limit;
                if (i != -1) {
                    limit = block.getHeight(i) - geoZ;
                    if (set.getInteger("height") > limit) {
                        set.set("height", limit - 48);
                    }
                }

                limit = set.getEnum("type", DoorType.class) == DoorType.WALL ? 192 : 48;
                boolean[][] inside = new boolean[sizeX][sizeY];
                Polygon polygon = new Polygon(id, coords);

                for (int ix = 0; ix < sizeX; ++ix) {
                    label56:
                    for (int iy = 0; iy < sizeY; ++iy) {
                        int gx = x + ix;
                        int gy = y + iy;
                        int z = GeoEngine.getInstance().getHeightNearest(gx, gy, posZ);
                        if (Math.abs(z - posZ) <= limit) {
                            int worldX = GeoEngine.getWorldX(gx);
                            int worldY = GeoEngine.getWorldY(gy);

                            for (int wix = worldX - 6; wix <= worldX + 6; wix += 2) {
                                for (int wiy = worldY - 6; wiy <= worldY + 6; wiy += 2) {
                                    if (polygon.isInside(wix, wiy)) {
                                        inside[ix][iy] = true;
                                        continue label56;
                                    }
                                }
                            }
                        }
                    }
                }

                set.set("geoX", x);
                set.set("geoY", y);
                set.set("geoZ", geoZ);
                set.set("geoData", GeoEngine.calculateGeoObject(inside));
                set.set("pAtk", 0);
                set.set("mAtk", 0);
                set.set("runSpd", 0);
                set.set("radius", 16);
                DoorTemplate template = new DoorTemplate(set);
                Door door = new Door(IdFactory.getInstance().getNextId(), template);
                door.setCurrentHpMp(door.getMaxHp(), door.getMaxMp());
                door.getPosition().set(posX, posY, posZ);
                this._doors.put(door.getDoorId(), door);
            });
        });
    }

    public final void reload() {
        Iterator var1 = this._doors.values().iterator();

        while (var1.hasNext()) {
            Door door = (Door) var1.next();
            door.openMe();
        }

        this._doors.clear();
        var1 = CastleManager.getInstance().getCastles().iterator();

        while (var1.hasNext()) {
            Castle castle = (Castle) var1.next();
            castle.getDoors().clear();
        }

        this.load();
        this.spawn();
    }

    public final void spawn() {
        Iterator var1 = this._doors.values().iterator();

        while (var1.hasNext()) {
            Door door = (Door) var1.next();
            door.initResidences();
            door.spawnMe();
        }

        var1 = CastleManager.getInstance().getCastles().iterator();

        while (var1.hasNext()) {
            Castle castle = (Castle) var1.next();
            castle.loadDoorUpgrade();
        }

    }

    public Door getDoor(int id) {
        return this._doors.get(id);
    }

    public Collection<Door> getDoors() {
        return this._doors.values();
    }


    public Door getDoor(String name) {
        return _doors.values().stream().filter(d -> d.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    private static class SingletonHolder {
        protected static final DoorData INSTANCE = new DoorData();
    }
}