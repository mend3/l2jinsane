package enginemods.main.engine.events;

import enginemods.main.data.ConfigData;
import enginemods.main.engine.AbstractMods;
import enginemods.main.holders.RewardHolder;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.data.xml.MapRegionData;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.Location;

import java.util.ArrayList;
import java.util.List;

public class CityElpys extends AbstractMods {
    private static final List<Npc> _mobs;

    static {
        _mobs = new ArrayList<>(ConfigData.ELPY_COUNT);
    }

    public CityElpys() {
        this.registerMod(ConfigData.ELPY_Enabled, ConfigData.ELPY_ENABLE_DAY);
    }

    private static void unspawnElpys() {
        for (Npc mob : _mobs) {
            mob.deleteMe();
        }

        _mobs.clear();
    }

    public static CityElpys getInstance() {
        return CityElpys.SingletonHolder.INSTANCE;
    }

    public void onModState() {
        switch (this.getState()) {
            case START:
                this.startTimer("spawnElpys", ConfigData.ELPY_EVENT_TIME * 60 * 1000, null, null, true);
                break;
            case END:
                unspawnElpys();
                this.cancelTimers("spawnElpys");
        }

    }

    public void onTimer(String timerName, Npc npc, Player player) {
        switch (timerName) {
            case "spawnElpys":
                unspawnElpys();
                Location loc = ConfigData.ELPY_LOC.get(Rnd.get(ConfigData.ELPY_LOC.size()));
                String locName = MapRegionData.getInstance().getClosestTownName(loc.getX(), loc.getY());
                World.announceToOnlinePlayers("Elpys spawn near " + locName, true);

                for (int i = 0; i < ConfigData.ELPY_COUNT; ++i) {
                    int x = loc.getX() + Rnd.get(-ConfigData.ELPY_RANGE_SPAWN, ConfigData.ELPY_RANGE_SPAWN);
                    int y = loc.getY() + Rnd.get(-ConfigData.ELPY_RANGE_SPAWN, ConfigData.ELPY_RANGE_SPAWN);
                    int z = loc.getZ();
                    Npc spawn = this.addSpawn(ConfigData.ELPY, new Location(x, y, z), false, 0L);
                    _mobs.add(spawn);
                }
            default:
        }
    }

    public void onKill(Creature killer, Creature victim, boolean isPet) {
        if (_mobs.contains(victim)) {
            _mobs.remove(victim);

            for (RewardHolder reward : ConfigData.ELPY_REWARDS) {
                if (Rnd.get(100) <= reward.getRewardChance()) {
                    int var10001 = reward.getRewardCount();
                    killer.sendMessage("Have won " + var10001 + " " + ItemTable.getInstance().getTemplate(reward.getRewardId()).getName());
                    killer.getActingPlayer().getInventory().addItem("PvpReward", reward.getRewardId(), reward.getRewardCount(), (Player) killer, victim);
                }
            }
        }

    }

    private static class SingletonHolder {
        protected static final CityElpys INSTANCE = new CityElpys();
    }
}
