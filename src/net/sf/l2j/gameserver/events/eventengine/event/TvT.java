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
        addTeam(Config.TVT_TEAM_1_NAME, Config.TVT_TEAM_1_COLOR, Config.TVT_TEAM_1_LOCATION);
        addTeam(Config.TVT_TEAM_2_NAME, Config.TVT_TEAM_2_COLOR, Config.TVT_TEAM_2_LOCATION);
        this.eventRes = new EventResTask(this);
        this.eventInfo = new EventInformation(this, Config.TVT_TEAM_1_NAME + ": %team1Score% | " + Config.TVT_TEAM_1_NAME + ": %team2Score%");
        this.eventInfo.addReplacement("%team1Score%", this.teams.get(0).getName().equals(Config.TVT_TEAM_1_NAME) ? this.teams.get(0).getScore() : this.teams.get(1).getScore());
        this.eventInfo.addReplacement("%team2Score%", this.teams.get(0).getName().equals(Config.TVT_TEAM_2_NAME) ? this.teams.get(0).getScore() : this.teams.get(1).getScore());
    }

    public void run() {
        if (canRunTheEvent()) {
            TvTEventManager.getInstance().setActiveEvent(this);
            announce("TvT Event Start", true);
            openRegistrations();
            schedule(() -> start(), Config.EVENT_REGISTRATION_TIME * 60 + 1);
        } else {
            abort();
        }
    }

    protected void start() {
        if (!enoughRegistered(Config.TVT_MIN_PLAYERS)) {
            abort();
            return;
        }
        super.start();
    }

    protected void end() {
        if (!draw()) {
            announceTopTeams(1);
            rewardTopTeams(1, Config.TVT_WINNER_REWARDS);
        } else {
            announce("The event ended in a draw.", true);
            rewardTopInDraw(Config.TVT_DRAW_REWARDS);
        }
        super.end();
    }

    protected void preparePlayers() {
        super.preparePlayers();
        for (Player player : this.players)
            player.setTitle("Kills: 0");
    }

    protected void increaseScore(Player player, int count) {
        super.increaseScore(player, count);
        player.setTitle("Kills: " + getScore(player));
        player.broadcastUserInfo();
        this.eventInfo.addReplacement("%team1Score%", this.teams.get(0).getName().equals(Config.TVT_TEAM_1_NAME) ? this.teams.get(0).getScore() : this.teams.get(1).getScore());
        this.eventInfo.addReplacement("%team2Score%", this.teams.get(0).getName().equals(Config.TVT_TEAM_2_NAME) ? this.teams.get(0).getScore() : this.teams.get(1).getScore());
    }

    public boolean isAutoAttackable(Player attacker, Player target) {
        return (getTeam(attacker) != getTeam(target));
    }

    public void onKill(Player killer, Player victim) {
        if (getTeam(killer) != getTeam(victim))
            increaseScore(killer, 1);
        this.eventRes.addPlayer(victim);
    }

    public boolean canHeal(Player healer, Player target) {
        return (getTeam(healer) == getTeam(target));
    }

    public boolean canAttack(Player attacker, Player target) {
        return (getTeam(attacker) != getTeam(target) && getState() == EventState.RUNNING);
    }

    public boolean allowDiePacket(Player player) {
        return false;
    }

    protected boolean canRunTheEvent() {
        return (CtfEventManager.getInstance().getActiveEvent() == null && DmEventManager.getInstance().getActiveEvent() == null);
    }
}
