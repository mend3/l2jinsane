package net.sf.l2j.gameserver.skills.basefuncs;

import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.conditions.Condition;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class FuncTemplate {
    private static final Logger _log = Logger.getLogger(FuncTemplate.class.getName());
    public final Constructor<?> constructor;
    public final Stats stat;
    public final int order;
    public final Lambda lambda;
    public Condition attachCond;
    public Condition applayCond;

    public FuncTemplate(Condition attachCond, Condition applyCond, String function, Stats stat, double value, Lambda pLambda) {
        this.attachCond = attachCond;
        this.order = (int) value;
        this.stat = stat;
        this.applayCond = applyCond;
        this.lambda = pLambda;

        try {
            final Class<?> functionClass = Class.forName("net.sf.l2j.gameserver.skills.basefuncs.Func" + function);
            this.constructor = functionClass.getConstructor(Stats.class, int.class, Object.class, Lambda.class);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public FuncTemplate(Condition pAttachCond, Condition pApplayCond, String pFunc, Stats pStat, int pOrder, Lambda pLambda) {
        this.attachCond = pAttachCond;
        this.applayCond = pApplayCond;
        this.stat = pStat;
        this.order = pOrder;
        this.lambda = pLambda;
        try {
            this.constructor = Class.forName("net.sf.l2j.gameserver.skills.basefuncs.Func" + pFunc).getConstructor(Stats.class, int.class, Object.class, Lambda.class);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public Func getFunc(Env env, Object owner) {
        if (this.attachCond != null && !this.attachCond.test(env))
            return null;
        try {
            Func f = (Func) this.constructor.newInstance(new Object[]{this.stat, this.order, owner, this.lambda});
            if (this.applayCond != null)
                f.setCondition(this.applayCond);
            return f;
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            _log.log(Level.WARNING, "", e);
            return null;
        }
    }
}
