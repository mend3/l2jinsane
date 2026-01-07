package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExVariationCancelResult;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestRefineCancel extends L2GameClientPacket {
    private int _targetItemObjId;

    protected void readImpl() {
        this._targetItemObjId = readD();
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(this._targetItemObjId);
        if (targetItem == null) {
            activeChar.sendPacket(new ExVariationCancelResult(0));
            return;
        }
        if (targetItem.getOwnerId() != activeChar.getObjectId())
            return;
        if (!targetItem.isAugmented()) {
            activeChar.sendPacket(SystemMessageId.AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM);
            activeChar.sendPacket(new ExVariationCancelResult(0));
            return;
        }
        int price = 0;
        switch (targetItem.getItem().getCrystalType()) {
            case C:
                if (targetItem.getCrystalCount() < 1720) {
                    price = 95000;
                    break;
                }
                if (targetItem.getCrystalCount() < 2452) {
                    price = 150000;
                    break;
                }
                price = 210000;
                break;
            case B:
                if (targetItem.getCrystalCount() < 1746) {
                    price = 240000;
                    break;
                }
                price = 270000;
                break;
            case A:
                if (targetItem.getCrystalCount() < 2160) {
                    price = 330000;
                    break;
                }
                if (targetItem.getCrystalCount() < 2824) {
                    price = 390000;
                    break;
                }
                price = 420000;
                break;
            case S:
                price = 480000;
                break;
            default:
                activeChar.sendPacket(new ExVariationCancelResult(0));
                return;
        }
        if (!activeChar.reduceAdena("RequestRefineCancel", price, null, true)) {
            activeChar.sendPacket(new ExVariationCancelResult(0));
            return;
        }
        if (targetItem.isEquipped())
            activeChar.disarmWeapons();
        targetItem.removeAugmentation();
        activeChar.sendPacket(new ExVariationCancelResult(1));
        InventoryUpdate iu = new InventoryUpdate();
        iu.addModifiedItem(targetItem);
        activeChar.sendPacket(iu);
        SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.AUGMENTATION_HAS_BEEN_SUCCESSFULLY_REMOVED_FROM_YOUR_S1);
        sm.addItemName(targetItem);
        activeChar.sendPacket(sm);
    }
}
