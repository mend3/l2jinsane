package mods.achievement.achievements;

import mods.achievement.achievements.base.Condition;
import net.sf.l2j.gameserver.data.manager.RaidPointManager;
import net.sf.l2j.gameserver.model.actor.Player;

public class RaidPoints extends Condition {
    public RaidPoints(Object value) {
        super(value);
        setName("Raid Points");
    }

    public String getStatus(Player player) {
        return "" + RaidPointManager.getInstance().getPointsByOwnerId(player.getObjectId());
    }

    public boolean meetConditionRequirements(Player player) {
        if (getValue() == null)
            return false;
        int val = Integer.parseInt(getValue().toString());
        return RaidPointManager.getInstance().getPointsByOwnerId(player.getObjectId()) >= val;
    }
}
