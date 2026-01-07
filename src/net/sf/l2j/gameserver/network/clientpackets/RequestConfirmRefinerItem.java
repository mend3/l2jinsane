package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.enums.items.CrystalType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExConfirmVariationRefiner;

public class RequestConfirmRefinerItem extends AbstractRefinePacket {
    private int _targetItemObjId;

    private int _refinerItemObjId;

    protected void readImpl() {
        this._targetItemObjId = readD();
        this._refinerItemObjId = readD();
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
        if (!isValid(activeChar, targetItem, refinerItem)) {
            activeChar.sendPacket(SystemMessageId.THIS_IS_NOT_A_SUITABLE_ITEM);
            return;
        }
        int refinerItemId = refinerItem.getItem().getItemId();
        CrystalType grade = targetItem.getItem().getCrystalType();
        int gemStoneId = getGemStoneId(grade);
        int gemStoneCount = getGemStoneCount(grade);
        activeChar.sendPacket(new ExConfirmVariationRefiner(this._refinerItemObjId, refinerItemId, gemStoneId, gemStoneCount));
    }
}
