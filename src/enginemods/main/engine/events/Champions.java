package enginemods.main.engine.events;

import enginemods.main.data.ConfigData;
import enginemods.main.engine.AbstractMods;
import enginemods.main.enums.ExpSpType;
import enginemods.main.enums.ItemDropType;
import enginemods.main.holders.RewardHolder;
import enginemods.main.instances.NpcDropsInstance;
import enginemods.main.instances.NpcExpInstance;
import enginemods.main.util.Util;
import net.sf.l2j.Config;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.TeamType;
import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.GrandBoss;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.actor.instance.RaidBoss;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Champions extends AbstractMods {
    private static final Map<Champions.ChampionType, Champions.ChampionInfoHolder> CHAMPIONS_INFO_STATS = new HashMap(3);
    private static final Map<Integer, Champions.ChampionInfoHolder> _champions = new ConcurrentHashMap();

    public Champions() {
        Champions.ChampionInfoHolder cih = null;
        cih = new ChampionInfoHolder(this);
        cih.type = Champions.ChampionType.WEAK_CHAMPION;
        cih.chanceToSpawn = ConfigData.CHANCE_SPAWN_WEAK;
        cih.allStats.putAll(ConfigData.CHAMPION_STAT_WEAK);
        cih.rewards.addAll(ConfigData.CHAMPION_REWARD_WEAK);
        CHAMPIONS_INFO_STATS.put(Champions.ChampionType.WEAK_CHAMPION, cih);
        cih = new ChampionInfoHolder(this);
        cih.type = Champions.ChampionType.SUPER_CHAMPION;
        cih.chanceToSpawn = ConfigData.CHANCE_SPAWN_SUPER;
        cih.allStats.putAll(ConfigData.CHAMPION_STAT_SUPER);
        cih.rewards.addAll(ConfigData.CHAMPION_REWARD_SUPER);
        CHAMPIONS_INFO_STATS.put(Champions.ChampionType.SUPER_CHAMPION, cih);
        cih = new ChampionInfoHolder(this);
        cih.type = Champions.ChampionType.HARD_CHAMPION;
        cih.chanceToSpawn = ConfigData.CHANCE_SPAWN_HARD;
        cih.allStats.putAll(ConfigData.CHAMPION_STAT_HARD);
        cih.rewards.addAll(ConfigData.CHAMPION_REWARD_HARD);
        CHAMPIONS_INFO_STATS.put(Champions.ChampionType.HARD_CHAMPION, cih);
        this.registerMod(ConfigData.ENABLE_Champions, ConfigData.CHAMPION_ENABLE_DAY);
    }

    private static boolean checkNpcType(WorldObject obj) {
        if (Util.areObjectType(RaidBoss.class, obj)) {
            return false;
        } else if (Util.areObjectType(GrandBoss.class, obj)) {
            return false;
        } else {
            return Util.areObjectType(Monster.class, obj);
        }
    }

    public static Champions getInstance() {
        return Champions.SingletonHolder.INSTANCE;
    }

    public void onModState() {
        switch (this.getState()) {
            case END:
                Iterator var1 = _champions.keySet().iterator();

                while (var1.hasNext()) {
                    int objId = (Integer) var1.next();
                    if (World.getInstance().getObjects().contains(objId)) {
                        Npc npc = (Npc) World.getInstance().getObject(objId);
                        if (npc != null) {
                            npc.setTeam(TeamType.NONE);
                            npc.deleteMe();
                        }
                    }
                }

                _champions.clear();
            case START:
            default:
        }
    }

    public void onSpawn(Npc npc) {
        if (checkNpcType(npc)) {
            Iterator var2 = CHAMPIONS_INFO_STATS.values().iterator();

            Champions.ChampionInfoHolder info;
            do {
                if (!var2.hasNext()) {
                    return;
                }

                info = (Champions.ChampionInfoHolder) var2.next();
            } while (Rnd.get(100) >= info.chanceToSpawn);

            _champions.put(npc.getObjectId(), info);
            npc.setTeam(TeamType.RED);
            npc.setCurrentHpMp((double) npc.getMaxHp() * info.allStats.get(Stats.MAX_HP), (double) npc.getMaxMp() * info.allStats.get(Stats.MAX_MP));
        }
    }

    public void onKill(Creature killer, Creature victim, boolean isPet) {
        if (_champions.containsKey(victim.getObjectId())) {
            Iterator var4 = ((Champions.ChampionInfoHolder) _champions.get(victim.getObjectId())).rewards.iterator();

            while (true) {
                while (true) {
                    RewardHolder reward;
                    do {
                        if (!var4.hasNext()) {
                            _champions.remove(victim.getObjectId());
                            victim.setTeam(TeamType.NONE);
                            return;
                        }

                        reward = (RewardHolder) var4.next();
                    } while (Rnd.get(100) > reward.getRewardChance());

                    if (victim.isRaidBoss() && Config.AUTO_LOOT_RAID || !victim.isRaidBoss() && Config.AUTO_LOOT) {
                        killer.getActingPlayer().doAutoLoot((Attackable) victim, new IntIntHolder(reward.getRewardId(), reward.getRewardCount()));
                    } else {
                        ((Monster) victim).dropItem((Player) killer, new IntIntHolder(reward.getRewardId(), reward.getRewardCount()));
                    }
                }
            }
        }
    }

    public String onSeeNpcTitle(int objectId) {
        return _champions.containsKey(objectId) ? ((Champions.ChampionInfoHolder) _champions.get(objectId)).type.name().replace("_", " ") : null;
    }

    public double onStats(Stats stat, Creature character, double value) {
        if (_champions.containsKey(character.getObjectId())) {
            Champions.ChampionInfoHolder cih = _champions.get(character.getObjectId());
            if (cih.allStats.containsKey(stat)) {
                return value * cih.allStats.get(stat);
            }
        }

        return value;
    }

    public void onNpcExpSp(Player killer, Attackable npc, NpcExpInstance instance) {
        if (_champions.containsKey(npc.getObjectId())) {
            instance.increaseRate(ExpSpType.EXP, ConfigData.CHAMPION_BONUS_RATE_EXP);
            instance.increaseRate(ExpSpType.SP, ConfigData.CHAMPION_BONUS_RATE_SP);
        }

    }

    public void onNpcDrop(Player killer, Attackable npc, NpcDropsInstance instance) {
        if (_champions.containsKey(npc.getObjectId())) {
            instance.increaseDrop(ItemDropType.NORMAL, ConfigData.CHAMPION_BONUS_DROP, ConfigData.CHAMPION_BONUS_DROP);
            instance.increaseDrop(ItemDropType.SPOIL, ConfigData.CHAMPION_BONUS_SPOIL, ConfigData.CHAMPION_BONUS_SPOIL);
            instance.increaseDrop(ItemDropType.HERB, ConfigData.CHAMPION_BONUS_HERB, ConfigData.CHAMPION_BONUS_HERB);
            instance.increaseDrop(ItemDropType.SEED, ConfigData.CHAMPION_BONUS_SEED, ConfigData.CHAMPION_BONUS_SEED);
        }

    }

    private enum ChampionType {
        WEAK_CHAMPION,
        SUPER_CHAMPION,
        HARD_CHAMPION;

        // $FF: synthetic method
        private static Champions.ChampionType[] $values() {
            return new Champions.ChampionType[]{WEAK_CHAMPION, SUPER_CHAMPION, HARD_CHAMPION};
        }
    }

    private static class ChampionInfoHolder {
        public Champions.ChampionType type;
        public int chanceToSpawn;
        public Map<Stats, Double> allStats = new HashMap();
        public List<RewardHolder> rewards = new ArrayList();

        private ChampionInfoHolder(final Champions param1) {
        }
    }

    private static class SingletonHolder {
        protected static final Champions INSTANCE = new Champions();
    }
}