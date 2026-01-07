package net.sf.l2j.gameserver.model.itemcontainer;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.enums.items.EtcItemType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Pet;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;

public class PetInventory extends Inventory {
    private final Pet _owner;

    public PetInventory(Pet owner) {
        this._owner = owner;
    }

    public Pet getOwner() {
        return this._owner;
    }

    public int getOwnerId() {
        int id;
        try {
            id = this._owner.getOwner().getObjectId();
        } catch (NullPointerException e) {
            return 0;
        }
        return id;
    }

    protected void refreshWeight() {
        super.refreshWeight();
        getOwner().updateAndBroadcastStatus(1);
        getOwner().sendPetInfosToOwner();
    }

    public boolean validateCapacity(ItemInstance item) {
        int slots = 0;
        if ((!item.isStackable() || getItemByItemId(item.getItemId()) == null) && item.getItemType() != EtcItemType.HERB)
            slots++;
        return validateCapacity(slots);
    }

    public boolean validateCapacity(int slots) {
        return (this._items.size() + slots <= this._owner.getInventoryLimit());
    }

    public boolean validateWeight(ItemInstance item, int count) {
        return validateWeight(count * item.getItem().getWeight());
    }

    public boolean validateWeight(int weight) {
        return (this._totalWeight + weight <= this._owner.getMaxLoad());
    }

    protected ItemInstance.ItemLocation getBaseLocation() {
        return ItemInstance.ItemLocation.PET;
    }

    protected ItemInstance.ItemLocation getEquipLocation() {
        return ItemInstance.ItemLocation.PET_EQUIP;
    }

    public void deleteMe() {
        Player petOwner = getOwner().getOwner();
        if (petOwner != null)
            for (ItemInstance item : this._items) {
                if (petOwner.getInventory().validateCapacity(1)) {
                    getOwner().transferItem("return", item.getObjectId(), item.getCount(), petOwner.getInventory(), petOwner, getOwner());
                    continue;
                }
                ItemInstance droppedItem = dropItem("drop", item.getObjectId(), item.getCount(), petOwner, getOwner());
                droppedItem.dropMe(getOwner(), getOwner().getX() + Rnd.get(-70, 70), getOwner().getY() + Rnd.get(-70, 70), getOwner().getZ() + 30);
            }
        this._items.clear();
    }
}
