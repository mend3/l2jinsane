package mods.achievement.achievements;

import mods.achievement.achievements.base.Condition;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.model.actor.Player;

public class CompleteAchievements extends Condition {
    public CompleteAchievements(StatSet value) {
        super(value);
    }

    @Override
    public Integer getValue() {
        return _set.getInteger("total", 10);
    }

    public String getStatus(Player player) {
        return "null";
    }

    public boolean meetConditionRequirements(Player player) {
        if (getValue() == null)
            return false;
        int val = Integer.parseInt(getValue().toString());
        return player.getCompletedAchievements().size() >= val;
    }
}
