/**/
package net.sf.l2j.gameserver.data.manager;

import net.sf.l2j.commons.data.xml.IXmlReader;
import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.WorldRegion;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.zone.SpawnZoneType;
import net.sf.l2j.gameserver.model.zone.ZoneType;
import net.sf.l2j.gameserver.model.zone.form.ZoneCuboid;
import net.sf.l2j.gameserver.model.zone.form.ZoneCylinder;
import net.sf.l2j.gameserver.model.zone.form.ZoneNPoly;
import net.sf.l2j.gameserver.model.zone.type.BossZone;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.lang.reflect.Constructor;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ZoneManager implements IXmlReader {
    private static final String DELETE_GRAND_BOSS_LIST = "DELETE FROM grandboss_list";
    private static final String INSERT_GRAND_BOSS_LIST = "INSERT INTO grandboss_list (player_id,zone) VALUES (?,?)";
    private final Map<Class<? extends ZoneType>, Map<Integer, ? extends ZoneType>> _zones = new HashMap();
    private final Map<Integer, ItemInstance> _debugItems = new ConcurrentHashMap();
    private int _lastDynamicId = 0;

    protected ZoneManager() {
    }

    public static <T extends ZoneType> void toAllPlayersInZoneType(Class<T> zoneType, L2GameServerPacket... packets) {
        Iterator var2 = getInstance().getAllZones(zoneType).iterator();

        while (var2.hasNext()) {
            ZoneType zone = (ZoneType) var2.next();
            Iterator var4 = zone.getKnownTypeInside(Player.class).iterator();

            while (var4.hasNext()) {
                Player player = (Player) var4.next();
                L2GameServerPacket[] var6 = packets;
                int var7 = packets.length;

                for (int var8 = 0; var8 < var7; ++var8) {
                    L2GameServerPacket packet = var6[var8];
                    player.sendPacket(packet);
                }
            }
        }

    }

    public static final ZoneManager getInstance() {
        return ZoneManager.SingletonHolder.INSTANCE;
    }

    public void load() {
        this.parseFile("./data/xml/zones");
        LOGGER.info("Loaded {} zones classes and total {} zones.", this._zones.size(), this._zones.values().stream().mapToInt(Map::size).sum());
    }

    public void parseDocument(Document doc, Path path) {
        this._lastDynamicId = this._lastDynamicId / 1000 * 1000 + 1000;
        String zoneType = StringUtil.getNameWithoutExtension(path.toFile().getName());

        Constructor zoneConstructor;
        try {
            zoneConstructor = Class.forName("net.sf.l2j.gameserver.model.zone.type." + zoneType).getConstructor(Integer.TYPE);
        } catch (Exception var6) {
            LOGGER.error("The zone type {} doesn't exist. Abort zones loading for {}.", var6, zoneType, path.toFile().getName());
            return;
        }

        this.forEach(doc, "list", (listNode) -> {
            this.forEach(listNode, "zone", (zoneNode) -> {
                NamedNodeMap attrs = zoneNode.getAttributes();
                Node attribute = attrs.getNamedItem("id");
                int var10000;
                if (attribute == null) {
                    int var10002 = this._lastDynamicId;
                    var10000 = var10002;
                    this._lastDynamicId = var10002 + 1;
                } else {
                    var10000 = Integer.parseInt(attribute.getNodeValue());
                }

                int zoneId = var10000;

                ZoneType temp;
                try {
                    temp = (ZoneType) zoneConstructor.newInstance(zoneId);
                } catch (Exception var18) {
                    LOGGER.error("The zone id {} couldn't be instantiated.", var18, zoneId);
                    return;
                }

                String zoneShape = this.parseString(attrs, "shape");
                int minZ = this.parseInteger(attrs, "minZ");
                int maxZ = this.parseInteger(attrs, "maxZ");
                List<IntIntHolder> nodes = new ArrayList();
                this.forEach(zoneNode, "node", (nodeNode) -> {
                    NamedNodeMap nodeAttrs = nodeNode.getAttributes();
                    nodes.add(new IntIntHolder(this.parseInteger(nodeAttrs, "x"), this.parseInteger(nodeAttrs, "y")));
                });
                if (nodes.isEmpty()) {
                    LOGGER.warn("Missing nodes for zone {} in file {}.", zoneId, zoneType);
                } else {
                    this.forEach(zoneNode, "stat", (statNode) -> {
                        NamedNodeMap statAttrs = statNode.getAttributes();
                        temp.setParameter(this.parseString(statAttrs, "name"), this.parseString(statAttrs, "val"));
                    });
                    if (temp instanceof SpawnZoneType) {
                        this.forEach(zoneNode, "spawn", (spawnNode) -> {
                            NamedNodeMap spawnAttrs = spawnNode.getAttributes();
                            ((SpawnZoneType) temp).addLoc(this.parseLocation(spawnNode), this.parseBoolean(spawnAttrs, "isChaotic", false));
                        });
                    }

                    IntIntHolder[] coords = nodes.toArray(new IntIntHolder[nodes.size()]);
                    byte var14 = -1;
                    switch (zoneShape.hashCode()) {
                        case -284734474:
                            if (zoneShape.equals("Cylinder")) {
                                var14 = 2;
                            }
                            break;
                        case 74528058:
                            if (zoneShape.equals("NPoly")) {
                                var14 = 1;
                            }
                            break;
                        case 2029234618:
                            if (zoneShape.equals("Cuboid")) {
                                var14 = 0;
                            }
                    }

                    int xLoc;
                    int y;
                    switch (var14) {
                        case 0:
                            if (coords.length != 2) {
                                LOGGER.warn("Missing cuboid nodes for zone {} in file {}.", zoneId, zoneType);
                                return;
                            }

                            temp.setZone(new ZoneCuboid(coords[0].getId(), coords[1].getId(), coords[0].getValue(), coords[1].getValue(), minZ, maxZ));
                            break;
                        case 1:
                            if (coords.length <= 2) {
                                LOGGER.warn("Missing NPoly nodes for zone {} in file {}.", zoneId, zoneType);
                                return;
                            }

                            int[] aX = new int[coords.length];
                            int[] aY = new int[coords.length];

                            for (y = 0; y < coords.length; ++y) {
                                aX[y] = coords[y].getId();
                                aY[y] = coords[y].getValue();
                            }

                            temp.setZone(new ZoneNPoly(aX, aY, minZ, maxZ));
                            break;
                        case 2:
                            xLoc = this.parseInteger(attrs, "rad");
                            if (coords.length != 1 || xLoc <= 0) {
                                LOGGER.warn("Missing Cylinder nodes for zone {} in file {}.", zoneId, zoneType);
                                return;
                            }

                            temp.setZone(new ZoneCylinder(coords[0].getId(), coords[0].getValue(), minZ, maxZ, xLoc));
                            break;
                        default:
                            LOGGER.warn("Unknown {} shape in file {}.", zoneShape, zoneType);
                            return;
                    }

                    this.addZone(zoneId, temp);
                    WorldRegion[][] regions = World.getInstance().getWorldRegions();

                    for (int x = 0; x < regions.length; ++x) {
                        xLoc = World.getRegionX(x);
                        int xLoc2 = World.getRegionX(x + 1);

                        for (y = 0; y < regions[x].length; ++y) {
                            if (temp.getZone().intersectsRectangle(xLoc, xLoc2, World.getRegionY(y), World.getRegionY(y + 1))) {
                                regions[x][y].addZone(temp);
                            }
                        }
                    }

                }
            });
        });
    }

    public void reload() {
        this.save();
        WorldRegion[][] var1 = World.getInstance().getWorldRegions();
        int var2 = var1.length;

        for (int var3 = 0; var3 < var2; ++var3) {
            WorldRegion[] regions = var1[var3];
            WorldRegion[] var5 = regions;
            int var6 = regions.length;

            for (int var7 = 0; var7 < var6; ++var7) {
                WorldRegion region = var5[var7];
                region.getZones().clear();
            }
        }

        this._zones.clear();
        this.clearDebugItems();
        this._lastDynamicId = 0;
        this.load();
        Iterator var9 = World.getInstance().getObjects().iterator();

        while (var9.hasNext()) {
            WorldObject object = (WorldObject) var9.next();
            if (object instanceof Creature) {
                ((Creature) object).revalidateZone(true);
            }
        }

    }

    public final void save() {
        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("DELETE FROM grandboss_list");

                try {
                    ps.executeUpdate();
                } catch (Throwable var10) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var9) {
                            var10.addSuppressed(var9);
                        }
                    }

                    throw var10;
                }

                if (ps != null) {
                    ps.close();
                }

                ps = con.prepareStatement("INSERT INTO grandboss_list (player_id,zone) VALUES (?,?)");

                try {
                    Iterator var3 = ((Map) this._zones.get(BossZone.class)).values().iterator();

                    while (true) {
                        if (!var3.hasNext()) {
                            ps.executeBatch();
                            break;
                        }

                        ZoneType zone = (ZoneType) var3.next();
                        Iterator var5 = ((BossZone) zone).getAllowedPlayers().iterator();

                        while (var5.hasNext()) {
                            int player = (Integer) var5.next();
                            ps.setInt(1, player);
                            ps.setInt(2, zone.getId());
                            ps.addBatch();
                        }
                    }
                } catch (Throwable var11) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var8) {
                            var11.addSuppressed(var8);
                        }
                    }

                    throw var11;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (Throwable var12) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var7) {
                        var12.addSuppressed(var7);
                    }
                }

                throw var12;
            }

            if (con != null) {
                con.close();
            }
        } catch (Exception var13) {
            LOGGER.error("Error storing boss zones.", var13);
        }

        LOGGER.info("Saved boss zones data.");
    }

    public <T extends ZoneType> void addZone(Integer id, T zone) {
        Map<Integer, T> map = (Map) this._zones.get(zone.getClass());
        if (map == null) {
            map = new HashMap();
            map.put(id, zone);
            this._zones.put(zone.getClass(), map);
        } else {
            map.put(id, zone);
        }

    }

    public <T extends ZoneType> Collection<T> getAllZones(Class<T> type) {
        return ((Map) this._zones.get(type)).values();
    }

    public ZoneType getZoneById(int id) {
        Iterator var2 = this._zones.values().iterator();

        Map map;
        do {
            if (!var2.hasNext()) {
                return null;
            }

            map = (Map) var2.next();
        } while (!map.containsKey(id));

        return (ZoneType) map.get(id);
    }

    public <T extends ZoneType> T getZoneById(int id, Class<T> type) {
        return (T) ((Map) this._zones.get(type)).get(id);
    }

    public List<ZoneType> getZones(WorldObject object) {
        return this.getZones(object.getX(), object.getY(), object.getZ());
    }

    public <T extends ZoneType> T getZone(WorldObject object, Class<T> type) {
        return object == null ? null : this.getZone(object.getX(), object.getY(), object.getZ(), type);
    }

    public List<ZoneType> getZones(int x, int y) {
        List<ZoneType> temp = new ArrayList();
        Iterator var4 = World.getInstance().getRegion(x, y).getZones().iterator();

        while (var4.hasNext()) {
            ZoneType zone = (ZoneType) var4.next();
            if (zone.isInsideZone(x, y)) {
                temp.add(zone);
            }
        }

        return temp;
    }

    public List<ZoneType> getZones(int x, int y, int z) {
        List<ZoneType> temp = new ArrayList();
        Iterator var5 = World.getInstance().getRegion(x, y).getZones().iterator();

        while (var5.hasNext()) {
            ZoneType zone = (ZoneType) var5.next();
            if (zone.isInsideZone(x, y, z)) {
                temp.add(zone);
            }
        }

        return temp;
    }

    public <T extends ZoneType> T getZone(int x, int y, Class<T> type) {
        Iterator var4 = World.getInstance().getRegion(x, y).getZones().iterator();

        ZoneType zone;
        do {
            if (!var4.hasNext()) {
                return null;
            }

            zone = (ZoneType) var4.next();
        } while (!zone.isInsideZone(x, y) || !type.isInstance(zone));

        return (T) zone;
    }

    public <T extends ZoneType> T getZone(int x, int y, int z, Class<T> type) {
        Iterator var5 = World.getInstance().getRegion(x, y).getZones().iterator();

        ZoneType zone;
        do {
            if (!var5.hasNext()) {
                return null;
            }

            zone = (ZoneType) var5.next();
        } while (!zone.isInsideZone(x, y, z) || !type.isInstance(zone));

        return (T) zone;
    }

    public void addDebugItem(ItemInstance item) {
        this._debugItems.put(item.getObjectId(), item);
    }

    public void clearDebugItems() {
        Iterator var1 = this._debugItems.values().iterator();

        while (var1.hasNext()) {
            ItemInstance item = (ItemInstance) var1.next();
            item.decayMe();
        }

        this._debugItems.clear();
    }

    private static class SingletonHolder {
        protected static final ZoneManager INSTANCE = new ZoneManager();
    }
}