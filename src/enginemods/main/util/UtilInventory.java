package enginemods.main.util;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.itemcontainer.PcInventory;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class UtilInventory {
    public static boolean hasItems(Player player, int... itemIds) {
        PcInventory inv = player.getInventory();
        for (int itemId : itemIds) {
            if (inv.getItemByItemId(itemId) == null)
                return false;
        }
        return true;
    }

    public static int getItemsCount(Player player, int itemId) {
        int count = 0;
        for (ItemInstance item : player.getInventory().getItems()) {
            if (item != null && item.getItemId() == itemId)
                count += item.getCount();
        }
        return count;
    }

    public static void giveItems(Player player, int itemId, int itemCount, int enchantLevel) {
        if (itemCount <= 0)
            return;
        ItemInstance item = player.getInventory().addItem("Engine", itemId, itemCount, player, player);
        if (item == null)
            return;
        if (enchantLevel > 0)
            item.setEnchantLevel(enchantLevel);
        if (itemId == 57) {
            SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_ADENA);
            smsg.addItemNumber(itemCount);
            player.sendPacket(smsg);
        } else if (itemCount > 1) {
            SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S);
            smsg.addItemName(itemId);
            smsg.addItemNumber(itemCount);
            player.sendPacket(smsg);
        } else {
            SystemMessage smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1);
            smsg.addItemName(itemId);
            player.sendPacket(smsg);
        }
        StatusUpdate su = new StatusUpdate(player);
        su.addAttribute(14, player.getCurrentLoad());
        player.sendPacket(su);
    }

    public static void takeItems(Player player, int itemId, int itemCount) {
        ItemInstance item = player.getInventory().getItemByItemId(itemId);
        if (item == null)
            return;
        if (itemCount < 0 || itemCount > item.getCount())
            itemCount = item.getCount();
        if (item.isEquipped()) {
            ItemInstance[] unequiped = player.getInventory().unEquipItemInBodySlotAndRecord(item.getItem().getBodyPart());
            InventoryUpdate iu = new InventoryUpdate();
            for (ItemInstance itm : unequiped)
                iu.addModifiedItem(itm);
            player.sendPacket(iu);
            player.broadcastUserInfo();
        }
        player.destroyItemByItemId("Quest", itemId, itemCount, player, true);
    }

    public void giveItems(Player player, int itemId, int itemCount) {
        giveItems(player, itemId, itemCount, 0);
    }
}
