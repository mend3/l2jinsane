package net.sf.l2j.gameserver.skills.funcs;

import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.basefuncs.Func;

public class FuncAtkAccuracy extends Func {
    static final FuncAtkAccuracy _faa_instance = new FuncAtkAccuracy();

    private FuncAtkAccuracy() {
        super(Stats.ACCURACY_COMBAT, 16, null, null);
    }

    public static Func getInstance() {
        return _faa_instance;
    }

    public void calc(Env env) {
        int level = env.getCharacter().getLevel();
        env.addValue(Formulas.BASE_EVASION_ACCURACY[env.getCharacter().getDEX()] + level);
        if (env.getCharacter() instanceof net.sf.l2j.gameserver.model.actor.Summon)
            env.addValue((level < 60) ? 4.0D : 5.0D);
    }
}
