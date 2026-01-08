package net.sf.l2j.gameserver.taskmanager;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.enums.AiEventType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.ai.type.CreatureAI;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class MovementTaskManager implements Runnable {
    private static final int MILLIS_PER_UPDATE = 100;
    private final Set<Creature> _characters = ConcurrentHashMap.newKeySet();
    private long _ticks;

    private MovementTaskManager() {
        ThreadPool.scheduleAtFixedRate(this, 100L, 100L);
    }

    public static MovementTaskManager getInstance() {
        return MovementTaskManager.SingletonHolder.INSTANCE;
    }

    public void run() {
        ++this._ticks;

        for (Creature character : this._characters) {
            if (character.updatePosition()) {
                this._characters.remove(character);
                CreatureAI ai = character.getAI();
                if (ai != null) {
                    ThreadPool.execute(() -> ai.notifyEvent(AiEventType.ARRIVED));
                }
            }
        }

    }

    public void add(Creature cha) {
        this._characters.add(cha);
    }

    public long getTicks() {
        return this._ticks;
    }

    private static class SingletonHolder {
        protected static final MovementTaskManager INSTANCE = new MovementTaskManager();
    }
}
