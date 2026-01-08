package net.sf.l2j.gameserver.events.eventengine;

import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;

import java.util.ArrayList;
import java.util.List;

public class EventResTask implements Runnable {
    private final AbstractEvent event;
    private final List<Player> players;

    public EventResTask(AbstractEvent event) {
        this.event = event;
        this.players = new ArrayList<>();
    }

    public void addPlayer(Player player) {
        this.players.add(player);
        player.sendMessage("You have been added to the ressurection task.");
    }

    public void run() {
        if (this.event.getState() == EventState.RUNNING) {
            for (Player player : this.players) {
                if (player.isDead()) {
                    player.doRevive();
                    player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
                    player.setCurrentCp(player.getMaxCp());
                    if (this.event.getTeam(player) == null) {
                        player.teleToLocation(this.event.getRandomLocation());
                    } else {
                        player.teleToLocation(this.event.getTeam(player).getLocation());
                    }

                    SkillTable.getInstance().getInfo(1323, 1).getEffects(player, player);
                    player.broadcastPacket(new MagicSkillUse(player, player, 1323, 1, 850, 0));
                }
            }

        }
    }
}
