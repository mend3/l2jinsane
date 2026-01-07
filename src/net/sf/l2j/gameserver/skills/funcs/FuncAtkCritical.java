package net.sf.l2j.gameserver.skills.funcs;

import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.basefuncs.Func;

public class FuncAtkCritical extends Func {
    static final FuncAtkCritical _fac_instance = new FuncAtkCritical();

    private FuncAtkCritical() {
        super(Stats.CRITICAL_RATE, 9, null, null);
    }

    public static Func getInstance() {
        return _fac_instance;
    }

    public void calc(Env env) {
        if (!(env.getCharacter() instanceof net.sf.l2j.gameserver.model.actor.Summon))
            env.mulValue(Formulas.DEX_BONUS[env.getCharacter().getDEX()]);
        env.mulValue(10.0D);
        env.setBaseValue(env.getValue());
    }
}
