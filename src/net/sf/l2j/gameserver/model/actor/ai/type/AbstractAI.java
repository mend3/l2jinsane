/**/
package net.sf.l2j.gameserver.model.actor.ai.type;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.enums.AiEventType;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.actor.ai.Desire;
import net.sf.l2j.gameserver.model.actor.ai.NextAction;
import net.sf.l2j.gameserver.model.actor.instance.Agathion;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.location.SpawnLocation;
import net.sf.l2j.gameserver.network.serverpackets.*;
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager;

import java.util.concurrent.Future;

abstract class AbstractAI {
    private static final int FOLLOW_INTERVAL = 1000;
    private static final int ATTACK_FOLLOW_INTERVAL = 500;
    protected final Creature _actor;
    protected final Desire _desire = new Desire();
    protected volatile boolean _clientMoving;
    protected Creature _followTarget;
    protected L2Skill _skill;
    protected int _clientMovingToPawnOffset;
    protected Future<?> _followTask = null;
    private NextAction _nextAction;
    private WorldObject _target;
    private long _moveToPawnTimeout;

    protected AbstractAI(Creature character) {
        this._actor = character;
    }

    public Creature getActor() {
        return this._actor;
    }

    public Desire getDesire() {
        return this._desire;
    }

    public synchronized void changeIntention(IntentionType intention, Object arg0, Object arg1) {
        this._desire.update(intention, arg0, arg1);
    }

    public final void setIntention(IntentionType intention) {
        this.setIntention(intention, null, null);
    }

    public final void setIntention(IntentionType intention, Object arg0) {
        this.setIntention(intention, arg0, null);
    }

    public final void setIntention(IntentionType intention, Object arg0, Object arg1) {
        if (intention != IntentionType.FOLLOW && intention != IntentionType.ATTACK) {
            this.stopFollow();
        }

        switch (intention) {
            case IDLE:
                this.onIntentionIdle();
                break;
            case ACTIVE:
                this.onIntentionActive();
                break;
            case REST:
                this.onIntentionRest();
                break;
            case ATTACK:
                this.onIntentionAttack((Creature) arg0);
                break;
            case CAST:
                this.onIntentionCast((L2Skill) arg0, (WorldObject) arg1);
                break;
            case MOVE_TO:
                this.onIntentionMoveTo((Location) arg0);
                break;
            case FOLLOW:
                this.onIntentionFollow((Creature) arg0);
                break;
            case PICK_UP:
                this.onIntentionPickUp((WorldObject) arg0);
                break;
            case INTERACT:
                this.onIntentionInteract((WorldObject) arg0);
        }

        if (this._nextAction != null && this._nextAction.getIntention() == intention) {
            this._nextAction = null;
        }

    }

    public final void notifyEvent(AiEventType evt) {
        this.notifyEvent(evt, null, null);
    }

    public final void notifyEvent(AiEventType evt, Object arg0) {
        this.notifyEvent(evt, arg0, null);
    }

    public final void notifyEvent(AiEventType evt, Object arg0, Object arg1) {
        if ((this._actor.isVisible() || this._actor.isTeleporting()) && this._actor.hasAI()) {
            switch (evt) {
                case THINK:
                    this.onEvtThink();
                    break;
                case ATTACKED:
                    this.onEvtAttacked((Creature) arg0);
                    break;
                case AGGRESSION:
                    this.onEvtAggression((Creature) arg0, ((Number) arg1).intValue());
                    break;
                case STUNNED:
                    this.onEvtStunned((Creature) arg0);
                    break;
                case PARALYZED:
                    this.onEvtParalyzed((Creature) arg0);
                    break;
                case SLEEPING:
                    this.onEvtSleeping((Creature) arg0);
                    break;
                case ROOTED:
                    this.onEvtRooted((Creature) arg0);
                    break;
                case CONFUSED:
                    this.onEvtConfused((Creature) arg0);
                    break;
                case MUTED:
                    this.onEvtMuted((Creature) arg0);
                    break;
                case EVADED:
                    this.onEvtEvaded((Creature) arg0);
                    break;
                case READY_TO_ACT:
                    if (!this._actor.isCastingNow() && !this._actor.isCastingSimultaneouslyNow()) {
                        this.onEvtReadyToAct();
                    }
                    break;
                case ARRIVED:
                    if (!this._actor.isCastingNow() && !this._actor.isCastingSimultaneouslyNow()) {
                        this.onEvtArrived();
                    }
                    break;
                case ARRIVED_BLOCKED:
                    this.onEvtArrivedBlocked((SpawnLocation) arg0);
                    break;
                case CANCEL:
                    this.onEvtCancel();
                    break;
                case DEAD:
                    this.onEvtDead();
                    break;
                case FAKE_DEATH:
                    this.onEvtFakeDeath();
                    break;
                case FINISH_CASTING:
                    this.onEvtFinishCasting();
            }

            if (this._nextAction != null && this._nextAction.getEvent() == evt) {
                this._nextAction.run();
                this._nextAction = null;
            }

        }
    }

