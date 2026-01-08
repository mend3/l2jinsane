package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.SiegeSide;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.entity.Siege;
import net.sf.l2j.gameserver.model.spawn.L2Spawn;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MoveToPawn;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.util.ArrayList;
import java.util.List;

public class ControlTower extends Npc {
    private final List<L2Spawn> _guards = new ArrayList<>();
    private boolean _isActive = true;

    public ControlTower(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public boolean isAttackable() {
        return this.getCastle() != null && this.getCastle().getSiege().isInProgress();
    }

    public boolean isAutoAttackable(Creature attacker) {
        return attacker instanceof Player && this.getCastle() != null && this.getCastle().getSiege().isInProgress() && this.getCastle().getSiege().checkSide(((Player) attacker).getClan(), SiegeSide.ATTACKER);
    }

    public void onForcedAttack(Player player) {
        this.onAction(player);
    }

    public void onAction(Player player) {
        if (player.getTarget() != this) {
            player.setTarget(this);
        } else if (this.isAutoAttackable(player) && Math.abs(player.getZ() - this.getZ()) < 100 && GeoEngine.getInstance().canSeeTarget(player, this)) {
            player.getAI().setIntention(IntentionType.ATTACK, this);
        } else {
            if (player.isMoving() || player.isInCombat()) {
                player.getAI().setIntention(IntentionType.IDLE);
            }

            player.sendPacket(new MoveToPawn(player, this, 150));
            player.sendPacket(ActionFailed.STATIC_PACKET);
        }

    }

    public boolean doDie(Creature killer) {
        if (this.getCastle() != null) {
            Siege siege = this.getCastle().getSiege();
            if (siege.isInProgress()) {
                this._isActive = false;

                for (L2Spawn spawn : this._guards) {
                    spawn.setRespawnState(false);
                }

                this._guards.clear();
                if (siege.getControlTowerCount() == 0) {
                    siege.announceToPlayers(SystemMessage.getSystemMessage(SystemMessageId.TOWER_DESTROYED_NO_RESURRECTION), false);
                }

                try {
                    L2Spawn spawn = new L2Spawn(NpcData.getInstance().getTemplate(13003));
                    spawn.setLoc(this.getPosition());
                    Npc tower = spawn.doSpawn(false);
                    tower.setCastle(this.getCastle());
                    siege.getDestroyedTowers().add(tower);
                } catch (Exception e) {
                    LOGGER.error("Couldn't spawn the control tower.", e);
                }
            }
        }

        return super.doDie(killer);
    }

    public boolean hasRandomAnimation() {
        return false;
    }

    public void registerGuard(L2Spawn guard) {
        this._guards.add(guard);
    }

    public final List<L2Spawn> getGuards() {
        return this._guards;
    }

    public final boolean isActive() {
        return this._isActive;
    }
}
