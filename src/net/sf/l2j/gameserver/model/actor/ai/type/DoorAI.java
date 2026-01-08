package net.sf.l2j.gameserver.model.actor.ai.type;

import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.instance.Door;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.location.SpawnLocation;

public class DoorAI extends CreatureAI {
    public DoorAI(Door door) {
        super(door);
    }

    protected void onIntentionIdle() {
    }

    protected void onIntentionActive() {
    }

    protected void onIntentionRest() {
    }

    protected void onIntentionAttack(Creature target) {
    }

    protected void onIntentionCast(L2Skill skill, WorldObject target) {
    }

    protected void onIntentionMoveTo(Location loc) {
    }

    protected void onIntentionFollow(Creature target) {
    }

    protected void onIntentionPickUp(WorldObject item) {
    }

    protected void onEvtStunned(Creature attacker) {
    }

    protected void onEvtSleeping(Creature attacker) {
    }

    protected void onEvtRooted(Creature attacker) {
    }

    protected void onEvtReadyToAct() {
    }

    protected void onEvtArrived() {
    }

    protected void onEvtArrivedBlocked(SpawnLocation loc) {
    }

    protected void onEvtCancel() {
    }

    protected void onEvtDead() {
    }
}
