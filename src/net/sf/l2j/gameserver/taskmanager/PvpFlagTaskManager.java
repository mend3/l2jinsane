package net.sf.l2j.gameserver.taskmanager;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PvpFlagTaskManager implements Runnable {
    private final Map<Player, Long> _players = new ConcurrentHashMap<>();

    private PvpFlagTaskManager() {
        ThreadPool.scheduleAtFixedRate(this, 1000L, 1000L);
    }

    public static PvpFlagTaskManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void run() {
        if (this._players.isEmpty())
            return;
        long currentTime = System.currentTimeMillis();
        for (Map.Entry<Player, Long> entry : this._players.entrySet()) {
            Player player = entry.getKey();
            long timeLeft = entry.getValue();
            if (player.isInsideZone(ZoneId.MULTI_FUNCTION) || player.isInsideZone(ZoneId.PVPEVENT) || player.isInsideZone(ZoneId.RANDOMZONE) || player.isInsideZone(ZoneId.ARENA_EVENT)) {
                this._players.remove(player);
                player.updatePvPFlag(1);
                continue;
            }
            if (currentTime > timeLeft) {
                player.updatePvPFlag(0);
                this._players.remove(player);
                continue;
            }
            if (currentTime > timeLeft - 5000L) {
                player.updatePvPFlag(2);
                continue;
            }
            player.updatePvPFlag(1);
        }
    }

    public void add(Player player, long time) {
        this._players.put(player, System.currentTimeMillis() + time);
    }

    public void remove(Player player) {
        this._players.remove(player);
    }

    private static class SingletonHolder {
        protected static final PvpFlagTaskManager INSTANCE = new PvpFlagTaskManager();
    }
}
