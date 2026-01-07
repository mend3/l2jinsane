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
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.SiegeGuard;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.location.Location;

import java.util.List;

public class SiegeGuardAI extends AttackableAI {
    public SiegeGuardAI(SiegeGuard guard) {
        super(guard);
    }

    protected boolean autoAttackCondition(Creature target) {
        if (!(target instanceof net.sf.l2j.gameserver.model.actor.Playable) || target.isAlikeDead())
            return false;
        Player player = target.getActingPlayer();
        if (player == null)
            return false;
        if (player.isGM() && player.getAppearance().getInvisible())
            return false;
        if (player.isSilentMoving() && !this._actor.isInsideRadius(player, 250, false, false))
            return false;
        return (this._actor.isAutoAttackable(target) && GeoEngine.getInstance().canSeeTarget(this._actor, target));
    }

    public synchronized void changeIntention(IntentionType intention, Object arg0, Object arg1) {
        if (intention == IntentionType.IDLE) {
            if (!this._actor.isAlikeDead())
                if (!getActiveChar().getKnownType(Player.class).isEmpty())
                    intention = IntentionType.ACTIVE;
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
        if (this._aiTask == null)
            this._aiTask = ThreadPool.scheduleAtFixedRate(this, 1000L, 1000L);
    }

    protected void thinkActive() {
        if (this._globalAggro != 0)
            if (this._globalAggro < 0) {
                this._globalAggro++;
            } else {
                this._globalAggro--;
            }
        if (this._globalAggro >= 0) {
            Attackable npc = (Attackable) this._actor;
            for (Creature target : npc.getKnownTypeInRadius(Creature.class, npc.getTemplate().getClanRange())) {
                if (autoAttackCondition(target))
                    if (npc.getHating(target) == 0)
                        npc.addDamageHate(target, 0, 1);
            }
            Creature hated = this._actor.isConfused() ? (Creature) getTarget() : npc.getMostHated();
            if (hated != null) {
                if (npc.getHating(hated) + this._globalAggro > 0) {
                    this._actor.setRunning();
                    setIntention(IntentionType.ATTACK, hated);
                }
                return;
            }
        }
        getActiveChar().returnHome();
    }

    protected void thinkAttack() {
        SiegeGuard actor = getActiveChar();
        if (actor.isCastingNow())
            return;
        if (!actor.isInsideZone(ZoneId.SIEGE)) {
            actor.returnHome();
            return;
        }
        Creature attackTarget = actor.getMostHated();
        if (attackTarget == null || this._attackTimeout < System.currentTimeMillis() || MathUtil.calculateDistance(actor, attackTarget, true) > 2000.0D) {
            actor.stopHating(attackTarget);
            attackTarget = targetReconsider(actor.getTemplate().getClanRange(), false);
            if (attackTarget == null) {
                setIntention(IntentionType.ACTIVE);
                actor.setWalking();
                return;
            }
        }
        int actorCollision = (int) actor.getCollisionRadius();
        int combinedCollision = (int) (actorCollision + attackTarget.getCollisionRadius());
        double dist = Math.sqrt(actor.getPlanDistanceSq(attackTarget.getX(), attackTarget.getY()));
        int range = combinedCollision;
        if (attackTarget.isMoving())
            range += 15;
        if (actor.isMoving())
            range += 15;
        setTarget(attackTarget);
        actor.setTarget(attackTarget);
        if (willCastASpell()) {
            List<L2Skill> defaultList = actor.getTemplate().getSkills(NpcTemplate.SkillType.HEAL);
            if (!defaultList.isEmpty()) {
                String[] clans = actor.getTemplate().getClans();
                for (Creature cha : actor.getKnownTypeInRadius(Creature.class, 1000)) {
                    if (cha.isAlikeDead() || !GeoEngine.getInstance().canSeeTarget(actor, cha) || cha.getCurrentHp() / cha.getMaxHp() > 0.75D)
                        continue;
                    if ((!actor.isAttackingDisabled() && cha instanceof Player && actor.getCastle().getSiege().checkSides(((Player) cha).getClan(), SiegeSide.DEFENDER, SiegeSide.OWNER)) || (cha instanceof Npc && ArraysUtil.contains(clans, (Object[]) ((Npc) cha).getTemplate().getClans())))
                        for (L2Skill sk : defaultList) {
                            if (!MathUtil.checkIfInRange(sk.getCastRange(), actor, cha, true))
                                continue;
                            clientStopMoving(null);
                            actor.setTarget(cha);
                            actor.doCast(sk);
                            actor.setTarget(attackTarget);
                            return;
                        }
                }
            }
            defaultList = actor.getTemplate().getSkills(NpcTemplate.SkillType.BUFF);
            if (!defaultList.isEmpty())
                for (L2Skill sk : defaultList) {
                    if (!checkSkillCastConditions(sk))
                        continue;
                    if (actor.getFirstEffect(sk) == null) {
                        clientStopMoving(null);
                        actor.setTarget(actor);
                        actor.doCast(sk);
                        actor.setTarget(attackTarget);
                        return;
                    }
                }
            defaultList = actor.getTemplate().getSkills(NpcTemplate.SkillType.DEBUFF);
            if (Rnd.get(100) < 10 && !defaultList.isEmpty())
                for (L2Skill sk : defaultList) {
                    if (!checkSkillCastConditions(sk) || ((sk.getCastRange() + range) <= dist && !canAura(sk)))
                        continue;
                    if (!GeoEngine.getInstance().canSeeTarget(actor, attackTarget))
                        continue;
                    if (attackTarget.getFirstEffect(sk) == null) {
                        clientStopMoving(null);
                        actor.doCast(sk);
                        return;
                    }
                }
            defaultList = actor.getTemplate().getSkills(NpcTemplate.SkillType.SHORT_RANGE);
            if (!defaultList.isEmpty() && dist <= 150.0D) {
                L2Skill skill = Rnd.get(defaultList);
                if (cast(skill, dist, skill.getCastRange()))
                    return;
            } else {
                defaultList = actor.getTemplate().getSkills(NpcTemplate.SkillType.LONG_RANGE);
                if (!defaultList.isEmpty() && dist > 150.0D) {
                    L2Skill skill = Rnd.get(defaultList);
                    if (cast(skill, dist, skill.getCastRange()))
                        return;
                }
            }
        }
        range += actor.getPhysicalAttackRange();
        if (actor.isMovementDisabled()) {
            if (dist > range)
                attackTarget = targetReconsider(range, true);
            if (attackTarget != null)
                actor.doAttack(attackTarget);
            return;
        }
        if (Rnd.get(100) <= 3)
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
                        if (GeoEngine.getInstance().canMoveToTarget(actor.getX(), actor.getY(), actor.getZ(), newX, newY, newZ))
                            moveTo(newX, newY, newZ);
                    }
                    return;
                }
            }
        if (actor.getTemplate().getAiType() == NpcTemplate.AIType.ARCHER && dist <= (60 + combinedCollision) && Rnd.get(4) > 1) {
            int posX = actor.getX() + ((attackTarget.getX() < actor.getX()) ? 300 : -300);
            int posY = actor.getY() + ((attackTarget.getY() < actor.getY()) ? 300 : -300);
            int posZ = actor.getZ() + 30;
            if (GeoEngine.getInstance().canMoveToTarget(actor.getX(), actor.getY(), actor.getZ(), posX, posY, posZ)) {
                setIntention(IntentionType.MOVE_TO, new Location(posX, posY, posZ));
                return;
            }
        }
        if (maybeMoveToPawn(getTarget(), actor.getPhysicalAttackRange()))
            return;
        clientStopMoving(null);
        this._actor.doAttack((Creature) getTarget());
    }

    protected Creature targetReconsider(int range, boolean rangeCheck) {
        SiegeGuard siegeGuard = getActiveChar();
        if (!siegeGuard.getAggroList().isEmpty()) {
            Creature previousMostHated = siegeGuard.getMostHated();
            int aggroMostHated = siegeGuard.getHating(previousMostHated);
            for (Creature obj : siegeGuard.getHateList()) {
                if (!autoAttackCondition(obj))
                    continue;
                if (rangeCheck) {
                    double dist = Math.sqrt(siegeGuard.getPlanDistanceSq(obj.getX(), obj.getY())) - obj.getCollisionRadius();
                    if (siegeGuard.isMoving())
                        dist -= 15.0D;
                    if (obj.isMoving())
                        dist -= 15.0D;
                    if (dist > range)
                        continue;
                }
                siegeGuard.stopHating(previousMostHated);
                siegeGuard.addDamageHate(obj, 0, (aggroMostHated > 0) ? aggroMostHated : 2000);
                return obj;
            }
        }
        return null;
    }

    public void stopAITask() {
        super.stopAITask();
        this._actor.detachAI();
    }

    private SiegeGuard getActiveChar() {
        return (SiegeGuard) this._actor;
    }
}
