package net.sf.l2j.gameserver.events.eventengine.event;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.events.eventengine.AbstractEvent;
import net.sf.l2j.gameserver.events.eventengine.EventInformation;
import net.sf.l2j.gameserver.events.eventengine.EventResTask;
import net.sf.l2j.gameserver.events.eventengine.EventState;
import net.sf.l2j.gameserver.events.eventengine.manager.CtfEventManager;
import net.sf.l2j.gameserver.events.eventengine.manager.DmEventManager;
import net.sf.l2j.gameserver.events.eventengine.manager.TvTEventManager;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.Location;

import java.util.Iterator;

public class DM extends AbstractEvent {
    public DM() {
        super("DeathMatch", 1, Config.DM_RUNNING_TIME);
        for (Location location : Config.DM_RESPAWN_SPOTS)
            addTeleportLocation(location);
        this.eventRes = new EventResTask(this);
        this.eventInfo = new EventInformation(this, "Top kills: %top%");
        this.eventInfo.addReplacement("%top%", getTopScore());
    }

    public void run() {
        if (canRunTheEvent()) {
            DmEventManager.getInstance().setActiveEvent(this);
            announce("Death Match Event Start", true);
            openRegistrations();
            schedule(() -> start(), Config.EVENT_REGISTRATION_TIME * 60 + 1);
        } else {
            abort();
        }
    }

    protected void start() {
        if (!enoughRegistered(Config.DM_MIN_PLAYERS)) {
            abort();
            return;
        }
        super.start();
    }

    protected void end() {
        announceTop(1);
        rewardTop(1, Config.DM_WINNER_REWARDS);
        super.end();
    }

    protected void preparePlayers() {
        super.preparePlayers();
        for (Player player : this.players)
            player.setTitle("Kills: 0");
    }

    protected void increaseScore(Player player, int count) {
        super.increaseScore(player, count);
        for (Iterator<Integer> iterator = Config.DM_ON_KILL_REWARDS.keySet().iterator(); iterator.hasNext(); ) {
            int itemId = iterator.next();
            player.addItem("Event reward.", itemId, Config.DM_ON_KILL_REWARDS.get(Integer.valueOf(itemId)), null, true);
        }
        player.setTitle("Kills: " + getScore(player));
        player.broadcastUserInfo();
        this.eventInfo.addReplacement("%top%", getTopScore());
    }

    public boolean isAutoAttackable(Player attacker, Player target) {
        return true;
    }

    public void onKill(Player killer, Player victim) {
        increaseScore(killer, 1);
        this.eventRes.addPlayer(victim);
    }

    public boolean canHeal(Player healer, Player target) {
        return (healer == target);
    }

    public boolean canAttack(Player attacker, Player target) {
        return (getState() == EventState.RUNNING);
    }

    public boolean allowDiePacket(Player player) {
        return false;
    }

    public boolean isDisguisedEvent() {
        return true;
    }

    protected boolean canRunTheEvent() {
        return (TvTEventManager.getInstance().getActiveEvent() == null && CtfEventManager.getInstance().getActiveEvent() == null);
    }
}
