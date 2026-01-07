package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.skills.Env;

public class ConditionPlayerLevel extends Condition {
    private final int _level;

    public ConditionPlayerLevel(int level) {
        this._level = level;
    }

    public boolean testImpl(Env env) {
        return (env.getCharacter().getLevel() >= this._level);
    }
}
