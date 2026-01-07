package net.sf.l2j.gameserver.skills.funcs;

import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.basefuncs.Func;

public class FuncMoveSpeed extends Func {
    static final FuncMoveSpeed _fms_instance = new FuncMoveSpeed();

    private FuncMoveSpeed() {
        super(Stats.RUN_SPEED, 48, null, null);
    }

    public static Func getInstance() {
        return _fms_instance;
    }

    public void calc(Env env) {
        env.mulValue(Formulas.DEX_BONUS[env.getCharacter().getDEX()]);
    }
}
