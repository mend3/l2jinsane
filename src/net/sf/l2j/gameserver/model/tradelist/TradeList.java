package net.sf.l2j.gameserver.model.tradelist;

import net.sf.l2j.gameserver.data.DollsData;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.model.ItemRequest;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.model.itemcontainer.PcInventory;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class TradeList {
    private final Player _owner;

    private final List<TradeItem> _items = new CopyOnWriteArrayList<>();

    private Player _partner;

    private String _title;

    private boolean _packaged;

    private boolean _confirmed;

    private boolean _locked;

    public TradeList(Player owner) {
        this._owner = owner;
    }

    public Player getOwner() {
        return this._owner;
    }

    public Player getPartner() {
        return this._partner;
    }

    public void setPartner(Player partner) {
        this._partner = partner;
    }

    public String getTitle() {
        return this._title;
    }

    public void setTitle(String title) {
        this._title = title;
    }

    public boolean isLocked() {
        return this._locked;
    }

    public boolean isConfirmed() {
        return this._confirmed;
    }

    public boolean isPackaged() {
        return this._packaged;
    }

    public void setPackaged(boolean value) {
        this._packaged = value;
    }

    public List<TradeItem> getItems() {
        return this._items;
    }

    public List<TradeItem> getAvailableItems(PcInventory inventory) {
        List<TradeItem> list = new ArrayList<>();
        for (TradeItem item : this._items) {
            item = new TradeItem(item, item.getCount(), item.getPrice());
            inventory.adjustAvailableItem(item);
            list.add(item);
        }
        return list;
    }

    public TradeItem adjustAvailableItem(ItemInstance item) {
        if (item.isStackable())
            for (TradeItem exclItem : this._items) {
                if (exclItem.getItem().getItemId() == item.getItemId()) {
                    if (item.getCount() <= exclItem.getCount())
                        return null;
                    return new TradeItem(item, item.getCount() - exclItem.getCount(), item.getReferencePrice());
                }
            }
        return new TradeItem(item, item.getCount(), item.getReferencePrice());
    }

    public void adjustItemRequest(ItemRequest item) {
        for (TradeItem filtItem : this._items) {
            if (filtItem.getObjectId() == item.getObjectId()) {
                if (filtItem.getCount() < item.getCount())
                    item.setCount(filtItem.getCount());
                return;
            }
        }
        item.setCount(0);
    }

    public TradeItem addItem(int objectId, int count) {
        return addItem(objectId, count, 0);
    }

    public synchronized TradeItem addItem(int objectId, int count, int price) {
        if (isLocked())
            return null;
        WorldObject o = World.getInstance().getObject(objectId);
        if (!(o instanceof ItemInstance item))
            return null;
        if (!item.isTradable() || item.isQuestItem())
            return null;
        if (count <= 0 || count > item.getCount())
            return null;
        if (!item.isStackable() && count > 1)
            return null;
        if (Integer.MAX_VALUE / count < price)
            return null;
        for (TradeItem checkitem : this._items) {
            if (checkitem.getObjectId() == objectId)
                return null;
        }
        TradeItem titem = new TradeItem(item, count, price);
        this._items.add(titem);
        invalidateConfirmation();
        return titem;
    }

    public synchronized TradeItem addItemByItemId(int itemId, int count, int price) {
        if (isLocked())
            return null;
        Item item = ItemTable.getInstance().getTemplate(itemId);
        if (item == null)
            return null;
        if (!item.isTradable() || item.isQuestItem())
            return null;
        if (!item.isStackable() && count > 1)
            return null;
        if (Integer.MAX_VALUE / count < price)
            return null;
        TradeItem titem = new TradeItem(item, count, price);
        this._items.add(titem);
        invalidateConfirmation();
        return titem;
    }

    public synchronized TradeItem removeItem(int objectId, int itemId, int count) {
        if (isLocked())
            return null;
        for (TradeItem titem : this._items) {
            if (titem.getObjectId() == objectId || titem.getItem().getItemId() == itemId) {
                if (this._partner != null) {
                    TradeList partnerList = this._partner.getActiveTradeList();
                    if (partnerList == null)
                        return null;
                    partnerList.invalidateConfirmation();
                }
                if (count != -1 && titem.getCount() > count) {
                    titem.setCount(titem.getCount() - count);
                } else {
                    this._items.remove(titem);
                }
                return titem;
            }
        }
        return null;
    }

    public synchronized void updateItems() {
        for (TradeItem titem : this._items) {
            ItemInstance item = this._owner.getInventory().getItemByObjectId(titem.getObjectId());
            if (item == null || titem.getCount() < 1) {
                removeItem(titem.getObjectId(), -1, -1);
                continue;
            }
            if (item.getCount() < titem.getCount())
                titem.setCount(item.getCount());
        }
    }

    public void lock() {
        this._locked = true;
    }

    public synchronized void clear() {
        this._items.clear();
        this._locked = false;
    }

    public boolean confirm() {
        if (this._confirmed)
            return true;
        if (this._partner != null) {
            TradeList sync1, sync2, partnerList = this._partner.getActiveTradeList();
            if (partnerList == null)
                return false;
            if (getOwner().getObjectId() > partnerList.getOwner().getObjectId()) {
                sync1 = partnerList;
                sync2 = this;
            } else {
                sync1 = this;
                sync2 = partnerList;
            }
            synchronized (sync1) {
                synchronized (sync2) {
                    this._confirmed = true;
                    if (partnerList.isConfirmed()) {
                        partnerList.lock();
                        lock();
                        if (!partnerList.validate())
                            return false;
                        if (!validate())
                            return false;
                        doExchange(partnerList);
                    } else {
                        this._partner.onTradeConfirm(this._owner);
                    }
                }
            }
        } else {
            this._confirmed = true;
        }
        return this._confirmed;
    }

    public void invalidateConfirmation() {
        this._confirmed = false;
    }

    private boolean validate() {
        if (this._owner == null || World.getInstance().getPlayer(this._owner.getObjectId()) == null)
            return false;
        for (TradeItem titem : this._items) {
            ItemInstance item = this._owner.checkItemManipulation(titem.getObjectId(), titem.getCount());
            if (item == null)
                return false;
        }
        return true;
    }

    private boolean transferItems(Player partner, InventoryUpdate ownerIU, InventoryUpdate partnerIU) {
        for (TradeItem titem : this._items) {
            ItemInstance oldItem = this._owner.getInventory().getItemByObjectId(titem.getObjectId());
            if (oldItem == null)
                return false;
            ItemInstance newItem = this._owner.getInventory().transferItem("Trade", titem.getObjectId(), titem.getCount(), partner.getInventory(), this._owner, this._partner);
            if (newItem == null)
                return false;
            if (ownerIU != null)
                if (oldItem.getCount() > 0 && oldItem != newItem) {
                    ownerIU.addModifiedItem(oldItem);
                } else {
                    ownerIU.addRemovedItem(oldItem);
                }
            if (partnerIU != null)
                if (newItem.getCount() > titem.getCount()) {
                    partnerIU.addModifiedItem(newItem);
                } else {
                    partnerIU.addNewItem(newItem);
                }
            DollsData.getSkillDoll(partner, newItem);
        }
        return true;
    }

    public int countItemsSlots(Player partner) {
        int slots = 0;
        for (TradeItem item : this._items) {
            if (item == null)
                continue;
            Item template = ItemTable.getInstance().getTemplate(item.getItem().getItemId());
            if (template == null)
                continue;
            if (!template.isStackable()) {
                slots += item.getCount();
                continue;
            }
            if (partner.getInventory().getItemByItemId(item.getItem().getItemId()) == null)
                slots++;
        }
        return slots;
    }

    public int calcItemsWeight() {
        int weight = 0;
        for (TradeItem item : this._items) {
            if (item == null)
                continue;
            Item template = ItemTable.getInstance().getTemplate(item.getItem().getItemId());
            if (template == null)
                continue;
            weight += item.getCount() * template.getWeight();
        }
        return Math.min(weight, 2147483647);
    }

    private void doExchange(TradeList partnerList) {
        boolean success = false;
        if (!getOwner().getInventory().validateWeight(partnerList.calcItemsWeight()) || !partnerList.getOwner().getInventory().validateWeight(calcItemsWeight())) {
            partnerList.getOwner().sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
            getOwner().sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
        } else if (!getOwner().getInventory().validateCapacity(partnerList.countItemsSlots(getOwner())) || !partnerList.getOwner().getInventory().validateCapacity(countItemsSlots(partnerList.getOwner()))) {
            partnerList.getOwner().sendPacket(SystemMessageId.SLOTS_FULL);
            getOwner().sendPacket(SystemMessageId.SLOTS_FULL);
        } else {
            InventoryUpdate ownerIU = new InventoryUpdate();
            InventoryUpdate partnerIU = new InventoryUpdate();
            partnerList.transferItems(getOwner(), partnerIU, ownerIU);
            transferItems(partnerList.getOwner(), ownerIU, partnerIU);
            this._owner.sendPacket(ownerIU);
            this._partner.sendPacket(partnerIU);
            StatusUpdate playerSU = new StatusUpdate(this._owner);
            playerSU.addAttribute(14, this._owner.getCurrentLoad());
            this._owner.sendPacket(playerSU);
            playerSU = new StatusUpdate(this._partner);
            playerSU.addAttribute(14, this._partner.getCurrentLoad());
            this._partner.sendPacket(playerSU);
            success = true;
        }
        partnerList.getOwner().onTradeFinish(success);
        getOwner().onTradeFinish(success);
    }

    public synchronized boolean privateStoreBuy(Player player, Set<ItemRequest> items) {
        if (this._locked)
            return false;
        if (!validate()) {
            lock();
            return false;
        }
        if (!this._owner.isOnline() || !player.isOnline())
            return false;
        int slots = 0;
        int weight = 0;
        int totalPrice = 0;
        PcInventory ownerInventory = this._owner.getInventory();
        PcInventory playerInventory = player.getInventory();
        for (ItemRequest item : items) {
            boolean found = false;
            for (TradeItem ti : this._items) {
                if (ti.getObjectId() == item.getObjectId()) {
                    if (ti.getPrice() == item.getPrice()) {
                        if (ti.getCount() < item.getCount())
                            item.setCount(ti.getCount());
                        found = true;
                    }
                    break;
                }
            }
            if (!found) {
                if (isPackaged())
                    return false;
                item.setCount(0);
                continue;
            }
            if (Integer.MAX_VALUE / item.getCount() < item.getPrice()) {
                lock();
                return false;
            }
            totalPrice += item.getCount() * item.getPrice();
            if (Integer.MAX_VALUE < totalPrice || totalPrice < 0) {
                lock();
                return false;
            }
            ItemInstance oldItem = this._owner.checkItemManipulation(item.getObjectId(), item.getCount());
            if (oldItem == null || !oldItem.isTradable()) {
                lock();
                return false;
            }
            Item template = ItemTable.getInstance().getTemplate(item.getItemId());
            if (template == null)
                continue;
            weight += item.getCount() * template.getWeight();
            if (!template.isStackable()) {
                slots += item.getCount();
                continue;
            }
            if (playerInventory.getItemByItemId(item.getItemId()) == null)
                slots++;
        }
        if (totalPrice > playerInventory.getAdena()) {
            player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
            return false;
        }
        if (!playerInventory.validateWeight(weight)) {
            player.sendPacket(SystemMessageId.WEIGHT_LIMIT_EXCEEDED);
            return false;
        }
        if (!playerInventory.validateCapacity(slots)) {
            player.sendPacket(SystemMessageId.SLOTS_FULL);
            return false;
        }
        InventoryUpdate ownerIU = new InventoryUpdate();
        InventoryUpdate playerIU = new InventoryUpdate();
        ItemInstance adenaItem = playerInventory.getAdenaInstance();
        if (!playerInventory.reduceAdena("PrivateStore", totalPrice, player, this._owner)) {
            player.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA);
            return false;
        }
        playerIU.addItem(adenaItem);
        ownerInventory.addAdena("PrivateStore", totalPrice, this._owner, player);
        boolean ok = true;
        for (ItemRequest item : items) {
            if (item.getCount() == 0)
                continue;
            ItemInstance oldItem = this._owner.checkItemManipulation(item.getObjectId(), item.getCount());
            if (oldItem == null) {
                lock();
                ok = false;
                break;
            }
            ItemInstance newItem = ownerInventory.transferItem("PrivateStore", item.getObjectId(), item.getCount(), playerInventory, this._owner, player);
            if (newItem == null) {
                ok = false;
                break;
            }
            removeItem(item.getObjectId(), -1, item.getCount());
            if (oldItem.getCount() > 0 && oldItem != newItem) {
                ownerIU.addModifiedItem(oldItem);
            } else {
                ownerIU.addRemovedItem(oldItem);
            }
            if (newItem.getCount() > item.getCount()) {
                playerIU.addModifiedItem(newItem);
            } else {
                playerIU.addNewItem(newItem);
            }
            if (newItem.isStackable()) {
                SystemMessage systemMessage = SystemMessage.getSystemMessage(SystemMessageId.S1_PURCHASED_S3_S2_S);
                systemMessage.addString(player.getName());
                systemMessage.addItemName(newItem.getItemId());
                systemMessage.addNumber(item.getCount());
                this._owner.sendPacket(systemMessage);
                systemMessage = SystemMessage.getSystemMessage(SystemMessageId.PURCHASED_S3_S2_S_FROM_S1);
                systemMessage.addString(this._owner.getName());
                systemMessage.addItemName(newItem.getItemId());
                systemMessage.addNumber(item.getCount());
                player.sendPacket(systemMessage);
                continue;
            }
            SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.S1_PURCHASED_S2);
            msg.addString(player.getName());
            msg.addItemName(newItem.getItemId());
            this._owner.sendPacket(msg);
            msg = SystemMessage.getSystemMessage(SystemMessageId.PURCHASED_S2_FROM_S1);
            msg.addString(this._owner.getName());
            msg.addItemName(newItem.getItemId());
            player.sendPacket(msg);
        }
        this._owner.sendPacket(ownerIU);
        player.sendPacket(playerIU);
        return ok;
    }

    public synchronized boolean privateStoreSell(Player player, ItemRequest[] items) {
        if (this._locked)
            return false;
        if (!this._owner.isOnline() || !player.isOnline())
            return false;
        boolean ok = false;
        PcInventory ownerInventory = this._owner.getInventory();
        PcInventory playerInventory = player.getInventory();
        InventoryUpdate ownerIU = new InventoryUpdate();
        InventoryUpdate playerIU = new InventoryUpdate();
        int totalPrice = 0;
        for (ItemRequest item : items) {
            boolean found = false;
            for (TradeItem ti : this._items) {
                if (ti.getItem().getItemId() == item.getItemId()) {
                    if (ti.getPrice() == item.getPrice()) {
                        if (ti.getCount() < item.getCount())
                            item.setCount(ti.getCount());
                        found = (item.getCount() > 0);
                    }
                    break;
                }
            }
            if (!found)
                continue;
            if (Integer.MAX_VALUE / item.getCount() < item.getPrice()) {
                lock();
                break;
            }
            int _totalPrice = totalPrice + item.getCount() * item.getPrice();
            if (Integer.MAX_VALUE < _totalPrice || _totalPrice < 0) {
                lock();
                break;
            }
            if (ownerInventory.getAdena() < _totalPrice)
                continue;
            int objectId = item.getObjectId();
            ItemInstance oldItem = player.checkItemManipulation(objectId, item.getCount());
            if (oldItem == null) {
                oldItem = playerInventory.getItemByItemId(item.getItemId());
                if (oldItem == null)
                    continue;
                objectId = oldItem.getObjectId();
                oldItem = player.checkItemManipulation(objectId, item.getCount());
                if (oldItem == null)
                    continue;
            }
            if (oldItem.getItemId() != item.getItemId())
                return false;
            if (oldItem.isTradable()) {
                ItemInstance newItem = playerInventory.transferItem("PrivateStore", objectId, item.getCount(), ownerInventory, player, this._owner);
                if (newItem != null) {
                    removeItem(-1, item.getItemId(), item.getCount());
                    ok = true;
                    totalPrice = _totalPrice;
                    if (oldItem.getCount() > 0 && oldItem != newItem) {
                        playerIU.addModifiedItem(oldItem);
                    } else {
                        playerIU.addRemovedItem(oldItem);
                    }
                    if (newItem.getCount() > item.getCount()) {
                        ownerIU.addModifiedItem(newItem);
                    } else {
                        ownerIU.addNewItem(newItem);
                    }
                    if (newItem.isStackable()) {
                        SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.PURCHASED_S3_S2_S_FROM_S1);
                        msg.addString(player.getName());
                        msg.addItemName(newItem.getItemId());
                        msg.addNumber(item.getCount());
                        this._owner.sendPacket(msg);
                        msg = SystemMessage.getSystemMessage(SystemMessageId.S1_PURCHASED_S3_S2_S);
                        msg.addString(this._owner.getName());
                        msg.addItemName(newItem.getItemId());
                        msg.addNumber(item.getCount());
                        player.sendPacket(msg);
                    } else {
                        SystemMessage msg = SystemMessage.getSystemMessage(SystemMessageId.PURCHASED_S2_FROM_S1);
                        msg.addString(player.getName());
                        msg.addItemName(newItem.getItemId());
                        this._owner.sendPacket(msg);
                        msg = SystemMessage.getSystemMessage(SystemMessageId.S1_PURCHASED_S2);
                        msg.addString(this._owner.getName());
                        msg.addItemName(newItem.getItemId());
                        player.sendPacket(msg);
                    }
                }
            }
            continue;
        }
        if (totalPrice > 0) {
            if (totalPrice > ownerInventory.getAdena())
                return false;
            ItemInstance adenaItem = ownerInventory.getAdenaInstance();
            ownerInventory.reduceAdena("PrivateStore", totalPrice, this._owner, player);
            ownerIU.addItem(adenaItem);
            playerInventory.addAdena("PrivateStore", totalPrice, player, this._owner);
            playerIU.addItem(playerInventory.getAdenaInstance());
        }
        if (ok) {
            this._owner.sendPacket(ownerIU);
            player.sendPacket(playerIU);
        }
        return ok;
    }
}
