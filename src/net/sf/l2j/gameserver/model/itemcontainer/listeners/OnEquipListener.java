package net.sf.l2j.gameserver.model.itemcontainer.listeners;

import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;

public interface OnEquipListener {
    void onEquip(int paramInt, ItemInstance paramItemInstance, Playable paramPlayable);

    void onUnequip(int paramInt, ItemInstance paramItemInstance, Playable paramPlayable);
}
