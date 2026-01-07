package mods.dungeon;

import net.sf.l2j.gameserver.model.location.Location;

import java.util.List;
import java.util.Map;

public record DungeonStage(int order, Location location, boolean teleport, int minutes,
                           Map<Integer, List<Location>> mobs) {
}
