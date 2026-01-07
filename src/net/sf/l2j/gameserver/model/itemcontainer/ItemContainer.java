package net.sf.l2j.gameserver.model.itemcontainer;

import net.sf.l2j.Config;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Item;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public abstract class ItemContainer {
    protected static final CLogger LOGGER = new CLogger(ItemContainer.class.getName());

    private static final String RESTORE_ITEMS = "SELECT object_id, item_id, count, enchant_level, loc, loc_data, custom_type1, custom_type2, mana_left, time FROM items WHERE owner_id=? AND (loc=?)";

    protected final Set<ItemInstance> _items = new ConcurrentSkipListSet<>();

    protected abstract Creature getOwner();

    protected abstract ItemInstance.ItemLocation getBaseLocation();

    public String getName() {
        return "ItemContainer";
    }

    public int getOwnerId() {
        return (getOwner() == null) ? 0 : getOwner().getObjectId();
    }

    public int getSize() {
        return this._items.size();
    }

    public Set<ItemInstance> getItems() {
        return this._items;
    }

    public boolean hasAtLeastOneItem(int... itemIds) {
        for (int itemId : itemIds) {
            if (getItemByItemId(itemId) != null)
                return true;
        }
        return false;
    }

    public List<ItemInstance> getItemsByItemId(int itemId) {
        List<ItemInstance> list = new ArrayList<>();
        for (ItemInstance item : this._items) {
            if (item.getItemId() == itemId)
                list.add(item);
        }
        return list;
    }

    public ItemInstance getItemByItemId(int itemId) {
        for (ItemInstance item : this._items) {
            if (item.getItemId() == itemId)
                return item;
        }
        return null;
    }

    public ItemInstance getItemByObjectId(int objectId) {
        for (ItemInstance item : this._items) {
            if (item.getObjectId() == objectId)
                return item;
        }
        return null;
    }

    public int getInventoryItemCount(int itemId, int enchantLevel) {
        return getInventoryItemCount(itemId, enchantLevel, true);
    }

    public int getInventoryItemCount(int itemId, int enchantLevel, boolean includeEquipped) {
        int count = 0;
        for (ItemInstance item : this._items) {
            if (item.getItemId() == itemId && (item.getEnchantLevel() == enchantLevel || enchantLevel < 0) && (includeEquipped || !item.isEquipped())) {
                if (item.isStackable())
                    return item.getCount();
                count++;
            }
        }
        return count;
    }

    public ItemInstance addItem(String process, ItemInstance item, Player actor, WorldObject reference) {
        ItemInstance olditem = getItemByItemId(item.getItemId());
        if (olditem != null && olditem.isStackable()) {
            int count = item.getCount();
            olditem.changeCount(process, count, actor, reference);
            olditem.setLastChange(ItemInstance.ItemState.MODIFIED);
            item.destroyMe(process, actor, reference);
            item.updateDatabase();
            item = olditem;
            if (item.getItemId() == 57 && count < 10000.0D * Config.RATE_DROP_ADENA) {
                if (Rnd.get(10) < 2)
                    item.updateDatabase();
            } else {
                item.updateDatabase();
            }
        } else {
            item.setOwnerId(process, getOwnerId(), actor, reference);
            item.setLocation(getBaseLocation());
            item.setLastChange(ItemInstance.ItemState.ADDED);
            addItem(item);
            item.updateDatabase();
        }
        refreshWeight();
        return item;
    }

    public ItemInstance addItem(String process, int itemId, int count, Player actor, WorldObject reference) {
        ItemInstance item = getItemByItemId(itemId);
        if (item != null && item.isStackable()) {
            item.changeCount(process, count, actor, reference);
            item.setLastChange(ItemInstance.ItemState.MODIFIED);
            if (itemId == 57 && count < 10000.0D * Config.RATE_DROP_ADENA) {
                if (Rnd.get(10) < 2)
                    item.updateDatabase();
            } else {
                item.updateDatabase();
            }
        } else {
            Item template = ItemTable.getInstance().getTemplate(itemId);
            if (template == null)
                return null;
            for (int i = 0; i < count; i++) {
                item = ItemInstance.create(itemId, template.isStackable() ? count : 1, actor, reference);
                item.setOwnerId(getOwnerId());
                item.setLocation(getBaseLocation());
                item.setLastChange(ItemInstance.ItemState.ADDED);
                addItem(item);
                item.updateDatabase();
                if (template.isStackable() || !Config.MULTIPLE_ITEM_DROP)
                    break;
            }
        }
        refreshWeight();
        return item;
    }

    public ItemInstance transferItem(String process, int objectId, int count, ItemContainer target, Player actor, WorldObject reference) {
        if (target == null)
            return null;
        ItemInstance sourceitem = getItemByObjectId(objectId);
        if (sourceitem == null)
            return null;
        ItemInstance targetitem = sourceitem.isStackable() ? target.getItemByItemId(sourceitem.getItemId()) : null;
        synchronized (sourceitem) {
            if (getItemByObjectId(objectId) != sourceitem)
                return null;
            if (count > sourceitem.getCount())
                count = sourceitem.getCount();
            if (sourceitem.getCount() == count && targetitem == null) {
                removeItem(sourceitem);
                target.addItem(process, sourceitem, actor, reference);
                targetitem = sourceitem;
            } else {
                if (sourceitem.getCount() > count) {
                    sourceitem.changeCount(process, -count, actor, reference);
                } else {
                    removeItem(sourceitem);
                    sourceitem.destroyMe(process, actor, reference);
                }
                if (targetitem != null) {
                    targetitem.changeCount(process, count, actor, reference);
                } else {
                    targetitem = target.addItem(process, sourceitem.getItemId(), count, actor, reference);
                }
            }
            sourceitem.updateDatabase();
            if (targetitem != sourceitem && targetitem != null)
                targetitem.updateDatabase();
            if (sourceitem.isAugmented())
                sourceitem.getAugmentation().removeBonus(actor);
            refreshWeight();
            target.refreshWeight();
        }
        return targetitem;
    }

    public ItemInstance destroyItem(String process, ItemInstance item, Player actor, WorldObject reference) {
        return destroyItem(process, item, item.getCount(), actor, reference);
    }

    public ItemInstance destroyItem(String process, ItemInstance item, int count, Player actor, WorldObject reference) {
        synchronized (item) {
            if (item.getCount() > count) {
                item.changeCount(process, -count, actor, reference);
                item.setLastChange(ItemInstance.ItemState.MODIFIED);
                if (process != null || Rnd.get(10) == 0)
                    item.updateDatabase();
                refreshWeight();
                return item;
            }
            if (item.getCount() < count)
                return null;
            boolean removed = removeItem(item);
            if (!removed)
                return null;
            item.destroyMe(process, actor, reference);
            item.updateDatabase();
            refreshWeight();
        }
        return item;
    }

    public ItemInstance destroyItem(String process, int objectId, int count, Player actor, WorldObject reference) {
        ItemInstance item = getItemByObjectId(objectId);
        if (item == null)
            return null;
        return destroyItem(process, item, count, actor, reference);
    }

    public ItemInstance destroyItemByItemId(String process, int itemId, int count, Player actor, WorldObject reference) {
        ItemInstance item = getItemByItemId(itemId);
        if (item == null)
            return null;
        return destroyItem(process, item, count, actor, reference);
    }

    public void destroyAllItems(String process, Player actor, WorldObject reference) {
        for (ItemInstance item : this._items)
            destroyItem(process, item, actor, reference);
    }

    public int getAdena() {
        for (ItemInstance item : this._items) {
            if (item.getItemId() == 57)
                return item.getCount();
        }
        return 0;
    }

    protected void addItem(ItemInstance item) {
        item.actualizeTime();
        this._items.add(item);
    }

    protected boolean removeItem(ItemInstance item) {
        return this._items.remove(item);
    }

    protected void refreshWeight() {
    }

    public void deleteMe() {
        if (getOwner() != null)
            for (ItemInstance item : this._items) {
                item.updateDatabase();
                World.getInstance().removeObject(item);
            }
        this._items.clear();
    }

    public void updateDatabase() {
        if (getOwner() != null)
            for (ItemInstance item : this._items)
                item.updateDatabase();
    }

    public void restore() {
        Player owner = (getOwner() == null) ? null : getOwner().getActingPlayer();
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("SELECT object_id, item_id, count, enchant_level, loc, loc_data, custom_type1, custom_type2, mana_left, time FROM items WHERE owner_id=? AND (loc=?)");
                try {
                    ps.setInt(1, getOwnerId());
                    ps.setString(2, getBaseLocation().name());
                    ResultSet rs = ps.executeQuery();
                    try {
                        while (rs.next()) {
                            ItemInstance item = ItemInstance.restoreFromDb(getOwnerId(), rs);
                            if (item == null)
                                continue;
                            World.getInstance().addObject(item);
                            if (item.isStackable() && getItemByItemId(item.getItemId()) != null) {
                                addItem("Restore", item, owner, null);
                                continue;
                            }
                            addItem(item);
                        }
                        if (rs != null)
                            rs.close();
                    } catch (Throwable throwable) {
                        if (rs != null)
                            try {
                                rs.close();
                            } catch (Throwable throwable1) {
                                throwable.addSuppressed(throwable1);
                            }
                        throw throwable;
                    }
                    if (ps != null)
                        ps.close();
                } catch (Throwable throwable) {
                    if (ps != null)
                        try {
                            ps.close();
                        } catch (Throwable throwable1) {
                            throwable.addSuppressed(throwable1);
                        }
                    throw throwable;
                }
                if (con != null)
                    con.close();
            } catch (Throwable throwable) {
                if (con != null)
                    try {
                        con.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                throw throwable;
            }
        } catch (Exception e) {
            LOGGER.error("Couldn't restore container for {}.", e, getOwnerId());
        }
        refreshWeight();
    }

    public boolean validateCapacity(int slots) {
        return true;
    }

    public boolean validateWeight(int weight) {
        return true;
    }
}
