package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.skills.Env;

public final class ConditionPlayerPledgeClass extends Condition {
    private final int _pledgeClass;

    public ConditionPlayerPledgeClass(int pledgeClass) {
        this._pledgeClass = pledgeClass;
    }

    public boolean testImpl(Env env) {
        if (env.getPlayer() == null)
            return false;
        if (env.getPlayer().getClan() == null)
            return false;
        if (this._pledgeClass == -1)
            return env.getPlayer().isClanLeader();
        return (env.getPlayer().getPledgeClass() >= this._pledgeClass);
    }
}
