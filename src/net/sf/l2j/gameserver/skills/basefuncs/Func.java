package net.sf.l2j.gameserver.skills.basefuncs;

import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.conditions.Condition;

public abstract class Func {
    public final Stats stat;
    public final int order;
    public final Object funcOwner;
    public Condition cond;
    public final Lambda _lambda;

    public Func(Stats pStat, int pOrder, Object owner, Lambda lambda) {
        this.stat = pStat;
        this.order = pOrder;
        this.funcOwner = owner;
        this._lambda = lambda;
    }

    public void setCondition(Condition pCond) {
        this.cond = pCond;
    }

    public abstract void calc(Env var1);
}
