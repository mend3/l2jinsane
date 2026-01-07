package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class RequestUnEquipItem extends L2GameClientPacket {
    private int _slot;

    protected void readImpl() {
        this._slot = readD();
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        ItemInstance item = activeChar.getInventory().getPaperdollItemByL2ItemId(this._slot);
        if (item == null)
            return;
        if (this._slot == 16384 && activeChar.isCursedWeaponEquipped())
            return;
        if (activeChar.isStunned() || activeChar.isSleeping() || activeChar.isParalyzed() || activeChar.isAfraid() || activeChar.isAlikeDead()) {
            activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addItemName(item));
            return;
        }
        if (activeChar.isCastingNow() || activeChar.isCastingSimultaneouslyNow())
            return;
        ItemInstance[] unequipped = activeChar.getInventory().unEquipItemInBodySlotAndRecord(this._slot);
        InventoryUpdate iu = new InventoryUpdate();
        for (ItemInstance itm : unequipped) {
            itm.unChargeAllShots();
            iu.addModifiedItem(itm);
        }
        activeChar.sendPacket(iu);
        activeChar.broadcastUserInfo();
        if (unequipped.length > 0) {
            SystemMessage sm = null;
            if (unequipped[0].getEnchantLevel() > 0) {
                sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED);
                sm.addNumber(unequipped[0].getEnchantLevel());
                sm.addItemName(unequipped[0]);
            } else {
                sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED);
                sm.addItemName(unequipped[0]);
            }
            activeChar.sendPacket(sm);
        }
    }
}
