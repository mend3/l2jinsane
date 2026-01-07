package net.sf.l2j.gameserver.taskmanager;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.model.actor.Npc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class RandomAnimationTaskManager implements Runnable {
    private final Map<Npc, Long> _characters = new ConcurrentHashMap<>();

    private RandomAnimationTaskManager() {
        ThreadPool.scheduleAtFixedRate(this, 1000L, 1000L);
    }

    public static RandomAnimationTaskManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void run() {
        if (this._characters.isEmpty())
            return;
        long time = System.currentTimeMillis();
        for (Map.Entry<Npc, Long> entry : this._characters.entrySet()) {
            Npc npc = entry.getKey();
            if (!npc.isInActiveRegion() || npc.isDead() || (npc instanceof net.sf.l2j.gameserver.model.actor.Attackable && npc.getAI().getDesire().getIntention() != IntentionType.ACTIVE)) {
                this._characters.remove(npc);
                continue;
            }
            if (time < entry.getValue())
                continue;
            if (!npc.isStunned() && !npc.isSleeping() && !npc.isParalyzed())
                npc.onRandomAnimation(Rnd.get(2, 3));
            add(npc, npc.calculateRandomAnimationTimer());
        }
    }

    public void add(Npc character, int interval) {
        this._characters.put(character, Long.valueOf(System.currentTimeMillis() + (interval * 1000L)));
    }

    private static final class SingletonHolder {
        private static final RandomAnimationTaskManager INSTANCE = new RandomAnimationTaskManager();
    }
}
