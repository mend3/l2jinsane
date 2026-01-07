package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.skills.Env;

public class ConditionPlayerSex extends Condition {
    private final int _sex;

    public ConditionPlayerSex(int sex) {
        this._sex = sex;
    }

    public boolean testImpl(Env env) {
        if (env.getPlayer() == null)
            return false;
        return (env.getPlayer().getAppearance().getSex().ordinal() == this._sex);
    }
}
