package net.sf.l2j.gameserver.skills.basefuncs;

import net.sf.l2j.gameserver.skills.Env;

public final class LambdaConst extends Lambda {
    private final double _value;

    public LambdaConst(double value) {
        this._value = value;
    }

    public double calc(Env env) {
        return this._value;
    }
}
