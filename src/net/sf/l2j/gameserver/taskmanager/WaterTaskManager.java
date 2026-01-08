package net.sf.l2j.gameserver.taskmanager;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.enums.GaugeColor;
import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SetupGauge;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class WaterTaskManager implements Runnable {
    private final Map<Player, Long> _players = new ConcurrentHashMap<>();

    private WaterTaskManager() {
        ThreadPool.scheduleAtFixedRate(this, 1000L, 1000L);
    }

    public static WaterTaskManager getInstance() {
        return WaterTaskManager.SingletonHolder.INSTANCE;
    }

    public void run() {
        if (!this._players.isEmpty()) {
            long time = System.currentTimeMillis();

            for (Map.Entry<Player, Long> entry : this._players.entrySet()) {
                if (time >= entry.getValue()) {
                    Player player = entry.getKey();
                    double hp = (double) player.getMaxHp() / (double) 100.0F;
                    player.reduceCurrentHp(hp, player, false, false, null);
                    player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DROWN_DAMAGE_S1).addNumber((int) hp));
                }
            }

        }
    }

    public void add(Player player) {
        if (!player.isDead() && !this._players.containsKey(player)) {
            int time = (int) player.calcStat(Stats.BREATH, (double) 60000.0F * player.getRace().getBreathMultiplier(), player, null);
            this._players.put(player, System.currentTimeMillis() + (long) time);
            player.sendPacket(new SetupGauge(GaugeColor.CYAN, time));
        }

    }

    public void remove(Player player) {
        if (this._players.remove(player) != null) {
            player.sendPacket(new SetupGauge(GaugeColor.CYAN, 0));
        }

    }

    private static class SingletonHolder {
        protected static final WaterTaskManager INSTANCE = new WaterTaskManager();
    }
}