    protected abstract void onIntentionIdle();

    protected abstract void onIntentionActive();

    protected abstract void onIntentionRest();

    protected abstract void onIntentionAttack(Creature var1);

    protected abstract void onIntentionCast(L2Skill var1, WorldObject var2);

    protected abstract void onIntentionMoveTo(Location var1);

    protected abstract void onIntentionFollow(Creature var1);

    protected abstract void onIntentionPickUp(WorldObject var1);

    protected abstract void onIntentionInteract(WorldObject var1);

    protected abstract void onEvtThink();

    protected abstract void onEvtAttacked(Creature var1);

    protected abstract void onEvtAggression(Creature var1, int var2);

    protected abstract void onEvtStunned(Creature var1);

    protected abstract void onEvtParalyzed(Creature var1);

    protected abstract void onEvtSleeping(Creature var1);

    protected abstract void onEvtRooted(Creature var1);

    protected abstract void onEvtConfused(Creature var1);

    protected abstract void onEvtMuted(Creature var1);

    protected abstract void onEvtEvaded(Creature var1);

    protected abstract void onEvtReadyToAct();

    protected abstract void onEvtArrived();

    protected abstract void onEvtArrivedBlocked(SpawnLocation var1);

    protected abstract void onEvtCancel();

    protected abstract void onEvtDead();

    protected abstract void onEvtFakeDeath();

    protected abstract void onEvtFinishCasting();

    protected void clientActionFailed() {
    }

    protected void moveToPawn(WorldObject pawn, int offset) {
        if (!this._actor.isMovementDisabled()) {
            if (offset < 10) {
                offset = 10;
            }

            if (this._clientMoving && this._target == pawn) {
                if (this._clientMovingToPawnOffset == offset) {
                    if (System.currentTimeMillis() < this._moveToPawnTimeout) {
                        this.clientActionFailed();
                        return;
                    }
                } else if (this._actor.isOnGeodataPath() && System.currentTimeMillis() < this._moveToPawnTimeout + 1000L) {
                    this.clientActionFailed();
                    return;
                }
            }

            this._clientMoving = true;
            this._clientMovingToPawnOffset = offset;
            this._target = pawn;
            this._moveToPawnTimeout = System.currentTimeMillis() + 1000L;
            if (pawn == null) {
                this.clientActionFailed();
                return;
            }

            this._actor.moveToLocation(pawn.getX(), pawn.getY(), pawn.getZ(), offset);
            if (!this._actor.isMoving()) {
                this.clientActionFailed();
                return;
            }

            if (pawn instanceof Creature) {
                if (this._actor.isOnGeodataPath()) {
                    this._actor.broadcastPacket(new MoveToLocation(this._actor));
                    this._clientMovingToPawnOffset = 0;
                } else {
                    this._actor.broadcastPacket(new MoveToPawn(this._actor, pawn, offset));
                }
            } else {
                this._actor.broadcastPacket(new MoveToLocation(this._actor));
            }
        } else {
            this.clientActionFailed();
        }

    }

    protected void moveTo(int x, int y, int z) {
        if (!this._actor.isMovementDisabled()) {
            this._clientMoving = true;
            this._clientMovingToPawnOffset = 0;
            this._actor.moveToLocation(x, y, z, 0);
            this._actor.broadcastPacket(new MoveToLocation(this._actor));
        } else {
            this.clientActionFailed();
        }

    }

