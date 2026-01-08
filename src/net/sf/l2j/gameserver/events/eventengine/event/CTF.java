package net.sf.l2j.gameserver.events.eventengine.event;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.events.eventengine.*;
import net.sf.l2j.gameserver.events.eventengine.manager.CtfEventManager;
import net.sf.l2j.gameserver.events.eventengine.manager.DmEventManager;
import net.sf.l2j.gameserver.events.eventengine.manager.TvTEventManager;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;

import java.util.HashMap;
import java.util.Map;

public class CTF extends AbstractEvent {
    private final Map<Npc, EventTeam> flags = new HashMap<>();
    private final Map<Npc, Player> flagWielders = new HashMap<>();

    public CTF() {
        super("CTF", 3, Config.CTF_RUNNING_TIME);
        this.addTeam(Config.CTF_TEAM_1_NAME, Config.CTF_TEAM_1_COLOR, Config.CTF_TEAM_1_LOCATION);
        this.addTeam(Config.CTF_TEAM_2_NAME, Config.CTF_TEAM_2_COLOR, Config.CTF_TEAM_2_LOCATION);
        this.eventRes = new EventResTask(this);
        this.eventInfo = new EventInformation(this, Config.CTF_TEAM_1_NAME + ": %team1Score% | " + Config.CTF_TEAM_2_NAME + ": %team2Score%");
        this.eventInfo.addReplacement("%team1Score%", ((EventTeam) this.teams.get(0)).getName().equals(Config.CTF_TEAM_1_NAME) ? ((EventTeam) this.teams.get(0)).getScore() : ((EventTeam) this.teams.get(1)).getScore());
        this.eventInfo.addReplacement("%team2Score%", ((EventTeam) this.teams.get(0)).getName().equals(Config.CTF_TEAM_2_NAME) ? ((EventTeam) this.teams.get(0)).getScore() : ((EventTeam) this.teams.get(1)).getScore());
    }

    public void run() {
        if (this.canRunTheEvent()) {
            CtfEventManager.getInstance().setActiveEvent(this);
            this.announce("CTF Event Start", true);
            this.openRegistrations();
            this.schedule(() -> this.start(), Config.EVENT_REGISTRATION_TIME * 60 + 1);
        } else {
            this.abort();
        }
    }

    protected void cleanUp() {
        for (Player player : this.flagWielders.values()) {
            player.destroyItemByItemId("Event flag.", 6718, 1, null, false);
        }

        this.flagWielders.clear();
        this.flags.clear();
        super.cleanUp();
    }

    protected void start() {
        if (!this.enoughRegistered(Config.CTF_MIN_PLAYERS)) {
            this.abort();
        } else {
            this.flags.put(this.spawnNpc(65534, Config.CTF_TEAM_1_FLAG_LOCATION, ((EventTeam) this.teams.get(0)).getName()), (EventTeam) this.teams.get(0));
            this.flags.put(this.spawnNpc(65534, Config.CTF_TEAM_2_FLAG_LOCATION, ((EventTeam) this.teams.get(1)).getName()), (EventTeam) this.teams.get(1));
            super.start();
        }
    }

    protected void end() {
        if (!this.draw()) {
            this.announceTopTeams(1);
            this.rewardTopTeams(1, Config.CTF_WINNER_REWARDS);
        } else {
            this.announce("The event ended in a draw.", true);
            this.rewardTopInDraw(Config.CTF_DRAW_REWARDS);
        }

        super.end();
    }

    protected void increaseScore(Player player, int count) {
        super.increaseScore(player, count);

        for (int itemId : Config.CTF_ON_SCORE_REWARDS.keySet()) {
            player.addItem("Event reward.", itemId, (Integer) Config.CTF_ON_SCORE_REWARDS.get(itemId), null, true);
        }

        this.eventInfo.addReplacement("%team1Score%", ((EventTeam) this.teams.get(0)).getName().equals(Config.TVT_TEAM_1_NAME) ? ((EventTeam) this.teams.get(0)).getScore() : ((EventTeam) this.teams.get(1)).getScore());
        this.eventInfo.addReplacement("%team2Score%", ((EventTeam) this.teams.get(0)).getName().equals(Config.TVT_TEAM_2_NAME) ? ((EventTeam) this.teams.get(0)).getScore() : ((EventTeam) this.teams.get(1)).getScore());
    }

    public boolean isAutoAttackable(Player attacker, Player target) {
        return this.getTeam(attacker) != this.getTeam(target);
    }

    public void onKill(Player killer, Player victim) {
        if (this.flagWielders.values().contains(victim)) {
            victim.destroyItemByItemId("Event flag.", 6718, 1, null, false);
            victim.broadcastUserInfo();
            this.announce("The " + this.getTeam(killer).getName() + " flag has been returned!", false);
            Npc remove = null;

            for (Npc flag : this.flags.keySet()) {
                if (this.flags.get(flag) == this.getTeam(killer)) {
                    remove = flag;
                    break;
                }
            }

            if (remove != null) {
                this.flagWielders.remove(remove);
            }
        }

        this.eventRes.addPlayer(victim);
    }

    public boolean onInterract(Player player, Npc npc) {
        if (this.flags.keySet().contains(npc)) {
            if (this.getState() != EventState.RUNNING) {
                return true;
            }

            if (this.flags.get(npc) == this.getTeam(player) && this.flagWielders.values().contains(player)) {
                player.destroyItemByItemId("Event flag.", 6718, 1, null, false);
                player.broadcastUserInfo();
                this.announce(player.getName() + " has scored for " + ((EventTeam) this.flags.get(npc)).getName() + "!", false);
                this.increaseScore(player, 1);
                Npc remove = null;

                for (Npc flag : this.flagWielders.keySet()) {
                    if (this.flagWielders.get(flag) == player) {
                        remove = flag;
                        break;
                    }
                }

                this.flagWielders.remove(remove);
                return true;
            }

            if (this.flags.get(npc) != this.getTeam(player) && !this.flagWielders.keySet().contains(npc)) {
                player.setWeaponSkinOption(0);
                ItemInstance flag = player.addItem("Event flag.", 6718, 1, null, false);
                player.useEquippableItem(flag, true);
                player.broadcastUserInfo();
                this.announce(player.getName() + " has got the " + ((EventTeam) this.flags.get(npc)).getName() + " flag!", false);
                player.broadcastPacket(new SocialAction(player, 16));
                this.flagWielders.put(npc, player);
                return true;
            }
        }

        return true;
    }

    public boolean canHeal(Player healer, Player target) {
        return this.getTeam(healer) == this.getTeam(target);
    }

    public boolean canAttack(Player attacker, Player target) {
        return this.getTeam(attacker) != this.getTeam(target) && this.getState() == EventState.RUNNING;
    }

    public boolean canUseItem(Player player, int itemId) {
        return !this.flagWielders.values().contains(player);
    }

    public boolean allowDiePacket(Player player) {
        return false;
    }

    protected boolean canRunTheEvent() {
        return TvTEventManager.getInstance().getActiveEvent() == null && DmEventManager.getInstance().getActiveEvent() == null;
    }
}
