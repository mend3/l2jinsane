package mods.achievement.achievements;

import mods.achievement.achievements.base.Condition;
import net.sf.l2j.gameserver.model.actor.Player;

public class Noble extends Condition {
    public Noble(Object value) {
        super(value);
        setName("Noble");
    }

    public String getStatus(Player player) {
        return "null";
    }

    public boolean meetConditionRequirements(Player player) {
        if (getValue() == null)
            return false;
        return player.isNoble();
    }
}
