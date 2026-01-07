package net.sf.l2j.gameserver.skills.funcs;

import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.basefuncs.Func;

public class FuncPAtkSpeed extends Func {
    static final FuncPAtkSpeed _fas_instance = new FuncPAtkSpeed();

    private FuncPAtkSpeed() {
        super(Stats.POWER_ATTACK_SPEED, 32, null, null);
    }

    public static Func getInstance() {
        return _fas_instance;
    }

    public void calc(Env env) {
        env.mulValue(Formulas.DEX_BONUS[env.getCharacter().getDEX()]);
    }
}
