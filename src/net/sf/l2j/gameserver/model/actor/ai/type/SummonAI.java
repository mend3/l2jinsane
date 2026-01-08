package net.sf.l2j.gameserver.model.actor.ai.type;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.location.SpawnLocation;
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager;

public class SummonAI extends PlayableAI {
    private static final int AVOID_RADIUS = 70;
    private volatile boolean _thinking;
    private volatile boolean _startFollow;
    private Creature _lastAttack;

    public SummonAI(Summon summon) {
        super(summon);
        this._startFollow = ((Summon) this._actor).getFollowStatus();
        this._lastAttack = null;
    }

    protected void onIntentionIdle() {
        this.stopFollow();
        this._startFollow = false;
        this.onIntentionActive();
    }

    protected void onIntentionActive() {
        Summon summon = (Summon) this._actor;
        if (this._startFollow) {
            this.setIntention(IntentionType.FOLLOW, summon.getOwner());
        } else {
            super.onIntentionActive();
        }

    }

    private void thinkAttack() {
        Creature target = (Creature) this.getTarget();
        if (this.checkTargetLostOrDead(target)) {
            this.setTarget(null);
        } else if (!this.maybeMoveToPawn(target, this._actor.getPhysicalAttackRange())) {
            this.clientStopMoving(null);
            this._actor.doAttack(target);
        }
    }

    private void thinkCast() {
        WorldObject target = this.getTarget();
        if (this.checkTargetLost(target)) {
            this.setTarget(null);
        } else {
            boolean val = this._startFollow;
            if (!this.maybeMoveToPawn(target, this._skill.getCastRange())) {
                this.clientStopMoving(null);
                ((Summon) this._actor).setFollowStatus(false);
                this.setIntention(IntentionType.IDLE);
                this._startFollow = val;
                this._actor.doCast(this._skill);
            }
        }
    }

    private void thinkPickUp() {
        WorldObject target = this.getTarget();
        if (!this.checkTargetLost(target)) {
            if (!this.maybeMoveToPawn(target, 36)) {
                this.setIntention(IntentionType.IDLE);
                ((Summon) this._actor).doPickupItem(target);
            }
        }
    }

    private void thinkInteract() {
        WorldObject target = this.getTarget();
        if (!this.checkTargetLost(target)) {
            if (!this.maybeMoveToPawn(target, 36)) {
                this.setIntention(IntentionType.IDLE);
            }
        }
    }

    protected void onEvtThink() {
        if (!this._thinking && !this._actor.isCastingNow() && !this._actor.isAllSkillsDisabled()) {
            this._thinking = true;

            try {
                switch (this._desire.getIntention()) {
                    case ATTACK -> this.thinkAttack();
                    case CAST -> this.thinkCast();
                    case PICK_UP -> this.thinkPickUp();
                    case INTERACT -> this.thinkInteract();
                }
            } finally {
                this._thinking = false;
            }

        }
    }

    protected void onEvtFinishCasting() {
        if (this._lastAttack == null) {
            ((Summon) this._actor).setFollowStatus(this._startFollow);
        } else {
            this.setIntention(IntentionType.ATTACK, this._lastAttack);
            this._lastAttack = null;
        }

    }

    protected void onEvtAttacked(Creature attacker) {
        super.onEvtAttacked(attacker);
        this.avoidAttack(attacker);
    }

    protected void onEvtEvaded(Creature attacker) {
        super.onEvtEvaded(attacker);
        this.avoidAttack(attacker);
    }

    public void startAttackStance() {
        this._actor.getActingPlayer().getAI().startAttackStance();
    }

    private void avoidAttack(Creature attacker) {
        Player owner = ((Summon) this._actor).getOwner();
        if (owner != null && owner != attacker && owner.isInsideRadius(this._actor, 140, true, false) && AttackStanceTaskManager.getInstance().isInAttackStance(owner)) {
            if (this._desire.getIntention() == IntentionType.ACTIVE || this._desire.getIntention() == IntentionType.FOLLOW) {
                if (!this._clientMoving && !this._actor.isDead() && !this._actor.isMovementDisabled()) {
                    int ownerX = owner.getX();
                    int ownerY = owner.getY();
                    double angle = Math.toRadians(Rnd.get(-90, 90)) + Math.atan2(ownerY - this._actor.getY(), ownerX - this._actor.getX());
                    int targetX = ownerX + (int) ((double) 70.0F * Math.cos(angle));
                    int targetY = ownerY + (int) ((double) 70.0F * Math.sin(angle));
                    if (GeoEngine.getInstance().canMoveToTarget(this._actor.getX(), this._actor.getY(), this._actor.getZ(), targetX, targetY, this._actor.getZ())) {
                        this.moveTo(targetX, targetY, this._actor.getZ());
                    }

                }
            }
        }
    }

    public void notifyFollowStatusChange() {
        this._startFollow = !this._startFollow;
        switch (this._desire.getIntention()) {
            case PICK_UP:
            case ACTIVE:
            case FOLLOW:
            case IDLE:
            case MOVE_TO:
                ((Summon) this._actor).setFollowStatus(this._startFollow);
            case INTERACT:
            default:
        }
    }

    public void setStartFollowController(boolean val) {
        this._startFollow = val;
    }

    protected void onIntentionCast(L2Skill skill, WorldObject target) {
        if (this._desire.getIntention() == IntentionType.ATTACK) {
            this._lastAttack = (Creature) this.getTarget();
        } else {
            this._lastAttack = null;
        }

        super.onIntentionCast(skill, target);
    }
}
