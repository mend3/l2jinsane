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

public class DM extends AbstractEvent {
    public DM() {
        super("DeathMatch", 1, Config.DM_RUNNING_TIME);

        for (Location location : Config.DM_RESPAWN_SPOTS) {
            this.addTeleportLocation(location);
        }

        this.eventRes = new EventResTask(this);
        this.eventInfo = new EventInformation(this, "Top kills: %top%");
        this.eventInfo.addReplacement("%top%", this.getTopScore());
    }

    public void run() {
        if (this.canRunTheEvent()) {
            DmEventManager.getInstance().setActiveEvent(this);
            this.announce("Death Match Event Start", true);
            this.openRegistrations();
            this.schedule(() -> this.start(), Config.EVENT_REGISTRATION_TIME * 60 + 1);
        } else {
            this.abort();
        }
    }

    protected void start() {
        if (!this.enoughRegistered(Config.DM_MIN_PLAYERS)) {
            this.abort();
        } else {
            super.start();
        }
    }

    protected void end() {
        this.announceTop(1);
        this.rewardTop(1, Config.DM_WINNER_REWARDS);
        super.end();
    }

    protected void preparePlayers() {
        super.preparePlayers();

        for (Player player : this.players) {
            player.setTitle("Kills: 0");
        }

    }

    protected void increaseScore(Player player, int count) {
        super.increaseScore(player, count);

        for (int itemId : Config.DM_ON_KILL_REWARDS.keySet()) {
            player.addItem("Event reward.", itemId, (Integer) Config.DM_ON_KILL_REWARDS.get(itemId), null, true);
        }

        int var10001 = this.getScore(player);
        player.setTitle("Kills: " + var10001);
        player.broadcastUserInfo();
        this.eventInfo.addReplacement("%top%", this.getTopScore());
    }

    public boolean isAutoAttackable(Player attacker, Player target) {
        return true;
    }

    public void onKill(Player killer, Player victim) {
        this.increaseScore(killer, 1);
        this.eventRes.addPlayer(victim);
    }

    public boolean canHeal(Player healer, Player target) {
        return healer == target;
    }

    public boolean canAttack(Player attacker, Player target) {
        return this.getState() == EventState.RUNNING;
    }

    public boolean allowDiePacket(Player player) {
        return false;
    }

    public boolean isDisguisedEvent() {
        return true;
    }

    protected boolean canRunTheEvent() {
        return TvTEventManager.getInstance().getActiveEvent() == null && CtfEventManager.getInstance().getActiveEvent() == null;
    }
}
