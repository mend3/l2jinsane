package net.sf.l2j.gameserver.model.actor.ai.type;

import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.util.ArraysUtil;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.SiegeSide;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.actor.*;
import net.sf.l2j.gameserver.model.actor.instance.SiegeGuard;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.location.SpawnLocation;

import java.util.Iterator;
import java.util.List;

public class SiegeGuardAI extends AttackableAI {
    public SiegeGuardAI(SiegeGuard guard) {
        super(guard);
    }

    protected boolean autoAttackCondition(Creature target) {
        if (target instanceof Playable && !target.isAlikeDead()) {
            Player player = target.getActingPlayer();
            if (player == null) {
                return false;
            } else if (player.isGM() && player.getAppearance().getInvisible()) {
                return false;
            } else if (player.isSilentMoving() && !this._actor.isInsideRadius(player, 250, false, false)) {
                return false;
            } else {
                return this._actor.isAutoAttackable(target) && GeoEngine.getInstance().canSeeTarget(this._actor, target);
            }
        } else {
            return false;
        }
    }

    public synchronized void changeIntention(IntentionType intention, Object arg0, Object arg1) {
        if (intention == IntentionType.IDLE) {
            if (!this._actor.isAlikeDead() && !this.getActiveChar().getKnownType(Player.class).isEmpty()) {
                intention = IntentionType.ACTIVE;
            }

            if (intention == IntentionType.IDLE) {
                super.changeIntention(IntentionType.IDLE, null, null);
                if (this._aiTask != null) {
                    this._aiTask.cancel(true);
                    this._aiTask = null;
                }

                this._actor.detachAI();
                return;
            }
        }

        super.changeIntention(intention, arg0, arg1);
        if (this._aiTask == null) {
            this._aiTask = ThreadPool.scheduleAtFixedRate(this, 1000L, 1000L);
        }

    }

    protected void thinkActive() {
        if (this._globalAggro != 0) {
            if (this._globalAggro < 0) {
                ++this._globalAggro;
            } else {
                --this._globalAggro;
            }
        }

        if (this._globalAggro >= 0) {
            Attackable npc = (Attackable) this._actor;

            for (Creature target : npc.getKnownTypeInRadius(Creature.class, npc.getTemplate().getClanRange())) {
                if (this.autoAttackCondition(target) && npc.getHating(target) == 0) {
                    npc.addDamageHate(target, 0, 1);
                }
            }

            Creature hated = (Creature) (this._actor.isConfused() ? this.getTarget() : npc.getMostHated());
            if (hated != null) {
                if (npc.getHating(hated) + this._globalAggro > 0) {
                    this._actor.setRunning();
                    this.setIntention(IntentionType.ATTACK, hated);
                }

                return;
            }
        }

        this.getActiveChar().returnHome();
    }

