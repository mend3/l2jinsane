package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.FloodProtectors;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.ItemList;

public final class RequestDropItem extends L2GameClientPacket {
    private int _objectId;

    private int _count;

    private int _x;

    private int _y;

    private int _z;

    protected void readImpl() {
        this._objectId = readD();
        this._count = readD();
        this._x = readD();
        this._y = readD();
        this._z = readD();
    }

    protected void runImpl() {
        if (!FloodProtectors.performAction(getClient(), FloodProtectors.Action.DROP_ITEM))
            return;
        Player activeChar = getClient().getPlayer();
        if (activeChar == null || activeChar.isDead())
            return;
        ItemInstance item = activeChar.validateItemManipulation(this._objectId);
        if (item == null || this._count == 0 || (!Config.ALLOW_DISCARDITEM && !activeChar.isGM()) || !item.isDropable()) {
            activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
            return;
        }
        if (item.isQuestItem())
            return;
        if (this._count > item.getCount() || item.isAgathionItem()) {
            activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
            return;
        }
        if (this._count < 0)
            return;
        if (!item.isStackable() && this._count > 1)
            return;
        if (!activeChar.getAccessLevel().allowTransaction()) {
            activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
            return;
        }
        if (activeChar.isProcessingTransaction() || activeChar.isInStoreMode()) {
            activeChar.sendPacket(SystemMessageId.CANNOT_TRADE_DISCARD_DROP_ITEM_WHILE_IN_SHOPMODE);
            return;
        }
        if (activeChar.isFishing()) {
            activeChar.sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_2);
            return;
        }
        if (item.isAugmented()) {
            activeChar.sendPacket(SystemMessageId.AUGMENTED_ITEM_CANNOT_BE_DISCARDED);
            return;
        }
        if (activeChar.isCastingNow())
            if (activeChar.getCurrentSkill().getSkill() != null && activeChar.getCurrentSkill().getSkill().getItemConsumeId() == item.getItemId()) {
                activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
                return;
            }
        if (activeChar.isCastingSimultaneouslyNow())
            if (activeChar.getLastSimultaneousSkillCast() != null && activeChar.getLastSimultaneousSkillCast().getItemConsumeId() == item.getItemId()) {
                activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_THIS_ITEM);
                return;
            }
        if (3 == item.getItem().getType2() && !activeChar.isGM()) {
            activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_EXCHANGE_ITEM);
            return;
        }
        if (!activeChar.isInsideRadius(this._x, this._y, 150, false) || Math.abs(this._z - activeChar.getZ()) > 50) {
            activeChar.sendPacket(SystemMessageId.CANNOT_DISCARD_DISTANCE_TOO_FAR);
            return;
        }
        if (item.isEquipped() && (!item.isStackable() || (item.isStackable() && this._count >= item.getCount()))) {
            ItemInstance[] unequipped = activeChar.getInventory().unEquipItemInBodySlotAndRecord(item);
            InventoryUpdate iu = new InventoryUpdate();
            for (ItemInstance itm : unequipped) {
                itm.unChargeAllShots();
                iu.addModifiedItem(itm);
            }
            activeChar.sendPacket(iu);
            activeChar.broadcastUserInfo();
            activeChar.sendPacket(new ItemList(activeChar, true));
        }
        activeChar.dropItem("Drop", this._objectId, this._count, this._x, this._y, this._z, null, false);
    }
}
