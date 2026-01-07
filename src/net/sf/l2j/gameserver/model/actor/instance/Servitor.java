package net.sf.l2j.gameserver.model.actor.instance;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.model.L2Skill;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.actor.npc.AggroInfo;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.network.serverpackets.SetSummonRemainTime;
import net.sf.l2j.gameserver.skills.l2skills.L2SkillSummon;
import net.sf.l2j.gameserver.taskmanager.DecayTaskManager;

import java.util.concurrent.Future;

public class Servitor extends Summon {
    public int lastShowntimeRemaining;
    private float _expPenalty = 0.0F;
    private int _itemConsumeId = 0;
    private int _itemConsumeCount = 0;
    private int _itemConsumeSteps = 0;
    private int _totalLifeTime = 1200000;
    private int _timeLostIdle = 1000;
    private int _timeLostActive = 1000;
    private int _timeRemaining;
    private int _nextItemConsumeTime;
    private Future<?> _summonLifeTask;

    public Servitor(int objectId, NpcTemplate template, Player owner, L2Skill skill) {
        super(objectId, template, owner);
        if (skill != null) {
            L2SkillSummon summonSkill = (L2SkillSummon) skill;
            this._itemConsumeId = summonSkill.getItemConsumeIdOT();
            this._itemConsumeCount = summonSkill.getItemConsumeOT();
            this._itemConsumeSteps = summonSkill.getItemConsumeSteps();
            this._totalLifeTime = summonSkill.getTotalLifeTime();
            this._timeLostIdle = summonSkill.getTimeLostIdle();
            this._timeLostActive = summonSkill.getTimeLostActive();
        }
        this._timeRemaining = this._totalLifeTime;
        this.lastShowntimeRemaining = this._totalLifeTime;
        if (this._itemConsumeId == 0 || this._itemConsumeSteps == 0) {
            this._nextItemConsumeTime = -1;
        } else {
            this._nextItemConsumeTime = this._totalLifeTime - this._totalLifeTime / (this._itemConsumeSteps + 1);
        }
        this._summonLifeTask = ThreadPool.scheduleAtFixedRate(new SummonLifetime(getOwner(), this), 1000L, 1000L);
    }

    public final int getLevel() {
        return (getTemplate() != null) ? getTemplate().getLevel() : 0;
    }

    public int getSummonType() {
        return 1;
    }

    public float getExpPenalty() {
        return this._expPenalty;
    }

    public void setExpPenalty(float expPenalty) {
        this._expPenalty = expPenalty;
    }

    public int getItemConsumeCount() {
        return this._itemConsumeCount;
    }

    public int getItemConsumeId() {
        return this._itemConsumeId;
    }

    public int getItemConsumeSteps() {
        return this._itemConsumeSteps;
    }

    public int getNextItemConsumeTime() {
        return this._nextItemConsumeTime;
    }

    public void setNextItemConsumeTime(int value) {
        this._nextItemConsumeTime = value;
    }

    public int getTotalLifeTime() {
        return this._totalLifeTime;
    }

    public int getTimeLostIdle() {
        return this._timeLostIdle;
    }

    public int getTimeLostActive() {
        return this._timeLostActive;
    }

    public int getTimeRemaining() {
        return this._timeRemaining;
    }

    public void decNextItemConsumeTime(int value) {
        this._nextItemConsumeTime -= value;
    }

    public void decTimeRemaining(int value) {
        this._timeRemaining -= value;
    }

    public void addExpAndSp(int addToExp, int addToSp) {
        getOwner().addExpAndSp(addToExp, addToSp);
    }

    public boolean doDie(Creature killer) {
        if (!super.doDie(killer))
            return false;
        for (Attackable mob : getKnownType(Attackable.class)) {
            if (mob.isDead())
                continue;
            AggroInfo info = mob.getAggroList().get(this);
            if (info != null)
                mob.addDamageHate(getOwner(), info.getDamage(), info.getHate());
        }
        if (isPhoenixBlessed())
            getOwner().reviveRequest(getOwner(), null, true);
        DecayTaskManager.getInstance().add(this, getTemplate().getCorpseTime());
        if (this._summonLifeTask != null) {
            this._summonLifeTask.cancel(false);
            this._summonLifeTask = null;
        }
        return true;
    }

    public void unSummon(Player owner) {
        if (this._summonLifeTask != null) {
            this._summonLifeTask.cancel(false);
            this._summonLifeTask = null;
        }
        super.unSummon(owner);
    }

    public boolean destroyItem(String process, int objectId, int count, WorldObject reference, boolean sendMessage) {
        return getOwner().destroyItem(process, objectId, count, reference, sendMessage);
    }

    public boolean destroyItemByItemId(String process, int itemId, int count, WorldObject reference, boolean sendMessage) {
        return getOwner().destroyItemByItemId(process, itemId, count, reference, sendMessage);
    }

    public void doPickupItem(WorldObject object) {
    }

    private record SummonLifetime(Player _player, Servitor _summon) implements Runnable {

        public void run() {
                double oldTimeRemaining = this._summon.getTimeRemaining();
                int maxTime = this._summon.getTotalLifeTime();
                if (this._summon.isAttackingNow()) {
                    this._summon.decTimeRemaining(this._summon.getTimeLostActive());
                } else {
                    this._summon.decTimeRemaining(this._summon.getTimeLostIdle());
                }
                double newTimeRemaining = this._summon.getTimeRemaining();
                if (newTimeRemaining < 0.0D) {
                    this._summon.unSummon(this._player);
                } else if (newTimeRemaining <= this._summon.getNextItemConsumeTime() && oldTimeRemaining > this._summon.getNextItemConsumeTime()) {
                    this._summon.decNextItemConsumeTime(maxTime / (this._summon.getItemConsumeSteps() + 1));
                    if (this._summon.getItemConsumeCount() > 0 && this._summon.getItemConsumeId() != 0 && !this._summon.isDead() && !this._summon.destroyItemByItemId("Consume", this._summon.getItemConsumeId(), this._summon.getItemConsumeCount(), this._player, true))
                        this._summon.unSummon(this._player);
                }
                if (this._summon.lastShowntimeRemaining - newTimeRemaining > (maxTime / 352)) {
                    this._player.sendPacket(new SetSummonRemainTime(maxTime, (int) newTimeRemaining));
                    this._summon.lastShowntimeRemaining = (int) newTimeRemaining;
                    this._summon.updateEffectIcons();
                }
            }
        }
}
