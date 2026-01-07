package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExConfirmVariationItem;

public final class RequestConfirmTargetItem extends AbstractRefinePacket {
    private int _itemObjId;

    protected void readImpl() {
        this._itemObjId = readD();
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        ItemInstance item = activeChar.getInventory().getItemByObjectId(this._itemObjId);
        if (item == null)
            return;
        if (!isValid(activeChar, item)) {
            if (item.isAugmented()) {
                activeChar.sendPacket(SystemMessageId.ONCE_AN_ITEM_IS_AUGMENTED_IT_CANNOT_BE_AUGMENTED_AGAIN);
                return;
            }
            activeChar.sendPacket(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM);
            return;
        }
        activeChar.sendPacket(new ExConfirmVariationItem(this._itemObjId));
    }
}