    protected void clientStopMoving(SpawnLocation loc) {
        if (this._actor.isMoving()) {
            this._actor.stopMove(loc);
        }

        this._clientMovingToPawnOffset = 0;
        if (this._clientMoving || loc != null) {
            this._clientMoving = false;
            this._actor.broadcastPacket(new StopMove(this._actor));
            if (loc != null) {
                this._actor.broadcastPacket(new StopRotation(this._actor.getObjectId(), loc.getHeading(), 0));
            }
        }

    }

    protected void clientStoppedMoving() {
        if (this._clientMovingToPawnOffset > 0) {
            this._clientMovingToPawnOffset = 0;
            this._actor.broadcastPacket(new StopMove(this._actor));
        }

        this._clientMoving = false;
    }

    public void startAttackStance() {
        if (!AttackStanceTaskManager.getInstance().isInAttackStance(this._actor)) {
            this._actor.broadcastPacket(new AutoAttackStart(this._actor.getObjectId()));
        }

        AttackStanceTaskManager.getInstance().add(this._actor);
    }

    public void stopAttackStance() {
        if (AttackStanceTaskManager.getInstance().remove(this._actor)) {
            this._actor.broadcastPacket(new AutoAttackStop(this._actor.getObjectId()));
        }

    }

    protected void clientNotifyDead() {
        this._actor.broadcastPacket(new Die(this._actor));
        this._desire.update(IntentionType.IDLE, null, null);
        this._target = null;
        this.stopFollow();
        this.stopAttackStance();
    }

    public void describeStateToPlayer(Player player) {
        if (this._desire.getIntention() == IntentionType.MOVE_TO) {
            if (this._clientMovingToPawnOffset != 0 && this._followTarget != null) {
                player.sendPacket(new MoveToPawn(this._actor, this._followTarget, this._clientMovingToPawnOffset));
            } else {
                player.sendPacket(new MoveToLocation(this._actor));
            }
        }

    }

    public synchronized void startFollow(Creature target) {
        if (this._followTask != null) {
            this._followTask.cancel(false);
            this._followTask = null;
        }

        this._followTarget = target;
        this._followTask = ThreadPool.scheduleAtFixedRate(new AbstractAI.FollowTask(), 5L, 1000L);
    }

    public synchronized void startFollow(Creature target, int range) {
        if (this._followTask != null) {
            this._followTask.cancel(false);
            this._followTask = null;
        }

        this._followTarget = target;
        this._followTask = ThreadPool.scheduleAtFixedRate(new AbstractAI.FollowTask(range), 5L, 500L);
    }

    public synchronized void stopFollow() {
        if (this._followTask != null) {
            this._followTask.cancel(false);
            this._followTask = null;
        }

        this._followTarget = null;
    }

    protected Creature getFollowTarget() {
        return this._followTarget;
    }

    public WorldObject getTarget() {
        return this._target;
    }

    protected void setTarget(WorldObject target) {
        this._target = target;
    }

    public void stopAITask() {
        this.stopFollow();
    }

    public void setNextAction(NextAction nextAction) {
        this._nextAction = nextAction;
    }

    public String toString() {
        return "Actor: " + this._actor;
    }

    private class FollowTask implements Runnable {
        protected int _range = 70;

        public FollowTask() {
        }

        public FollowTask(int range) {
            this._range = range;
        }

        public void run() {
            if (AbstractAI.this._followTask != null) {
                Creature followTarget = AbstractAI.this._followTarget;
                if (followTarget == null) {
                    if (AbstractAI.this._actor instanceof Summon) {
                        ((Summon) AbstractAI.this._actor).setFollowStatus(false);
                    }

                    AbstractAI.this.setIntention(IntentionType.IDLE);
                } else {
                    if (AbstractAI.this._actor instanceof Agathion) {
                        this._range = 30;
                    }

                    if (!AbstractAI.this._actor.isInsideRadius(followTarget, this._range, true, false)) {
                        AbstractAI.this.moveToPawn(followTarget, this._range);
                    }

                }
            }
        }
    }
}