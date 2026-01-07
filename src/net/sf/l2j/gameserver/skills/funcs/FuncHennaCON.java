package net.sf.l2j.gameserver.skills.funcs;

import net.sf.l2j.gameserver.enums.actors.HennaType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.basefuncs.Func;

public class FuncHennaCON extends Func {
    private static final HennaType STAT = HennaType.CON;

    private static final FuncHennaCON INSTANCE = new FuncHennaCON();

    private FuncHennaCON() {
        super(STAT.getStats(), 16, null, null);
    }

    public static Func getInstance() {
        return INSTANCE;
    }

    public void calc(Env env) {
        Player player = env.getPlayer();
        if (player != null)
            env.addValue(player.getHennaList().getStat(STAT));
    }
}
