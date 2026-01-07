package net.sf.l2j.gameserver.model.itemcontainer.listeners;

import net.sf.l2j.gameserver.enums.items.WeaponType;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;

public class BowRodListener implements OnEquipListener {
    private static final BowRodListener instance = new BowRodListener();

    public static BowRodListener getInstance() {
        return instance;
    }

    public void onEquip(int slot, ItemInstance item, Playable actor) {
        if (slot != 7)
            return;
        if (item.getItemType() == WeaponType.BOW) {
            ItemInstance arrow = actor.getInventory().findArrowForBow(item.getItem());
            if (arrow != null)
                actor.getInventory().setPaperdollItem(8, arrow);
        }
    }

    public void onUnequip(int slot, ItemInstance item, Playable actor) {
        if (slot != 7)
            return;
        if (item.getItemType() == WeaponType.BOW) {
            ItemInstance arrow = actor.getInventory().getPaperdollItem(8);
            if (arrow != null)
                actor.getInventory().setPaperdollItem(8, null);
        } else if (item.getItemType() == WeaponType.FISHINGROD) {
            ItemInstance lure = actor.getInventory().getPaperdollItem(8);
            if (lure != null)
                actor.getInventory().setPaperdollItem(8, null);
        }
    }
}
