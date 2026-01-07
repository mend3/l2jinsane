package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.skills.Env;

public class ConditionPlayerCharges extends Condition {
    private final int _charges;

    public ConditionPlayerCharges(int charges) {
        this._charges = charges;
    }

    public boolean testImpl(Env env) {
        return (env.getPlayer() != null && env.getPlayer().getCharges() >= this._charges);
    }
}
