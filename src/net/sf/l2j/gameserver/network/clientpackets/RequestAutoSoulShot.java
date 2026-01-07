package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExAutoSoulShot;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestAutoSoulShot extends L2GameClientPacket {
    private int _itemId;

    private int _type;

    protected void readImpl() {
        this._itemId = readD();
        this._type = readD();
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        if (!activeChar.isInStoreMode() && activeChar.getActiveRequester() == null && !activeChar.isDead()) {
            ItemInstance item = activeChar.getInventory().getItemByItemId(this._itemId);
            if (item == null)
                return;
            if (this._type == 1) {
                if (this._itemId < 6535 || this._itemId > 6540)
                    if (this._itemId == 6645 || this._itemId == 6646 || this._itemId == 6647) {
                        if (activeChar.getSummon() != null) {
                            if (this._itemId == 6647 && activeChar.isInOlympiadMode()) {
                                activeChar.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
                                return;
                            }
                            if (this._itemId == 6645) {
                                if (activeChar.getSummon().getSoulShotsPerHit() > item.getCount()) {
                                    activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_SOULSHOTS_FOR_PET);
                                    return;
                                }
                            } else if (activeChar.getSummon().getSpiritShotsPerHit() > item.getCount()) {
                                activeChar.sendPacket(SystemMessageId.NOT_ENOUGH_SPIRITSHOTS_FOR_PET);
                                return;
                            }
                            activeChar.addAutoSoulShot(this._itemId);
                            activeChar.sendPacket(new ExAutoSoulShot(this._itemId, this._type));
                            activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.USE_OF_S1_WILL_BE_AUTO).addItemName(this._itemId));
                            activeChar.rechargeShots(true, true);
                            activeChar.getSummon().rechargeShots(true, true);
                        } else {
                            activeChar.sendPacket(SystemMessageId.NO_SERVITOR_CANNOT_AUTOMATE_USE);
                        }
                    } else {
                        if (this._itemId >= 3947 && this._itemId <= 3952 && activeChar.isInOlympiadMode()) {
                            activeChar.sendPacket(SystemMessageId.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
                            return;
                        }
                        activeChar.addAutoSoulShot(this._itemId);
                        activeChar.sendPacket(new ExAutoSoulShot(this._itemId, this._type));
                        if (activeChar.getActiveWeaponInstance() != null && item.getItem().getCrystalType() == activeChar.getActiveWeaponItem().getCrystalType()) {
                            activeChar.rechargeShots(true, true);
                        } else if ((this._itemId >= 2509 && this._itemId <= 2514) || (this._itemId >= 3947 && this._itemId <= 3952) || this._itemId == 5790) {
                            activeChar.sendPacket(SystemMessageId.SPIRITSHOTS_GRADE_MISMATCH);
                        } else {
                            activeChar.sendPacket(SystemMessageId.SOULSHOTS_GRADE_MISMATCH);
                        }
                        activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.USE_OF_S1_WILL_BE_AUTO).addItemName(this._itemId));
                    }
            } else if (this._type == 0) {
                activeChar.removeAutoSoulShot(this._itemId);
                activeChar.sendPacket(new ExAutoSoulShot(this._itemId, this._type));
                activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED).addItemName(this._itemId));
            }
        }
    }
}
