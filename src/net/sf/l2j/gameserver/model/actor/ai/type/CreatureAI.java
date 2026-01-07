/**/
package net.sf.l2j.gameserver.model.actor.ai.type;

import net.sf.l2j.commons.util.ArraysUtil;
import net.sf.l2j.gameserver.enums.AiEventType;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.*;
import net.sf.l2j.gameserver.model.actor.ai.Desire;
import net.sf.l2j.gameserver.model.actor.instance.Door;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance.ItemLocation;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.location.SpawnLocation;

import java.util.Iterator;

public class CreatureAI extends AbstractAI {
    public CreatureAI(Creature character) {
        super(character);
    }

    public Desire getNextIntention() {
        return null;
    }

    protected void onEvtAttacked(Creature attacker) {
    }

    protected void onIntentionIdle() {
        this.changeIntention(IntentionType.IDLE, null, null);
        this.setTarget(null);
        this.clientStopMoving(null);
    }

    protected void onIntentionActive() {
        if (this._desire.getIntention() != IntentionType.ACTIVE) {
            this.changeIntention(IntentionType.ACTIVE, null, null);
            this.setTarget(null);
            this.clientStopMoving(null);
            if (this._actor instanceof Attackable) {
                ((Npc) this._actor).startRandomAnimationTimer();
            }

            this.onEvtThink();
        }

    }

    protected void onIntentionRest() {
        this.setIntention(IntentionType.IDLE);
    }

    protected void onIntentionAttack(Creature target) {
        if (target == null) {
            this.clientActionFailed();
        } else if (this._desire.getIntention() == IntentionType.REST) {
            this.clientActionFailed();
        } else if (!this._actor.isAllSkillsDisabled() && !this._actor.isCastingNow() && !this._actor.isAfraid()) {
            if (this._desire.getIntention() == IntentionType.ATTACK) {
                if (this.getTarget() != target) {
                    this.setTarget(target);
                    this.stopFollow();
                    this.notifyEvent(AiEventType.THINK, null);
                } else {
                    this.clientActionFailed();
                    if (this.getActor() instanceof Playable && this.getActor().isAttackingNow() && !target.isAutoAttackable(this.getActor())) {
                        this.changeIntention(IntentionType.IDLE, null, null);
                    }
                }
            } else {
                this.changeIntention(IntentionType.ATTACK, target, null);
                this.setTarget(target);
                this.stopFollow();
                this.notifyEvent(AiEventType.THINK, null);
            }

        } else {
            this.clientActionFailed();
        }
    }

    protected void onIntentionCast(L2Skill skill, WorldObject target) {
        if (this._desire.getIntention() == IntentionType.REST && skill.isMagic()) {
            this.clientActionFailed();
            this._actor.setIsCastingNow(false);
        } else {
            this.setTarget(target);
            this._skill = skill;
            this.changeIntention(IntentionType.CAST, skill, target);
            this.notifyEvent(AiEventType.THINK, null);
        }
    }

    protected void onIntentionMoveTo(Location loc) {
        if (this._desire.getIntention() == IntentionType.REST) {
            this.clientActionFailed();
        } else if (!this._actor.isAllSkillsDisabled() && !this._actor.isCastingNow()) {
            this.changeIntention(IntentionType.MOVE_TO, loc, null);
            this.moveTo(loc.getX(), loc.getY(), loc.getZ());
        } else {
            this.clientActionFailed();
        }
    }

    protected void onIntentionFollow(Creature target) {
        if (this._desire.getIntention() == IntentionType.REST) {
            this.clientActionFailed();
        } else if (!this._actor.isAllSkillsDisabled() && !this._actor.isCastingNow()) {
            if (this._actor.isMovementDisabled()) {
                this.clientActionFailed();
            } else if (this._actor.isDead()) {
                this.clientActionFailed();
            } else if (this._actor == target) {
                this.clientActionFailed();
            } else {
                this.changeIntention(IntentionType.FOLLOW, target, null);
                this.startFollow(target);
            }
        } else {
            this.clientActionFailed();
        }
    }

    protected void onIntentionPickUp(WorldObject object) {
        if (this._desire.getIntention() == IntentionType.REST) {
            this.clientActionFailed();
        } else if (!this._actor.isAllSkillsDisabled() && !this._actor.isCastingNow() && !this._actor.isAttackingNow()) {
            if (!(object instanceof ItemInstance) || ((ItemInstance) object).getLocation() == ItemLocation.VOID) {
                this.changeIntention(IntentionType.PICK_UP, object, null);
                this.setTarget(object);
                this.moveToPawn(object, 20);
            }
        } else {
            this.clientActionFailed();
        }
    }

    protected void onIntentionInteract(WorldObject object) {
    }

    protected void onEvtThink() {
    }

    protected void onEvtAggression(Creature target, int aggro) {
    }

    protected void onEvtStunned(Creature attacker) {
        this.clientStopMoving(null);
        this.onEvtAttacked(attacker);
    }

