package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.taskmanager.GameTimeTaskManager;

public class ConditionGameTime extends Condition {
    private final boolean _night;

    public ConditionGameTime(boolean night) {
        this._night = night;
    }

    public boolean testImpl(Env env) {
        return (GameTimeTaskManager.getInstance().isNight() == this._night);
    }
}
