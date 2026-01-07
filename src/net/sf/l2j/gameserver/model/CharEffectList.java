/**/
package net.sf.l2j.gameserver.model;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.enums.skills.L2EffectFlag;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.Summon;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.model.olympiad.OlympiadGameManager;
import net.sf.l2j.gameserver.model.olympiad.OlympiadGameTask;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.AbnormalStatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ExOlympiadSpelledInfo;
import net.sf.l2j.gameserver.network.serverpackets.PartySpelled;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class CharEffectList {
    private static final L2Effect[] EMPTY_EFFECTS = new L2Effect[0];
    private final AtomicBoolean queueLock = new AtomicBoolean();
    private final Creature _owner;
    private final Object _buildEffectLock = new Object();
    private List<L2Effect> _buffs;
    private List<L2Effect> _debuffs;
    private Map<String, List<L2Effect>> _stackedEffects;
    private volatile boolean _hasBuffsRemovedOnAnyAction = false;
    private volatile boolean _hasBuffsRemovedOnDamage = false;
    private volatile boolean _hasDebuffsRemovedOnDamage = false;
    private boolean _queuesInitialized = false;
    private LinkedBlockingQueue<L2Effect> _addQueue;
    private LinkedBlockingQueue<L2Effect> _removeQueue;
    private int _effectFlags;
    private boolean _partyOnly = false;
    private L2Effect[] _effectCache;
    private volatile boolean _rebuildCache = true;

    public CharEffectList(Creature owner) {
        this._owner = owner;
    }

    public final L2Effect[] getAllEffects() {
        if ((this._buffs == null || this._buffs.isEmpty()) && (this._debuffs == null || this._debuffs.isEmpty())) {
            return EMPTY_EFFECTS;
        } else {
            synchronized (this._buildEffectLock) {
                if (!this._rebuildCache) {
                    return this._effectCache;
                } else {
                    this._rebuildCache = false;
                    List<L2Effect> temp = new ArrayList();
                    if (this._buffs != null && !this._buffs.isEmpty()) {
                        temp.addAll(this._buffs);
                    }

                    if (this._debuffs != null && !this._debuffs.isEmpty()) {
                        temp.addAll(this._debuffs);
                    }

                    L2Effect[] tempArray = new L2Effect[temp.size()];
                    temp.toArray(tempArray);
                    return this._effectCache = tempArray;
                }
            }
        }
    }

    public final L2Effect getFirstEffect(L2EffectType tp) {
        L2Effect effectNotInUse = null;
        Iterator var3;
        L2Effect e;
        if (this._buffs != null && !this._buffs.isEmpty()) {
            var3 = this._buffs.iterator();

            while (var3.hasNext()) {
                e = (L2Effect) var3.next();
                if (e != null && e.getEffectType() == tp) {
                    if (e.getInUse()) {
                        return e;
                    }

                    effectNotInUse = e;
                }
            }
        }

        if (effectNotInUse == null && this._debuffs != null && !this._debuffs.isEmpty()) {
            var3 = this._debuffs.iterator();

            while (var3.hasNext()) {
                e = (L2Effect) var3.next();
                if (e != null && e.getEffectType() == tp) {
                    if (e.getInUse()) {
                        return e;
                    }

                    effectNotInUse = e;
                }
            }
        }

        return effectNotInUse;
    }

    public final L2Effect getFirstEffect(L2Skill skill) {
        L2Effect effectNotInUse = null;
        Iterator var3;
        L2Effect e;
        if (skill.isDebuff()) {
            if (this._debuffs != null && !this._debuffs.isEmpty()) {
                var3 = this._debuffs.iterator();

                while (var3.hasNext()) {
                    e = (L2Effect) var3.next();
                    if (e != null && e.getSkill() == skill) {
                        if (e.getInUse()) {
                            return e;
                        }

                        effectNotInUse = e;
                    }
                }
            }
        } else if (this._buffs != null && !this._buffs.isEmpty()) {
            var3 = this._buffs.iterator();

            while (var3.hasNext()) {
                e = (L2Effect) var3.next();
                if (e != null && e.getSkill() == skill) {
                    if (e.getInUse()) {
                        return e;
                    }

                    effectNotInUse = e;
                }
            }
        }

        return effectNotInUse;
    }

    public final L2Effect getFirstEffect(int skillId) {
        L2Effect effectNotInUse = null;
        Iterator var3;
        L2Effect e;
        if (this._buffs != null && !this._buffs.isEmpty()) {
            var3 = this._buffs.iterator();

            while (var3.hasNext()) {
                e = (L2Effect) var3.next();
                if (e != null && e.getSkill().getId() == skillId) {
                    if (e.getInUse()) {
                        return e;
                    }

                    effectNotInUse = e;
                }
            }
        }

        if (effectNotInUse == null && this._debuffs != null && !this._debuffs.isEmpty()) {
            var3 = this._debuffs.iterator();

            while (var3.hasNext()) {
                e = (L2Effect) var3.next();
                if (e != null && e.getSkill().getId() == skillId) {
                    if (e.getInUse()) {
                        return e;
                    }

                    effectNotInUse = e;
                }
            }
        }

        return effectNotInUse;
    }

    private boolean doesStack(L2Skill checkSkill) {
        if (this._buffs != null && !this._buffs.isEmpty()) {
            if (checkSkill._effectTemplates != null && !checkSkill._effectTemplates.isEmpty()) {
                String stackType = checkSkill._effectTemplates.get(0).stackType;
                if (stackType != null && !"none".equals(stackType)) {
                    Iterator var3 = this._buffs.iterator();

                    L2Effect e;
                    do {
                        if (!var3.hasNext()) {
                            return false;
                        }

                        e = (L2Effect) var3.next();
                    } while (e.getStackType() == null || !e.getStackType().equals(stackType));

                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public int getBuffCount() {
        if (this._buffs != null && !this._buffs.isEmpty()) {
            int buffCount = 0;
            Iterator var2 = this._buffs.iterator();

            while (var2.hasNext()) {
                L2Effect e = (L2Effect) var2.next();
                if (e != null && e.getShowIcon() && !e.getSkill().is7Signs()) {
                    switch (e.getSkill().getSkillType()) {
                        case BUFF:
                        case COMBATPOINTHEAL:
                        case REFLECT:
                        case HEAL_PERCENT:
                        case HEAL_STATIC:
                        case MANAHEAL_PERCENT:
                            ++buffCount;
                    }
                }
            }

            return buffCount;
        } else {
            return 0;
        }
    }

    public int getDanceCount() {
        if (this._buffs != null && !this._buffs.isEmpty()) {
            int danceCount = 0;
            Iterator var2 = this._buffs.iterator();

            while (var2.hasNext()) {
                L2Effect e = (L2Effect) var2.next();
                if (e != null && e.getSkill().isDance() && e.getInUse()) {
                    ++danceCount;
                }
            }

            return danceCount;
        } else {
            return 0;
        }
    }

    public final void stopAllEffects() {
        L2Effect[] effects = this.getAllEffects();
        L2Effect[] var2 = effects;
        int var3 = effects.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            L2Effect e = var2[var4];
            if (e != null) {
                e.exit(true);
            }
        }

    }

    public final void stopAllEffectsExceptThoseThatLastThroughDeath() {
        L2Effect[] effects = this.getAllEffects();
        L2Effect[] var2 = effects;
        int var3 = effects.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            L2Effect e = var2[var4];
            if (e != null && !e.getSkill().isStayAfterDeath()) {
                e.exit(true);
            }
        }

    }

    public void stopAllToggles() {
        if (this._buffs != null && !this._buffs.isEmpty()) {
            Iterator var1 = this._buffs.iterator();

            while (var1.hasNext()) {
                L2Effect e = (L2Effect) var1.next();
                if (e != null && e.getSkill().isToggle()) {
                    e.exit();
                }
            }
        }

    }

    public final void stopEffects(L2EffectType type) {
        Iterator var2;
        L2Effect e;
        if (this._buffs != null && !this._buffs.isEmpty()) {
            var2 = this._buffs.iterator();

            while (var2.hasNext()) {
                e = (L2Effect) var2.next();
                if (e != null && e.getEffectType() == type) {
                    e.exit();
                }
            }
        }

        if (this._debuffs != null && !this._debuffs.isEmpty()) {
            var2 = this._debuffs.iterator();

            while (var2.hasNext()) {
                e = (L2Effect) var2.next();
                if (e != null && e.getEffectType() == type) {
                    e.exit();
                }
            }
        }

    }

    public final void stopSkillEffects(int skillId) {
        Iterator var2;
        L2Effect e;
        if (this._buffs != null && !this._buffs.isEmpty()) {
            var2 = this._buffs.iterator();

            while (var2.hasNext()) {
                e = (L2Effect) var2.next();
                if (e != null && e.getSkill().getId() == skillId) {
                    e.exit();
                }
            }
        }

        if (this._debuffs != null && !this._debuffs.isEmpty()) {
            var2 = this._debuffs.iterator();

            while (var2.hasNext()) {
                e = (L2Effect) var2.next();
                if (e != null && e.getSkill().getId() == skillId) {
                    e.exit();
                }
            }
        }

    }

    public final void stopSkillEffects(L2SkillType skillType, int negateLvl) {
        Iterator var3;
        L2Effect e;
        if (this._buffs != null && !this._buffs.isEmpty()) {
            var3 = this._buffs.iterator();

            label96:
            while (true) {
                do {
                    do {
                        do {
                            if (!var3.hasNext()) {
                                break label96;
                            }

                            e = (L2Effect) var3.next();
                        } while (e == null);
                    } while (e.getSkill().getSkillType() != skillType && (e.getSkill().getEffectType() == null || e.getSkill().getEffectType() != skillType));
                } while (negateLvl != -1 && (e.getSkill().getEffectType() == null || e.getSkill().getEffectAbnormalLvl() < 0 || e.getSkill().getEffectAbnormalLvl() > negateLvl) && (e.getSkill().getAbnormalLvl() < 0 || e.getSkill().getAbnormalLvl() > negateLvl));

                e.exit();
            }
        }

        if (this._debuffs != null && !this._debuffs.isEmpty()) {
            var3 = this._debuffs.iterator();

            while (true) {
                do {
                    do {
                        do {
                            if (!var3.hasNext()) {
                                return;
                            }

                            e = (L2Effect) var3.next();
                        } while (e == null);
                    } while (e.getSkill().getSkillType() != skillType && (e.getSkill().getEffectType() == null || e.getSkill().getEffectType() != skillType));
                } while (negateLvl != -1 && (e.getSkill().getEffectType() == null || e.getSkill().getEffectAbnormalLvl() < 0 || e.getSkill().getEffectAbnormalLvl() > negateLvl) && (e.getSkill().getAbnormalLvl() < 0 || e.getSkill().getAbnormalLvl() > negateLvl));

                e.exit();
            }
        }
    }

    public void stopEffectsOnAction() {
        if (this._hasBuffsRemovedOnAnyAction && this._buffs != null && !this._buffs.isEmpty()) {
            Iterator var1 = this._buffs.iterator();

            while (var1.hasNext()) {
                L2Effect e = (L2Effect) var1.next();
                if (e != null && e.getSkill().isRemovedOnAnyActionExceptMove()) {
                    e.exit(true);
                }
            }
        }

    }

    public void stopEffectsOnDamage(boolean awake) {
        Iterator var2;
        L2Effect e;
        if (this._hasBuffsRemovedOnDamage && this._buffs != null && !this._buffs.isEmpty()) {
            var2 = this._buffs.iterator();

            label67:
            while (true) {
                do {
                    do {
                        do {
                            if (!var2.hasNext()) {
                                break label67;
                            }

                            e = (L2Effect) var2.next();
                        } while (e == null);
                    } while (!e.getSkill().isRemovedOnDamage());
                } while (!awake && e.getSkill().getSkillType() == L2SkillType.SLEEP);

                e.exit(true);
            }
        }

        if (this._hasDebuffsRemovedOnDamage && this._debuffs != null && !this._debuffs.isEmpty()) {
            var2 = this._debuffs.iterator();

            while (true) {
                do {
                    do {
                        do {
                            if (!var2.hasNext()) {
                                return;
                            }

                            e = (L2Effect) var2.next();
                        } while (e == null);
                    } while (!e.getSkill().isRemovedOnDamage());
                } while (!awake && e.getSkill().getSkillType() == L2SkillType.SLEEP);

                e.exit(true);
            }
        }
    }

    public void updateEffectIcons(boolean partyOnly) {
        if (this._buffs != null || this._debuffs != null) {
            if (partyOnly) {
                this._partyOnly = true;
            }

            this.queueRunner();
        }
    }

    public void queueEffect(L2Effect effect, boolean remove) {
        if (effect != null) {
            if (!this._queuesInitialized) {
                this.init();
            }

            if (remove) {
                this._removeQueue.offer(effect);
            } else {
                this._addQueue.offer(effect);
            }

            this.queueRunner();
        }
    }

    private synchronized void init() {
        if (!this._queuesInitialized) {
            this._addQueue = new LinkedBlockingQueue();
            this._removeQueue = new LinkedBlockingQueue();
            this._queuesInitialized = true;
        }
    }

    private void queueRunner() {
        if (this.queueLock.compareAndSet(false, true)) {
            try {
                do {
                    do {
                        L2Effect effect;
                        while ((effect = this._removeQueue.poll()) != null) {
                            this.removeEffectFromQueue(effect);
                            this._partyOnly = false;
                        }

                        if ((effect = this._addQueue.poll()) != null) {
                            this.addEffectFromQueue(effect);
                            this._partyOnly = false;
                        }
                    } while (!this._addQueue.isEmpty());
                } while (!this._removeQueue.isEmpty());

                this.computeEffectFlags();
                this.updateEffectIcons();
            } finally {
                this.queueLock.set(false);
            }

        }
    }

    protected void removeEffectFromQueue(L2Effect effect) {
        if (effect != null) {
            this._rebuildCache = true;
            List effectList;
            if (effect.getSkill().isDebuff()) {
                if (this._debuffs == null) {
                    return;
                }

                effectList = this._debuffs;
            } else {
                if (this._buffs == null) {
                    return;
                }

                effectList = this._buffs;
            }

            if ("none".equals(effect.getStackType())) {
                this._owner.removeStatsByOwner(effect);
            } else {
                if (this._stackedEffects == null) {
                    return;
                }

                List<L2Effect> stackQueue = this._stackedEffects.get(effect.getStackType());
                if (stackQueue == null || stackQueue.isEmpty()) {
                    return;
                }

                int index = stackQueue.indexOf(effect);
                if (index >= 0) {
                    stackQueue.remove(effect);
                    if (index == 0) {
                        this._owner.removeStatsByOwner(effect);
                        if (!stackQueue.isEmpty()) {
                            L2Effect newStackedEffect = this.listsContains(stackQueue.get(0));
                            if (newStackedEffect != null && newStackedEffect.setInUse(true)) {
                                this._owner.addStatFuncs(newStackedEffect.getStatFuncs());
                            }
                        }
                    }

                    if (stackQueue.isEmpty()) {
                        this._stackedEffects.remove(effect.getStackType());
                    } else {
                        this._stackedEffects.put(effect.getStackType(), stackQueue);
                    }
                }
            }

            if (effectList.remove(effect) && this._owner instanceof Player && effect.getShowIcon()) {
                SystemMessage sm;
                if (effect.getSkill().isToggle()) {
                    sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_ABORTED);
                } else {
                    sm = SystemMessage.getSystemMessage(SystemMessageId.EFFECT_S1_DISAPPEARED);
                }

                sm.addSkillName(effect);
                this._owner.sendPacket(sm);
            }

        }
    }

    protected void addEffectFromQueue(L2Effect newEffect) {
        if (newEffect != null) {
            L2Skill newSkill = newEffect.getSkill();
            this._rebuildCache = true;
            if (this.isAffected(newEffect.getEffectFlags()) && !newEffect.onSameEffect(null)) {
                newEffect.stopEffectTask();
            } else {
                Iterator var3;
                L2Effect effectToAdd;
                L2Effect effectToRemove;
                if (newSkill.isDebuff()) {
                    if (this._debuffs == null) {
                        this._debuffs = new CopyOnWriteArrayList();
                    }

                    var3 = this._debuffs.iterator();

                    while (var3.hasNext()) {
                        effectToAdd = (L2Effect) var3.next();
                        if (effectToAdd != null && effectToAdd.getSkill().getId() == newEffect.getSkill().getId() && effectToAdd.getEffectType() == newEffect.getEffectType() && effectToAdd.getStackOrder() == newEffect.getStackOrder() && effectToAdd.getStackType().equals(newEffect.getStackType())) {
                            newEffect.stopEffectTask();
                            return;
                        }
                    }

                    this._debuffs.add(newEffect);
                } else {
                    if (this._buffs == null) {
                        this._buffs = new CopyOnWriteArrayList();
                    }

                    var3 = this._buffs.iterator();

                    while (var3.hasNext()) {
                        effectToAdd = (L2Effect) var3.next();
                        if (effectToAdd != null && effectToAdd.getSkill().getId() == newEffect.getSkill().getId() && effectToAdd.getEffectType() == newEffect.getEffectType() && effectToAdd.getStackOrder() == newEffect.getStackOrder() && effectToAdd.getStackType().equals(newEffect.getStackType())) {
                            effectToAdd.exit();
                        }
                    }

                    if (newEffect.isHerbEffect() && this.getBuffCount() >= this._owner.getMaxBuffCount()) {
                        newEffect.stopEffectTask();
                        return;
                    }

                    int effectsToRemove;
                    Iterator var9;
                    if (!this.doesStack(newSkill) && !newSkill.is7Signs()) {
                        effectsToRemove = this.getBuffCount() - this._owner.getMaxBuffCount();
                        if (effectsToRemove >= 0) {
                            switch (newSkill.getSkillType()) {
                                case BUFF:
                                case COMBATPOINTHEAL:
                                case REFLECT:
                                case HEAL_PERCENT:
                                case HEAL_STATIC:
                                case MANAHEAL_PERCENT:
                                    var9 = this._buffs.iterator();

                                    label156:
                                    while (var9.hasNext()) {
                                        effectToRemove = (L2Effect) var9.next();
                                        if (effectToRemove != null) {
                                            switch (effectToRemove.getSkill().getSkillType()) {
                                                case BUFF:
                                                case COMBATPOINTHEAL:
                                                case REFLECT:
                                                case HEAL_PERCENT:
                                                case HEAL_STATIC:
                                                case MANAHEAL_PERCENT:
                                                    effectToRemove.exit();
                                                    --effectsToRemove;
                                                    if (effectsToRemove < 0) {
                                                        break label156;
                                                    }
                                            }
                                        }
                                    }
                            }
                        }
                    }

                    if (newSkill.isToggle()) {
                        this._buffs.add(newEffect);
                    } else {
                        effectsToRemove = 0;
                        var9 = this._buffs.iterator();

                        while (var9.hasNext()) {
                            effectToRemove = (L2Effect) var9.next();
                            if (effectToRemove != null && !effectToRemove.getSkill().isToggle() && !effectToRemove.getSkill().is7Signs()) {
                                ++effectsToRemove;
                            }
                        }

                        this._buffs.add(effectsToRemove, newEffect);
                    }
                }

                if ("none".equals(newEffect.getStackType())) {
                    if (newEffect.setInUse(true)) {
                        this._owner.addStatFuncs(newEffect.getStatFuncs());
                    }

                } else {
                    effectToAdd = null;
                    effectToRemove = null;
                    if (this._stackedEffects == null) {
                        this._stackedEffects = new HashMap();
                    }

                    List<L2Effect> stackQueue = this._stackedEffects.get(newEffect.getStackType());
                    if (stackQueue != null) {
                        int pos = 0;
                        if (!stackQueue.isEmpty()) {
                            effectToRemove = this.listsContains((L2Effect) ((List) stackQueue).get(0));

                            for (Iterator queueIterator = ((List) stackQueue).iterator(); queueIterator.hasNext() && newEffect.getStackOrder() < ((L2Effect) queueIterator.next()).getStackOrder(); ++pos) {
                            }

                            stackQueue.add(pos, newEffect);
                            if (Config.EFFECT_CANCELING && !newEffect.isHerbEffect() && stackQueue.size() > 1) {
                                if (newSkill.isDebuff()) {
                                    this._debuffs.remove(((List) stackQueue).remove(1));
                                } else {
                                    this._buffs.remove(((List) stackQueue).remove(1));
                                }
                            }
                        } else {
                            stackQueue.add(0, newEffect);
                        }
                    } else {
                        stackQueue = new ArrayList();
                        stackQueue.add(0, newEffect);
                    }

                    this._stackedEffects.put(newEffect.getStackType(), stackQueue);
                    if (!stackQueue.isEmpty()) {
                        effectToAdd = this.listsContains((L2Effect) ((List) stackQueue).get(0));
                    }

                    if (effectToRemove != effectToAdd) {
                        if (effectToRemove != null) {
                            this._owner.removeStatsByOwner(effectToRemove);
                            effectToRemove.setInUse(false);
                        }

                        if (effectToAdd != null && effectToAdd.setInUse(true)) {
                            this._owner.addStatFuncs(effectToAdd.getStatFuncs());
                        }
                    }

                }
            }
        }
    }

    protected void updateEffectIcons() {
        if (this._owner != null) {
            if (!(this._owner instanceof Playable)) {
                this.updateEffectFlags();
            } else {
                AbnormalStatusUpdate mi = null;
                PartySpelled ps = null;
                ExOlympiadSpelledInfo os = null;
                if (this._owner instanceof Player) {
                    if (this._partyOnly) {
                        this._partyOnly = false;
                    } else {
                        mi = new AbnormalStatusUpdate();
                    }

                    if (this._owner.isInParty()) {
                        ps = new PartySpelled(this._owner);
                    }

                    if (((Player) this._owner).isInOlympiadMode() && ((Player) this._owner).isOlympiadStart()) {
                        os = new ExOlympiadSpelledInfo((Player) this._owner);
                    }
                } else if (this._owner instanceof Summon) {
                    ps = new PartySpelled(this._owner);
                }

                boolean foundRemovedOnAction = false;
                boolean foundRemovedOnDamage = false;
                Iterator var6;
                L2Effect e;
                if (this._buffs != null && !this._buffs.isEmpty()) {
                    var6 = this._buffs.iterator();

                    while (var6.hasNext()) {
                        e = (L2Effect) var6.next();
                        if (e != null) {
                            if (e.getSkill().isRemovedOnAnyActionExceptMove()) {
                                foundRemovedOnAction = true;
                            }

                            if (e.getSkill().isRemovedOnDamage()) {
                                foundRemovedOnDamage = true;
                            }

                            if (e.getShowIcon()) {
                                switch (e.getEffectType()) {
                                    case SIGNET_GROUND:
                                        break;
                                    default:
                                        if (e.getInUse()) {
                                            if (mi != null) {
                                                e.addIcon(mi);
                                            }

                                            if (ps != null) {
                                                e.addPartySpelledIcon(ps);
                                            }

                                            if (os != null) {
                                                e.addOlympiadSpelledIcon(os);
                                            }
                                        }
                                }
                            }
                        }
                    }
                }

                this._hasBuffsRemovedOnAnyAction = foundRemovedOnAction;
                this._hasBuffsRemovedOnDamage = foundRemovedOnDamage;
                foundRemovedOnDamage = false;
                if (this._debuffs != null && !this._debuffs.isEmpty()) {
                    var6 = this._debuffs.iterator();

                    while (var6.hasNext()) {
                        e = (L2Effect) var6.next();
                        if (e != null) {
                            if (e.getSkill().isRemovedOnAnyActionExceptMove()) {
                                foundRemovedOnAction = true;
                            }

                            if (e.getSkill().isRemovedOnDamage()) {
                                foundRemovedOnDamage = true;
                            }

                            if (e.getShowIcon()) {
                                switch (e.getEffectType()) {
                                    case SIGNET_GROUND:
                                        break;
                                    default:
                                        if (e.getInUse()) {
                                            if (mi != null) {
                                                e.addIcon(mi);
                                            }

                                            if (ps != null) {
                                                e.addPartySpelledIcon(ps);
                                            }

                                            if (os != null) {
                                                e.addOlympiadSpelledIcon(os);
                                            }
                                        }
                                }
                            }
                        }
                    }
                }

                this._hasDebuffsRemovedOnDamage = foundRemovedOnDamage;
                if (mi != null) {
                    this._owner.sendPacket(mi);
                }

                if (ps != null) {
                    if (this._owner instanceof Summon) {
                        Player summonOwner = ((Summon) this._owner).getOwner();
                        if (summonOwner != null) {
                            Party party = summonOwner.getParty();
                            if (party != null) {
                                party.broadcastPacket(ps);
                            } else {
                                summonOwner.sendPacket(ps);
                            }
                        }
                    } else if (this._owner instanceof Player && this._owner.isInParty()) {
                        this._owner.getParty().broadcastPacket(ps);
                    }
                }

                if (os != null) {
                    OlympiadGameTask game = OlympiadGameManager.getInstance().getOlympiadTask(((Player) this._owner).getOlympiadGameId());
                    if (game != null && game.isBattleStarted()) {
                        game.getZone().broadcastPacketToObservers(os);
                    }
                }

            }
        }
    }

    protected void updateEffectFlags() {
        boolean foundRemovedOnAction = false;
        boolean foundRemovedOnDamage = false;
        Iterator var3;
        L2Effect e;
        if (this._buffs != null && !this._buffs.isEmpty()) {
            var3 = this._buffs.iterator();

            while (var3.hasNext()) {
                e = (L2Effect) var3.next();
                if (e != null) {
                    if (e.getSkill().isRemovedOnAnyActionExceptMove()) {
                        foundRemovedOnAction = true;
                    }

                    if (e.getSkill().isRemovedOnDamage()) {
                        foundRemovedOnDamage = true;
                    }
                }
            }
        }

        this._hasBuffsRemovedOnAnyAction = foundRemovedOnAction;
        this._hasBuffsRemovedOnDamage = foundRemovedOnDamage;
        foundRemovedOnDamage = false;
        if (this._debuffs != null && !this._debuffs.isEmpty()) {
            var3 = this._debuffs.iterator();

            while (var3.hasNext()) {
                e = (L2Effect) var3.next();
                if (e != null && e.getSkill().isRemovedOnDamage()) {
                    foundRemovedOnDamage = true;
                }
            }
        }

        this._hasDebuffsRemovedOnDamage = foundRemovedOnDamage;
    }

    private L2Effect listsContains(L2Effect effect) {
        if (this._buffs != null && !this._buffs.isEmpty() && this._buffs.contains(effect)) {
            return effect;
        } else {
            return this._debuffs != null && !this._debuffs.isEmpty() && this._debuffs.contains(effect) ? effect : null;
        }
    }

    private final void computeEffectFlags() {
        int flags = 0;
        Iterator var2;
        L2Effect e;
        if (this._buffs != null) {
            var2 = this._buffs.iterator();

            while (var2.hasNext()) {
                e = (L2Effect) var2.next();
                if (e != null) {
                    flags |= e.getEffectFlags();
                }
            }
        }

        if (this._debuffs != null) {
            var2 = this._debuffs.iterator();

            while (var2.hasNext()) {
                e = (L2Effect) var2.next();
                if (e != null) {
                    flags |= e.getEffectFlags();
                }
            }
        }

        this._effectFlags = flags;
    }

    public boolean isAffected(L2EffectFlag flag) {
        return this.isAffected(flag.getMask());
    }

    public boolean isAffected(int mask) {
        return (this._effectFlags & mask) != 0;
    }

    public void clear() {
        this._addQueue = null;
        this._removeQueue = null;
        this._buffs = null;
        this._debuffs = null;
        this._stackedEffects = null;
        this._queuesInitialized = false;
    }
}