/**/
package net.sf.l2j.gameserver.model;

import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.enums.skills.AbnormalEffect;
import net.sf.l2j.gameserver.enums.skills.L2EffectFlag;
import net.sf.l2j.gameserver.enums.skills.L2EffectType;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Servitor;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.AbnormalStatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ExOlympiadSpelledInfo;
import net.sf.l2j.gameserver.network.serverpackets.PartySpelled;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.basefuncs.Func;
import net.sf.l2j.gameserver.skills.basefuncs.FuncTemplate;
import net.sf.l2j.gameserver.skills.basefuncs.Lambda;
import net.sf.l2j.gameserver.skills.effects.EffectTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class L2Effect {
    protected static final Logger _log = Logger.getLogger(L2Effect.class.getName());
    private final Creature _effector;
    private final Creature _effected;
    private final L2Skill _skill;
    private final boolean _isHerbEffect;
    private final Lambda _lambda;
    private final int _period;
    private final EffectTemplate _template;
    private final List<FuncTemplate> _funcTemplates;
    private final int _totalCount;
    private final AbnormalEffect _abnormalEffect;
    private final boolean _icon;
    private final String _stackType;
    private final float _stackOrder;
    private final double _effectPower;
    private final L2SkillType _effectSkillType;
    public boolean preventExitUpdate;
    protected long _periodStartTime;
    protected int _periodFirstTime;
    private EffectState _state;
    private int _count;
    private boolean _isSelfEffect = false;
    private ScheduledFuture<?> _currentFuture;
    private boolean _inUse = false;
    private boolean _startConditionsCorrect = true;

    protected L2Effect(Env env, EffectTemplate template) {
        this._state = L2Effect.EffectState.CREATED;
        this._skill = env.getSkill();
        this._template = template;
        this._effected = env.getTarget();
        this._effector = env.getCharacter();
        this._lambda = template.lambda;
        this._funcTemplates = template.funcTemplates;
        this._count = template.counter;
        this._totalCount = this._count;
        int temp = template.period;
        if (this._skill.getId() > 2277 && this._skill.getId() < 2286 && (this._effected instanceof Servitor || this._effected instanceof Player && this._effected.getSummon() != null)) {
            temp /= 2;
        }

        if (env.isSkillMastery()) {
            temp *= 2;
        }

        this._period = temp;
        this._abnormalEffect = template.abnormalEffect;
        this._stackType = template.stackType;
        this._stackOrder = template.stackOrder;
        this._periodStartTime = System.currentTimeMillis();
        this._periodFirstTime = 0;
        this._icon = template.icon;
        this._effectPower = template.effectPower;
        this._effectSkillType = template.effectType;
        this._isHerbEffect = this._skill.getName().contains("Herb");
    }

    public int getCount() {
        return this._count;
    }

    public void setCount(int newcount) {
        this._count = Math.min(newcount, this._totalCount);
    }

    public int getTotalCount() {
        return this._totalCount;
    }

    public void setFirstTime(int newFirstTime) {
        this._periodFirstTime = Math.min(newFirstTime, this._period);
        this._periodStartTime = System.currentTimeMillis() - (long) (this._periodFirstTime * 1000);
    }

    public boolean getShowIcon() {
        return this._icon;
    }

    public int getPeriod() {
        return this._period;
    }

    public int getTime() {
        return (int) ((System.currentTimeMillis() - this._periodStartTime) / 1000L);
    }

    public int getTaskTime() {
        return this._count == this._totalCount ? 0 : Math.abs(this._count - this._totalCount + 1) * this._period + this.getTime() + 1;
    }

    public boolean getInUse() {
        return this._inUse;
    }

    public boolean setInUse(boolean inUse) {
        this._inUse = inUse;
        if (this._inUse) {
            this._startConditionsCorrect = this.onStart();
        } else {
            this.onExit();
        }

        return this._startConditionsCorrect;
    }

    public String getStackType() {
        return this._stackType;
    }

    public float getStackOrder() {
        return this._stackOrder;
    }

    public final L2Skill getSkill() {
        return this._skill;
    }

    public final Creature getEffector() {
        return this._effector;
    }

    public final Creature getEffected() {
        return this._effected;
    }

    public boolean isSelfEffect() {
        return this._isSelfEffect;
    }

    public void setSelfEffect() {
        this._isSelfEffect = true;
    }

    public boolean isHerbEffect() {
        return this._isHerbEffect;
    }

    public final double calc() {
        Env env = new Env();
        env.setCharacter(this._effector);
        env.setTarget(this._effected);
        env.setSkill(this._skill);
        return this._lambda.calc(env);
    }

    private final synchronized void startEffectTask() {
        if (this._period > 0) {
            this.stopEffectTask();
            int initialDelay = Math.max((this._period - this._periodFirstTime) * 1000, 5);
            if (this._count > 1) {
                this._currentFuture = ThreadPool.scheduleAtFixedRate(new EffectTask(), initialDelay, this._period * 1000);
            } else {
                this._currentFuture = ThreadPool.schedule(new EffectTask(), initialDelay);
            }
        }

        if (this._state == L2Effect.EffectState.ACTING) {
            if (this.isSelfEffectType()) {
                this._effector.addEffect(this);
            } else {
                this._effected.addEffect(this);
            }
        }

    }

    public final void exit() {
        this.exit(false);
    }

    public final void exit(boolean preventUpdate) {
        this.preventExitUpdate = preventUpdate;
        this._state = L2Effect.EffectState.FINISHING;
        this.scheduleEffect();
    }

    public final synchronized void stopEffectTask() {
        if (this._currentFuture != null) {
            this._currentFuture.cancel(false);
            this._currentFuture = null;
            if (this.isSelfEffectType() && this.getEffector() != null) {
                this.getEffector().removeEffect(this);
            } else if (this.getEffected() != null) {
                this.getEffected().removeEffect(this);
            }
        }

    }

    public abstract L2EffectType getEffectType();

    public boolean onStart() {
        if (this._abnormalEffect != AbnormalEffect.NULL) {
            this.getEffected().startAbnormalEffect(this._abnormalEffect);
        }

        return true;
    }

    public void onExit() {
        if (this._abnormalEffect != AbnormalEffect.NULL) {
            this.getEffected().stopAbnormalEffect(this._abnormalEffect);
        }

    }

    public abstract boolean onActionTime();

    public final void rescheduleEffect() {
        if (this._state != L2Effect.EffectState.ACTING) {
            this.scheduleEffect();
        } else if (this._period != 0) {
            this.startEffectTask();
            return;
        }

    }

    public final void scheduleEffect() {
        switch (this._state.ordinal()) {
            case 0:
                this._state = L2Effect.EffectState.ACTING;
                if (this._skill.isPvpSkill() && this._icon && this.getEffected() instanceof Player) {
                    SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
                    smsg.addSkillName(this._skill);
                    this.getEffected().sendPacket(smsg);
                }

                if (this._period != 0) {
                    this.startEffectTask();
                    return;
                } else {
                    this._startConditionsCorrect = this.onStart();
                }
            case 1:
                if (this._count > 0) {
                    --this._count;
                    if (this.getInUse()) {
                        if (this.onActionTime() && this._startConditionsCorrect && this._count > 0) {
                            return;
                        }
                    } else if (this._count > 0) {
                        return;
                    }
                }

                this._state = L2Effect.EffectState.FINISHING;
            case 2:
                if (this._count == 0 && this._icon && this.getEffected() instanceof Player) {
                    this.getEffected().sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_WORN_OFF).addSkillName(this._skill));
                }

                if (this._currentFuture == null && this.getEffected() != null) {
                    this.getEffected().removeEffect(this);
                }

                this.stopEffectTask();
                if ((this.getInUse() || this._count <= 1 && this._period <= 0) && this._startConditionsCorrect) {
                    this.onExit();
                }
            default:
        }
    }

    public List<Func> getStatFuncs() {
        if (this._funcTemplates == null) {
            return Collections.emptyList();
        } else {
            List<Func> funcs = new ArrayList<>(this._funcTemplates.size());
            Env env = new Env();
            env.setCharacter(this.getEffector());
            env.setTarget(this.getEffected());
            env.setSkill(this.getSkill());

            for (FuncTemplate t : this._funcTemplates) {
                Func f = t.getFunc(env, this);
                if (f != null) {
                    funcs.add(f);
                }
            }

            return funcs;
        }
    }

    public final void addIcon(AbnormalStatusUpdate mi) {
        if (this._state == L2Effect.EffectState.ACTING) {
            ScheduledFuture<?> future = this._currentFuture;
            L2Skill sk = this.getSkill();
            if (this._totalCount > 1) {
                if (sk.isPotion()) {
                    mi.addEffect(sk.getId(), this.getLevel(), sk.getBuffDuration() - this.getTaskTime() * 1000);
                } else {
                    mi.addEffect(sk.getId(), this.getLevel(), -1);
                }
            } else if (future != null) {
                mi.addEffect(sk.getId(), this.getLevel(), (int) future.getDelay(TimeUnit.MILLISECONDS));
            } else if (this._period == -1) {
                mi.addEffect(sk.getId(), this.getLevel(), this._period);
            }

        }
    }

    public final void addPartySpelledIcon(PartySpelled ps) {
        if (this._state == L2Effect.EffectState.ACTING) {
            ScheduledFuture<?> future = this._currentFuture;
            L2Skill sk = this.getSkill();
            if (future != null) {
                ps.addPartySpelledEffect(sk.getId(), this.getLevel(), (int) future.getDelay(TimeUnit.MILLISECONDS));
            } else if (this._period == -1) {
                ps.addPartySpelledEffect(sk.getId(), this.getLevel(), this._period);
            }

        }
    }

    public final void addOlympiadSpelledIcon(ExOlympiadSpelledInfo os) {
        if (this._state == L2Effect.EffectState.ACTING) {
            ScheduledFuture<?> future = this._currentFuture;
            L2Skill sk = this.getSkill();
            if (future != null) {
                os.addEffect(sk.getId(), this.getLevel(), (int) future.getDelay(TimeUnit.MILLISECONDS));
            } else if (this._period == -1) {
                os.addEffect(sk.getId(), this.getLevel(), this._period);
            }

        }
    }

    public int getLevel() {
        return this.getSkill().getLevel();
    }

    public EffectTemplate getEffectTemplate() {
        return this._template;
    }

    public double getEffectPower() {
        return this._effectPower;
    }

    public L2SkillType getSkillType() {
        return this._effectSkillType;
    }

    public int getEffectFlags() {
        return L2EffectFlag.NONE.getMask();
    }

    public String toString() {
        String var10000 = String.valueOf(this._skill);
        return "L2Effect [_skill=" + var10000 + ", _state=" + String.valueOf(this._state) + ", _period=" + this._period + "]";
    }

    public boolean isSelfEffectType() {
        return false;
    }

    public boolean onSameEffect(L2Effect effect) {
        return true;
    }

    public static enum EffectState {
        CREATED,
        ACTING,
        FINISHING;
    }

    protected final class EffectTask implements Runnable {
        public void run() {
            try {
                L2Effect.this._periodFirstTime = 0;
                L2Effect.this._periodStartTime = System.currentTimeMillis();
                L2Effect.this.scheduleEffect();
            } catch (Exception e) {
                L2Effect._log.log(Level.SEVERE, "", e);
            }

        }
    }
}
