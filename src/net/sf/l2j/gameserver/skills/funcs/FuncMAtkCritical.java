package net.sf.l2j.gameserver.skills.funcs;

import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.basefuncs.Func;

public class FuncMAtkCritical extends Func {
    static final FuncMAtkCritical _fac_instance = new FuncMAtkCritical();

    private FuncMAtkCritical() {
        super(Stats.MCRITICAL_RATE, 9, null, null);
    }

    public static Func getInstance() {
        return _fac_instance;
    }

    public void calc(Env env) {
        Creature player = env.getCharacter();
        if (player instanceof net.sf.l2j.gameserver.model.actor.Player) {
            if (player.getActiveWeaponInstance() != null)
                env.mulValue(Formulas.WIT_BONUS[player.getWIT()]);
        } else {
            env.mulValue(Formulas.WIT_BONUS[player.getWIT()]);
        }
        env.setBaseValue(env.getValue());
    }
}
