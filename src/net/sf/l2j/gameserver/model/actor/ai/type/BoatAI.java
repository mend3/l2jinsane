package net.sf.l2j.gameserver.model.actor.ai.type;

import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Boat;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.SpawnLocation;
import net.sf.l2j.gameserver.network.serverpackets.VehicleDeparture;
import net.sf.l2j.gameserver.network.serverpackets.VehicleInfo;
import net.sf.l2j.gameserver.network.serverpackets.VehicleStarted;

public class BoatAI extends CreatureAI {
    public BoatAI(Boat boat) {
        super(boat);
    }

    protected void moveTo(int x, int y, int z) {
        if (!this._actor.isMovementDisabled()) {
            if (!this._clientMoving) {
                this._actor.broadcastPacket(new VehicleStarted(this.getActor(), 1));
            }

            this._clientMoving = true;
            this._actor.moveToLocation(x, y, z, 0);
            this._actor.broadcastPacket(new VehicleDeparture(this.getActor()));
        }

    }

    protected void clientStopMoving(SpawnLocation loc) {
        if (this._actor.isMoving()) {
            this._actor.stopMove(loc);
        }

        if (this._clientMoving || loc != null) {
            this._clientMoving = false;
            this._actor.broadcastPacket(new VehicleStarted(this.getActor(), 0));
            this._actor.broadcastPacket(new VehicleInfo(this.getActor()));
        }

    }

    public void describeStateToPlayer(Player player) {
        if (this._clientMoving) {
            player.sendPacket(new VehicleDeparture(this.getActor()));
        }

    }

    public Boat getActor() {
        return (Boat) this._actor;
    }

    protected void onIntentionAttack(Creature target) {
    }

    protected void onIntentionCast(L2Skill skill, WorldObject target) {
    }

    protected void onIntentionFollow(Creature target) {
    }

    protected void onIntentionPickUp(WorldObject item) {
    }

    protected void onEvtAttacked(Creature attacker) {
    }

    protected void onEvtAggression(Creature target, int aggro) {
    }

    protected void onEvtStunned(Creature attacker) {
    }

    protected void onEvtSleeping(Creature attacker) {
    }

    protected void onEvtRooted(Creature attacker) {
    }

    protected void onEvtCancel() {
    }

    protected void onEvtDead() {
    }

    protected void onEvtFakeDeath() {
    }

    protected void onEvtFinishCasting() {
    }

    protected void clientActionFailed() {
    }

    protected void moveToPawn(WorldObject pawn, int offset) {
    }

    protected void clientStoppedMoving() {
    }
}
