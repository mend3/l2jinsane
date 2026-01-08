package net.sf.l2j.gameserver.model;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.gameserver.data.sql.PlayerInfoTable;
import net.sf.l2j.gameserver.data.sql.SpawnTable;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.spawn.L2Spawn;
import net.sf.l2j.gameserver.model.zone.ZoneType;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class World {
    public static final int TILE_X_MIN = 16;
    public static final int TILE_X_MAX = 26;
    public static final int TILE_Y_MIN = 10;
    public static final int TILE_Y_MAX = 25;
    public static final int TILE_SIZE = 32768;
    public static final int WORLD_X_MIN = -131072;
    public static final int WORLD_X_MAX = 229376;
    public static final int WORLD_Y_MIN = -262144;
    public static final int WORLD_Y_MAX = 262144;
    private static final CLogger LOGGER = new CLogger(World.class.getName());
    private static final int REGION_SIZE = 2048;
    private static final int REGIONS_X = 176;
    private static final int REGIONS_Y = 256;
    private static final int REGION_X_OFFSET = Math.abs(-64);
    private static final int REGION_Y_OFFSET = Math.abs(-128);
    private final Map<Integer, WorldObject> _objects = new ConcurrentHashMap<>();
    private final Map<Integer, Pet> _pets = new ConcurrentHashMap<>();
    private final Map<Integer, Player> _players = new ConcurrentHashMap<>();
    private final WorldRegion[][] _worldRegions = new WorldRegion[REGIONS_X + 1][REGIONS_Y + 1];

    private World() {
        for (int i = 0; i <= 176; ++i) {
            for (int j = 0; j <= 256; ++j) {
                this._worldRegions[i][j] = new WorldRegion(i, j);
            }
        }

        for (int x = 0; x <= 176; ++x) {
            for (int y = 0; y <= 256; ++y) {
                for (int a = -1; a <= 1; ++a) {
                    for (int b = -1; b <= 1; ++b) {
                        if (validRegion(x + a, y + b)) {
                            this._worldRegions[x + a][y + b].addSurroundingRegion(this._worldRegions[x][y]);
                        }
                    }
                }
            }
        }

        LOGGER.info("World grid ({} by {}) is now set up.", REGIONS_X, REGIONS_Y);
    }

    public static int getRegionX(int regionX) {
        return (regionX - REGION_X_OFFSET) * 2048;
    }

    public static int getRegionY(int regionY) {
        return (regionY - REGION_Y_OFFSET) * 2048;
    }

    private static boolean validRegion(int x, int y) {
        return x >= 0 && x <= 176 && y >= 0 && y <= 256;
    }

    public static void toAllOnlinePlayers(L2GameServerPacket packet) {
        for (Player player : getInstance().getPlayers()) {
            if (player.isOnline()) {
                player.sendPacket(packet);
            }
        }

    }

    public static void announceToOnlinePlayers(String text) {
        toAllOnlinePlayers(new CreatureSay(0, 10, "", text));
    }

    public static void announceToOnlinePlayers(String text, boolean critical) {
        toAllOnlinePlayers(new CreatureSay(0, critical ? 18 : 10, "", text));
    }

    public static World getInstance() {
        return World.SingletonHolder.INSTANCE;
    }

    public void addObject(WorldObject object) {
        this._objects.putIfAbsent(object.getObjectId(), object);
    }

    public void removeObject(WorldObject object) {
        this._objects.remove(object.getObjectId());
    }

    public Collection<WorldObject> getObjects() {
        return this._objects.values();
    }

    public WorldObject getObject(int objectId) {
        return this._objects.get(objectId);
    }

    public void addPlayer(Player cha) {
        this._players.putIfAbsent(cha.getObjectId(), cha);
    }

    public void removePlayer(Player cha) {
        this._players.remove(cha.getObjectId());
    }

    public Collection<Player> getPlayers() {
        return this._players.values();
    }

    public Player getPlayer(String name) {
        return this._players.get(PlayerInfoTable.getInstance().getPlayerObjectId(name));
    }

    public Player getPlayer(int objectId) {
        return this._players.get(objectId);
    }

    public void addPet(int ownerId, Pet pet) {
        this._pets.putIfAbsent(ownerId, pet);
    }

    public void removePet(int ownerId) {
        this._pets.remove(ownerId);
    }

    public Pet getPet(int ownerId) {
        return this._pets.get(ownerId);
    }

    public WorldRegion getRegion(Location loc) {
        return this.getRegion(loc.getX(), loc.getY());
    }

    public WorldRegion getRegion(int x, int y) {
        return this._worldRegions[(x - WORLD_X_MIN) / REGION_SIZE][(y - WORLD_Y_MIN) / REGION_SIZE];
    }

    public WorldRegion getRegion(ZoneType zone) {
        for (int i = 0; i <= 176; ++i) {
            for (int j = 0; j <= 256; ++j) {
                WorldRegion region = this._worldRegions[i][j];
                if (region.containsZone(zone.getId())) {
                    return region;
                }
            }
        }

        return null;
    }

    public WorldRegion[][] getWorldRegions() {
        return this._worldRegions;
    }

    public void deleteVisibleNpcSpawns() {
        LOGGER.info("Deleting all visible NPCs.");

        for (int i = 0; i <= 176; ++i) {
            for (int j = 0; j <= 256; ++j) {
                for (WorldObject obj : this._worldRegions[i][j].getObjects()) {
                    if (obj instanceof Npc) {
                        ((Npc) obj).deleteMe();
                        L2Spawn spawn = ((Npc) obj).getSpawn();
                        if (spawn != null) {
                            spawn.setRespawnState(false);
                            SpawnTable.getInstance().deleteSpawn(spawn, false);
                        }
                    }
                }
            }
        }

        LOGGER.info("All visibles NPCs are now deleted.");
    }

    private static class SingletonHolder {
        protected static final World INSTANCE = new World();
    }
}
