package net.sf.l2j.gameserver.skills.funcs;

import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.basefuncs.Func;

public class FuncMAtkMod extends Func {
    static final FuncMAtkMod _fpa_instance = new FuncMAtkMod();

    private FuncMAtkMod() {
        super(Stats.MAGIC_ATTACK, 32, null, null);
    }

    public static Func getInstance() {
        return _fpa_instance;
    }

    public void calc(Env env) {
        double intb = Formulas.INT_BONUS[env.getCharacter().getINT()];
        double lvlb = env.getCharacter().getLevelMod();
        env.mulValue(lvlb * lvlb * intb * intb);
    }
}
