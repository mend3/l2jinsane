package mods.achievement.achievements;

import mods.achievement.achievements.base.Condition;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.model.actor.Player;

public class Pk extends Condition {
    public Pk(StatSet value) {
        super(value);
    }

    public String getStatus(Player player) {
        return "" + player.getPkKills();
    }

    @Override
    public String getValue() {
        return _set.getString("minPkCount", null);
    }

    public boolean meetConditionRequirements(Player player) {
        if (getValue() == null)
            return false;
        int val = Integer.parseInt(getValue());
        return player.getPkKills() >= val;
    }
}
