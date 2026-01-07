package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.skills.Env;

public class ConditionLogicAnd extends Condition {
    private static final Condition[] _emptyConditions = new Condition[0];

    public Condition[] conditions = _emptyConditions;

    public void add(Condition condition) {
        if (condition == null)
            return;
        if (getListener() != null)
            condition.setListener(this);
        int len = this.conditions.length;
        Condition[] tmp = new Condition[len + 1];
        System.arraycopy(this.conditions, 0, tmp, 0, len);
        tmp[len] = condition;
        this.conditions = tmp;
    }

    void setListener(ConditionListener listener) {
        if (listener != null) {
            for (Condition c : this.conditions)
                c.setListener(this);
        } else {
            for (Condition c : this.conditions)
                c.setListener(null);
        }
        super.setListener(listener);
    }

    public boolean testImpl(Env env) {
        for (Condition c : this.conditions) {
            if (!c.test(env))
                return false;
        }
        return true;
    }
}
