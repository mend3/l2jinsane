package net.sf.l2j.gameserver.model.itemcontainer;

import enginemods.main.EngineModsManager;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.data.manager.HeroManager;
import net.sf.l2j.gameserver.enums.items.ArmorType;
import net.sf.l2j.gameserver.enums.items.EtcItemType;
import net.sf.l2j.gameserver.enums.items.WeaponType;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.model.itemcontainer.listeners.OnEquipListener;
import net.sf.l2j.gameserver.model.itemcontainer.listeners.StatsListener;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public abstract class Inventory extends ItemContainer {
    public static final int PAPERDOLL_UNDER = 0;

    public static final int PAPERDOLL_LEAR = 1;

    public static final int PAPERDOLL_REAR = 2;

    public static final int PAPERDOLL_NECK = 3;

    public static final int PAPERDOLL_LFINGER = 4;

    public static final int PAPERDOLL_RFINGER = 5;

    public static final int PAPERDOLL_HEAD = 6;

    public static final int PAPERDOLL_RHAND = 7;

    public static final int PAPERDOLL_LHAND = 8;

    public static final int PAPERDOLL_GLOVES = 9;

    public static final int PAPERDOLL_CHEST = 10;

    public static final int PAPERDOLL_LEGS = 11;

    public static final int PAPERDOLL_FEET = 12;

    public static final int PAPERDOLL_BACK = 13;

    public static final int PAPERDOLL_FACE = 14;

    public static final int PAPERDOLL_HAIR = 15;

    public static final int PAPERDOLL_HAIRALL = 16;

    public static final int PAPERDOLL_TOTALSLOTS = 17;

    private static final String RESTORE_INVENTORY = "SELECT object_id, item_id, count, enchant_level, loc, loc_data, custom_type1, custom_type2, mana_left, time FROM items WHERE owner_id=? AND (loc=? OR loc=?) ORDER BY loc_data";

    private final ItemInstance[] _paperdoll;

    private final List<OnEquipListener> _paperdollListeners;

    protected int _totalWeight;

    private int _wornMask;

    protected Inventory() {
        this._paperdoll = new ItemInstance[17];
        this._paperdollListeners = new ArrayList<>();
        addPaperdollListener(StatsListener.getInstance());
    }

    public static int getPaperdollIndex(int slot) {
        switch (slot) {
            case 1:
                return 0;
            case 2:
                return 2;
            case 4:
                return 1;
            case 8:
                return 3;
            case 16:
                return 5;
            case 32:
                return 4;
            case 64:
                return 6;
            case 128:
            case 16384:
                return 7;
            case 256:
                return 8;
            case 512:
                return 9;
            case 1024:
            case 32768:
            case 131072:
                return 10;
            case 2048:
                return 11;
            case 4096:
                return 12;
            case 8192:
                return 13;
            case 65536:
            case 524288:
                return 14;
            case 262144:
                return 15;
        }
        return -1;
    }

    protected abstract ItemInstance.ItemLocation getEquipLocation();

    public ChangeRecorder newRecorder() {
        return new ChangeRecorder(this);
    }

    public ItemInstance dropItem(String process, ItemInstance item, Player actor, WorldObject reference) {
        if (item == null)
            return null;
        synchronized (item) {
            if (!this._items.contains(item))
                return null;
            removeItem(item);
            item.setOwnerId(process, 0, actor, reference);
            item.setLocation(ItemInstance.ItemLocation.VOID);
            item.setLastChange(ItemInstance.ItemState.REMOVED);
            item.updateDatabase();
            refreshWeight();
        }
        return item;
    }

    public ItemInstance dropItem(String process, int objectId, int count, Player actor, WorldObject reference) {
        ItemInstance item = getItemByObjectId(objectId);
        if (item == null)
            return null;
        synchronized (item) {
            if (!this._items.contains(item))
                return null;
            if (item.getCount() > count) {
                item.changeCount(process, -count, actor, reference);
                item.setLastChange(ItemInstance.ItemState.MODIFIED);
                item.updateDatabase();
                item = ItemInstance.create(item.getItemId(), count, actor, reference);
                item.updateDatabase();
                refreshWeight();
                return item;
            }
        }
        return dropItem(process, item, actor, reference);
    }

    protected void addItem(ItemInstance item) {
        super.addItem(item);
        if (item.isEquipped())
            equipItem(item);
    }

    protected boolean removeItem(ItemInstance item) {
        for (int i = 0; i < this._paperdoll.length; i++) {
            if (this._paperdoll[i] == item)
                unEquipItemInSlot(i);
        }
        return super.removeItem(item);
    }

    public ItemInstance getPaperdollItem(int slot) {
        return this._paperdoll[slot];
    }

    public List<ItemInstance> getPaperdollItems() {
        List<ItemInstance> itemsList = new ArrayList<>();
        for (ItemInstance item : this._paperdoll) {
            if (item != null)
                itemsList.add(item);
        }
        return itemsList;
    }

    public ItemInstance getPaperdollItemByL2ItemId(int slot) {
        int index = getPaperdollIndex(slot);
        if (index == -1)
            return null;
        return this._paperdoll[index];
    }

    public int getPaperdollItemId(int slot) {
        ItemInstance item = this._paperdoll[slot];
        if (item != null)
            return item.getItemId();
        return 0;
    }

    public int getPaperdollAugmentationId(int slot) {
        ItemInstance item = this._paperdoll[slot];
        if (item != null)
            if (item.getAugmentation() != null)
                return item.getAugmentation().getAugmentationId();
        return 0;
    }

    public int getPaperdollObjectId(int slot) {
        ItemInstance item = this._paperdoll[slot];
        if (item != null)
            return item.getObjectId();
        return 0;
    }

    public synchronized void addPaperdollListener(OnEquipListener listener) {
        assert !this._paperdollListeners.contains(listener);
        this._paperdollListeners.add(listener);
    }

    public synchronized void removePaperdollListener(OnEquipListener listener) {
        this._paperdollListeners.remove(listener);
    }

    public synchronized ItemInstance setPaperdollItem(int slot, ItemInstance item) {
        ItemInstance old = this._paperdoll[slot];
        if (old != item) {
            if (old != null) {
                this._paperdoll[slot] = null;
                old.setLocation(getBaseLocation());
                old.setLastChange(ItemInstance.ItemState.MODIFIED);
                this._wornMask &= old.getItem().getItemMask() ^ 0xFFFFFFFF;
                for (OnEquipListener listener : this._paperdollListeners) {
                    if (listener == null)
                        continue;
                    listener.onUnequip(slot, old, (Playable) getOwner());
                }
                EngineModsManager.onUnequip(getOwner());
                old.updateDatabase();
            }
            if (item != null) {
                this._paperdoll[slot] = item;
                item.setLocation(getEquipLocation(), slot);
                item.setLastChange(ItemInstance.ItemState.MODIFIED);
                Item armor = item.getItem();
                if (armor.getBodyPart() == 1024) {
                    ItemInstance legs = this._paperdoll[11];
                    if (legs != null && legs.getItem().getItemMask() == armor.getItemMask())
                        this._wornMask |= armor.getItemMask();
                } else if (armor.getBodyPart() == 2048) {
                    ItemInstance legs = this._paperdoll[10];
                    if (legs != null && legs.getItem().getItemMask() == armor.getItemMask())
                        this._wornMask |= armor.getItemMask();
                } else {
                    this._wornMask |= armor.getItemMask();
                }
                for (OnEquipListener listener : this._paperdollListeners) {
                    if (listener == null)
                        continue;
                    listener.onEquip(slot, item, (Playable) getOwner());
                }
                EngineModsManager.onEquip(getOwner());
                item.updateDatabase();
            }
        }
        return old;
    }

    public int getWornMask() {
        return this._wornMask;
    }

    public int getSlotFromItem(ItemInstance item) {
        int slot = -1;
        int location = item.getLocationSlot();
        switch (location) {
            case 0:
                slot = 1;
                break;
            case 1:
                slot = 4;
                break;
            case 2:
                slot = 2;
                break;
            case 3:
                slot = 8;
                break;
            case 5:
                slot = 16;
                break;
            case 4:
                slot = 32;
                break;
            case 15:
                slot = 262144;
                break;
            case 14:
                slot = 65536;
                break;
            case 6:
                slot = 64;
                break;
            case 7:
                slot = 128;
                break;
            case 8:
                slot = 256;
                break;
            case 9:
                slot = 512;
                break;
            case 10:
                slot = item.getItem().getBodyPart();
                break;
            case 11:
                slot = 2048;
                break;
            case 13:
                slot = 8192;
                break;
            case 12:
                slot = 4096;
                break;
        }
        return slot;
    }

    public ItemInstance[] unEquipItemInBodySlotAndRecord(ItemInstance item) {
        ChangeRecorder recorder = newRecorder();
        try {
            unEquipItemInBodySlot(getSlotFromItem(item));
        } finally {
            removePaperdollListener(recorder);
        }
        return recorder.getChangedItems();
    }

    public ItemInstance[] unEquipItemInBodySlotAndRecord(int slot) {
        ChangeRecorder recorder = newRecorder();
        try {
            unEquipItemInBodySlot(slot);
        } finally {
            removePaperdollListener(recorder);
        }
        return recorder.getChangedItems();
    }

    public ItemInstance unEquipItemInSlot(int pdollSlot) {
        return setPaperdollItem(pdollSlot, null);
    }

    public ItemInstance[] unEquipItemInSlotAndRecord(int slot) {
        ChangeRecorder recorder = newRecorder();
        try {
            unEquipItemInSlot(slot);
            if (getOwner() instanceof Player)
                ((Player) getOwner()).refreshExpertisePenalty();
        } finally {
            removePaperdollListener(recorder);
        }
        return recorder.getChangedItems();
    }

    public ItemInstance unEquipItemInBodySlot(int slot) {
        int pdollSlot = -1;
        switch (slot) {
            case 4:
                pdollSlot = 1;
                break;
            case 2:
                pdollSlot = 2;
                break;
            case 8:
                pdollSlot = 3;
                break;
            case 16:
                pdollSlot = 5;
                break;
            case 32:
                pdollSlot = 4;
                break;
            case 262144:
                pdollSlot = 15;
                break;
            case 65536:
                pdollSlot = 14;
                break;
            case 524288:
                setPaperdollItem(14, null);
                pdollSlot = 14;
                break;
            case 64:
                pdollSlot = 6;
                break;
            case 128:
            case 16384:
                pdollSlot = 7;
                break;
            case 256:
                pdollSlot = 8;
                break;
            case 512:
                pdollSlot = 9;
                break;
            case 1024:
            case 32768:
            case 131072:
                pdollSlot = 10;
                break;
            case 2048:
                pdollSlot = 11;
                break;
            case 8192:
                pdollSlot = 13;
                break;
            case 4096:
                pdollSlot = 12;
                break;
            case 1:
                pdollSlot = 0;
                break;
            default:
                LOGGER.warn("Slot type {} is unhandled.", Integer.valueOf(slot));
                break;
        }
        if (pdollSlot >= 0) {
            ItemInstance old = setPaperdollItem(pdollSlot, null);
            if (old != null)
                if (getOwner() instanceof Player)
                    ((Player) getOwner()).refreshExpertisePenalty();
            return old;
        }
        return null;
    }

    public ItemInstance[] equipItemAndRecord(ItemInstance item) {
        ChangeRecorder recorder = newRecorder();
        try {
            equipItem(item);
        } finally {
            removePaperdollListener(recorder);
        }
        return recorder.getChangedItems();
    }

    public void equipItem(ItemInstance item) {
        ItemInstance rh, chest, hair, face;
        if (getOwner() instanceof Player)
            if (((Player) getOwner()).isInStoreMode() || (item.isHeroItem() && !HeroManager.getInstance().isActiveHero(getOwnerId())))
                return;
        int targetSlot = item.getItem().getBodyPart();
        ItemInstance formal = getPaperdollItem(10);
        if (formal != null && formal.getItem().getBodyPart() == 131072)
            switch (targetSlot) {
                case 128:
                case 256:
                case 16384:
                    unEquipItemInBodySlotAndRecord(131072);
                    break;
                case 64:
                case 512:
                case 2048:
                case 4096:
                    return;
            }
        switch (targetSlot) {
            case 16384:
                setPaperdollItem(8, null);
                setPaperdollItem(7, item);
                return;
            case 256:
                rh = getPaperdollItem(7);
                if (rh != null && rh.getItem().getBodyPart() == 16384 && (rh.getItemType() != WeaponType.BOW || item.getItemType() != EtcItemType.ARROW) && (rh.getItemType() != WeaponType.FISHINGROD || item.getItemType() != EtcItemType.LURE))
                    setPaperdollItem(7, null);
                setPaperdollItem(8, item);
                return;
            case 128:
                setPaperdollItem(7, item);
                return;
            case 2:
            case 4:
            case 6:
                if (this._paperdoll[1] == null) {
                    setPaperdollItem(1, item);
                } else if (this._paperdoll[2] == null) {
                    setPaperdollItem(2, item);
                } else if (this._paperdoll[2].getItemId() == item.getItemId()) {
                    setPaperdollItem(1, item);
                } else if (this._paperdoll[1].getItemId() == item.getItemId()) {
                    setPaperdollItem(2, item);
                } else {
                    setPaperdollItem(1, item);
                }
                return;
            case 16:
            case 32:
            case 48:
                if (this._paperdoll[4] == null) {
                    setPaperdollItem(4, item);
                } else if (this._paperdoll[5] == null) {
                    setPaperdollItem(5, item);
                } else if (this._paperdoll[5].getItemId() == item.getItemId()) {
                    setPaperdollItem(4, item);
                } else if (this._paperdoll[4].getItemId() == item.getItemId()) {
                    setPaperdollItem(5, item);
                } else {
                    setPaperdollItem(4, item);
                }
                return;
            case 8:
                setPaperdollItem(3, item);
                return;
            case 32768:
                setPaperdollItem(11, null);
                setPaperdollItem(10, item);
                return;
            case 1024:
                setPaperdollItem(10, item);
                return;
            case 2048:
                chest = getPaperdollItem(10);
                if (chest != null && chest.getItem().getBodyPart() == 32768)
                    setPaperdollItem(10, null);
                setPaperdollItem(11, item);
                return;
            case 4096:
                setPaperdollItem(12, item);
                return;
            case 512:
                setPaperdollItem(9, item);
                return;
            case 64:
                setPaperdollItem(6, item);
                return;
            case 65536:
                hair = getPaperdollItem(15);
                if (hair != null && hair.getItem().getBodyPart() == 524288)
                    setPaperdollItem(15, null);
                setPaperdollItem(14, item);
                return;
            case 262144:
                face = getPaperdollItem(14);
                if (face != null && face.getItem().getBodyPart() == 524288)
                    setPaperdollItem(14, null);
                setPaperdollItem(15, item);
                return;
            case 524288:
                setPaperdollItem(14, null);
                setPaperdollItem(15, item);
                return;
            case 1:
                setPaperdollItem(0, item);
                return;
            case 8192:
                setPaperdollItem(13, item);
                return;
            case 131072:
                setPaperdollItem(11, null);
                setPaperdollItem(8, null);
                setPaperdollItem(7, null);
                setPaperdollItem(6, null);
                setPaperdollItem(12, null);
                setPaperdollItem(9, null);
                setPaperdollItem(10, item);
                return;
        }
        LOGGER.warn("Unknown body slot {} for itemId {}.", Integer.valueOf(targetSlot), Integer.valueOf(item.getItemId()));
    }

    public void equipPetItem(ItemInstance item) {
        if (getOwner() instanceof Player)
            if (((Player) getOwner()).isInStoreMode())
                return;
        if (item.isPetItem())
            if (item.getItemType() == WeaponType.PET) {
                setPaperdollItem(7, item);
            } else if (item.getItemType() == ArmorType.PET) {
                setPaperdollItem(10, item);
            }
    }

    protected void refreshWeight() {
        int weight = 0;
        for (ItemInstance item : this._items) {
            if (item != null && item.getItem() != null)
                weight += item.getItem().getWeight() * item.getCount();
        }
        this._totalWeight = weight;
    }

    public int getTotalWeight() {
        return this._totalWeight;
    }

    public ItemInstance findArrowForBow(Item bow) {
        if (bow == null)
            return null;
        int arrowsId = 0;
        switch (bow.getCrystalType()) {
            default:
                arrowsId = 17;
                return getItemByItemId(arrowsId);
            case D:
                arrowsId = 1341;
                return getItemByItemId(arrowsId);
            case C:
                arrowsId = 1342;
                return getItemByItemId(arrowsId);
            case B:
                arrowsId = 1343;
                return getItemByItemId(arrowsId);
            case A:
                arrowsId = 1344;
                return getItemByItemId(arrowsId);
            case S:
                break;
        }
        arrowsId = 1345;
        return getItemByItemId(arrowsId);
    }

    public void restore() {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("SELECT object_id, item_id, count, enchant_level, loc, loc_data, custom_type1, custom_type2, mana_left, time FROM items WHERE owner_id=? AND (loc=? OR loc=?) ORDER BY loc_data");
                try {
                    ps.setInt(1, getOwnerId());
                    ps.setString(2, getBaseLocation().name());
                    ps.setString(3, getEquipLocation().name());
                    ResultSet rs = ps.executeQuery();
                    try {
                        while (rs.next()) {
                            ItemInstance item = ItemInstance.restoreFromDb(getOwnerId(), rs);
                            if (item == null)
                                continue;
                            if (getOwner() instanceof Player && item.isHeroItem() && !HeroManager.getInstance().isActiveHero(getOwnerId()))
                                item.setLocation(ItemInstance.ItemLocation.INVENTORY);
                            World.getInstance().addObject(item);
                            if (item.isStackable() && getItemByItemId(item.getItemId()) != null) {
                                addItem("Restore", item, getOwner().getActingPlayer(), null);
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
            LOGGER.error("Couldn't restore inventory for {}.", e, Integer.valueOf(getOwnerId()));
        }
        refreshWeight();
    }

    public void reloadEquippedItems() {
        for (ItemInstance element : this._paperdoll) {
            if (element != null) {
                int slot = element.getLocationSlot();
                for (OnEquipListener listener : this._paperdollListeners) {
                    if (listener == null)
                        continue;
                    listener.onUnequip(slot, element, (Playable) getOwner());
                    listener.onEquip(slot, element, (Playable) getOwner());
                }
            }
        }
    }

    private static final class ChangeRecorder implements OnEquipListener {
        private final Inventory _inventory;

        private final List<ItemInstance> _changed;

        ChangeRecorder(Inventory inventory) {
            this._inventory = inventory;
            this._changed = new ArrayList<>();
            this._inventory.addPaperdollListener(this);
        }

        public void onEquip(int slot, ItemInstance item, Playable actor) {
            if (!this._changed.contains(item))
                this._changed.add(item);
        }

        public void onUnequip(int slot, ItemInstance item, Playable actor) {
            if (!this._changed.contains(item))
                this._changed.add(item);
        }

        public ItemInstance[] getChangedItems() {
            return this._changed.toArray(new ItemInstance[this._changed.size()]);
        }
    }
}
