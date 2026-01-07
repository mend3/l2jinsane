package mods.achievement.achievements;

import mods.achievement.achievements.base.Condition;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.model.actor.Player;

public class Pvp extends Condition {

    public Pvp(StatSet value) {
        super(value);
    }

    public String getStatus(Player player) {
        return "" + player.getPvpKills();
    }

    @Override
    public String getValue() {
        return _set.getString("minPvPCount", null);
    }

    public boolean meetConditionRequirements(Player player) {
        if (getValue() == null)
            return false;
        return player.getPvpKills() >= Integer.parseInt(getValue());
    }
}
