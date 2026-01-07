package net.sf.l2j.gameserver.skills.funcs;

import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.Formulas;
import net.sf.l2j.gameserver.skills.basefuncs.Func;

public class FuncMDefMod extends Func {
    static final FuncMDefMod _fpa_instance = new FuncMDefMod();

    private FuncMDefMod() {
        super(Stats.MAGIC_DEFENCE, 32, null, null);
    }

    public static Func getInstance() {
        return _fpa_instance;
    }

    public void calc(Env env) {
        if (env.getCharacter() instanceof Player) {
            Player player = env.getPlayer();
            if (player.getInventory().getPaperdollItem(4) != null)
                env.subValue(5.0D);
            if (player.getInventory().getPaperdollItem(5) != null)
                env.subValue(5.0D);
            if (player.getInventory().getPaperdollItem(1) != null)
                env.subValue(9.0D);
            if (player.getInventory().getPaperdollItem(2) != null)
                env.subValue(9.0D);
            if (player.getInventory().getPaperdollItem(3) != null)
                env.subValue(13.0D);
        }
        env.mulValue(Formulas.MEN_BONUS[env.getCharacter().getMEN()] * env.getCharacter().getLevelMod());
    }
}
