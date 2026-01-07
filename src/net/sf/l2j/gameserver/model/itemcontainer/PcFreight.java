package net.sf.l2j.gameserver.model.itemcontainer;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class PcFreight extends ItemContainer {
    private final Player _owner;

    private int _activeLocationId;

    private int _tempOwnerId = 0;

    public PcFreight(Player owner) {
        this._owner = owner;
    }

    public String getName() {
        return "Freight";
    }

    public Player getOwner() {
        return this._owner;
    }

    public ItemInstance.ItemLocation getBaseLocation() {
        return ItemInstance.ItemLocation.FREIGHT;
    }

    public void setActiveLocation(int locationId) {
        this._activeLocationId = locationId;
    }

    public int getSize() {
        int size = 0;
        for (ItemInstance item : this._items) {
            if (item.getLocationSlot() == 0 || this._activeLocationId == 0 || item.getLocationSlot() == this._activeLocationId)
                size++;
        }
        return size;
    }

    public Set<ItemInstance> getItems() {
        if (this._items.isEmpty())
            return Collections.emptySet();
        return this._items.stream().filter(i -> (i.getLocationSlot() == 0 || i.getLocationSlot() == this._activeLocationId)).collect(Collectors.toSet());
    }

    public ItemInstance getItemByItemId(int itemId) {
        for (ItemInstance item : this._items) {
            if (item.getItemId() == itemId && (item.getLocationSlot() == 0 || this._activeLocationId == 0 || item.getLocationSlot() == this._activeLocationId))
                return item;
        }
        return null;
    }

    protected void addItem(ItemInstance item) {
        super.addItem(item);
        if (this._activeLocationId > 0)
            item.setLocation(item.getLocation(), this._activeLocationId);
    }

    public void restore() {
        int locationId = this._activeLocationId;
        this._activeLocationId = 0;
        super.restore();
        this._activeLocationId = locationId;
    }

    public boolean validateCapacity(int slots) {
        return (getSize() + slots <= ((this._owner == null) ? Config.FREIGHT_SLOTS : this._owner.getFreightLimit()));
    }

    public int getOwnerId() {
        return (this._owner == null) ? this._tempOwnerId : super.getOwnerId();
    }

    public void doQuickRestore(int val) {
        this._tempOwnerId = val;
        restore();
    }
}
