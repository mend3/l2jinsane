package mods.achievement.achievements;

import mods.achievement.achievements.base.Condition;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.data.manager.RaidPointManager;
import net.sf.l2j.gameserver.model.actor.Player;

import java.util.Map;

public class RaidKill extends Condition {
    public RaidKill(StatSet value) {
        super(value);
    }

    @Override
    public String getValue() {
        return _set.getString("minRaidPoints", null);
    }

    public String getStatus(Player player) {
        return "null";
    }

    public boolean meetConditionRequirements(Player player) {
        if (getValue() == null)
            return false;
        int val = Integer.parseInt(getValue());
        Map<Integer, Integer> list = RaidPointManager.getInstance().getList(player);
        for (int bid : list.keySet()) {
            if (bid == val)
                if (RaidPointManager.getInstance().getList(player).get(bid) > 0)
                    return true;
        }
        return false;
    }
}
