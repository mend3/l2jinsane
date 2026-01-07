package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.data.xml.AugmentationData;
import net.sf.l2j.gameserver.model.L2Augmentation;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExVariationResult;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.StatusUpdate;

public final class RequestRefine extends AbstractRefinePacket {
    private int _targetItemObjId;

    private int _refinerItemObjId;

    private int _gemStoneItemObjId;

    private int _gemStoneCount;

    protected void readImpl() {
        this._targetItemObjId = readD();
        this._refinerItemObjId = readD();
        this._gemStoneItemObjId = readD();
        this._gemStoneCount = readD();
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        ItemInstance targetItem = activeChar.getInventory().getItemByObjectId(this._targetItemObjId);
        if (targetItem == null) {
            activeChar.sendPacket(new ExVariationResult(0, 0, 0));
            activeChar.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
            return;
        }
        ItemInstance refinerItem = activeChar.getInventory().getItemByObjectId(this._refinerItemObjId);
        if (refinerItem == null) {
            activeChar.sendPacket(new ExVariationResult(0, 0, 0));
            activeChar.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
            return;
        }
        ItemInstance gemStoneItem = activeChar.getInventory().getItemByObjectId(this._gemStoneItemObjId);
        if (gemStoneItem == null) {
            activeChar.sendPacket(new ExVariationResult(0, 0, 0));
            activeChar.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
            return;
        }
        if (!isValid(activeChar, targetItem, refinerItem, gemStoneItem)) {
            activeChar.sendPacket(new ExVariationResult(0, 0, 0));
            activeChar.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
            return;
        }
        AbstractRefinePacket.LifeStone ls = getLifeStone(refinerItem.getItemId());
        if (ls == null) {
            activeChar.sendPacket(new ExVariationResult(0, 0, 0));
            activeChar.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
            return;
        }
        int lifeStoneLevel = ls.getLevel();
        int lifeStoneGrade = ls.getGrade();
        if (this._gemStoneCount != getGemStoneCount(targetItem.getItem().getCrystalType())) {
            activeChar.sendPacket(new ExVariationResult(0, 0, 0));
            activeChar.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
            return;
        }
        if (targetItem.isEquipped()) {
            ItemInstance[] unequipped = activeChar.getInventory().unEquipItemInSlotAndRecord(targetItem.getLocationSlot());
            InventoryUpdate inventoryUpdate = new InventoryUpdate();
            for (ItemInstance itm : unequipped)
                inventoryUpdate.addModifiedItem(itm);
            activeChar.sendPacket(inventoryUpdate);
            activeChar.broadcastUserInfo();
        }
        if (!activeChar.destroyItem("RequestRefine", refinerItem, 1, null, false)) {
            activeChar.sendPacket(new ExVariationResult(0, 0, 0));
            activeChar.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
            return;
        }
        if (!activeChar.destroyItem("RequestRefine", gemStoneItem, this._gemStoneCount, null, false)) {
            activeChar.sendPacket(new ExVariationResult(0, 0, 0));
            activeChar.sendPacket(SystemMessageId.AUGMENTATION_FAILED_DUE_TO_INAPPROPRIATE_CONDITIONS);
            return;
        }
        L2Augmentation aug = AugmentationData.getInstance().generateRandomAugmentation(lifeStoneLevel, lifeStoneGrade);
        targetItem.setAugmentation(aug);
        int stat12 = 0xFFFF & aug.getAugmentationId();
        int stat34 = aug.getAugmentationId() >> 16;
        activeChar.sendPacket(new ExVariationResult(stat12, stat34, 1));
        InventoryUpdate iu = new InventoryUpdate();
        iu.addModifiedItem(targetItem);
        activeChar.sendPacket(iu);
        StatusUpdate su = new StatusUpdate(activeChar);
        su.addAttribute(14, activeChar.getCurrentLoad());
        activeChar.sendPacket(su);
    }
}
