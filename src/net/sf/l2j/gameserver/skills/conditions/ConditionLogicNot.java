package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.skills.Env;

public class ConditionLogicNot extends Condition {
    private final Condition _condition;

    public ConditionLogicNot(Condition condition) {
        this._condition = condition;
        if (getListener() != null)
            this._condition.setListener(this);
    }

    void setListener(ConditionListener listener) {
        if (listener != null) {
            this._condition.setListener(this);
        } else {
            this._condition.setListener(null);
        }
        super.setListener(listener);
    }

    public boolean testImpl(Env env) {
        return !this._condition.test(env);
    }
}
