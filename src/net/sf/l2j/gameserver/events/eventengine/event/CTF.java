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
import java.util.Iterator;
import java.util.Map;

public class CTF extends AbstractEvent {
    private final Map<Npc, EventTeam> flags = new HashMap<>();

    private final Map<Npc, Player> flagWielders = new HashMap<>();

    public CTF() {
        super("CTF", 3, Config.CTF_RUNNING_TIME);
        addTeam(Config.CTF_TEAM_1_NAME, Config.CTF_TEAM_1_COLOR, Config.CTF_TEAM_1_LOCATION);
        addTeam(Config.CTF_TEAM_2_NAME, Config.CTF_TEAM_2_COLOR, Config.CTF_TEAM_2_LOCATION);
        this.eventRes = new EventResTask(this);
        this.eventInfo = new EventInformation(this, Config.CTF_TEAM_1_NAME + ": %team1Score% | " + Config.CTF_TEAM_1_NAME + ": %team2Score%");
        this.eventInfo.addReplacement("%team1Score%", this.teams.get(0).getName().equals(Config.CTF_TEAM_1_NAME) ? this.teams.get(0).getScore() : this.teams.get(1).getScore());
        this.eventInfo.addReplacement("%team2Score%", this.teams.get(0).getName().equals(Config.CTF_TEAM_2_NAME) ? this.teams.get(0).getScore() : this.teams.get(1).getScore());
    }

    public void run() {
        if (canRunTheEvent()) {
            CtfEventManager.getInstance().setActiveEvent(this);
            announce("CTF Event Start", true);
            openRegistrations();
            schedule(() -> start(), Config.EVENT_REGISTRATION_TIME * 60 + 1);
        } else {
            abort();
        }
    }

    protected void cleanUp() {
        for (Player player : this.flagWielders.values())
            player.destroyItemByItemId("Event flag.", 6718, 1, null, false);
        this.flagWielders.clear();
        this.flags.clear();
        super.cleanUp();
    }

    protected void start() {
        if (!enoughRegistered(Config.CTF_MIN_PLAYERS)) {
            abort();
            return;
        }
        this.flags.put(spawnNpc(65534, Config.CTF_TEAM_1_FLAG_LOCATION, this.teams.get(0).getName()), this.teams.get(0));
        this.flags.put(spawnNpc(65534, Config.CTF_TEAM_2_FLAG_LOCATION, this.teams.get(1).getName()), this.teams.get(1));
        super.start();
    }

    protected void end() {
        if (!draw()) {
            announceTopTeams(1);
            rewardTopTeams(1, Config.CTF_WINNER_REWARDS);
        } else {
            announce("The event ended in a draw.", true);
            rewardTopInDraw(Config.CTF_DRAW_REWARDS);
        }
        super.end();
    }

    protected void increaseScore(Player player, int count) {
        super.increaseScore(player, count);
        for (Iterator<Integer> iterator = Config.CTF_ON_SCORE_REWARDS.keySet().iterator(); iterator.hasNext(); ) {
            int itemId = iterator.next();
            player.addItem("Event reward.", itemId, Config.CTF_ON_SCORE_REWARDS.get(itemId), null, true);
        }
        this.eventInfo.addReplacement("%team1Score%", this.teams.get(0).getName().equals(Config.TVT_TEAM_1_NAME) ? this.teams.get(0).getScore() : this.teams.get(1).getScore());
        this.eventInfo.addReplacement("%team2Score%", this.teams.get(0).getName().equals(Config.TVT_TEAM_2_NAME) ? this.teams.get(0).getScore() : this.teams.get(1).getScore());
    }

    public boolean isAutoAttackable(Player attacker, Player target) {
        return (getTeam(attacker) != getTeam(target));
    }

    public void onKill(Player killer, Player victim) {
        if (this.flagWielders.containsValue(victim)) {
            victim.destroyItemByItemId("Event flag.", 6718, 1, null, false);
            victim.broadcastUserInfo();
            announce("The " + getTeam(killer).getName() + " flag has been returned!", false);
            Npc remove = null;
            for (Npc flag : this.flags.keySet()) {
                if (this.flags.get(flag) == getTeam(killer)) {
                    remove = flag;
                    break;
                }
            }
            if (remove != null)
                this.flagWielders.remove(remove);
        }
        this.eventRes.addPlayer(victim);
    }

    public boolean onInterract(Player player, Npc npc) {
        if (this.flags.containsKey(npc)) {
            if (getState() != EventState.RUNNING)
                return true;
            if (this.flags.get(npc) == getTeam(player) && this.flagWielders.containsValue(player)) {
                player.destroyItemByItemId("Event flag.", 6718, 1, null, false);
                player.broadcastUserInfo();
                announce(player.getName() + " has scored for " + player.getName() + "!", false);
                increaseScore(player, 1);
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
            if (this.flags.get(npc) != getTeam(player) && !this.flagWielders.containsKey(npc)) {
                player.setWeaponSkinOption(0);
                ItemInstance flag = player.addItem("Event flag.", 6718, 1, null, false);
                player.useEquippableItem(flag, true);
                player.broadcastUserInfo();
                announce(player.getName() + " has got the " + player.getName() + " flag!", false);
                player.broadcastPacket(new SocialAction(player, 16));
                this.flagWielders.put(npc, player);
                return true;
            }
        }
        return true;
    }

    public boolean canHeal(Player healer, Player target) {
        return (getTeam(healer) == getTeam(target));
    }

    public boolean canAttack(Player attacker, Player target) {
        return (getTeam(attacker) != getTeam(target) && getState() == EventState.RUNNING);
    }

    public boolean canUseItem(Player player, int itemId) {
        return !this.flagWielders.containsValue(player);
    }

    public boolean allowDiePacket(Player player) {
        return false;
    }

    protected boolean canRunTheEvent() {
        return (TvTEventManager.getInstance().getActiveEvent() == null && DmEventManager.getInstance().getActiveEvent() == null);
    }
}
