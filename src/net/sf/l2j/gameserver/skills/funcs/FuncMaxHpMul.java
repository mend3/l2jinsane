package net.sf.l2j.gameserver.skills.funcs;

import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.basefuncs.Func;

public class FuncMaxHpMul extends Func {
    static final FuncMaxHpMul _fmhm_instance = new FuncMaxHpMul();

    private FuncMaxHpMul() {
        super(Stats.MAX_HP, 32, null, null);
    }

    public static Func getInstance() {
        return _fmhm_instance;
    }

    public void calc(Env env) {
        env.mulValue(Formulas.CON_BONUS[env.getCharacter().getCON()]);
    }
}
