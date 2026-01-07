package net.sf.l2j.gameserver.taskmanager;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;

import java.util.Collection;

public class StatusRealTimeTaskManager implements Runnable {
    public StatusRealTimeTaskManager() {
        ThreadPool.scheduleAtFixedRate(this, 500L, 500L);
    }

    public static StatusRealTimeTaskManager getInstance() {
        return SingletonHolder._instance;
    }

    private static void updateStatus(Player _target, Player player) {
        StatusUpdate su = new StatusUpdate(_target);
        su.addAttribute(9, (int) _target.getStatus().getCurrentHp());
        su.addAttribute(33, (int) _target.getStatus().getCurrentCp());
        su.addAttribute(10, _target.getStat().getMaxHp());
        su.addAttribute(34, _target.getStat().getMaxCp());
        player.sendPacket(su);
    }

    public void run() {
        try {
            Collection<Player> allPlayers = World.getInstance().getPlayers();
            for (Player player : allPlayers) {
                try {
                    if (player != null &&
                            player.getTarget() != null && player.getTarget() instanceof Player target) {
                        updateStatus(target, player);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final class SingletonHolder {
        private static final StatusRealTimeTaskManager _instance = new StatusRealTimeTaskManager();
    }
}
