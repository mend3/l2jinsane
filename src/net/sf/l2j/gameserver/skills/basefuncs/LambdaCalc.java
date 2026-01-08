package net.sf.l2j.gameserver.skills.basefuncs;

import net.sf.l2j.gameserver.skills.Env;

import java.util.ArrayList;
import java.util.List;

public final class LambdaCalc extends Lambda {
    private final List<Func> _funcs = new ArrayList<>();

    public double calc(Env env) {
        double saveValue = env.getValue();

        double var9;
        try {
            env.setValue(0.0F);

            for (Func f : this._funcs) {
                f.calc(env);
            }

            var9 = env.getValue();
        } finally {
            env.setValue(saveValue);
        }

        return var9;
    }

    public void addFunc(Func f) {
        this._funcs.add(f);
    }

    public List<Func> getFuncs() {
        return this._funcs;
    }
}
