package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.enums.items.CrystalType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestCrystallizeItem extends L2GameClientPacket {
    private int _objectId;

    private int _count;

    protected void readImpl() {
        this._objectId = readD();
        this._count = readD();
    }

    protected void runImpl() {
        if (this._count <= 0)
            return;
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        if (player.isInStoreMode() || player.isCrystallizing()) {
            player.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
            return;
        }
        int skillLevel = player.getSkillLevel(248);
        if (skillLevel <= 0) {
            player.sendPacket(SystemMessageId.CRYSTALLIZE_LEVEL_TOO_LOW);
            return;
        }
        ItemInstance item = player.getInventory().getItemByObjectId(this._objectId);
        if (item == null || item.isHeroItem() || item.isShadowItem())
            return;
        if (!item.getItem().isCrystallizable() || item.getItem().getCrystalCount() <= 0 || item.getItem().getCrystalType() == CrystalType.NONE)
            return;
        this._count = Math.min(this._count, item.getCount());
        boolean canCrystallize = true;
        switch (item.getItem().getCrystalType()) {
            case C:
                if (skillLevel == 1)
                    canCrystallize = false;
                break;
            case B:
                if (skillLevel <= 2)
                    canCrystallize = false;
                break;
            case A:
                if (skillLevel <= 3)
                    canCrystallize = false;
                break;
            case S:
                if (skillLevel <= 4)
                    canCrystallize = false;
                break;
        }
        if (!canCrystallize) {
            player.sendPacket(SystemMessageId.CRYSTALLIZE_LEVEL_TOO_LOW);
            player.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        player.setCrystallizing(true);
        if (item.isEquipped()) {
            SystemMessage msg;
            InventoryUpdate inventoryUpdate = new InventoryUpdate();
            for (ItemInstance items : player.getInventory().unEquipItemInSlotAndRecord(item.getLocationSlot()))
                inventoryUpdate.addModifiedItem(items);
            player.sendPacket(inventoryUpdate);
            if (item.getEnchantLevel() > 0) {
                msg = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED).addNumber(item.getEnchantLevel()).addItemName(item.getItemId());
            } else {
                msg = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED).addItemName(item.getItemId());
            }
            player.sendPacket(msg);
        }
        ItemInstance removedItem = player.getInventory().destroyItem("Crystalize", this._objectId, this._count, player, null);
        InventoryUpdate iu = new InventoryUpdate();
        iu.addRemovedItem(removedItem);
        player.sendPacket(iu);
        player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CRYSTALLIZED).addItemName(removedItem.getItemId()));
        ItemInstance crystals = player.getInventory().addItem("Crystalize", item.getItem().getCrystalItemId(), item.getCrystalCount(), player, player);
        player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(crystals.getItemId()).addItemNumber(item.getCrystalCount()));
        player.broadcastUserInfo();
        player.setCrystallizing(false);
    }
}
