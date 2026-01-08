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
    public final Class<?> func;
    public final Constructor<?> constructor;
    public final Stats stat;
    public final int order;
    public final Lambda lambda;
    public final Condition attachCond;
    public final Condition applayCond;

    public FuncTemplate(Condition pAttachCond, Condition pApplayCond, String pFunc, Stats pStat, int pOrder, Lambda pLambda) {
        this.attachCond = pAttachCond;
        this.applayCond = pApplayCond;
        this.stat = pStat;
        this.order = pOrder;
        this.lambda = pLambda;

        try {
            this.func = Class.forName("net.sf.l2j.gameserver.skills.basefuncs.Func" + pFunc);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        try {
            this.constructor = this.func.getConstructor(Stats.class, Integer.TYPE, Object.class, Lambda.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public Func getFunc(Env env, Object owner) {
        if (this.attachCond != null && !this.attachCond.test(env)) {
            return null;
        } else {
            try {
                Func f = (Func) this.constructor.newInstance(this.stat, this.order, owner, this.lambda);
                if (this.applayCond != null) {
                    f.setCondition(this.applayCond);
                }

                return f;
            } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                _log.log(Level.WARNING, "", e);
                return null;
            }
        }
    }
}
