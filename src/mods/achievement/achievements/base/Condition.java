package mods.achievement.achievements.base;

import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.model.actor.Player;

public abstract class Condition {

    protected final StatSet _set;

    public Condition(StatSet set) {
        _set = set;
    }

    public Object getValue() {
        return true;
    }

    public String getName() {
        return this._set.getString("name");
    }

    public abstract boolean meetConditionRequirements(Player paramPlayer);

    public abstract String getStatus(Player paramPlayer);
}
