package mods.dungeon;

import java.util.Map;

public class DungeonTemplate {
    private final int id;

    private final String name;

    private final int players;

    private final Map<Integer, Integer> rewards;

    private final String rewardHtm;

    private final Map<Integer, DungeonStage> stages;

    public DungeonTemplate(int id, String name, int players, Map<Integer, Integer> rewards, String rewardHtm, Map<Integer, DungeonStage> stages) {
        this.id = id;
        this.name = name;
        this.players = players;
        this.rewards = rewards;
        this.rewardHtm = rewardHtm;
        this.stages = stages;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public int getPlayers() {
        return this.players;
    }

    public Map<Integer, Integer> getRewards() {
        return this.rewards;
    }

    public String getRewardHtm() {
        return this.rewardHtm;
    }

    public Map<Integer, DungeonStage> getStages() {
        return this.stages;
    }
}
