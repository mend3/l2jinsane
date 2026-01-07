package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExConfirmCancelItem;

public final class RequestConfirmCancelItem extends L2GameClientPacket {
    private int _objectId;

    protected void readImpl() {
        this._objectId = readD();
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        ItemInstance item = activeChar.getInventory().getItemByObjectId(this._objectId);
        if (item == null)
            return;
        if (item.getOwnerId() != activeChar.getObjectId())
            return;
        if (!item.isAugmented()) {
            activeChar.sendPacket(SystemMessageId.AUGMENTATION_REMOVAL_CAN_ONLY_BE_DONE_ON_AN_AUGMENTED_ITEM);
            return;
        }
        int price = 0;
        switch (item.getItem().getCrystalType()) {
            case C:
                if (item.getCrystalCount() < 1720) {
                    price = 95000;
                    break;
                }
                if (item.getCrystalCount() < 2452) {
                    price = 150000;
                    break;
                }
                price = 210000;
                break;
            case B:
                if (item.getCrystalCount() < 1746) {
                    price = 240000;
                    break;
                }
                price = 270000;
                break;
            case A:
                if (item.getCrystalCount() < 2160) {
                    price = 330000;
                    break;
                }
                if (item.getCrystalCount() < 2824) {
                    price = 390000;
                    break;
                }
                price = 420000;
                break;
            case S:
                price = 480000;
                break;
            default:
                return;
        }
        activeChar.sendPacket(new ExConfirmCancelItem(item, price));
    }
}
