package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.skills.Env;

public class ConditionPlayerHp extends Condition {
    private final int _hp;

    public ConditionPlayerHp(int hp) {
        this._hp = hp;
    }

    public boolean testImpl(Env env) {
        return (env.getCharacter().getCurrentHp() * 100.0D / env.getCharacter().getMaxHp() <= this._hp);
    }
}
