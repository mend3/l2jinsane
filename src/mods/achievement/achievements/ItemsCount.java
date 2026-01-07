package mods.achievement.achievements;

import mods.achievement.achievements.base.Condition;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.model.actor.Player;

public class ItemsCount extends Condition {
    public ItemsCount(StatSet value) {
        super(value);
    }

    @Override
    public String getValue() {
        return _set.getString("itemAmmount", "57,1000");
    }

    public String getStatus(Player player) {
        if (getValue() == null)
            return "null";
        String[] split = getValue().split(",");
        int id = Integer.parseInt(split[0]);
        return (player.getInventory().getInventoryItemCount(id, 0) > 0) ? ("" + player.getInventory().getInventoryItemCount(id, 0)) : "null";
    }

    public boolean meetConditionRequirements(Player player) {
        if (getValue() == null)
            return false;
        String[] split = getValue().split(",");
        int id = Integer.parseInt(split[0]);
        int amount = Integer.parseInt(split[1]);
        return player.getInventory().getInventoryItemCount(id, 0) >= amount;
    }
}
