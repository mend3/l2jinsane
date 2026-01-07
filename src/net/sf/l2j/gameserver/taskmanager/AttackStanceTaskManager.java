package net.sf.l2j.gameserver.taskmanager;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.actor.instance.Cubic;
import net.sf.l2j.gameserver.network.serverpackets.AutoAttackStop;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class AttackStanceTaskManager implements Runnable {
    private static final long ATTACK_STANCE_PERIOD = 15000L;

    private final Map<Creature, Long> _creatures = new ConcurrentHashMap<>();

    private AttackStanceTaskManager() {
        ThreadPool.scheduleAtFixedRate(this, 1000L, 1000L);
    }

    public static AttackStanceTaskManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void run() {
        if (this._creatures.isEmpty())
            return;
        long time = System.currentTimeMillis();
        for (Map.Entry<Creature, Long> entry : this._creatures.entrySet()) {
            if (time < entry.getValue())
                continue;
            Creature creature = entry.getKey();
            creature.broadcastPacket(new AutoAttackStop(creature.getObjectId()));
            if (creature instanceof Player) {
                Summon summon = creature.getSummon();
                if (summon != null)
                    summon.broadcastPacket(new AutoAttackStop(summon.getObjectId()));
            }
            this._creatures.remove(creature);
        }
    }

    public void add(Creature creature) {
        if (creature instanceof net.sf.l2j.gameserver.model.actor.Playable)
            for (Cubic cubic : creature.getActingPlayer().getCubics().values()) {
                if (cubic.getId() != 3)
                    cubic.doAction();
            }
        this._creatures.put(creature, System.currentTimeMillis() + 15000L);
    }

    public boolean remove(Creature creature) {
        if (this._creatures.isEmpty())
            return false;
        Player player = null;
        if (creature instanceof Summon)
            player = creature.getActingPlayer();
        return (this._creatures.remove(player) != null);
    }

    public boolean isInAttackStance(Creature creature) {
        if (this._creatures.isEmpty())
            return false;
        Player player = null;
        if (creature instanceof Summon)
            player = creature.getActingPlayer();
        return this._creatures.containsKey(player);
    }

    private static class SingletonHolder {
        protected static final AttackStanceTaskManager INSTANCE = new AttackStanceTaskManager();
    }
}
