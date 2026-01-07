package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Folk;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.itemcontainer.ItemContainer;
import net.sf.l2j.gameserver.model.itemcontainer.PcFreight;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.util.ArrayList;
import java.util.List;

public final class RequestPackageSend extends L2GameClientPacket {
    private List<IntIntHolder> _items;

    private int _objectID;

    protected void readImpl() {
        this._objectID = readD();
        int count = readD();
        if (count < 0 || count > Config.MAX_ITEM_IN_PACKET)
            return;
        this._items = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int id = readD();
            int cnt = readD();
            this._items.add(new IntIntHolder(id, cnt));
        }
    }

    protected void runImpl() {
        if (this._items == null || this._items.isEmpty() || !Config.ALLOW_FREIGHT)
            return;
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        if (!player.getAccountChars().containsKey(this._objectID))
            return;
        PcFreight freight = player.getDepositedFreight(this._objectID);
        player.setActiveWarehouse(freight);
        ItemContainer warehouse = player.getActiveWarehouse();
        if (warehouse == null)
            return;
        Folk folk = player.getCurrentFolk();
        if ((folk == null || !player.isInsideRadius(folk, 150, false, false)) && !player.isGM())
            return;
        if (warehouse instanceof PcFreight && !player.getAccessLevel().allowTransaction()) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
            return;
        }
        if (!Config.KARMA_PLAYER_CAN_USE_WH && player.getKarma() > 0)
            return;
        int fee = this._items.size() * Config.ALT_GAME_FREIGHT_PRICE;
        int currentAdena = player.getAdena();
        int slots = 0;
        for (IntIntHolder i : this._items) {
            int count = i.getValue();
            ItemInstance item = player.checkItemManipulation(i.getId(), count);
            if (item == null) {
                i.setId(0);
                i.setValue(0);
                continue;
            }
            if (!item.isTradable() || item.isQuestItem())
                return;
            if (item.getItemId() == 57)
                currentAdena -= count;
            if (!item.isStackable()) {
                slots += count;
                continue;
            }
            if (warehouse.getItemByItemId(item.getItemId()) == null)
                slots++;
        }
        if (!warehouse.validateCapacity(slots)) {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_EXCEEDED_QUANTITY_THAT_CAN_BE_INPUTTED));
            return;
        }
        if (currentAdena < fee || !player.reduceAdena("Warehouse", fee, player.getCurrentFolk(), false)) {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_NOT_ENOUGH_ADENA));
            return;
        }
        InventoryUpdate playerIU = new InventoryUpdate();
        for (IntIntHolder i : this._items) {
            int objectId = i.getId();
            int count = i.getValue();
            if (objectId == 0 && count == 0)
                continue;
            ItemInstance oldItem = player.getInventory().getItemByObjectId(objectId);
            if (oldItem == null || oldItem.isHeroItem())
                continue;
            ItemInstance newItem = player.getInventory().transferItem("Warehouse", objectId, count, warehouse, player, player.getCurrentFolk());
            if (newItem == null)
                continue;
            if (oldItem.getCount() > 0 && oldItem != newItem) {
                playerIU.addModifiedItem(oldItem);
                continue;
            }
            playerIU.addRemovedItem(oldItem);
        }
        player.sendPacket(playerIU);
        StatusUpdate su = new StatusUpdate(player);
        su.addAttribute(14, player.getCurrentLoad());
        player.sendPacket(su);
    }
}
