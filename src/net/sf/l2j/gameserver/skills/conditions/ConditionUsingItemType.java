package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.skills.Env;

public final class ConditionUsingItemType extends Condition {
    private final int _mask;

    public ConditionUsingItemType(int mask) {
        this._mask = mask;
    }

    public boolean testImpl(Env env) {
        if (!(env.getCharacter() instanceof net.sf.l2j.gameserver.model.actor.Player))
            return false;
        return ((this._mask & env.getPlayer().getInventory().getWornMask()) != 0);
    }
}
