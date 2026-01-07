package mods.achievement.achievements;

import mods.achievement.achievements.base.Condition;
import net.sf.l2j.gameserver.model.actor.Player;

public class Pk extends Condition {
    public Pk(Object value) {
        super(value);
        setName("PK Count");
    }

    public String getStatus(Player player) {
        return "" + player.getPkKills();
    }

    public boolean meetConditionRequirements(Player player) {
        if (getValue() == null)
            return false;
        int val = Integer.parseInt(getValue().toString());
        return player.getPkKills() >= val;
    }
}
