package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExConfirmVariationGemstone;

public final class RequestConfirmGemStone extends AbstractRefinePacket {
    private int _targetItemObjId;

    private int _refinerItemObjId;

    private int _gemstoneItemObjId;

    private int _gemStoneCount;

    protected void readImpl() {
        this._targetItemObjId = readD();
        this._refinerItemObjId = readD();
        this._gemstoneItemObjId = readD();
        this._gemStoneCount = readD();
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(this._targetItemObjId);
        if (targetItem == null)
            return;
        ItemInstance refinerItem = activeChar.getInventory().getItemByObjectId(this._refinerItemObjId);
        if (refinerItem == null)
            return;
        ItemInstance gemStoneItem = activeChar.getInventory().getItemByObjectId(this._gemstoneItemObjId);
        if (gemStoneItem == null)
            return;
        if (!isValid(activeChar, targetItem, refinerItem, gemStoneItem)) {
            activeChar.sendPacket(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM);
            return;
        }
        AbstractRefinePacket.LifeStone ls = getLifeStone(refinerItem.getItemId());
        if (ls == null)
            return;
        if (this._gemStoneCount != getGemStoneCount(targetItem.getItem().getCrystalType())) {
            activeChar.sendPacket(SystemMessageId.GEMSTONE_QUANTITY_IS_INCORRECT);
            return;
        }
        activeChar.sendPacket(new ExConfirmVariationGemstone(this._gemstoneItemObjId, this._gemStoneCount));
    }
}
