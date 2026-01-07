package net.sf.l2j.gameserver.skills.funcs;

import net.sf.l2j.gameserver.enums.actors.HennaType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.basefuncs.Func;

public class FuncHennaDEX extends Func {
    private static final HennaType STAT = HennaType.DEX;

    private static final FuncHennaDEX INSTANCE = new FuncHennaDEX();

    private FuncHennaDEX() {
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
