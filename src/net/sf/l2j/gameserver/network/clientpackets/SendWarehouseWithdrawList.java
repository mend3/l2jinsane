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

public final class SendWarehouseWithdrawList extends L2GameClientPacket {
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
        if (player.isCommunityWarehouse()) {
            if (!(warehouse instanceof net.sf.l2j.gameserver.model.itemcontainer.PcWarehouse) && !player.getAccessLevel().allowTransaction()) {
                player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
                return;
            }
            if (!Config.KARMA_PLAYER_CAN_USE_WH && player.getKarma() > 0)
                return;
            if (Config.ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH) {
                if (warehouse instanceof net.sf.l2j.gameserver.model.itemcontainer.ClanWarehouse && (player.getClanPrivileges() & 0x8) != 8)
                    return;
            } else if (warehouse instanceof net.sf.l2j.gameserver.model.itemcontainer.ClanWarehouse && !player.isClanLeader()) {
                player.sendPacket(SystemMessageId.ONLY_CLAN_LEADER_CAN_RETRIEVE_ITEMS_FROM_CLAN_WAREHOUSE);
                return;
            }
            long weight = 0L;
            int slots = 0;
            for (IntIntHolder i : this._items) {
                ItemInstance item = warehouse.getItemByObjectId(i.getId());
                if (item == null || item.getCount() < i.getValue())
                    return;
                weight += (long) i.getValue() * item.getItem().getWeight();
                if (!item.isStackable()) {
                    slots += i.getValue();
                } else if (player.getInventory().getItemByItemId(item.getItemId()) == null) {
                    slots++;
                }
            }
            if (!player.getInventory().validateCapacity(slots)) {
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SLOTS_FULL));
                return;
            }
            if (weight > 2147483647L || weight < 0L || !player.getInventory().validateWeight((int) weight)) {
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
                return;
            }
            InventoryUpdate playerIU = new InventoryUpdate();
            for (IntIntHolder i : this._items) {
                ItemInstance oldItem = warehouse.getItemByObjectId(i.getId());
                if (oldItem == null || oldItem.getCount() < i.getValue()) {
                    LOGGER.warn("Error withdrawing a warehouse object for " + player.getName() + " (olditem == null)");
                    return;
                }
                ItemInstance newItem = warehouse.transferItem(warehouse.getName(), i.getId(), i.getValue(), player.getInventory(), player, player);
                if (newItem == null) {
                    LOGGER.warn("Error withdrawing a warehouse object for " + player.getName() + " (newitem == null)");
                    return;
                }
                if (newItem.getCount() > i.getValue()) {
                    playerIU.addModifiedItem(newItem);
                } else {
                    playerIU.addNewItem(newItem);
                }
            }
            player.sendPacket(playerIU);
        } else {
            Folk folk = player.getCurrentFolk();
            if (folk == null || !folk.isWarehouse() || !folk.canInteract(player))
                return;
            if (!(warehouse instanceof net.sf.l2j.gameserver.model.itemcontainer.PcWarehouse) && !player.getAccessLevel().allowTransaction()) {
                player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
                return;
            }
            if (!Config.KARMA_PLAYER_CAN_USE_WH && player.getKarma() > 0)
                return;
            if (Config.ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH) {
                if (warehouse instanceof net.sf.l2j.gameserver.model.itemcontainer.ClanWarehouse && (player.getClanPrivileges() & 0x8) != 8)
                    return;
            } else if (warehouse instanceof net.sf.l2j.gameserver.model.itemcontainer.ClanWarehouse && !player.isClanLeader()) {
                player.sendPacket(SystemMessageId.ONLY_CLAN_LEADER_CAN_RETRIEVE_ITEMS_FROM_CLAN_WAREHOUSE);
                return;
            }
            int weight = 0;
            int slots = 0;
            for (IntIntHolder i : this._items) {
                ItemInstance item = warehouse.getItemByObjectId(i.getId());
                if (item == null || item.getCount() < i.getValue())
                    return;
                weight += i.getValue() * item.getItem().getWeight();
                if (!item.isStackable()) {
                    slots += i.getValue();
                } else if (player.getInventory().getItemByItemId(item.getItemId()) == null) {
                    slots++;
                }
            }
            if (!player.getInventory().validateCapacity(slots)) {
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.SLOTS_FULL));
                return;
            }
            if (!player.getInventory().validateWeight(weight)) {
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED));
                return;
            }
            InventoryUpdate playerIU = new InventoryUpdate();
            for (IntIntHolder i : this._items) {
                ItemInstance oldItem = warehouse.getItemByObjectId(i.getId());
                if (oldItem == null || oldItem.getCount() < i.getValue())
                    return;
                ItemInstance newItem = warehouse.transferItem(warehouse.getName(), i.getId(), i.getValue(), player.getInventory(), player, folk);
                if (newItem == null)
                    return;
                if (newItem.getCount() > i.getValue()) {
                    playerIU.addModifiedItem(newItem);
                } else {
                    playerIU.addNewItem(newItem);
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
