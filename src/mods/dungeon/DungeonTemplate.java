package mods.dungeon;

import java.util.Map;

public record DungeonTemplate(int id, String name, int players, Map<Integer, Integer> rewards, String rewardHtm,
                              Map<Integer, DungeonStage> stages) {
}
