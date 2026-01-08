package net.sf.l2j.gameserver.model.itemcontainer;

import net.sf.l2j.gameserver.data.DollsData;
import net.sf.l2j.gameserver.enums.ShortcutType;
import net.sf.l2j.gameserver.enums.items.EtcItemType;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.itemcontainer.listeners.ArmorSetListener;
import net.sf.l2j.gameserver.model.itemcontainer.listeners.BowRodListener;
import net.sf.l2j.gameserver.model.itemcontainer.listeners.ItemPassiveSkillsListener;
import net.sf.l2j.gameserver.model.tradelist.TradeItem;
import net.sf.l2j.gameserver.model.tradelist.TradeList;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.taskmanager.ShadowItemTaskManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PcInventory extends Inventory {
    public static final int ADENA_ID = 57;

    public static final int ANCIENT_ADENA_ID = 5575;

    private final Player _owner;

    private ItemInstance _adena;

    private ItemInstance _ancientAdena;

    public PcInventory(Player owner) {
        this._owner = owner;
        addPaperdollListener(ArmorSetListener.getInstance());
        addPaperdollListener(BowRodListener.getInstance());
        addPaperdollListener(ItemPassiveSkillsListener.getInstance());
        addPaperdollListener(ShadowItemTaskManager.getInstance());
    }

    public Player getOwner() {
        return this._owner;
    }

    protected ItemInstance.ItemLocation getBaseLocation() {
        return ItemInstance.ItemLocation.INVENTORY;
    }

    protected ItemInstance.ItemLocation getEquipLocation() {
        return ItemInstance.ItemLocation.PAPERDOLL;
    }

    public ItemInstance getAdenaInstance() {
        return this._adena;
    }

    public int getAdena() {
        return (this._adena != null) ? this._adena.getCount() : 0;
    }

    public ItemInstance getAncientAdenaInstance() {
        return this._ancientAdena;
    }

    public int getAncientAdena() {
        return (this._ancientAdena != null) ? this._ancientAdena.getCount() : 0;
    }

    public ItemInstance[] getUniqueItems(boolean allowAdena, boolean allowAncientAdena) {
        return getUniqueItems(allowAdena, allowAncientAdena, true);
    }

    public ItemInstance[] getUniqueItems(boolean allowAdena, boolean allowAncientAdena, boolean onlyAvailable) {
        List<ItemInstance> list = new ArrayList<>();
        for (ItemInstance item : this._items) {
            if (item == null)
                continue;
            if (!allowAdena && item.getItemId() == 57)
                continue;
            if (!allowAncientAdena && item.getItemId() == 5575)
                continue;
            boolean isDuplicate = false;
            for (ItemInstance litem : list) {
                if (litem.getItemId() == item.getItemId()) {
                    isDuplicate = true;
                    break;
                }
            }
            if (!isDuplicate && (!onlyAvailable || (item.isSellable() && item.isAvailable(getOwner(), false, false))))
                list.add(item);
        }
        return list.toArray(new ItemInstance[0]);
    }

    public ItemInstance[] getUniqueItemsByEnchantLevel(boolean allowAdena, boolean allowAncientAdena) {
        return getUniqueItemsByEnchantLevel(allowAdena, allowAncientAdena, true);
    }

    public ItemInstance[] getUniqueItemsByEnchantLevel(boolean allowAdena, boolean allowAncientAdena, boolean onlyAvailable) {
        List<ItemInstance> list = new ArrayList<>();
        for (ItemInstance item : this._items) {
            if (item == null)
                continue;
            if (!allowAdena && item.getItemId() == 57)
                continue;
            if (!allowAncientAdena && item.getItemId() == 5575)
                continue;
            boolean isDuplicate = false;
            for (ItemInstance litem : list) {
                if (litem.getItemId() == item.getItemId() && litem.getEnchantLevel() == item.getEnchantLevel()) {
                    isDuplicate = true;
                    break;
                }
            }
            if (!isDuplicate && (!onlyAvailable || (item.isSellable() && item.isAvailable(getOwner(), false, false))))
                list.add(item);
        }
        return list.toArray(new ItemInstance[0]);
    }

    public ItemInstance[] getAllItemsByItemId(int itemId) {
        return getAllItemsByItemId(itemId, true);
    }

    public ItemInstance[] getAllItemsByItemId(int itemId, boolean includeEquipped) {
        List<ItemInstance> list = new ArrayList<>();
        for (ItemInstance item : this._items) {
            if (item == null)
                continue;
            if (item.getItemId() == itemId && (includeEquipped || !item.isEquipped()))
                list.add(item);
        }
        return list.toArray(new ItemInstance[0]);
    }

    public ItemInstance[] getAllItemsByItemId(int itemId, int enchantment) {
        return getAllItemsByItemId(itemId, enchantment, true);
    }

    public ItemInstance[] getAllItemsByItemId(int itemId, int enchantment, boolean includeEquipped) {
        List<ItemInstance> list = new ArrayList<>();
        for (ItemInstance item : this._items) {
            if (item == null)
                continue;
            if (item.getItemId() == itemId && item.getEnchantLevel() == enchantment && (includeEquipped || !item.isEquipped()))
                list.add(item);
        }
        return list.toArray(new ItemInstance[0]);
    }

    public ItemInstance[] getAvailableItems(boolean allowAdena, boolean allowNonTradeable) {
        List<ItemInstance> list = new ArrayList<>();
        for (ItemInstance item : this._items) {
            if (item != null && item.isAvailable(getOwner(), allowAdena, allowNonTradeable))
                list.add(item);
        }
        return list.toArray(new ItemInstance[0]);
    }

    public List<ItemInstance> getSellableItems() {
        return this._items.stream().filter(i -> (!i.isEquipped() && i.isSellable() && (getOwner().getSummon() == null || i.getObjectId() != getOwner().getSummon().getControlItemId()))).collect(Collectors.toList());
    }

    public ItemInstance[] getAugmentedItems() {
        List<ItemInstance> list = new ArrayList<>();
        for (ItemInstance item : this._items) {
            if (item != null && item.isAugmented())
                list.add(item);
        }
        return list.toArray(new ItemInstance[0]);
    }

    public TradeItem[] getAvailableItems(TradeList tradeList) {
        List<TradeItem> list = new ArrayList<>();
        for (ItemInstance item : this._items) {
            if (item != null && item.isAvailable(getOwner(), false, false)) {
                TradeItem adjItem = tradeList.adjustAvailableItem(item);
                if (adjItem != null)
                    list.add(adjItem);
            }
        }
        return list.toArray(new TradeItem[0]);
    }

    public void adjustAvailableItem(TradeItem item) {
        int i = 0;
        boolean notAllEquipped = false;
        for (ItemInstance adjItem : getItemsByItemId(item.getItem().getItemId())) {
            if (adjItem.isEquipable()) {
                if (!adjItem.isEquipped())
                    i += 1;
                continue;
            }
            i |= 0x1;
        }
        if (i != 0) {
            ItemInstance adjItem = getItemByItemId(item.getItem().getItemId());
            item.setObjectId(adjItem.getObjectId());
            item.setEnchant(adjItem.getEnchantLevel());
            if (adjItem.getCount() < item.getCount())
                item.setCount(adjItem.getCount());
            return;
        }
        item.setCount(0);
    }

    public void addAdena(String process, int count, Player actor, WorldObject reference) {
        if (count > 0)
            addItem(process, 57, count, actor, reference);
    }

    public boolean reduceAdena(String process, int count, Player actor, WorldObject reference) {
        if (count > 0)
            return (destroyItemByItemId(process, 57, count, actor, reference) != null);
        return false;
    }

    public void addAncientAdena(String process, int count, Player actor, WorldObject reference) {
        if (count > 0)
            addItem(process, 5575, count, actor, reference);
    }

    public boolean reduceAncientAdena(String process, int count, Player actor, WorldObject reference) {
        if (count > 0)
            return (destroyItemByItemId(process, 5575, count, actor, reference) != null);
        return false;
    }

    public ItemInstance addItem(String process, ItemInstance item, Player actor, WorldObject reference) {
        item = super.addItem(process, item, actor, reference);
        if (item == null)
            return null;
        if (item.getItemId() == 57 && !item.equals(this._adena)) {
            this._adena = item;
        } else if (item.getItemId() == 5575 && !item.equals(this._ancientAdena)) {
            this._ancientAdena = item;
        }
        DollsData.getSkillDoll(actor, item);
        return item;
    }

    public ItemInstance addItem(String process, int itemId, int count, Player actor, WorldObject reference) {
        ItemInstance item = super.addItem(process, itemId, count, actor, reference);
        if (item == null)
            return null;
        if (item.getItemId() == 57 && !item.equals(this._adena)) {
            this._adena = item;
        } else if (item.getItemId() == 5575 && !item.equals(this._ancientAdena)) {
            this._ancientAdena = item;
        }
        if (actor != null) {
            InventoryUpdate playerIU = new InventoryUpdate();
            playerIU.addItem(item);
            actor.sendPacket(playerIU);
            StatusUpdate su = new StatusUpdate(actor);
            su.addAttribute(14, actor.getCurrentLoad());
            actor.sendPacket(su);
            DollsData.getSkillDoll(actor, item);
        }
        return item;
    }

    public ItemInstance transferItem(String process, int objectId, int count, ItemContainer target, Player actor, WorldObject reference) {
        ItemInstance item = super.transferItem(process, objectId, count, target, actor, reference);
        if (this._adena != null && (this._adena.getCount() <= 0 || this._adena.getOwnerId() != getOwnerId()))
            this._adena = null;
        if (this._ancientAdena != null && (this._ancientAdena.getCount() <= 0 || this._ancientAdena.getOwnerId() != getOwnerId()))
            this._ancientAdena = null;
        DollsData.getSkillDoll(actor, item);
        return item;
    }

    public ItemInstance destroyItem(String process, ItemInstance item, Player actor, WorldObject reference) {
        return destroyItem(process, item, item.getCount(), actor, reference);
    }

    public ItemInstance destroyItem(String process, ItemInstance item, int count, Player actor, WorldObject reference) {
        item = super.destroyItem(process, item, count, actor, reference);
        if (this._adena != null && this._adena.getCount() <= 0)
            this._adena = null;
        if (this._ancientAdena != null && this._ancientAdena.getCount() <= 0)
            this._ancientAdena = null;
        DollsData.getSkillDoll(actor, item);
        return item;
    }

    public ItemInstance destroyItem(String process, int objectId, int count, Player actor, WorldObject reference) {
        ItemInstance item = getItemByObjectId(objectId);
        if (item == null)
            return null;
        DollsData.getSkillDoll(actor, item);
        return destroyItem(process, item, count, actor, reference);
    }

    public ItemInstance destroyItemByItemId(String process, int itemId, int count, Player actor, WorldObject reference) {
        ItemInstance item = getItemByItemId(itemId);
        if (item == null)
            return null;
        return destroyItem(process, item, count, actor, reference);
    }

    public ItemInstance dropItem(String process, ItemInstance item, Player actor, WorldObject reference) {
        item = super.dropItem(process, item, actor, reference);
        if (this._adena != null && (this._adena.getCount() <= 0 || this._adena.getOwnerId() != getOwnerId()))
            this._adena = null;
        if (this._ancientAdena != null && (this._ancientAdena.getCount() <= 0 || this._ancientAdena.getOwnerId() != getOwnerId()))
            this._ancientAdena = null;
        DollsData.getSkillDoll(actor, item);
        return item;
    }

    public ItemInstance dropItem(String process, int objectId, int count, Player actor, WorldObject reference) {
        ItemInstance item = super.dropItem(process, objectId, count, actor, reference);
        if (this._adena != null && (this._adena.getCount() <= 0 || this._adena.getOwnerId() != getOwnerId()))
            this._adena = null;
        if (this._ancientAdena != null && (this._ancientAdena.getCount() <= 0 || this._ancientAdena.getOwnerId() != getOwnerId()))
            this._ancientAdena = null;
        DollsData.getSkillDoll(actor, item);
        return item;
    }

    protected boolean removeItem(ItemInstance item) {
        getOwner().getShortcutList().deleteShortcuts(item.getObjectId(), ShortcutType.ITEM);
        if (item.equals(getOwner().getActiveEnchantItem()))
            getOwner().setActiveEnchantItem(null);
        if (item.getItemId() == 57) {
            this._adena = null;
        } else if (item.getItemId() == 5575) {
            this._ancientAdena = null;
        }
        return super.removeItem(item);
    }

    public void refreshWeight() {
        super.refreshWeight();
        getOwner().refreshOverloaded();
    }

    public void restore() {
        super.restore();
        this._adena = getItemByItemId(57);
        this._ancientAdena = getItemByItemId(5575);
    }

    public boolean validateCapacity(ItemInstance item) {
        int slots = 0;
        if ((!item.isStackable() || getItemByItemId(item.getItemId()) == null) && item.getItemType() != EtcItemType.HERB)
            slots++;
        return validateCapacity(slots);
    }

    public boolean validateCapacityByItemId(int ItemId) {
        int slots = 0;
        ItemInstance invItem = getItemByItemId(ItemId);
        if (invItem == null || !invItem.isStackable())
            slots++;
        return validateCapacity(slots);
    }

    public boolean validateCapacity(int slots) {
        return (this._items.size() + slots <= this._owner.getInventoryLimit());
    }

    public boolean validateWeight(int weight) {
        return (this._totalWeight + weight <= this._owner.getMaxLoad());
    }

    public String toString() {
        return getClass().getSimpleName() + "[" + getClass().getSimpleName() + "]";
    }
}
