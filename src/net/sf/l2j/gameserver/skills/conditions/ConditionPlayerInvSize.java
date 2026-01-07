package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.skills.Env;

public class ConditionPlayerInvSize extends Condition {
    private final int _size;

    public ConditionPlayerInvSize(int size) {
        this._size = size;
    }

    public boolean testImpl(Env env) {
        if (env.getPlayer() != null)
            return (env.getPlayer().getInventory().getSize() <= env.getPlayer().getInventoryLimit() - this._size);
        return true;
    }
}
