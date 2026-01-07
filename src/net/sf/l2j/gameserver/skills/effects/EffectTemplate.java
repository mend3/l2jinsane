package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.enums.skills.AbnormalEffect;
import net.sf.l2j.gameserver.enums.skills.L2SkillType;
import net.sf.l2j.gameserver.model.ChanceCondition;
import net.sf.l2j.gameserver.model.L2Effect;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.basefuncs.FuncTemplate;
import net.sf.l2j.gameserver.skills.basefuncs.Lambda;
import net.sf.l2j.gameserver.skills.conditions.Condition;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class EffectTemplate {
    static Logger _log = Logger.getLogger(EffectTemplate.class.getName());
    public final Condition attachCond;
    public final Condition applayCond;
    public final Lambda lambda;
    public final int counter;
    public final int period;
    public final AbnormalEffect abnormalEffect;
    public final String stackType;
    public final float stackOrder;
    public final boolean icon;
    public final double effectPower;
    public final L2SkillType effectType;
    public final int triggeredId;
    public final int triggeredLevel;
    public final ChanceCondition chanceCondition;
    private final Class<?> _func;
    private final Constructor<?> _constructor;
    public List<FuncTemplate> funcTemplates;

    public EffectTemplate(Condition pAttachCond, Condition pApplayCond, String func, Lambda pLambda, int pCounter, int pPeriod, AbnormalEffect pAbnormalEffect, String pStackType, float pStackOrder, boolean showicon, double ePower, L2SkillType eType, int trigId, int trigLvl, ChanceCondition chanceCond) {
        this.attachCond = pAttachCond;
        this.applayCond = pApplayCond;
        this.lambda = pLambda;
        this.counter = pCounter;
        this.period = pPeriod;
        this.abnormalEffect = pAbnormalEffect;
        this.stackType = pStackType;
        this.stackOrder = pStackOrder;
        this.icon = showicon;
        this.effectPower = ePower;
        this.effectType = eType;
        this.triggeredId = trigId;
        this.triggeredLevel = trigLvl;
        this.chanceCondition = chanceCond;
        try {
            this._func = Class.forName("net.sf.l2j.gameserver.skills.effects.Effect" + func);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            this._constructor = this._func.getConstructor(Env.class, EffectTemplate.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public L2Effect getEffect(Env env) {
        if (this.attachCond != null && !this.attachCond.test(env))
            return null;
        try {
            return (L2Effect) this._constructor.newInstance(new Object[]{env, this});
        } catch (IllegalAccessException | InstantiationException e) {
            _log.log(Level.WARNING, "", e);
            return null;
        } catch (InvocationTargetException e) {
            _log.log(Level.WARNING, "Error creating new instance of Class " + this._func + " Exception was: " + e.getTargetException().getMessage(), e.getTargetException());
            return null;
        }
    }

    public void attach(FuncTemplate f) {
        if (this.funcTemplates == null)
            this.funcTemplates = new ArrayList<>();
        this.funcTemplates.add(f);
    }
}
