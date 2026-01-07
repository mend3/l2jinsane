package net.sf.l2j.gameserver.skills.conditions;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.skills.Env;

public class ConditionPlayerWeight extends Condition {
    private final int _weight;

    public ConditionPlayerWeight(int weight) {
        this._weight = weight;
    }

    public boolean testImpl(Env env) {
        Player player = env.getPlayer();
        if (player != null && player.getMaxLoad() > 0) {
            int weightproc = player.getCurrentLoad() * 100 / player.getMaxLoad();
            return (weightproc < this._weight);
        }
        return true;
    }
}
