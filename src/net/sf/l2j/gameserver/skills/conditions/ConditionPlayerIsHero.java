package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.skills.Env;

public class ConditionPlayerIsHero extends Condition {
    private final boolean _val;

    public ConditionPlayerIsHero(boolean val) {
        this._val = val;
    }

    public boolean testImpl(Env env) {
        if (env.getPlayer() == null)
            return false;
        return (env.getPlayer().isHero() == this._val);
    }
}
