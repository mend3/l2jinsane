package net.sf.l2j.gameserver.skills.funcs;

import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.basefuncs.Func;

public class FuncPDefMod extends Func {
    static final FuncPDefMod _fpa_instance = new FuncPDefMod();

    private FuncPDefMod() {
        super(Stats.POWER_DEFENCE, 32, null, null);
    }

    public static Func getInstance() {
        return _fpa_instance;
    }

    public void calc(Env env) {
        if (env.getCharacter() instanceof Player) {
            Player player = env.getPlayer();
            boolean isMage = player.isMageClass();
            if (player.getInventory().getPaperdollItem(6) != null)
                env.subValue(12.0D);
            if (player.getInventory().getPaperdollItem(10) != null)
                env.subValue(isMage ? 15.0D : 31.0D);
            if (player.getInventory().getPaperdollItem(11) != null)
                env.subValue(isMage ? 8.0D : 18.0D);
            if (player.getInventory().getPaperdollItem(9) != null)
                env.subValue(8.0D);
            if (player.getInventory().getPaperdollItem(12) != null)
                env.subValue(7.0D);
        }
        env.mulValue(env.getCharacter().getLevelMod());
    }
}
