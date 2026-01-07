package net.sf.l2j.gameserver.model.actor.ai.type;

import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.actor.ai.Desire;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.AutoAttackStart;
import net.sf.l2j.gameserver.taskmanager.AttackStanceTaskManager;

public class PlayerAI extends PlayableAI {
    private final Desire _nextIntention = new Desire();
    private boolean _thinking;

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
        if (intention != IntentionType.CAST || (arg0 != null && ((L2Skill) arg0).isOffensive())) {
            this._nextIntention.reset();
            super.changeIntention(intention, arg0, arg1);
            return;
        }
        if (this._desire.equals(intention, arg0, arg1))
            return;
        this._nextIntention.update(this._desire);
        super.changeIntention(intention, arg0, arg1);
    }

    protected void onEvtReadyToAct() {
        if (!this._nextIntention.isBlank()) {
            setIntention(this._nextIntention.getIntention(), this._nextIntention.getFirstParameter(), this._nextIntention.getSecondParameter());
            this._nextIntention.reset();
        }
        super.onEvtReadyToAct();
    }

    protected void onEvtCancel() {
        this._nextIntention.reset();
        super.onEvtCancel();
    }

    protected void onEvtFinishCasting() {
        if (this._desire.getIntention() == IntentionType.CAST)
            if (!this._nextIntention.isBlank() && this._nextIntention.getIntention() != IntentionType.CAST) {
                setIntention(this._nextIntention.getIntention(), this._nextIntention.getFirstParameter(), this._nextIntention.getSecondParameter());
            } else {
                setIntention(IntentionType.IDLE);
            }
    }

    protected void onIntentionRest() {
        if (this._desire.getIntention() != IntentionType.REST) {
            changeIntention(IntentionType.REST, null, null);
            setTarget(null);
            clientStopMoving(null);
        }
    }

    protected void onIntentionActive() {
        setIntention(IntentionType.IDLE);
    }

    protected void onIntentionMoveTo(Location loc) {
        if (this._desire.getIntention() == IntentionType.REST) {
            clientActionFailed();
            return;
        }
        if (this._actor.isAllSkillsDisabled() || this._actor.isCastingNow() || this._actor.isAttackingNow()) {
            clientActionFailed();
            this._nextIntention.update(IntentionType.MOVE_TO, loc, null);
            return;
        }
        changeIntention(IntentionType.MOVE_TO, loc, null);
        moveTo(loc.getX(), loc.getY(), loc.getZ());
    }

    protected void onIntentionInteract(WorldObject object) {
        if (this._desire.getIntention() == IntentionType.REST) {
            clientActionFailed();
            return;
        }
        if (this._actor.isAllSkillsDisabled() || this._actor.isCastingNow()) {
            clientActionFailed();
            this._nextIntention.update(IntentionType.INTERACT, object, null);
            return;
        }
        changeIntention(IntentionType.INTERACT, object, null);
        setTarget(object);
        moveToPawn(object, 60);
    }

    protected void clientNotifyDead() {
        this._clientMovingToPawnOffset = 0;
        this._clientMoving = false;
        super.clientNotifyDead();
    }

    public void startAttackStance() {
        if (!AttackStanceTaskManager.getInstance().isInAttackStance(this._actor)) {
            Summon summon = this._actor.getSummon();
            if (summon != null)
                summon.broadcastPacket(new AutoAttackStart(summon.getObjectId()));
            this._actor.broadcastPacket(new AutoAttackStart(this._actor.getObjectId()));
        }
        AttackStanceTaskManager.getInstance().add(this._actor);
    }

    private void thinkAttack() {
        Creature target = (Creature) getTarget();
        if (target == null) {
            setTarget(null);
            setIntention(IntentionType.ACTIVE);
            return;
        }
        if (maybeMoveToPawn(target, this._actor.getPhysicalAttackRange()))
            return;
        if (target.isAlikeDead())
            if (target instanceof Player && ((Player) target).isFakeDeath()) {
                target.stopFakeDeath(true);
            } else {
                setIntention(IntentionType.ACTIVE);
                return;
            }
        clientStopMoving(null);
        this._actor.doAttack(target);
    }

    private void thinkCast() {
        Creature target = (Creature) getTarget();
        if (this._skill.getTargetType() == L2Skill.SkillTargetType.TARGET_GROUND && this._actor instanceof Player) {
            if (maybeMoveToPosition(((Player) this._actor).getCurrentSkillWorldPosition(), this._skill.getCastRange())) {
                this._actor.setIsCastingNow(false);
                return;
            }
        } else {
            if (checkTargetLost(target)) {
                if (this._skill.isOffensive() && getTarget() != null)
                    setTarget(null);
                this._actor.setIsCastingNow(false);
                return;
            }
            if (target != null && maybeMoveToPawn(target, this._skill.getCastRange())) {
                this._actor.setIsCastingNow(false);
                return;
            }
        }
        if (this._skill.getHitTime() > 50 && !this._skill.isSimultaneousCast())
            clientStopMoving(null);
        this._actor.doCast(this._skill);
    }

    private void thinkPickUp() {
        if (this._actor.isAllSkillsDisabled() || this._actor.isCastingNow() || this._actor.isAttackingNow())
            return;
        WorldObject target = getTarget();
        if (checkTargetLost(target))
            return;
        if (maybeMoveToPawn(target, 36))
            return;
        setIntention(IntentionType.IDLE);
        this._actor.getActingPlayer().doPickupItem(target);
    }

    private void thinkInteract() {
        if (this._actor.isAllSkillsDisabled() || this._actor.isCastingNow())
            return;
        WorldObject target = getTarget();
        if (checkTargetLost(target))
            return;
        if (maybeMoveToPawn(target, 36))
            return;
        if (!(target instanceof net.sf.l2j.gameserver.model.actor.instance.StaticObject))
            this._actor.getActingPlayer().doInteract((Creature) target);
        setIntention(IntentionType.IDLE);
    }

    protected void onEvtThink() {
        if (this._thinking && this._desire.getIntention() != IntentionType.CAST)
            return;
        this._thinking = true;
        try {
            switch (this._desire.getIntention()) {
                case ATTACK:
                    thinkAttack();
                    break;
                case CAST:
                    thinkCast();
                    break;
                case PICK_UP:
                    thinkPickUp();
                    break;
                case INTERACT:
                    thinkInteract();
                    break;
            }
        } finally {
            this._thinking = false;
        }
    }
}
