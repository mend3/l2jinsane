package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.Config;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.manager.HeroManager;
import net.sf.l2j.gameserver.data.manager.RaidBossManager;
import net.sf.l2j.gameserver.data.manager.RaidPointManager;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.ai.type.AttackableAI;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.spawn.L2Spawn;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.util.concurrent.ScheduledFuture;

public class RaidBoss extends Monster {
    private ScheduledFuture<?> _maintenanceTask;

    public RaidBoss(int objectId, NpcTemplate template) {
        super(objectId, template);
        this.setRaid(true);
    }

    public void onSpawn() {
        this.setIsNoRndWalk(true);
        super.onSpawn();
        this._maintenanceTask = ThreadPool.scheduleAtFixedRate(() -> {
            if (!this.isDead()) {
                if (!this.isInCombat()) {
                    if (this.getNpcId() != 29095 && Rnd.nextBoolean()) {
                        L2Spawn spawn = this.getSpawn();
                        if (spawn == null) {
                            return;
                        }

                        if (!this.isInsideRadius(spawn.getLocX(), spawn.getLocY(), spawn.getLocZ(), Math.max(Config.MAX_DRIFT_RANGE, 200), true, false)) {
                            this.teleportTo(spawn.getLoc(), 0);
                        }
                    }
                } else if (Rnd.get(5) == 0) {
                    ((AttackableAI) this.getAI()).aggroReconsider();
                }
            }

            if (this.hasMinions()) {
                for (Monster minion : this.getMinionList().getSpawnedMinions()) {
                    if (minion.isDead() || !minion.isInCombat()) {
                        return;
                    }

                    if (Rnd.get(3) == 0) {
                        ((AttackableAI) minion.getAI()).aggroReconsider();
                    }
                }
            }

        }, 1000L, 60000L);
    }

    public boolean doDie(Creature killer) {
        if (!super.doDie(killer)) {
            return false;
        } else {
            if (this._maintenanceTask != null) {
                this._maintenanceTask.cancel(false);
                this._maintenanceTask = null;
            }

            if (killer != null) {
                Player player = killer.getActingPlayer();
                if (player != null) {
                    this.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.RAID_WAS_SUCCESSFUL));
                    this.broadcastPacket(new PlaySound("systemmsg_e.1209"));
                    if (Config.ENABLE_RAIDBOSS_NOBLES && this.getNpcId() == Config.RAIDBOSS_NOBLES_ID && !player.isNoble()) {
                        player.broadcastPacket(new SocialAction(player, 16));
                        player.setNoble(true, true);
                        player.sendMessage("You are the killer !!");
                        player.sendMessage("You have dealt the final blow to the boss of the raid. Congratulations !!");
                        player.getInventory().addItem("Nobles Circlets", 7694, 1, player, null);
                    }

                    Party party = player.getParty();
                    if (party != null) {
                        for (Player member : party.getMembers()) {
                            RaidPointManager.getInstance().addPoints(member, this.getNpcId(), this.getLevel() / 2 + Rnd.get(-5, 5));
                            if (member.isNoble()) {
                                HeroManager.getInstance().setRBkilled(member.getObjectId(), this.getNpcId());
                            }
                        }
                    } else {
                        RaidPointManager.getInstance().addPoints(player, this.getNpcId(), this.getLevel() / 2 + Rnd.get(-5, 5));
                        if (player.isNoble()) {
                            HeroManager.getInstance().setRBkilled(player.getObjectId(), this.getNpcId());
                        }
                    }
                }
            }

            RaidBossManager.getInstance().onDeath(this);
            return true;
        }
    }

    public void deleteMe() {
        if (this._maintenanceTask != null) {
            this._maintenanceTask.cancel(false);
            this._maintenanceTask = null;
        }

        super.deleteMe();
    }
}
