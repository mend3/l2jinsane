package mods.achievement.achievements;

import mods.achievement.achievements.base.Condition;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;

public class WeaponEnchant extends Condition {
    public WeaponEnchant(Object value) {
        super(value);
        setName("Weapon Enchant");
    }

    public String getStatus(Player player) {
        return "null";
    }

    public boolean meetConditionRequirements(Player player) {
        if (getValue() == null)
            return false;
        int val = Integer.parseInt(getValue().toString());
        ItemInstance weapon = player.getInventory().getPaperdollItem(7);
        return weapon != null &&
                weapon.getEnchantLevel() >= val;
    }
}
