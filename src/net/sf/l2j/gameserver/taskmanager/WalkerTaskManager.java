package net.sf.l2j.gameserver.taskmanager;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.model.actor.instance.Walker;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class WalkerTaskManager implements Runnable {
    private final Map<Walker, Long> _walkers = new ConcurrentHashMap<>();

    private WalkerTaskManager() {
        ThreadPool.scheduleAtFixedRate(this, 1000L, 1000L);
    }

    public static WalkerTaskManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void run() {
        if (this._walkers.isEmpty())
            return;
        long time = System.currentTimeMillis();
        for (Map.Entry<Walker, Long> entry : this._walkers.entrySet()) {
            if (time < entry.getValue())
                continue;
            Walker walker = entry.getKey();
            walker.getAI().moveToNextPoint();
            this._walkers.remove(walker);
        }
    }

    public void add(Walker walker, int delay) {
        this._walkers.put(walker, Long.valueOf(System.currentTimeMillis() + delay));
    }

    private static class SingletonHolder {
        protected static final WalkerTaskManager INSTANCE = new WalkerTaskManager();
    }
}
