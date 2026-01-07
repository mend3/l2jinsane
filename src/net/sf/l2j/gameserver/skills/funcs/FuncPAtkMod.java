package net.sf.l2j.gameserver.skills.funcs;

import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.basefuncs.Func;

public class FuncPAtkMod extends Func {
    static final FuncPAtkMod _fpa_instance = new FuncPAtkMod();

    private FuncPAtkMod() {
        super(Stats.POWER_ATTACK, 48, null, null);
    }

    public static Func getInstance() {
        return _fpa_instance;
    }

    public void calc(Env env) {
        env.mulValue(Formulas.STR_BONUS[env.getCharacter().getSTR()] * env.getCharacter().getLevelMod());
    }
}
