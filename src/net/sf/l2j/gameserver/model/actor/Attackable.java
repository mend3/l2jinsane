package net.sf.l2j.gameserver.model.actor;

import enginemods.main.EngineModsManager;
import net.sf.l2j.Config;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.ScriptEventType;
import net.sf.l2j.gameserver.events.soloboss.SoloBossManager;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.ai.type.AttackableAI;
import net.sf.l2j.gameserver.model.actor.ai.type.CreatureAI;
import net.sf.l2j.gameserver.model.actor.npc.AggroInfo;
import net.sf.l2j.gameserver.model.actor.status.AttackableStatus;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.scripting.Quest;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Attackable extends Npc {
    private final Map<Creature, AggroInfo> _aggroList = new ConcurrentHashMap<>();

    private final Set<Creature> _attackedBy = ConcurrentHashMap.newKeySet();

    private boolean _isReturningToSpawnPoint;

    private boolean _seeThroughSilentMove;

    public Attackable(int objectId, NpcTemplate template) {
        super(objectId, template);
    }

    public void initCharStatus() {
        setStatus(new AttackableStatus(this));
    }

    public AttackableStatus getStatus() {
        return (AttackableStatus) super.getStatus();
    }

    public CreatureAI getAI() {
        CreatureAI ai = this._ai;
        if (ai == null)
            synchronized (this) {
                if (this._ai == null)
                    this._ai = new AttackableAI(this);
                return this._ai;
            }
        return ai;
    }

    public void addKnownObject(WorldObject object) {
        if (object instanceof Player && getAI().getDesire().getIntention() == IntentionType.IDLE)
            getAI().setIntention(IntentionType.ACTIVE, null);
    }

    public void removeKnownObject(WorldObject object) {
        super.removeKnownObject(object);
        if (object instanceof Creature)
            getAggroList().remove(object);
    }

    public void reduceCurrentHp(double damage, Creature attacker, L2Skill skill) {
        reduceCurrentHp(damage, attacker, true, false, skill);
    }

    public void reduceCurrentHp(double damage, Creature attacker, boolean awake, boolean isDOT, L2Skill skill) {
        if (attacker != null && !isDead()) {
            List<Quest> scripts = getTemplate().getEventQuests(ScriptEventType.ON_ATTACK);
            if (scripts != null)
                for (Quest quest : scripts)
                    quest.notifyAttack(this, attacker, (int) damage, skill);
        }
        super.reduceCurrentHp(damage, attacker, awake, isDOT, skill);
    }

    public boolean doDie(Creature killer) {
        if (!super.doDie(killer))
            return false;
        EngineModsManager.onKill(killer, this, killer instanceof Summon);
        SoloBossManager.getInstance().onKill(this);
        List<Quest> scripts = getTemplate().getEventQuests(ScriptEventType.ON_KILL);
        if (scripts != null)
            for (Quest quest : scripts)
                ThreadPool.schedule(() -> quest.notifyKill(this, killer), 3000L);
        this._attackedBy.clear();
        return true;
    }

    public void onSpawn() {
        super.onSpawn();
        this._aggroList.clear();
        setWalking();
        if (!isInActiveRegion() && hasAI())
            getAI().stopAITask();
    }

    public int calculateRandomAnimationTimer() {
        return Rnd.get(Config.MIN_MONSTER_ANIMATION, Config.MAX_MONSTER_ANIMATION);
    }

    public boolean hasRandomAnimation() {
        return (Config.MAX_MONSTER_ANIMATION > 0 && !isRaidRelated());
    }

    public void addAttacker(Creature attacker) {
        if (attacker == null || attacker == this)
            return;
        this._attackedBy.add(attacker);
    }

    public void addDamageHate(Creature attacker, int damage, int aggro) {
        if (attacker == null)
            return;
        AggroInfo ai = this._aggroList.computeIfAbsent(attacker, AggroInfo::new);
        ai.addDamage(damage);
        ai.addHate(aggro);
        if (aggro == 0) {
            Player targetPlayer = attacker.getActingPlayer();
            if (targetPlayer != null) {
                List<Quest> scripts = getTemplate().getEventQuests(ScriptEventType.ON_AGGRO);
                if (scripts != null)
                    for (Quest quest : scripts)
                        quest.notifyAggro(this, targetPlayer, attacker instanceof Summon);
            } else {
                aggro = 1;
                ai.addHate(1);
            }
        } else if (aggro > 0 && getAI().getDesire().getIntention() == IntentionType.IDLE) {
            getAI().setIntention(IntentionType.ACTIVE);
        }
    }

    public void reduceHate(Creature target, int amount) {
        if (target == null) {
            Creature mostHated = getMostHated();
            if (mostHated == null) {
                ((AttackableAI) getAI()).setGlobalAggro(-25);
                return;
            }
            for (AggroInfo aggroInfo : this._aggroList.values())
                aggroInfo.addHate(-amount);
            amount = getHating(mostHated);
            if (amount <= 0) {
                ((AttackableAI) getAI()).setGlobalAggro(-25);
                this._aggroList.clear();
                getAI().setIntention(IntentionType.ACTIVE);
                setWalking();
            }
            return;
        }
        AggroInfo ai = this._aggroList.get(target);
        if (ai == null)
            return;
        ai.addHate(-amount);
        if (ai.getHate() <= 0 && getMostHated() == null) {
            ((AttackableAI) getAI()).setGlobalAggro(-25);
            this._aggroList.clear();
            getAI().setIntention(IntentionType.ACTIVE);
            setWalking();
        }
    }

    public void stopHating(Creature target) {
        if (target == null)
            return;
        AggroInfo ai = this._aggroList.get(target);
        if (ai != null)
            ai.stopHate();
    }

    public void cleanAllHate() {
        for (AggroInfo ai : this._aggroList.values())
            ai.stopHate();
    }

    public Creature getMostHated() {
        if (this._aggroList.isEmpty() || isAlikeDead())
            return null;
        Creature mostHated = null;
        int maxHate = 0;
        for (AggroInfo ai : this._aggroList.values()) {
            if (ai.checkHate(this) > maxHate) {
                mostHated = ai.getAttacker();
                maxHate = ai.getHate();
            }
        }
        return mostHated;
    }

    public List<Creature> getHateList() {
        if (this._aggroList.isEmpty() || isAlikeDead())
            return Collections.emptyList();
        List<Creature> result = new ArrayList<>();
        for (AggroInfo ai : this._aggroList.values()) {
            ai.checkHate(this);
            result.add(ai.getAttacker());
        }
        return result;
    }

    public int getHating(Creature target) {
        if (this._aggroList.isEmpty() || target == null)
            return 0;
        AggroInfo ai = this._aggroList.get(target);
        if (ai == null)
            return 0;
        if (ai.getAttacker() instanceof Player && ((Player) ai.getAttacker()).getAppearance().getInvisible()) {
            this._aggroList.remove(target);
            return 0;
        }
        if (!ai.getAttacker().isVisible()) {
            this._aggroList.remove(target);
            return 0;
        }
        if (ai.getAttacker().isAlikeDead()) {
            ai.stopHate();
            return 0;
        }
        return ai.getHate();
    }

    public void useMagic(L2Skill skill) {
        if (skill == null || isAlikeDead())
            return;
        if (skill.isPassive())
            return;
        if (isCastingNow())
            return;
        if (isSkillDisabled(skill))
            return;
        if (getCurrentMp() < (getStat().getMpConsume(skill) + getStat().getMpInitialConsume(skill)))
            return;
        if (getCurrentHp() <= skill.getHpConsume())
            return;
        if (skill.isMagic()) {
            if (isMuted())
                return;
        } else if (isPhysicalMuted()) {
            return;
        }
        WorldObject target = skill.getFirstOfTargetList(this);
        if (target == null)
            return;
        getAI().setIntention(IntentionType.CAST, skill, target);
    }

    public boolean returnHome() {
        if (isDead())
            return false;
        if (isMinion() && !isRaidRelated()) {
            deleteMe();
            return true;
        }
        if (getSpawn() != null && !isInsideRadius(getSpawn().getLocX(), getSpawn().getLocY(), getDriftRange(), false)) {
            cleanAllHate();
            setIsReturningToSpawnPoint(true);
            setWalking();
            getAI().setIntention(IntentionType.MOVE_TO, getSpawn().getLoc());
            return true;
        }
        return false;
    }

    public int getDriftRange() {
        return Config.MAX_DRIFT_RANGE;
    }

    public final Set<Creature> getAttackByList() {
        return this._attackedBy;
    }

    public final Map<Creature, AggroInfo> getAggroList() {
        return this._aggroList;
    }

    public final boolean isReturningToSpawnPoint() {
        return this._isReturningToSpawnPoint;
    }

    public final void setIsReturningToSpawnPoint(boolean value) {
        this._isReturningToSpawnPoint = value;
    }

    public boolean canSeeThroughSilentMove() {
        return this._seeThroughSilentMove;
    }

    public void seeThroughSilentMove(boolean val) {
        this._seeThroughSilentMove = val;
    }

    public ItemInstance getActiveWeapon() {
        return null;
    }

    public Attackable getMaster() {
        return null;
    }

    public boolean isGuard() {
        return false;
    }
}
