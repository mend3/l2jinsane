package mods.achievement.achievements;

import mods.achievement.achievements.base.Condition;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.manager.RaidPointManager;
import net.sf.l2j.gameserver.model.actor.Player;

public class RaidPoints extends Condition {
    private final int minLevel = _set.getInteger("minLevel", 0);

    public RaidPoints(StatSet value) {
        super(value);
    }

    @Override
    public String getValue() {
        return _set.getString("minRaidPoints", null);
    }

    public String getStatus(Player player) {
        return "" + RaidPointManager.getInstance().getPointsByOwnerId(player.getObjectId());
    }

    public boolean meetConditionRequirements(Player player) {
        if (getValue() == null)
            return false;
        if (minLevel > 0 && player.getLevel() < minLevel) return false;

        int val = Integer.parseInt(getValue());
        return RaidPointManager.getInstance().getPointsByOwnerId(player.getObjectId()) >= val;
    }
}
