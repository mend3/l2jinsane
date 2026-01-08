package enginemods.main.engine.events;

import enginemods.main.data.ConfigData;
import enginemods.main.engine.AbstractMods;
import enginemods.main.holders.RewardHolder;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.Location;

public class RandomBossSpawn extends AbstractMods {
    private static final String[] LOCATIONS = new String[]{"in the colliseum", "near the entrance of the Garden of Eva", "close to the western entrance of the Cemetary", "at Gludin's Harbor"};
    private static final Location[] SPAWNS = new Location[]{new Location(150086, 46733, -3407), new Location(84805, 233832, -3669), new Location(161385, 21032, -3671), new Location(89199, 149962, -3581)};
    private static Npc _raid = null;

    public RandomBossSpawn() {
        this.registerMod(ConfigData.ENABLE_RandomBossSpawn);
    }

    public static RandomBossSpawn getInstance() {
        return RandomBossSpawn.SingletonHolder.INSTANCE;
    }

    public void onModState() {
        switch (this.getState()) {
            case START ->
                    this.startTimer("spawnRaids", ConfigData.RANDOM_BOSS_SPWNNED_TIME * 1000 * 60, null, null, true);
            case END -> this.cancelTimers("spawnRaids");
        }

    }

    public void onTimer(String timerName, Npc npc, Player player) {
        switch (timerName) {
            case "spawnRaids":
                int random = Rnd.get(4);
                _raid = this.addSpawn(ConfigData.RANDOM_BOSS_NPC_ID.get(Rnd.get(ConfigData.RANDOM_BOSS_NPC_ID.size())), SPAWNS[random], false, ConfigData.RANDOM_BOSS_SPWNNED_TIME * 1000 * 60);
                String var10000 = _raid.getName();
                World.announceToOnlinePlayers("Raid " + var10000 + " spawn " + LOCATIONS[random]);
                World.announceToOnlinePlayers("Have " + ConfigData.RANDOM_BOSS_SPWNNED_TIME + " minutes to kill");
            default:
        }
    }

    public void onKill(Creature killer, Creature victim, boolean isPet) {
        if (victim == _raid) {
            for (RewardHolder reward : ConfigData.RANDOM_BOSS_REWARDS) {
                if (Rnd.get(100) <= reward.getRewardChance()) {
                    int var10001 = reward.getRewardCount();
                    killer.sendMessage("Have won " + var10001 + " " + ItemTable.getInstance().getTemplate(reward.getRewardId()).getName());
                    killer.getActingPlayer().getInventory().addItem("PvpReward", reward.getRewardId(), reward.getRewardCount(), (Player) killer, victim);
                }
            }
        }

    }

    private static class SingletonHolder {
        protected static final RandomBossSpawn INSTANCE = new RandomBossSpawn();
    }
}
