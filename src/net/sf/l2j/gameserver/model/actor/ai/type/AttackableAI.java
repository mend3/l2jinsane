/**/
package net.sf.l2j.gameserver.model.actor.ai.type;

import net.sf.l2j.Config;
import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.commons.util.ArraysUtil;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.ScriptEventType;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.L2Skill.SkillTargetType;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.*;
import net.sf.l2j.gameserver.model.actor.instance.*;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate.AIType;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate.SkillType;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.scripting.Quest;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class AttackableAI extends CreatureAI implements Runnable {
    protected static final int RANDOM_WALK_RATE = 30;
    protected static final int MAX_ATTACK_TIMEOUT = 90000;
    private final Set<Creature> _seenCreatures = ConcurrentHashMap.newKeySet();
    protected Future<?> _aiTask;
    protected long _attackTimeout = Long.MAX_VALUE;
    protected int _globalAggro = -10;
    protected boolean _thinking;

    public AttackableAI(Attackable attackable) {
        super(attackable);
        this._seenCreatures.clear();
    }

    public void run() {
        this.onEvtThink();
    }

    protected boolean autoAttackCondition(Creature target) {
        if (target != null && !(target instanceof Door) && !target.isAlikeDead()) {
            Attackable me = this.getActiveChar();
            if (target instanceof Playable) {
                if (!me.isInsideRadius(target, me.getTemplate().getAggroRange(), true, false)) {
                    return false;
                }

                if (!me.isRaidRelated() && !me.canSeeThroughSilentMove() && ((Playable) target).isSilentMoving()) {
                    return false;
                }

                Player targetPlayer = target.getActingPlayer();
                if (targetPlayer != null) {
                    if (targetPlayer.isGM() && (targetPlayer.getAppearance().getInvisible() || !targetPlayer.getAccessLevel().canTakeAggro())) {
                        return false;
                    }

                    if (ArraysUtil.contains(me.getTemplate().getClans(), "varka_silenos_clan") && targetPlayer.isAlliedWithVarka()) {
                        return false;
                    }

                    if (ArraysUtil.contains(me.getTemplate().getClans(), "ketra_orc_clan") && targetPlayer.isAlliedWithKetra()) {
                        return false;
                    }

                    if (targetPlayer.isRecentFakeDeath()) {
                        return false;
                    }

                    if (me instanceof RiftInvader && targetPlayer.isInParty() && targetPlayer.getParty().isInDimensionalRift() && !targetPlayer.getParty().getDimensionalRift().isInCurrentRoomZone(me)) {
                        return false;
                    }
                }
            }

            if (me instanceof Guard) {
                if (target instanceof Player && ((Player) target).getKarma() > 0) {
                    return GeoEngine.getInstance().canSeeTarget(me, target);
                } else if (target instanceof Monster && Config.GUARD_ATTACK_AGGRO_MOB) {
                    return ((Monster) target).isAggressive() && GeoEngine.getInstance().canSeeTarget(me, target);
                } else {
                    return false;
                }
            } else if (me instanceof FriendlyMonster) {
                if (target instanceof Player && ((Player) target).getKarma() > 0) {
                    return GeoEngine.getInstance().canSeeTarget(me, target);
                } else {
                    return false;
                }
            } else if (target instanceof Attackable && me.isConfused()) {
                return GeoEngine.getInstance().canSeeTarget(me, target);
            } else if (target instanceof Npc) {
                return false;
            } else if (!Config.ALT_MOB_AGRO_IN_PEACEZONE && target.isInsideZone(ZoneId.PEACE)) {
                return false;
            } else {
                return me.isAggressive() && GeoEngine.getInstance().canSeeTarget(me, target);
            }
        } else {
            return false;
        }
    }

    public void stopAITask() {
        if (this._aiTask != null) {
            this._aiTask.cancel(false);
            this._aiTask = null;
        }

        super.stopAITask();
    }

    public synchronized void changeIntention(IntentionType intention, Object arg0, Object arg1) {
        if (intention == IntentionType.IDLE || intention == IntentionType.ACTIVE) {
            Attackable npc = this.getActiveChar();
            if (!npc.isAlikeDead()) {
                if (!npc.getKnownType(Player.class).isEmpty()) {
                    intention = IntentionType.ACTIVE;
                } else if (npc.getSpawn() != null) {
                    int range = Config.MAX_DRIFT_RANGE;
                    if (!npc.isInsideRadius(npc.getSpawn().getLocX(), npc.getSpawn().getLocY(), npc.getSpawn().getLocZ(), range + range, true, false)) {
                        intention = IntentionType.ACTIVE;
                    }
                }
            }

            if (intention == IntentionType.IDLE) {
                super.changeIntention(IntentionType.IDLE, null, null);
                this.stopAITask();
                this._actor.detachAI();
                return;
            }
        }

        super.changeIntention(intention, arg0, arg1);
        if (this._aiTask == null) {
            this._aiTask = ThreadPool.scheduleAtFixedRate(this, 1000L, 1000L);
        }

    }

    protected void onIntentionAttack(Creature target) {
        this._attackTimeout = System.currentTimeMillis() + 90000L;
        this.checkBuffAndSetBackTarget(target);
        super.onIntentionAttack(target);
    }

    private void thinkCast() {
        if (this.checkTargetLost(this.getTarget())) {
            this.setTarget(null);
        } else if (!this.maybeMoveToPawn(this.getTarget(), this._skill.getCastRange())) {
            this.clientStopMoving(null);
            this.setIntention(IntentionType.ACTIVE);
            this._actor.doCast(this._skill);
        }
    }

    protected void thinkActive() {
        Attackable npc = this.getActiveChar();
        if (this._globalAggro != 0) {
            if (this._globalAggro < 0) {
                ++this._globalAggro;
            } else {
                --this._globalAggro;
            }
        }

        if (this._globalAggro >= 0) {
            List<Quest> scripts = npc.getTemplate().getEventQuests(ScriptEventType.ON_CREATURE_SEE);

            for (Creature target : npc.getKnownType(Creature.class)) {
                if (!(npc instanceof FestivalMonster) || !(target instanceof Player) || ((Player) target).isFestivalParticipant()) {
                    if (scripts != null) {
                        if (this._seenCreatures.contains(target)) {
                            if (!npc.isInsideRadius(target, 400, true, false)) {
                                this._seenCreatures.remove(target);
                            }
                        } else if (npc.isInsideRadius(target, 400, true, false)) {
                            this._seenCreatures.add(target);

                            for (Quest quest : scripts) {
                                quest.notifyCreatureSee(npc, target);
                            }
                        }
                    }

                    if (this.autoAttackCondition(target) && npc.getHating(target) == 0) {
                        npc.addDamageHate(target, 0, 0);
                    }
                }
            }

            if (!npc.isCoreAIDisabled()) {
                Creature hated = (Creature) (npc.isConfused() ? this.getTarget() : npc.getMostHated());
                if (hated != null) {
                    if (npc.getHating(hated) + this._globalAggro > 0) {
                        npc.setRunning();
                        this.setIntention(IntentionType.ATTACK, hated);
                    }

                    return;
                }
            }
        }

        if (!(npc instanceof FestivalMonster)) {
            if (!this.checkBuffAndSetBackTarget(this._actor.getTarget())) {
                Attackable master = npc.getMaster();
                if (master != null && !master.isAlikeDead()) {
                    if (!npc.isCastingNow()) {
                        int offset = (int) ((double) 100.0F + npc.getCollisionRadius() + master.getCollisionRadius());
                        int minRadius = (int) (master.getCollisionRadius() + (double) 30.0F);
                        if (master.isRunning()) {
                            npc.setRunning();
                        } else {
                            npc.setWalking();
                        }

                        if (npc.getPlanDistanceSq(master.getX(), master.getY()) > (double) (offset * offset)) {
                            int x1 = Rnd.get(minRadius * 2, offset * 2);
                            int y1 = Rnd.get(x1, offset * 2);
                            y1 = (int) Math.sqrt(y1 * y1 - x1 * x1);
                            if (x1 > offset + minRadius) {
                                x1 = master.getX() + x1 - offset;
                            } else {
                                x1 = master.getX() - x1 + minRadius;
                            }

                            if (y1 > offset + minRadius) {
                                y1 = master.getY() + y1 - offset;
                            } else {
                                y1 = master.getY() - y1 + minRadius;
                            }

                            this.moveTo(x1, y1, master.getZ());
                        }
                    }
                } else {
                    if (npc.returnHome()) {
                        return;
                    }

                    if (npc.getSpawn() != null && !npc.isNoRndWalk() && Rnd.get(30) == 0) {
                        int x1 = npc.getSpawn().getLocX();
                        int y1 = npc.getSpawn().getLocY();
                        int z1 = npc.getSpawn().getLocZ();
                        int range = Config.MAX_DRIFT_RANGE;
                        x1 = Rnd.get(range * 2);
                        y1 = Rnd.get(x1, range * 2);
                        y1 = (int) Math.sqrt(y1 * y1 - x1 * x1);
                        x1 += npc.getSpawn().getLocX() - range;
                        y1 += npc.getSpawn().getLocY() - range;
                        z1 = npc.getZ();
                        this.moveTo(x1, y1, z1);
                    }
                }

            }
        }
    }

    protected void thinkAttack() {
        Attackable npc = this.getActiveChar();
        if (!npc.isCastingNow()) {
            Creature attackTarget = npc.getMostHated();
            if (attackTarget != null && this._attackTimeout >= System.currentTimeMillis() && !(MathUtil.calculateDistance(npc, attackTarget, true) > (double) 2000.0F)) {
                if (!npc.isCoreAIDisabled()) {
                    this.setTarget(attackTarget);
                    npc.setTarget(attackTarget);
                    int actorCollision = (int) npc.getCollisionRadius();
                    int combinedCollision = (int) ((double) actorCollision + attackTarget.getCollisionRadius());
                    double dist = Math.sqrt(npc.getPlanDistanceSq(attackTarget.getX(), attackTarget.getY()));
                    int range = combinedCollision;
                    if (attackTarget.isMoving()) {
                        range = combinedCollision + 15;
                    }

                    if (npc.isMoving()) {
                        range += 15;
                    }

                    if (this.willCastASpell()) {
                        List<L2Skill> defaultList = npc.getTemplate().getSkills(SkillType.SUICIDE);
                        if (!defaultList.isEmpty() && npc.getCurrentHp() / (double) npc.getMaxHp() < 0.15) {
                            L2Skill skill = Rnd.get(defaultList);
                            if (this.cast(skill, dist, range + skill.getSkillRadius())) {
                                return;
                            }
                        }

                        defaultList = npc.getTemplate().getSkills(SkillType.HEAL);
                        if (!defaultList.isEmpty()) {
                            Attackable master = npc.getMaster();
                            if (master != null && !master.isDead() && master.getCurrentHp() / (double) master.getMaxHp() < (double) 0.75F) {
                                for (L2Skill sk : defaultList) {
                                    if (sk.getTargetType() != SkillTargetType.TARGET_SELF && this.checkSkillCastConditions(sk)) {
                                        int overallRange = (int) ((double) (sk.getCastRange() + actorCollision) + master.getCollisionRadius());
                                        if (!MathUtil.checkIfInRange(overallRange, npc, master, false) && sk.getTargetType() != SkillTargetType.TARGET_PARTY && !npc.isMovementDisabled()) {
                                            this.moveToPawn(master, overallRange);
                                            return;
                                        }

                                        if (GeoEngine.getInstance().canSeeTarget(npc, master)) {
                                            this.clientStopMoving(null);
                                            npc.setTarget(master);
                                            npc.doCast(sk);
                                            return;
                                        }
                                    }
                                }
                            }

                            if (npc.getCurrentHp() / (double) npc.getMaxHp() < (double) 0.75F) {
                                for (L2Skill sk : defaultList) {
                                    if (this.checkSkillCastConditions(sk)) {
                                        this.clientStopMoving(null);
                                        npc.setTarget(npc);
                                        npc.doCast(sk);
                                        return;
                                    }
                                }
                            }

                            for (L2Skill sk : defaultList) {
                                if (this.checkSkillCastConditions(sk) && sk.getTargetType() == SkillTargetType.TARGET_ONE) {
                                    String[] actorClans = npc.getTemplate().getClans();

                                    for (Attackable obj : npc.getKnownTypeInRadius(Attackable.class, sk.getCastRange() + actorCollision)) {
                                        if (!obj.isDead() && ArraysUtil.contains(actorClans, obj.getTemplate().getClans()) && obj.getCurrentHp() / (double) obj.getMaxHp() < (double) 0.75F && GeoEngine.getInstance().canSeeTarget(npc, obj)) {
                                            this.clientStopMoving(null);
                                            npc.setTarget(obj);
                                            npc.doCast(sk);
                                            return;
                                        }
                                    }

                                    if (sk.getTargetType() == SkillTargetType.TARGET_PARTY) {
                                        this.clientStopMoving(null);
                                        npc.doCast(sk);
                                        return;
                                    }
                                }
                            }
                        }

                        defaultList = npc.getTemplate().getSkills(SkillType.BUFF);
                        if (!defaultList.isEmpty()) {
                            for (L2Skill sk : defaultList) {
                                if (this.checkSkillCastConditions(sk) && npc.getFirstEffect(sk) == null) {
                                    this.clientStopMoving(null);
                                    npc.setTarget(npc);
                                    npc.doCast(sk);
                                    npc.setTarget(attackTarget);
                                    return;
                                }
                            }
                        }

                        defaultList = npc.getTemplate().getSkills(SkillType.DEBUFF);
                        if (Rnd.get(100) < 10 && !defaultList.isEmpty()) {
                            for (L2Skill sk : defaultList) {
                                if (this.checkSkillCastConditions(sk) && (!((double) sk.getCastRange() + npc.getCollisionRadius() + attackTarget.getCollisionRadius() <= dist) || this.canAura(sk)) && GeoEngine.getInstance().canSeeTarget(npc, attackTarget) && attackTarget.getFirstEffect(sk) == null) {
                                    this.clientStopMoving(null);
                                    npc.doCast(sk);
                                    return;
                                }
                            }
                        }

                        defaultList = npc.getTemplate().getSkills(SkillType.SHORT_RANGE);
                        if (!defaultList.isEmpty() && dist <= (double) 150.0F) {
                            L2Skill skill = Rnd.get(defaultList);
                            if (this.cast(skill, dist, skill.getCastRange())) {
                                return;
                            }
                        } else {
                            defaultList = npc.getTemplate().getSkills(SkillType.LONG_RANGE);
                            if (!defaultList.isEmpty() && dist > (double) 150.0F) {
                                L2Skill skill = Rnd.get(defaultList);
                                if (this.cast(skill, dist, skill.getCastRange())) {
                                    return;
                                }
                            }
                        }
                    }

                    range += npc.getPhysicalAttackRange();
                    if (npc.isMovementDisabled()) {
                        if (dist > (double) range) {
                            attackTarget = this.targetReconsider(range, true);
                        }

                        if (attackTarget != null) {
                            this._actor.doAttack(attackTarget);
                        }

                    } else {
                        if (Rnd.get(100) <= 3) {
                            for (Attackable nearby : npc.getKnownTypeInRadius(Attackable.class, actorCollision)) {
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

                                    if (!npc.isInsideRadius(newX, newY, actorCollision, false)) {
                                        int newZ = npc.getZ() + 30;
                                        if (GeoEngine.getInstance().canMoveToTarget(npc.getX(), npc.getY(), npc.getZ(), newX, newY, newZ)) {
                                            this.moveTo(newX, newY, newZ);
                                        }
                                    }

                                    return;
                                }
                            }
                        }

                        if (npc.getTemplate().getAiType() == AIType.ARCHER && dist <= (double) (60 + combinedCollision) && Rnd.get(4) > 1) {
                            int posX = npc.getX() + (attackTarget.getX() < npc.getX() ? 300 : -300);
                            int posY = npc.getY() + (attackTarget.getY() < npc.getY() ? 300 : -300);
                            int posZ = npc.getZ() + 30;
                            if (GeoEngine.getInstance().canMoveToTarget(npc.getX(), npc.getY(), npc.getZ(), posX, posY, posZ)) {
                                this.setIntention(IntentionType.MOVE_TO, new Location(posX, posY, posZ));
                                return;
                            }
                        }

                        if (!(dist > (double) range) && GeoEngine.getInstance().canSeeTarget(npc, attackTarget)) {
                            this._actor.doAttack((Creature) this.getTarget());
                        } else {
                            if (attackTarget.isMoving()) {
                                range -= 30;
                            }

                            if (range < 5) {
                                range = 5;
                            }

                            this.moveToPawn(attackTarget, range);
                        }
                    }
                }
            } else {
                npc.stopHating(attackTarget);
                this.setIntention(IntentionType.ACTIVE);
                npc.setWalking();
            }
        }
    }

    protected boolean cast(L2Skill sk, double distance, int range) {
        if (sk == null) {
            return false;
        } else {
            Attackable caster = this.getActiveChar();
            if (caster.isCastingNow() && !sk.isSimultaneousCast()) {
                return false;
            } else if (!this.checkSkillCastConditions(sk)) {
                return false;
            } else {
                Creature attackTarget = (Creature) this.getTarget();
                if (attackTarget == null) {
                    return false;
                } else {
                    boolean canAoe = sk.getTargetType() == SkillTargetType.TARGET_AURA || sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AURA || sk.getTargetType() == SkillTargetType.TARGET_FRONT_AURA;
                    boolean canArea = sk.getTargetType() == SkillTargetType.TARGET_AREA || sk.getTargetType() == SkillTargetType.TARGET_BEHIND_AREA || sk.getTargetType() == SkillTargetType.TARGET_FRONT_AREA;
                    switch (sk.getSkillType()) {
                        case BUFF:
                            if (caster.getFirstEffect(sk) == null) {
                                this.clientStopMoving(null);
                                caster.setTarget(caster);
                                caster.doCast(sk);
                                return true;
                            }

                            if (sk.getTargetType() == SkillTargetType.TARGET_SELF) {
                                return false;
                            }

                            if (sk.getTargetType() == SkillTargetType.TARGET_ONE) {
                                Creature target = this.targetReconsider(sk.getCastRange(), true);
                                if (target != null) {
                                    this.clientStopMoving(null);
                                    caster.setTarget(target);
                                    caster.doCast(sk);
                                    caster.setTarget(attackTarget);
                                    return true;
                                }
                            }

                            if (this.canParty(sk)) {
                                this.clientStopMoving(null);
                                caster.setTarget(caster);
                                caster.doCast(sk);
                                caster.setTarget(attackTarget);
                                return true;
                            }
                            break;
                        case HEAL:
                        case HOT:
                        case HEAL_PERCENT:
                        case HEAL_STATIC:
                        case BALANCE_LIFE:
                            if (sk.getTargetType() != SkillTargetType.TARGET_SELF) {
                                Attackable master = caster.getMaster();
                                if (master != null && !master.isDead() && (double) Rnd.get(100) > master.getCurrentHp() / (double) master.getMaxHp() * (double) 100.0F) {
                                    int overallRange = (int) ((double) sk.getCastRange() + caster.getCollisionRadius() + master.getCollisionRadius());
                                    if (!MathUtil.checkIfInRange(overallRange, caster, master, false) && sk.getTargetType() != SkillTargetType.TARGET_PARTY && !caster.isMovementDisabled()) {
                                        this.moveToPawn(master, overallRange);
                                    }

                                    if (GeoEngine.getInstance().canSeeTarget(caster, master)) {
                                        this.clientStopMoving(null);
                                        caster.setTarget(master);
                                        caster.doCast(sk);
                                        return true;
                                    }
                                }
                            }

                            double percentage = caster.getCurrentHp() / (double) caster.getMaxHp() * (double) 100.0F;
                            if ((double) Rnd.get(100) < ((double) 100.0F - percentage) / (double) 3.0F) {
                                this.clientStopMoving(null);
                                caster.setTarget(caster);
                                caster.doCast(sk);
                                return true;
                            }

                            if (sk.getTargetType() == SkillTargetType.TARGET_ONE) {
                                for (Attackable obj : caster.getKnownTypeInRadius(Attackable.class, (int) ((double) sk.getCastRange() + caster.getCollisionRadius()))) {
                                    if (!obj.isDead() && ArraysUtil.contains(caster.getTemplate().getClans(), obj.getTemplate().getClans())) {
                                        percentage = obj.getCurrentHp() / (double) obj.getMaxHp() * (double) 100.0F;
                                        if ((double) Rnd.get(100) < ((double) 100.0F - percentage) / (double) 10.0F && GeoEngine.getInstance().canSeeTarget(caster, obj)) {
                                            this.clientStopMoving(null);
                                            caster.setTarget(obj);
                                            caster.doCast(sk);
                                            return true;
                                        }
                                    }
                                }
                            }

                            if (sk.getTargetType() == SkillTargetType.TARGET_PARTY) {
                                for (Attackable obj : caster.getKnownTypeInRadius(Attackable.class, (int) ((double) sk.getSkillRadius() + caster.getCollisionRadius()))) {
                                    if (ArraysUtil.contains(caster.getTemplate().getClans(), obj.getTemplate().getClans()) && obj.getCurrentHp() < (double) obj.getMaxHp() && Rnd.get(100) <= 20) {
                                        this.clientStopMoving(null);
                                        caster.setTarget(caster);
                                        caster.doCast(sk);
                                        return true;
                                    }
                                }
                            }
                            break;
                        case DEBUFF:
                        case POISON:
                        case DOT:
                        case MDOT:
                        case BLEED:
                            if (GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !this.canAOE(sk) && !attackTarget.isDead() && distance <= (double) range) {
                                if (attackTarget.getFirstEffect(sk) == null) {
                                    this.clientStopMoving(null);
                                    caster.doCast(sk);
                                    return true;
                                }
                            } else if (this.canAOE(sk)) {
                                if (canAoe) {
                                    this.clientStopMoving(null);
                                    caster.doCast(sk);
                                    return true;
                                }

                                if (canArea && GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && distance <= (double) range) {
                                    this.clientStopMoving(null);
                                    caster.doCast(sk);
                                    return true;
                                }
                            } else if (sk.getTargetType() == SkillTargetType.TARGET_ONE) {
                                Creature target = this.targetReconsider(sk.getCastRange(), true);
                                if (target != null) {
                                    this.clientStopMoving(null);
                                    caster.doCast(sk);
                                    return true;
                                }
                            }
                            break;
                        case SLEEP:
                            if (sk.getTargetType() == SkillTargetType.TARGET_ONE) {
                                if (!attackTarget.isDead() && distance <= (double) range && (distance > (double) range || attackTarget.isMoving()) && attackTarget.getFirstEffect(sk) == null) {
                                    this.clientStopMoving(null);
                                    caster.doCast(sk);
                                    return true;
                                }

                                Creature target = this.targetReconsider(sk.getCastRange(), true);
                                if (target != null) {
                                    this.clientStopMoving(null);
                                    caster.doCast(sk);
                                    return true;
                                }
                            } else if (this.canAOE(sk)) {
                                if (canAoe) {
                                    this.clientStopMoving(null);
                                    caster.doCast(sk);
                                    return true;
                                }

                                if (canArea && GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && distance <= (double) range) {
                                    this.clientStopMoving(null);
                                    caster.doCast(sk);
                                    return true;
                                }
                            }
                            break;
                        case ROOT:
                        case STUN:
                        case PARALYZE:
                            if (GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !this.canAOE(sk) && distance <= (double) range) {
                                if (attackTarget.getFirstEffect(sk) == null) {
                                    this.clientStopMoving(null);
                                    caster.doCast(sk);
                                    return true;
                                }
                            } else if (this.canAOE(sk)) {
                                if (canAoe) {
                                    this.clientStopMoving(null);
                                    caster.doCast(sk);
                                    return true;
                                }

                                if (canArea && GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && distance <= (double) range) {
                                    this.clientStopMoving(null);
                                    caster.doCast(sk);
                                    return true;
                                }
                            } else if (sk.getTargetType() == SkillTargetType.TARGET_ONE) {
                                Creature target = this.targetReconsider(sk.getCastRange(), true);
                                if (target != null) {
                                    this.clientStopMoving(null);
                                    caster.doCast(sk);
                                    return true;
                                }
                            }
                            break;
                        case MUTE:
                        case FEAR:
                            if (GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !this.canAOE(sk) && distance <= (double) range) {
                                if (attackTarget.getFirstEffect(sk) == null) {
                                    this.clientStopMoving(null);
                                    caster.doCast(sk);
                                    return true;
                                }
                            } else if (this.canAOE(sk)) {
                                if (canAoe) {
                                    this.clientStopMoving(null);
                                    caster.doCast(sk);
                                    return true;
                                }

                                if (canArea && GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && distance <= (double) range) {
                                    this.clientStopMoving(null);
                                    caster.doCast(sk);
                                    return true;
                                }
                            } else if (sk.getTargetType() == SkillTargetType.TARGET_ONE) {
                                Creature target = this.targetReconsider(sk.getCastRange(), true);
                                if (target != null) {
                                    this.clientStopMoving(null);
                                    caster.doCast(sk);
                                    return true;
                                }
                            }
                            break;
                        case CANCEL:
                        case NEGATE:
                            if (Rnd.get(50) != 0) {
                                return true;
                            }

                            if (sk.getTargetType() == SkillTargetType.TARGET_ONE) {
                                if (attackTarget.getFirstEffect(L2EffectType.BUFF) != null && GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && distance <= (double) range) {
                                    this.clientStopMoving(null);
                                    caster.doCast(sk);
                                    return true;
                                }

                                Creature target = this.targetReconsider(sk.getCastRange(), true);
                                if (target != null) {
                                    this.clientStopMoving(null);
                                    caster.setTarget(target);
                                    caster.doCast(sk);
                                    caster.setTarget(attackTarget);
                                    return true;
                                }
                            } else if (this.canAOE(sk)) {
                                if ((canAoe) && GeoEngine.getInstance().canSeeTarget(caster, attackTarget)) {
                                    this.clientStopMoving(null);
                                    caster.doCast(sk);
                                    return true;
                                }

                                if (canArea && GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && distance <= (double) range) {
                                    this.clientStopMoving(null);
                                    caster.doCast(sk);
                                    return true;
                                }
                            }
                            break;
                        default:
                            if (this.canAura(sk)) {
                                this.clientStopMoving(null);
                                caster.doCast(sk);
                                return true;
                            }

                            if (GeoEngine.getInstance().canSeeTarget(caster, attackTarget) && !attackTarget.isDead() && distance <= (double) range) {
                                this.clientStopMoving(null);
                                caster.doCast(sk);
                                return true;
                            }

                            Creature target = this.targetReconsider(sk.getCastRange(), true);
                            if (target != null) {
                                this.clientStopMoving(null);
                                caster.setTarget(target);
                                caster.doCast(sk);
                                caster.setTarget(attackTarget);
                                return true;
                            }
                    }

                    return false;
                }
            }
        }
    }

    protected boolean checkSkillCastConditions(L2Skill skill) {
        if ((double) skill.getMpConsume() >= this.getActiveChar().getCurrentMp()) {
            return false;
        } else if (this.getActiveChar().isSkillDisabled(skill)) {
            return false;
        } else {
            return (!skill.isMagic() || !this.getActiveChar().isMuted()) && !this.getActiveChar().isPhysicalMuted();
        }
    }

    protected boolean willCastASpell() {
        switch (this.getActiveChar().getTemplate().getAiType()) {
            case HEALER:
            case MAGE:
                return !this.getActiveChar().isMuted();
            default:
                if (this.getActiveChar().isPhysicalMuted()) {
                    return false;
                } else {
                    return Rnd.get(100) < 10;
                }
        }
    }

    protected Creature targetReconsider(int range, boolean rangeCheck) {
        Attackable actor;
        Creature previousMostHated;
        int aggroMostHated;
        Creature obj;
        label83:
        {
            actor = this.getActiveChar();
            if (!actor.getAggroList().isEmpty()) {
                previousMostHated = actor.getMostHated();
                aggroMostHated = actor.getHating(previousMostHated);

                for (Creature creature : actor.getHateList()) {
                    obj = creature;
                    if (this.autoAttackCondition(obj)) {
                        if (!rangeCheck) {
                            break label83;
                        }

                        double dist = Math.sqrt(actor.getPlanDistanceSq(obj.getX(), obj.getY())) - obj.getCollisionRadius();
                        if (actor.isMoving()) {
                            dist -= 15.0F;
                        }

                        if (obj.isMoving()) {
                            dist -= 15.0F;
                        }

                        if (!(dist > (double) range)) {
                            break label83;
                        }
                    }
                }
            }

            Creature target;
            label84:
            {
                if (actor.isAggressive()) {
                    for (Creature creature : actor.getKnownTypeInRadius(Creature.class, actor.getTemplate().getAggroRange())) {
                        target = creature;
                        if (this.autoAttackCondition(target)) {
                            if (!rangeCheck) {
                                break label84;
                            }

                            double dist = Math.sqrt(actor.getPlanDistanceSq(target.getX(), target.getY())) - target.getCollisionRadius();
                            if (actor.isMoving()) {
                                dist -= 15.0F;
                            }

                            if (target.isMoving()) {
                                dist -= 15.0F;
                            }

                            if (!(dist > (double) range)) {
                                break label84;
                            }
                        }
                    }
                }

                return null;
            }

            actor.addDamageHate(target, 0, 1);
            return target;
        }

        actor.stopHating(previousMostHated);
        actor.addDamageHate(obj, 0, aggroMostHated > 0 ? aggroMostHated : 2000);
        return obj;
    }

    public void aggroReconsider() {
        Attackable actor = this.getActiveChar();
        if (actor.getHateList().size() > 1) {
            Creature mostHated = actor.getMostHated();
            Creature victim = Rnd.get(actor.getHateList().stream().filter(this::autoAttackCondition).collect(Collectors.toList()));
            if (victim != null && mostHated != victim) {
                actor.addDamageHate(victim, 0, actor.getHating(mostHated));
                this.setIntention(IntentionType.ATTACK, victim);
            }

        }
    }

    protected void onEvtThink() {
        if (!this._thinking && !this._actor.isAllSkillsDisabled()) {
            this._thinking = true;

            try {
                switch (this._desire.getIntention()) {
                    case ACTIVE -> this.thinkActive();
                    case ATTACK -> this.thinkAttack();
                    case CAST -> this.thinkCast();
                }
            } finally {
                this._thinking = false;
            }

        }
    }

    protected void onEvtAttacked(Creature attacker) {
        Attackable me = this.getActiveChar();
        this._attackTimeout = System.currentTimeMillis() + 90000L;
        if (this._globalAggro < 0) {
            this._globalAggro = 0;
        }

        me.addDamageHate(attacker, 0, 1);
        if (!me.isCoreAIDisabled() && (this._desire.getIntention() != IntentionType.ATTACK || me.getMostHated() != this.getTarget())) {
            me.setRunning();
            this.setIntention(IntentionType.ATTACK, attacker);
        }

        if (me instanceof Monster master) {
            if (master.hasMinions()) {
                master.getMinionList().onAssist(me, attacker);
            } else {
                master = master.getMaster();
                if (master != null && master.hasMinions()) {
                    master.getMinionList().onAssist(me, attacker);
                }
            }
        }

        if (attacker != null) {
            String[] actorClans = me.getTemplate().getClans();
            if (actorClans != null && me.getAttackByList().contains(attacker)) {
                for (Attackable called : me.getKnownTypeInRadius(Attackable.class, me.getTemplate().getClanRange())) {
                    if (called.hasAI() && !called.isDead() && ArraysUtil.contains(actorClans, called.getTemplate().getClans()) && !ArraysUtil.contains(called.getTemplate().getIgnoredIds(), me.getNpcId())) {
                        IntentionType calledIntention = called.getAI().getDesire().getIntention();
                        if ((calledIntention == IntentionType.IDLE || calledIntention == IntentionType.ACTIVE || calledIntention == IntentionType.MOVE_TO && !called.isRunning()) && GeoEngine.getInstance().canSeeTarget(me, called)) {
                            if (attacker instanceof Playable) {
                                List<Quest> scripts = called.getTemplate().getEventQuests(ScriptEventType.ON_FACTION_CALL);
                                if (scripts != null) {
                                    Player player = attacker.getActingPlayer();
                                    boolean isSummon = attacker instanceof Summon;

                                    for (Quest quest : scripts) {
                                        quest.notifyFactionCall(called, me, player, isSummon);
                                    }
                                }
                            } else {
                                called.addDamageHate(attacker, 0, me.getHating(attacker));
                                called.getAI().setIntention(IntentionType.ATTACK, attacker);
                            }
                        }
                    }
                }
            }
        }

        super.onEvtAttacked(attacker);
    }

    protected void onEvtAggression(Creature target, int aggro) {
        Attackable me = this.getActiveChar();
        me.addDamageHate(target, 0, aggro);
        if (!me.isCoreAIDisabled() && this._desire.getIntention() != IntentionType.ATTACK) {
            me.setRunning();
            this.setIntention(IntentionType.ATTACK, target);
        }

        if (me instanceof Monster master) {
            if (master.hasMinions()) {
                master.getMinionList().onAssist(me, target);
            } else {
                master = master.getMaster();
                if (master != null && master.hasMinions()) {
                    master.getMinionList().onAssist(me, target);
                }
            }
        }

        if (target != null) {
            String[] actorClans = me.getTemplate().getClans();
            if (actorClans != null && me.getAttackByList().contains(target)) {
                for (Attackable called : me.getKnownTypeInRadius(Attackable.class, me.getTemplate().getClanRange())) {
                    if (called.hasAI() && !called.isDead() && ArraysUtil.contains(actorClans, called.getTemplate().getClans()) && !ArraysUtil.contains(called.getTemplate().getIgnoredIds(), me.getNpcId())) {
                        IntentionType calledIntention = called.getAI().getDesire().getIntention();
                        if ((calledIntention == IntentionType.IDLE || calledIntention == IntentionType.ACTIVE || calledIntention == IntentionType.MOVE_TO && !called.isRunning()) && GeoEngine.getInstance().canSeeTarget(me, called)) {
                            if (target instanceof Playable) {
                                List<Quest> scripts = called.getTemplate().getEventQuests(ScriptEventType.ON_FACTION_CALL);
                                if (scripts != null) {
                                    Player player = target.getActingPlayer();
                                    boolean isSummon = target instanceof Summon;

                                    for (Quest quest : scripts) {
                                        quest.notifyFactionCall(called, me, player, isSummon);
                                    }
                                }
                            } else {
                                called.addDamageHate(target, 0, me.getHating(target));
                                called.getAI().setIntention(IntentionType.ATTACK, target);
                            }
                        }
                    }
                }
            }

        }
    }

    protected void onIntentionActive() {
        this._attackTimeout = Long.MAX_VALUE;
        super.onIntentionActive();
    }

    public void setGlobalAggro(int value) {
        this._globalAggro = value;
    }

    private Attackable getActiveChar() {
        return (Attackable) this._actor;
    }

    private boolean checkBuffAndSetBackTarget(WorldObject target) {
        if (Rnd.get(30) != 0) {
            return false;
        } else {
            for (L2Skill sk : this.getActiveChar().getTemplate().getSkills(SkillType.BUFF)) {
                if (this.getActiveChar().getFirstEffect(sk) == null) {
                    this.clientStopMoving(null);
                    this._actor.setTarget(this._actor);
                    this._actor.doCast(sk);
                    this._actor.setTarget(target);
                    return true;
                }
            }

            return false;
        }
    }
}
