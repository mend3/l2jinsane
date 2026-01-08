package net.sf.l2j.gameserver.taskmanager;

import net.sf.l2j.Config;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DebugMovementTaskManager implements Runnable {
    private final Map<ItemInstance, Long> _items = new ConcurrentHashMap<>();

    private DebugMovementTaskManager() {
        ThreadPool.scheduleAtFixedRate(this, 1000L, 1000L);
    }

    public static DebugMovementTaskManager getInstance() {
        return DebugMovementTaskManager.SingletonHolder.INSTANCE;
    }

    public void run() {
        long time = System.currentTimeMillis();

        for (Map.Entry<ItemInstance, Long> entry : this._items.entrySet()) {
            if (time >= entry.getValue()) {
                ItemInstance item = entry.getKey();
                item.decayMe();
                this._items.remove(item);
            }
        }

    }

    public void addItem(WorldObject character, int x, int y, int z) {
        int itemId = character instanceof Playable ? 57 : 1831;
        ItemInstance item = new ItemInstance(IdFactory.getInstance().getNextId(), itemId);
        item.setCount(1);
        item.spawnMe(x, y, z + 5);
        this._items.put(item, System.currentTimeMillis() + (long) Config.DEBUG_MOVEMENT);
    }

    private static class SingletonHolder {
        protected static final DebugMovementTaskManager INSTANCE = new DebugMovementTaskManager();
    }
}
