/**/
package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.MoveToPawn;

import java.util.concurrent.ScheduledFuture;

public class TownPet extends Folk {
    private ScheduledFuture<?> _aiTask;

    public TownPet(int objectId, NpcTemplate template) {
        super(objectId, template);
        this.setRunning();
        this._aiTask = ThreadPool.scheduleAtFixedRate(new RandomWalkTask(), 1000L, 10000L);
    }

    public void onAction(Player player) {
        if (player.getTarget() != this) {
            player.setTarget(this);
        } else if (!this.canInteract(player)) {
            player.getAI().setIntention(IntentionType.INTERACT, this);
        } else {
            if (player.isMoving() || player.isInCombat()) {
                player.getAI().setIntention(IntentionType.IDLE);
            }

            player.sendPacket(new MoveToPawn(player, this, 150));
            player.sendPacket(ActionFailed.STATIC_PACKET);
        }

    }

    public void deleteMe() {
        if (this._aiTask != null) {
            this._aiTask.cancel(true);
            this._aiTask = null;
        }

        super.deleteMe();
    }

    public class RandomWalkTask implements Runnable {
        public void run() {
            if (TownPet.this.getSpawn() != null) {
                TownPet.this.getAI().setIntention(IntentionType.MOVE_TO, GeoEngine.getInstance().canMoveToTargetLoc(TownPet.this.getX(), TownPet.this.getY(), TownPet.this.getZ(), TownPet.this.getSpawn().getLocX() + Rnd.get(-75, 75), TownPet.this.getSpawn().getLocY() + Rnd.get(-75, 75), TownPet.this.getZ()));
            }
        }
    }
}
