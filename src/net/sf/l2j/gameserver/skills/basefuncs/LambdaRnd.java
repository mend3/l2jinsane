package net.sf.l2j.gameserver.skills.basefuncs;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.skills.Env;

public final class LambdaRnd extends Lambda {
    private final Lambda _max;
    private final boolean _linear;

    public LambdaRnd(Lambda max, boolean linear) {
        this._max = max;
        this._linear = linear;
    }

    public double calc(Env env) {
        return this._max.calc(env) * (this._linear ? Rnd.nextDouble() : Rnd.nextGaussian());
    }
}
