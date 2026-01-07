package mods.achievement.achievements;

import mods.achievement.achievements.base.Condition;
import net.sf.l2j.gameserver.data.manager.RaidPointManager;
import net.sf.l2j.gameserver.model.actor.Player;

import java.util.Iterator;
import java.util.Map;

public class RaidKill extends Condition {
    public RaidKill(Object value) {
        super(value);
        setName("Raid Kill");
    }

    public String getStatus(Player player) {
        return "null";
    }

    public boolean meetConditionRequirements(Player player) {
        if (getValue() == null)
            return false;
        int val = Integer.parseInt(getValue().toString());
        Map<Integer, Integer> list = RaidPointManager.getInstance().getList(player);
        if (list != null)
            for (Iterator<Integer> iterator = list.keySet().iterator(); iterator.hasNext(); ) {
                int bid = iterator.next();
                if (bid == val)
                    if (RaidPointManager.getInstance().getList(player).get(Integer.valueOf(bid)) > 0)
                        return true;
            }
        return false;
    }
}