    protected void thinkAttack() {
        SiegeGuard actor = this.getActiveChar();
        if (!actor.isCastingNow()) {
            if (!actor.isInsideZone(ZoneId.SIEGE)) {
                actor.returnHome();
            } else {
                Creature attackTarget = actor.getMostHated();
                if (attackTarget == null || this._attackTimeout < System.currentTimeMillis() || MathUtil.calculateDistance(actor, attackTarget, true) > (double) 2000.0F) {
                    actor.stopHating(attackTarget);
                    attackTarget = this.targetReconsider(actor.getTemplate().getClanRange(), false);
                    if (attackTarget == null) {
                        this.setIntention(IntentionType.ACTIVE);
                        actor.setWalking();
                        return;
                    }
                }

                int actorCollision = (int) actor.getCollisionRadius();
                int combinedCollision = (int) ((double) actorCollision + attackTarget.getCollisionRadius());
                double dist = Math.sqrt(actor.getPlanDistanceSq(attackTarget.getX(), attackTarget.getY()));
                int range = combinedCollision;
                if (attackTarget.isMoving()) {
                    range = combinedCollision + 15;
                }

                if (actor.isMoving()) {
                    range += 15;
                }

                this.setTarget(attackTarget);
                actor.setTarget(attackTarget);
                if (this.willCastASpell()) {
                    List<L2Skill> defaultList = actor.getTemplate().getSkills(NpcTemplate.SkillType.HEAL);
                    if (!defaultList.isEmpty()) {
                        String[] clans = actor.getTemplate().getClans();

                        for (Creature cha : actor.getKnownTypeInRadius(Creature.class, 1000)) {
                            if (!cha.isAlikeDead() && GeoEngine.getInstance().canSeeTarget(actor, cha) && !(cha.getCurrentHp() / (double) cha.getMaxHp() > (double) 0.75F) && (!actor.isAttackingDisabled() && cha instanceof Player && actor.getCastle().getSiege().checkSides(((Player) cha).getClan(), new SiegeSide[]{SiegeSide.DEFENDER, SiegeSide.OWNER}) || cha instanceof Npc && ArraysUtil.contains(clans, ((Npc) cha).getTemplate().getClans()))) {
                                for (L2Skill sk : defaultList) {
                                    if (MathUtil.checkIfInRange(sk.getCastRange(), actor, cha, true)) {
                                        this.clientStopMoving(null);
                                        actor.setTarget(cha);
                                        actor.doCast(sk);
                                        actor.setTarget(attackTarget);
                                        return;
                                    }
                                }
                            }
                        }
                    }

                    defaultList = actor.getTemplate().getSkills(NpcTemplate.SkillType.BUFF);
                    if (!defaultList.isEmpty()) {
                        for (L2Skill sk : defaultList) {
                            if (this.checkSkillCastConditions(sk) && actor.getFirstEffect(sk) == null) {
                                this.clientStopMoving(null);
                                actor.setTarget(actor);
                                actor.doCast(sk);
                                actor.setTarget(attackTarget);
                                return;
                            }
                        }
                    }

                    defaultList = actor.getTemplate().getSkills(NpcTemplate.SkillType.DEBUFF);
                    if (Rnd.get(100) < 10 && !defaultList.isEmpty()) {
                        for (L2Skill sk : defaultList) {
                            if (this.checkSkillCastConditions(sk) && (!((double) (sk.getCastRange() + range) <= dist) || this.canAura(sk)) && GeoEngine.getInstance().canSeeTarget(actor, attackTarget) && attackTarget.getFirstEffect(sk) == null) {
                                this.clientStopMoving(null);
                                actor.doCast(sk);
                                return;
                            }
                        }
                    }

                    defaultList = actor.getTemplate().getSkills(NpcTemplate.SkillType.SHORT_RANGE);
                    if (!defaultList.isEmpty() && dist <= (double) 150.0F) {
                        L2Skill skill = Rnd.get(defaultList);
                        if (this.cast(skill, dist, skill.getCastRange())) {
                            return;
                        }
                    } else {
                        defaultList = actor.getTemplate().getSkills(NpcTemplate.SkillType.LONG_RANGE);
                        if (!defaultList.isEmpty() && dist > (double) 150.0F) {
                            L2Skill skill = Rnd.get(defaultList);
                            if (this.cast(skill, dist, skill.getCastRange())) {
                                return;
                            }
                        }
                    }
                }

                range += actor.getPhysicalAttackRange();
                if (actor.isMovementDisabled()) {
                    if (dist > (double) range) {
                        attackTarget = this.targetReconsider(range, true);
                    }

                    if (attackTarget != null) {
                        actor.doAttack(attackTarget);
                    }

                } else {
                    if (Rnd.get(100) <= 3) {
                        for (Attackable nearby : actor.getKnownTypeInRadius(Attackable.class, actorCollision)) {
                            if (nearby != attackTarget) {
                                int newX = combinedCollision + Rnd.get(40);
                                if (Rnd.nextBoolean()) {
                                    newX = attackTarget.getX() + newX;
                                } else {
                                    newX = attackTarget.getX() - newX;
                                }

                                int newY = combinedCollision + Rnd.get(40);
                                if (Rnd.nextBoolean()) {
                                    newY = attackTarget.getY() + newY;
                                } else {
                                    newY = attackTarget.getY() - newY;
                                }

                                if (!actor.isInsideRadius(newX, newY, actorCollision, false)) {
                                    int newZ = actor.getZ() + 30;
                                    if (GeoEngine.getInstance().canMoveToTarget(actor.getX(), actor.getY(), actor.getZ(), newX, newY, newZ)) {
                                        this.moveTo(newX, newY, newZ);
                                    }
                                }

                                return;
                            }
                        }
                    }

                    if (actor.getTemplate().getAiType() == NpcTemplate.AIType.ARCHER && dist <= (double) (60 + combinedCollision) && Rnd.get(4) > 1) {
                        int posX = actor.getX() + (attackTarget.getX() < actor.getX() ? 300 : -300);
                        int posY = actor.getY() + (attackTarget.getY() < actor.getY() ? 300 : -300);
                        int posZ = actor.getZ() + 30;
                        if (GeoEngine.getInstance().canMoveToTarget(actor.getX(), actor.getY(), actor.getZ(), posX, posY, posZ)) {
                            this.setIntention(IntentionType.MOVE_TO, new Location(posX, posY, posZ));
                            return;
                        }
                    }

                    if (!this.maybeMoveToPawn(this.getTarget(), actor.getPhysicalAttackRange())) {
                        this.clientStopMoving(null);
                        this._actor.doAttack((Creature) this.getTarget());
                    }
                }
            }
        }
    }

    protected Creature targetReconsider(int range, boolean rangeCheck) {
        Attackable actor = this.getActiveChar();
        if (!actor.getAggroList().isEmpty()) {
            Creature previousMostHated = actor.getMostHated();
            int aggroMostHated = actor.getHating(previousMostHated);
            Iterator<Creature> var6 = actor.getHateList().iterator();

            Creature obj;
            while (true) {
                if (!var6.hasNext()) {
                    return null;
                }

                obj = var6.next();
                if (this.autoAttackCondition(obj)) {
                    if (!rangeCheck) {
                        break;
                    }

                    double dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY())) - obj.getCollisionRadius();
                    if (actor.isMoving()) {
                        dist -= 15.0F;
                    }

                    if (obj.isMoving()) {
                        dist -= 15.0F;
                    }

                    if (!(dist > (double) range)) {
                        break;
                    }
                }
            }

            actor.stopHating(previousMostHated);
            actor.addDamageHate(obj, 0, aggroMostHated > 0 ? aggroMostHated : 2000);
            return obj;
        } else {
            return null;
        }
    }

    public void stopAITask() {
        super.stopAITask();
        this._actor.detachAI();
    }

    private SiegeGuard getActiveChar() {
        return (SiegeGuard) this._actor;
    }
}
