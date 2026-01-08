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

public class TvT extends AbstractEvent {
    public TvT() {
        super("TvT", 2, Config.TVT_RUNNING_TIME);
        this.addTeam(Config.TVT_TEAM_1_NAME, Config.TVT_TEAM_1_COLOR, Config.TVT_TEAM_1_LOCATION);
        this.addTeam(Config.TVT_TEAM_2_NAME, Config.TVT_TEAM_2_COLOR, Config.TVT_TEAM_2_LOCATION);
        this.eventRes = new EventResTask(this);
        this.eventInfo = new EventInformation(this, Config.TVT_TEAM_1_NAME + ": %team1Score% | " + Config.TVT_TEAM_2_NAME + ": %team2Score%");
        this.eventInfo.addReplacement("%team1Score%", (this.teams.get(0)).getName().equals(Config.TVT_TEAM_1_NAME) ? (this.teams.get(0)).getScore() : (this.teams.get(1)).getScore());
        this.eventInfo.addReplacement("%team2Score%", (this.teams.get(0)).getName().equals(Config.TVT_TEAM_2_NAME) ? (this.teams.get(0)).getScore() : (this.teams.get(1)).getScore());
    }

    public void run() {
        if (this.canRunTheEvent()) {
            TvTEventManager.getInstance().setActiveEvent(this);
            this.announce("TvT Event Start", true);
            this.openRegistrations();
            this.schedule(this::start, Config.EVENT_REGISTRATION_TIME * 60 + 1);
        } else {
            this.abort();
        }
    }

    protected void start() {
        if (!this.enoughRegistered(Config.TVT_MIN_PLAYERS)) {
            this.abort();
        } else {
            super.start();
        }
    }

    protected void end() {
        if (!this.draw()) {
            this.announceTopTeams(1);
            this.rewardTopTeams(1, Config.TVT_WINNER_REWARDS);
        } else {
            this.announce("The event ended in a draw.", true);
            this.rewardTopInDraw(Config.TVT_DRAW_REWARDS);
        }

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
        int var10001 = this.getScore(player);
        player.setTitle("Kills: " + var10001);
        player.broadcastUserInfo();
        this.eventInfo.addReplacement("%team1Score%", (this.teams.get(0)).getName().equals(Config.TVT_TEAM_1_NAME) ? (this.teams.get(0)).getScore() : (this.teams.get(1)).getScore());
        this.eventInfo.addReplacement("%team2Score%", (this.teams.get(0)).getName().equals(Config.TVT_TEAM_2_NAME) ? (this.teams.get(0)).getScore() : (this.teams.get(1)).getScore());
    }

    public boolean isAutoAttackable(Player attacker, Player target) {
        return this.getTeam(attacker) != this.getTeam(target);
    }

    public void onKill(Player killer, Player victim) {
        if (this.getTeam(killer) != this.getTeam(victim)) {
            this.increaseScore(killer, 1);
        }

        this.eventRes.addPlayer(victim);
    }

    public boolean canHeal(Player healer, Player target) {
        return this.getTeam(healer) == this.getTeam(target);
    }

    public boolean canAttack(Player attacker, Player target) {
        return this.getTeam(attacker) != this.getTeam(target) && this.getState() == EventState.RUNNING;
    }

    public boolean allowDiePacket(Player player) {
        return false;
    }

    protected boolean canRunTheEvent() {
        return CtfEventManager.getInstance().getActiveEvent() == null && DmEventManager.getInstance().getActiveEvent() == null;
    }
}
