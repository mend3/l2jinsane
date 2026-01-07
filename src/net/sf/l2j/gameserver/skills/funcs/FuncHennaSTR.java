package net.sf.l2j.gameserver.skills.funcs;

import net.sf.l2j.gameserver.enums.actors.HennaType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.basefuncs.Func;

public class FuncHennaSTR extends Func {
    private static final HennaType STAT = HennaType.STR;

    private static final FuncHennaSTR INSTANCE = new FuncHennaSTR();

    private FuncHennaSTR() {
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
