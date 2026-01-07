package mods.achievement.achievements;

import mods.achievement.achievements.base.Condition;
import net.sf.l2j.commons.util.StatSet;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;

public class WeaponEnchant extends Condition {
    public WeaponEnchant(StatSet value) {
        super(value);
    }

    @Override
    public String getValue() {
        return _set.getString("minWeaponEnchant", null);
    }

    public String getStatus(Player player) {
        return "null";
    }

    public boolean meetConditionRequirements(Player player) {
        if (getValue() == null)
            return false;
        ItemInstance weapon = player.getInventory().getPaperdollItem(7);
        if (weapon == null) {
            player.sendMessage("You must equip your weapon in order to get rewarded.");
            return false;
        }
        int val = Integer.parseInt(getValue());
        return weapon.getEnchantLevel() >= val;
    }
}
