package net.sf.l2j.gameserver.skills.funcs;

import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.basefuncs.Func;

public class FuncMaxCpMul extends Func {
    static final FuncMaxCpMul _fmcm_instance = new FuncMaxCpMul();

    private FuncMaxCpMul() {
        super(Stats.MAX_CP, 32, null, null);
    }

    public static Func getInstance() {
        return _fmcm_instance;
    }

    public void calc(Env env) {
        env.mulValue(Formulas.CON_BONUS[env.getCharacter().getCON()]);
    }
}
