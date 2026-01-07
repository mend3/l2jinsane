package mods.achievement.achievements;

import mods.achievement.achievements.base.Condition;
import net.sf.l2j.gameserver.model.actor.Player;

public class Marry extends Condition {
    public Marry(Object value) {
        super(value);
        setName("Married");
    }

    public String getStatus(Player player) {
        return "null";
    }

    public boolean meetConditionRequirements(Player player) {
        if (getValue() == null)
            return false;
        return false;
    }
}
