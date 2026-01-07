package mods.achievement.achievements;

import mods.achievement.achievements.base.Condition;
import net.sf.l2j.gameserver.model.actor.Player;

public class Hero extends Condition {
    public Hero(Object value) {
        super(value);
        setName("Hero");
    }

    public String getStatus(Player player) {
        if (getValue() == null)
            return "null";
        if (player.isHero())
            return "True";
        return "null";
    }

    public boolean meetConditionRequirements(Player player) {
        if (getValue() == null)
            return false;
        return player.isHero();
    }
}
