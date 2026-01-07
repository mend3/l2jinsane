package mods.achievement.achievements.base;

import net.sf.l2j.gameserver.model.actor.Player;

public abstract class Condition {
    private final Object _value;

    private String _name;

    public Condition(Object value) {
        this._value = value;
    }

    public Object getValue() {
        return this._value;
    }

    public String getName() {
        return this._name;
    }

    public void setName(String s) {
        this._name = s;
    }

    public abstract boolean meetConditionRequirements(Player paramPlayer);

    public abstract String getStatus(Player paramPlayer);
}