    protected void onEvtParalyzed(Creature attacker) {
        this.clientStopMoving(null);
        this.onEvtAttacked(attacker);
    }

    protected void onEvtSleeping(Creature attacker) {
        this.clientStopMoving(null);
    }

    protected void onEvtRooted(Creature attacker) {
        this.clientStopMoving(null);
        this.onEvtAttacked(attacker);
    }

    protected void onEvtConfused(Creature attacker) {
        this.clientStopMoving(null);
        this.onEvtAttacked(attacker);
    }

    protected void onEvtMuted(Creature attacker) {
        this.onEvtAttacked(attacker);
    }

    protected void onEvtEvaded(Creature attacker) {
    }

    protected void onEvtReadyToAct() {
        this.onEvtThink();
    }

    protected void onEvtArrived() {
        this._actor.revalidateZone(true);
        if (!this._actor.moveToNextRoutePoint()) {
            if (this._actor instanceof Attackable) {
                ((Attackable) this._actor).setIsReturningToSpawnPoint(false);
            }

            this.clientStoppedMoving();
            if (this._desire.getIntention() == IntentionType.MOVE_TO) {
                this.setIntention(IntentionType.ACTIVE);
            }

            this.onEvtThink();
        }
    }

    protected void onEvtArrivedBlocked(SpawnLocation loc) {
        if (this._desire.getIntention() == IntentionType.MOVE_TO || this._desire.getIntention() == IntentionType.CAST) {
            this.setIntention(IntentionType.ACTIVE);
        }

        this.clientStopMoving(loc);
        this.onEvtThink();
    }

    protected void onEvtCancel() {
        this._actor.abortCast();
        this.stopFollow();
        this.onEvtThink();
    }

    protected void onEvtDead() {
        this.stopAITask();
        this.clientNotifyDead();
        if (!(this._actor instanceof Playable)) {
            this._actor.setWalking();
        }

    }

    protected void onEvtFakeDeath() {
        this.stopFollow();
        this.clientStopMoving(null);
        this._desire.update(IntentionType.IDLE, null, null);
        this.setTarget(null);
    }

    protected void onEvtFinishCasting() {
    }

    protected boolean maybeMoveToPosition(Location worldPosition, int offset) {
        if (worldPosition == null) {
            return false;
        } else if (offset < 0) {
            return false;
        } else if (!this._actor.isInsideRadius(worldPosition.getX(), worldPosition.getY(), (int) ((double) offset + this._actor.getCollisionRadius()), false)) {
            if (this._actor.isMovementDisabled()) {
                return true;
            } else {
                if (!(this instanceof PlayerAI) && !(this instanceof SummonAI)) {
                    this._actor.setRunning();
                }

                this.stopFollow();
                int x = this._actor.getX();
                int y = this._actor.getY();
                double dx = worldPosition.getX() - x;
                double dy = worldPosition.getY() - y;
                double dist = Math.sqrt(dx * dx + dy * dy);
                double sin = dy / dist;
                double cos = dx / dist;
                dist -= offset - 5;
                x += (int) (dist * cos);
                y += (int) (dist * sin);
                this.moveTo(x, y, worldPosition.getZ());
                return true;
            }
        } else {
            if (this.getFollowTarget() != null) {
                this.stopFollow();
            }

            return false;
        }
    }

    protected boolean maybeMoveToPawn(WorldObject target, int offset) {
        if (target != null && offset >= 0) {
            offset = (int) ((double) offset + this._actor.getCollisionRadius());
            if (target instanceof Creature) {
                offset = (int) ((double) offset + ((Creature) target).getCollisionRadius());
            }

            if (!this._actor.isInsideRadius(target, offset, false, false)) {
                if (this.getFollowTarget() != null) {
                    if (!this._actor.isInsideRadius(target, offset + 100, false, false)) {
                        return true;
                    } else {
                        this.stopFollow();
                        return false;
                    }
                } else if (this._actor.isMovementDisabled()) {
                    if (this._desire.getIntention() == IntentionType.ATTACK) {
                        this.setIntention(IntentionType.IDLE);
                        this.clientActionFailed();
                    }

                    return true;
                } else {
                    if (!(this instanceof PlayerAI) && !(this instanceof SummonAI)) {
                        this._actor.setRunning();
                    }

                    this.stopFollow();
                    if (target instanceof Creature && !(target instanceof Door)) {
                        if (((Creature) target).isMoving()) {
                            offset -= 30;
                        }

                        if (offset < 5) {
                            offset = 5;
                        }

                        this.startFollow((Creature) target, offset);
                    } else {
                        this.moveToPawn(target, offset);
                    }

                    return true;
                }
            } else {
                if (this.getFollowTarget() != null) {
                    this.stopFollow();
                }

                return false;
            }
        } else {
            return false;
        }
    }

    protected boolean checkTargetLostOrDead(Creature target) {
        if (target != null && !target.isAlikeDead()) {
            return false;
        } else if (target instanceof Player && ((Player) target).isFakeDeath()) {
            target.stopFakeDeath(true);
            return false;
        } else {
            this.setIntention(IntentionType.ACTIVE);
            return true;
        }
    }

