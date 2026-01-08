package net.sf.l2j.gameserver.model.actor.ai.type;

import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.actor.ai.Desire;
import net.sf.l2j.gameserver.model.actor.instance.StaticObject;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.AutoAttackStart;
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager;

public class PlayerAI extends PlayableAI {
    private boolean _thinking;
    private final Desire _nextIntention = new Desire();

    public PlayerAI(Player player) {
        super(player);
    }

    protected void clientActionFailed() {
        this._actor.sendPacket(ActionFailed.STATIC_PACKET);
    }

    public Desire getNextIntention() {
        return this._nextIntention;
    }

    public synchronized void changeIntention(IntentionType intention, Object arg0, Object arg1) {
        if (intention == IntentionType.CAST && (arg0 == null || !((L2Skill) arg0).isOffensive())) {
            if (!this._desire.equals(intention, arg0, arg1)) {
                this._nextIntention.update(this._desire);
                super.changeIntention(intention, arg0, arg1);
            }
        } else {
            this._nextIntention.reset();
            super.changeIntention(intention, arg0, arg1);
        }
    }

    protected void onEvtReadyToAct() {
        if (!this._nextIntention.isBlank()) {
            this.setIntention(this._nextIntention.getIntention(), this._nextIntention.getFirstParameter(), this._nextIntention.getSecondParameter());
            this._nextIntention.reset();
        }

        super.onEvtReadyToAct();
    }

    protected void onEvtCancel() {
        this._nextIntention.reset();
        super.onEvtCancel();
    }

    protected void onEvtFinishCasting() {
        if (this._desire.getIntention() == IntentionType.CAST) {
            if (!this._nextIntention.isBlank() && this._nextIntention.getIntention() != IntentionType.CAST) {
                this.setIntention(this._nextIntention.getIntention(), this._nextIntention.getFirstParameter(), this._nextIntention.getSecondParameter());
            } else {
                this.setIntention(IntentionType.IDLE);
            }
        }

    }

    protected void onIntentionRest() {
        if (this._desire.getIntention() != IntentionType.REST) {
            this.changeIntention(IntentionType.REST, null, null);
            this.setTarget(null);
            this.clientStopMoving(null);
        }

    }

    protected void onIntentionActive() {
        this.setIntention(IntentionType.IDLE);
    }

    protected void onIntentionMoveTo(Location loc) {
        if (this._desire.getIntention() == IntentionType.REST) {
            this.clientActionFailed();
        } else if (!this._actor.isAllSkillsDisabled() && !this._actor.isCastingNow() && !this._actor.isAttackingNow()) {
            this.changeIntention(IntentionType.MOVE_TO, loc, null);
            this.moveTo(loc.getX(), loc.getY(), loc.getZ());
        } else {
            this.clientActionFailed();
            this._nextIntention.update(IntentionType.MOVE_TO, loc, null);
        }
    }

    protected void onIntentionInteract(WorldObject object) {
        if (this._desire.getIntention() == IntentionType.REST) {
            this.clientActionFailed();
        } else if (!this._actor.isAllSkillsDisabled() && !this._actor.isCastingNow()) {
            this.changeIntention(IntentionType.INTERACT, object, null);
            this.setTarget(object);
            this.moveToPawn(object, 60);
        } else {
            this.clientActionFailed();
            this._nextIntention.update(IntentionType.INTERACT, object, null);
        }
    }

    protected void clientNotifyDead() {
        this._clientMovingToPawnOffset = 0;
        this._clientMoving = false;
        super.clientNotifyDead();
    }

    public void startAttackStance() {
        if (!AttackStanceTaskManager.getInstance().isInAttackStance(this._actor)) {
            Summon summon = this._actor.getSummon();
            if (summon != null) {
                summon.broadcastPacket(new AutoAttackStart(summon.getObjectId()));
            }

            this._actor.broadcastPacket(new AutoAttackStart(this._actor.getObjectId()));
        }

        AttackStanceTaskManager.getInstance().add(this._actor);
    }

    private void thinkAttack() {
        Creature target = (Creature) this.getTarget();
        if (target == null) {
            this.setTarget(null);
            this.setIntention(IntentionType.ACTIVE);
        } else if (!this.maybeMoveToPawn(target, this._actor.getPhysicalAttackRange())) {
            if (target.isAlikeDead()) {
                if (!(target instanceof Player) || !((Player) target).isFakeDeath()) {
                    this.setIntention(IntentionType.ACTIVE);
                    return;
                }

                target.stopFakeDeath(true);
            }

            this.clientStopMoving(null);
            this._actor.doAttack(target);
        }
    }

    private void thinkCast() {
        Creature target = (Creature) this.getTarget();
        if (this._skill.getTargetType() == L2Skill.SkillTargetType.TARGET_GROUND && this._actor instanceof Player) {
            if (this.maybeMoveToPosition(((Player) this._actor).getCurrentSkillWorldPosition(), this._skill.getCastRange())) {
                this._actor.setIsCastingNow(false);
                return;
            }
        } else {
            if (this.checkTargetLost(target)) {
                if (this._skill.isOffensive() && this.getTarget() != null) {
                    this.setTarget(null);
                }

                this._actor.setIsCastingNow(false);
                return;
            }

            if (target != null && this.maybeMoveToPawn(target, this._skill.getCastRange())) {
                this._actor.setIsCastingNow(false);
                return;
            }
        }

        if (this._skill.getHitTime() > 50 && !this._skill.isSimultaneousCast()) {
            this.clientStopMoving(null);
        }

        this._actor.doCast(this._skill);
    }

    private void thinkPickUp() {
        if (!this._actor.isAllSkillsDisabled() && !this._actor.isCastingNow() && !this._actor.isAttackingNow()) {
            WorldObject target = this.getTarget();
            if (!this.checkTargetLost(target)) {
                if (!this.maybeMoveToPawn(target, 36)) {
                    this.setIntention(IntentionType.IDLE);
                    this._actor.getActingPlayer().doPickupItem(target);
                }
            }
        }
    }

    private void thinkInteract() {
        if (!this._actor.isAllSkillsDisabled() && !this._actor.isCastingNow()) {
            WorldObject target = this.getTarget();
            if (!this.checkTargetLost(target)) {
                if (!this.maybeMoveToPawn(target, 36)) {
                    if (!(target instanceof StaticObject)) {
                        this._actor.getActingPlayer().doInteract((Creature) target);
                    }

                    this.setIntention(IntentionType.IDLE);
                }
            }
        }
    }

    protected void onEvtThink() {
        if (!this._thinking || this._desire.getIntention() == IntentionType.CAST) {
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
}
