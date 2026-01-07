package mods.achievement.achievements;

import mods.achievement.achievements.base.Condition;
import net.sf.l2j.gameserver.model.actor.Player;

public class Pvp extends Condition {
    int val = Integer.parseInt(getValue().toString());

    public Pvp(Object value) {
        super(value);
        setName("PvP Count");
    }

    public String getStatus(Player player) {
        return "" + player.getPvpKills();
    }

    public boolean meetConditionRequirements(Player player) {
        if (getValue() == null)
            return false;
        return player.getPvpKills() >= this.val;
    }
}
