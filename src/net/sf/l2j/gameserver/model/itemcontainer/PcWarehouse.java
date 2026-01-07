package net.sf.l2j.gameserver.model.itemcontainer;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;

public class PcWarehouse extends ItemContainer {
    private final Player _owner;

    public PcWarehouse(Player owner) {
        this._owner = owner;
    }

    public String getName() {
        return "Warehouse";
    }

    public Player getOwner() {
        return this._owner;
    }

    public ItemInstance.ItemLocation getBaseLocation() {
        return ItemInstance.ItemLocation.WAREHOUSE;
    }

    public boolean validateCapacity(int slots) {
        return (this._items.size() + slots <= this._owner.getWareHouseLimit());
    }
}
