package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.skills.Env;

public class ConditionPlayerMp extends Condition {
    private final int _mp;

    public ConditionPlayerMp(int mp) {
        this._mp = mp;
    }

    public boolean testImpl(Env env) {
        return (env.getCharacter().getCurrentMp() * 100.0D / env.getCharacter().getMaxMp() <= this._mp);
    }
}
