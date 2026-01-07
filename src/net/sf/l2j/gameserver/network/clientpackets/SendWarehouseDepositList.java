package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Folk;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.itemcontainer.ItemContainer;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.EnchantResult;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class SendWarehouseDepositList extends L2GameClientPacket {
    private static final int BATCH_LENGTH = 8;

    private IntIntHolder[] _items = null;

    protected void readImpl() {
        int count = readD();
        if (count <= 0 || count > Config.MAX_ITEM_IN_PACKET || count * 8 != this._buf.remaining())
            return;
        this._items = new IntIntHolder[count];
        for (int i = 0; i < count; i++) {
            int objId = readD();
            int cnt = readD();
            if (objId < 1 || cnt < 0) {
                this._items = null;
                return;
            }
            this._items[i] = new IntIntHolder(objId, cnt);
        }
    }

    protected void runImpl() {
        if (this._items == null)
            return;
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        if (player.isProcessingTransaction()) {
            player.sendPacket(SystemMessageId.ALREADY_TRADING);
            return;
        }
        if (player.getActiveEnchantItem() != null) {
            player.setActiveEnchantItem(null);
            player.sendPacket(EnchantResult.CANCELLED);
            player.sendPacket(SystemMessageId.ENCHANT_SCROLL_CANCELLED);
        }
        ItemContainer warehouse = player.getActiveWarehouse();
        if (warehouse == null)
            return;
        boolean isPrivate = warehouse instanceof net.sf.l2j.gameserver.model.itemcontainer.PcWarehouse;
        if (player.isCommunityWarehouse()) {
            if (!isPrivate && !player.getAccessLevel().allowTransaction()) {
                player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
                return;
            }
            if (!Config.KARMA_PLAYER_CAN_USE_WH && player.getKarma() > 0)
                return;
            int fee = this._items.length * 30;
            int currentAdena = player.getAdena();
            int slots = 0;
            for (IntIntHolder i : this._items) {
                ItemInstance item = player.checkItemManipulation(i.getId(), i.getValue());
                if (item == null) {
                    LOGGER.warn("Error depositing a warehouse object for char " + player.getName() + " (validity check)");
                    return;
                }
                if (item.getItemId() == 57)
                    currentAdena -= i.getValue();
                if (!item.isStackable()) {
                    slots += i.getValue();
                } else if (warehouse.getItemByItemId(item.getItemId()) == null) {
                    slots++;
                }
            }
            if (!warehouse.validateCapacity(slots)) {
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
                return;
            }
            if (currentAdena < fee || !player.reduceAdena(warehouse.getName(), fee, player, false)) {
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
                return;
            }
            if (player.getActiveTradeList() != null)
                return;
            InventoryUpdate playerIU = new InventoryUpdate();
            for (IntIntHolder i : this._items) {
                ItemInstance oldItem = player.checkItemManipulation(i.getId(), i.getValue());
                if (oldItem == null) {
                    LOGGER.warn("Error depositing a warehouse object for char " + player.getName() + " (olditem == null)");
                    return;
                }
                if (oldItem.isDepositable(isPrivate) && oldItem.isAvailable(player, true, isPrivate)) {
                    ItemInstance newItem = player.getInventory().transferItem(warehouse.getName(), i.getId(), i.getValue(), warehouse, player, player);
                    if (newItem == null) {
                        LOGGER.warn("Error depositing a warehouse object for char " + player.getName() + " (newitem == null)");
                    } else if (oldItem.getCount() > 0 && oldItem != newItem) {
                        playerIU.addModifiedItem(oldItem);
                    } else {
                        playerIU.addRemovedItem(oldItem);
                    }
                }
            }
            player.sendPacket(playerIU);
        } else {
            Folk folk = player.getCurrentFolk();
            if (folk == null || !folk.isWarehouse() || !folk.canInteract(player))
                return;
            if (!isPrivate && !player.getAccessLevel().allowTransaction()) {
                player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
                return;
            }
            if (!Config.KARMA_PLAYER_CAN_USE_WH && player.getKarma() > 0)
                return;
            int fee = this._items.length * 30;
            int currentAdena = player.getAdena();
            int slots = 0;
            for (IntIntHolder i : this._items) {
                ItemInstance item = player.checkItemManipulation(i.getId(), i.getValue());
                if (item == null)
                    return;
                if (item.getItemId() == 57)
                    currentAdena -= i.getValue();
                if (!item.isStackable()) {
                    slots += i.getValue();
                } else if (warehouse.getItemByItemId(item.getItemId()) == null) {
                    slots++;
                }
            }
            if (!warehouse.validateCapacity(slots)) {
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
                return;
            }
            if (currentAdena < fee || !player.reduceAdena(warehouse.getName(), fee, folk, false)) {
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
                return;
            }
            if (player.getActiveTradeList() != null)
                return;
            InventoryUpdate playerIU = new InventoryUpdate();
            for (IntIntHolder i : this._items) {
                ItemInstance oldItem = player.checkItemManipulation(i.getId(), i.getValue());
                if (oldItem == null)
                    return;
                if (oldItem.isDepositable(isPrivate) && oldItem.isAvailable(player, true, isPrivate)) {
                    ItemInstance newItem = player.getInventory().transferItem(warehouse.getName(), i.getId(), i.getValue(), warehouse, player, folk);
                    if (newItem != null)
                        if (oldItem.getCount() > 0 && oldItem != newItem) {
                            playerIU.addModifiedItem(oldItem);
                        } else {
                            playerIU.addRemovedItem(oldItem);
                        }
                }
            }
            player.sendPacket(playerIU);
        }
        player.setCommunityWarehouse(false);
        StatusUpdate su = new StatusUpdate(player);
        su.addAttribute(14, player.getCurrentLoad());
        player.sendPacket(su);
    }
}