    protected boolean checkTargetLost(WorldObject target) {
        if (target instanceof Player victim) {
            if (victim.isFakeDeath()) {
                victim.stopFakeDeath(true);
                return false;
            }
        }

        if (target == null) {
            this.setIntention(IntentionType.ACTIVE);
            return true;
        } else {
            return false;
        }
    }

    public boolean canAura(L2Skill sk) {
        if (sk.getTargetType() == SkillTargetType.TARGET_AURA || sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AURA || sk.getTargetType() == SkillTargetType.TARGET_FRONT_AURA) {
            Iterator var2 = this._actor.getKnownTypeInRadius(Creature.class, sk.getSkillRadius()).iterator();

            while (var2.hasNext()) {
                WorldObject target = (WorldObject) var2.next();
                if (target == this.getTarget()) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean canAOE(L2Skill sk) {
        boolean cancast;
        Iterator var3;
        Creature target;
        L2Effect[] effects;
        if (sk.getSkillType() == L2SkillType.NEGATE && sk.getSkillType() == L2SkillType.CANCEL) {
            if (sk.getTargetType() != SkillTargetType.TARGET_AURA && sk.getTargetType() != SkillTargetType.TARGET_BEHIND_AURA && sk.getTargetType() != SkillTargetType.TARGET_FRONT_AURA) {
                if (sk.getTargetType() == SkillTargetType.TARGET_AREA || sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AREA || sk.getTargetType() == SkillTargetType.TARGET_FRONT_AREA) {
                    cancast = true;
                    var3 = this.getTarget().getKnownTypeInRadius(Creature.class, sk.getSkillRadius()).iterator();

                    while (true) {
                        do {
                            do {
                                if (!var3.hasNext()) {
                                    return cancast;
                                }

                                target = (Creature) var3.next();
                            } while (!GeoEngine.getInstance().canSeeTarget(this._actor, target));
                        } while (target instanceof Attackable && !this._actor.isConfused());

                        if (target.getFirstEffect(sk) != null) {
                            cancast = false;
                        }
                    }
                }
            } else {
                cancast = false;
                var3 = this._actor.getKnownTypeInRadius(Creature.class, sk.getSkillRadius()).iterator();

                while (true) {
                    do {
                        do {
                            if (!var3.hasNext()) {
                                return cancast;
                            }

                            target = (Creature) var3.next();
                        } while (!GeoEngine.getInstance().canSeeTarget(this._actor, target));
                    } while (target instanceof Attackable && !this._actor.isConfused());

                    effects = target.getAllEffects();
                    if (effects.length > 0) {
                        cancast = true;
                    }
                }
            }
        } else if (sk.getTargetType() != SkillTargetType.TARGET_AURA && sk.getTargetType() != SkillTargetType.TARGET_BEHIND_AURA && sk.getTargetType() != SkillTargetType.TARGET_FRONT_AURA) {
            if (sk.getTargetType() == SkillTargetType.TARGET_AREA || sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AREA || sk.getTargetType() == SkillTargetType.TARGET_FRONT_AREA) {
                cancast = true;
                var3 = this.getTarget().getKnownTypeInRadius(Creature.class, sk.getSkillRadius()).iterator();

                while (true) {
                    do {
                        do {
                            if (!var3.hasNext()) {
                                return cancast;
                            }

                            target = (Creature) var3.next();
                        } while (!GeoEngine.getInstance().canSeeTarget(this._actor, target));
                    } while (target instanceof Attackable && !this._actor.isConfused());

                    effects = target.getAllEffects();
                    if (effects.length > 0) {
                        cancast = true;
                    }
                }
            }
        } else {
            cancast = true;
            var3 = this._actor.getKnownTypeInRadius(Creature.class, sk.getSkillRadius()).iterator();

            while (true) {
                do {
                    do {
                        if (!var3.hasNext()) {
                            return cancast;
                        }

                        target = (Creature) var3.next();
                    } while (!GeoEngine.getInstance().canSeeTarget(this._actor, target));
                } while (target instanceof Attackable && !this._actor.isConfused());

                if (target.getFirstEffect(sk) != null) {
                    cancast = false;
                }
            }
        }

        return false;
    }

    public boolean canParty(L2Skill sk) {
        if (sk.getTargetType() != SkillTargetType.TARGET_PARTY) {
            return false;
        } else {
            int count = 0;
            int ccount = 0;
            String[] actorClans = ((Npc) this._actor).getTemplate().getClans();
            Iterator var5 = this._actor.getKnownTypeInRadius(Attackable.class, sk.getSkillRadius()).iterator();

            while (var5.hasNext()) {
                Attackable target = (Attackable) var5.next();
                if (GeoEngine.getInstance().canSeeTarget(this._actor, target) && ArraysUtil.contains(actorClans, target.getTemplate().getClans())) {
                    ++count;
                    if (target.getFirstEffect(sk) != null) {
                        ++ccount;
                    }
                }
            }

            return ccount < count;
        }
    }
}