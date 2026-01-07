package net.sf.l2j.gameserver.taskmanager;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.actor.instance.Monster;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DecayTaskManager implements Runnable {
    private final Map<Creature, Long> _creatures = new ConcurrentHashMap<>();

    private DecayTaskManager() {
        ThreadPool.scheduleAtFixedRate(this, 1000L, 1000L);
    }

    public static DecayTaskManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void run() {
        if (this._creatures.isEmpty())
            return;
        long time = System.currentTimeMillis();
        for (Map.Entry<Creature, Long> entry : this._creatures.entrySet()) {
            Creature creature = entry.getKey();
            if (creature instanceof Summon && ((Summon) creature).getOwner().getSummon() != creature) {
                this._creatures.remove(creature);
                continue;
            }
            if (time < entry.getValue())
                continue;
            creature.onDecay();
            this._creatures.remove(creature);
        }
    }

    public void add(Creature creature, int interval) {
        if (creature instanceof Monster monster) {
            if (monster.getSpoilerId() != 0 || monster.isSeeded())
                interval *= 2;
        }
        this._creatures.put(creature, Long.valueOf(System.currentTimeMillis() + (interval * 1000L)));
    }

    public void cancel(Creature creature) {
        this._creatures.remove(creature);
    }

    public boolean isCorpseActionAllowed(Monster monster) {
        Long time = this._creatures.get(monster);
        if (time == null)
            return false;
        int corpseTime = monster.getTemplate().getCorpseTime() * 1000 / 2;
        if (monster.getSpoilerId() != 0 || monster.isSeeded())
            corpseTime *= 2;
        return (System.currentTimeMillis() < time - corpseTime);
    }

    private static final class SingletonHolder {
        private static final DecayTaskManager INSTANCE = new DecayTaskManager();
    }
}
