package net.sf.l2j.gameserver.skills.basefuncs;

import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.skills.Env;

public class FuncAddMul extends Func {
    public FuncAddMul(Stats pStat, int pOrder, Object owner, Lambda lambda) {
        super(pStat, pOrder, owner, lambda);
    }

    public void calc(Env env) {
        if (this.cond == null || this.cond.test(env))
            env.mulValue(1.0D - this._lambda.calc(env) / 100.0D);
    }
}
