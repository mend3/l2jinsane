package mods.dungeon;

import net.sf.l2j.gameserver.model.location.Location;

import java.util.List;
import java.util.Map;

public class DungeonStage {
    private final int order;

    private final Location location;

    private final boolean teleport;

    private final int minutes;

    private final Map<Integer, List<Location>> mobs;

    public DungeonStage(int order, Location location, boolean teleport, int minutes, Map<Integer, List<Location>> mobs) {
        this.order = order;
        this.location = location;
        this.teleport = teleport;
        this.minutes = minutes;
        this.mobs = mobs;
    }

    public int getOrder() {
        return this.order;
    }

    public Location getLocation() {
        return this.location;
    }

    public boolean teleport() {
        return this.teleport;
    }

    public int getMinutes() {
        return this.minutes;
    }

    public Map<Integer, List<Location>> getMobs() {
        return this.mobs;
    }
}
