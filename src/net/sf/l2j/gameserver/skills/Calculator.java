/**/
package net.sf.l2j.gameserver.skills;

import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.skills.basefuncs.Func;

import java.util.ArrayList;
import java.util.List;

public final class Calculator {
    private static final Func[] _emptyFuncs = new Func[0];
    private Func[] _functions;

    public Calculator() {
        this._functions = _emptyFuncs;
    }

    public Calculator(Calculator c) {
        this._functions = c._functions;
    }

    public int size() {
        return this._functions.length;
    }

    public synchronized void addFunc(Func f) {
        Func[] funcs = this._functions;
        Func[] tmp = new Func[funcs.length + 1];
        int order = f.order;

        int i;
        for (i = 0; i < funcs.length && order >= funcs[i].order; ++i) {
            tmp[i] = funcs[i];
        }

        for (tmp[i] = f; i < funcs.length; ++i) {
            tmp[i + 1] = funcs[i];
        }

        this._functions = tmp;
    }

    public synchronized void removeFunc(Func f) {
        Func[] funcs = this._functions;
        Func[] tmp = new Func[funcs.length - 1];

        int i;
        for (i = 0; i < funcs.length && f != funcs[i]; ++i) {
            tmp[i] = funcs[i];
        }

        if (i != funcs.length) {
            ++i;

            while (i < funcs.length) {
                tmp[i - 1] = funcs[i];
                ++i;
            }

            if (tmp.length == 0) {
                this._functions = _emptyFuncs;
            } else {
                this._functions = tmp;
            }

        }
    }

    public synchronized List<Stats> removeOwner(Object owner) {
        List<Stats> modifiedStats = new ArrayList<>();
        Func[] var3 = this._functions;
        int var4 = var3.length;

        for (Func func : var3) {
            if (func.funcOwner == owner) {
                modifiedStats.add(func.stat);
                this.removeFunc(func);
            }
        }

        return modifiedStats;
    }

    public void calc(Env env) {
        Func[] var2 = this._functions;
        int var3 = var2.length;

        for (Func func : var2) {
            func.calc(env);
        }

    }
}