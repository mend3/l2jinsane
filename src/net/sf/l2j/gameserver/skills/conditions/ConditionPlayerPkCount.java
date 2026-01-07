package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.skills.Env;

public class ConditionPlayerPkCount extends Condition {
    public final int _pk;

    public ConditionPlayerPkCount(int pk) {
        this._pk = pk;
    }

    public boolean testImpl(Env env) {
        if (env.getPlayer() == null)
            return false;
        return (env.getPlayer().getPkKills() <= this._pk);
    }
}
