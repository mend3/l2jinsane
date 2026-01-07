package net.sf.l2j.gameserver.skills.funcs;

import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.basefuncs.Func;

public class FuncMaxMpMul extends Func {
    static final FuncMaxMpMul _fmmm_instance = new FuncMaxMpMul();

    private FuncMaxMpMul() {
        super(Stats.MAX_MP, 32, null, null);
    }

    public static Func getInstance() {
        return _fmmm_instance;
    }

    public void calc(Env env) {
        env.mulValue(Formulas.MEN_BONUS[env.getCharacter().getMEN()]);
    }
}
