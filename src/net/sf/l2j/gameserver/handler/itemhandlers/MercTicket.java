package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.data.manager.SevenSignsManager;
import net.sf.l2j.gameserver.enums.SealType;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.item.MercenaryTicket;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class MercTicket implements IItemHandler {
    public void useItem(Playable playable, ItemInstance item, boolean forceUse) {
        Player activeChar = (Player) playable;
        if (activeChar == null)
            return;
        Castle castle = CastleManager.getInstance().getCastle(activeChar);
        if (castle == null)
            return;
        int castleId = castle.getCastleId();
        if (!activeChar.isCastleLord(castleId)) {
            activeChar.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_AUTHORITY_TO_POSITION_MERCENARIES);
            return;
        }
        int itemId = item.getItemId();
        MercenaryTicket ticket = castle.getTicket(itemId);
        if (ticket == null) {
            activeChar.sendPacket(SystemMessageId.MERCENARIES_CANNOT_BE_POSITIONED_HERE);
            return;
        }
        if (castle.getSiege().isInProgress()) {
            activeChar.sendPacket(SystemMessageId.THIS_MERCENARY_CANNOT_BE_POSITIONED_ANYMORE);
            return;
        }
        if (!SevenSignsManager.getInstance().isSealValidationPeriod()) {
            activeChar.sendPacket(SystemMessageId.THIS_MERCENARY_CANNOT_BE_POSITIONED_ANYMORE);
            return;
        }
        if (!ticket.isSsqType(SevenSignsManager.getInstance().getSealOwner(SealType.STRIFE))) {
            activeChar.sendPacket(SystemMessageId.THIS_MERCENARY_CANNOT_BE_POSITIONED_ANYMORE);
            return;
        }
        if (castle.getDroppedTicketsCount(itemId) >= ticket.getMaxAmount()) {
            activeChar.sendPacket(SystemMessageId.THIS_MERCENARY_CANNOT_BE_POSITIONED_ANYMORE);
            return;
        }
        if (castle.isTooCloseFromDroppedTicket(activeChar.getX(), activeChar.getY(), activeChar.getZ())) {
            activeChar.sendPacket(SystemMessageId.POSITIONING_CANNOT_BE_DONE_BECAUSE_DISTANCE_BETWEEN_MERCENARIES_TOO_SHORT);
            return;
        }
        ItemInstance droppedTicket = activeChar.dropItem("Consume", item.getObjectId(), 1, activeChar.getX(), activeChar.getY(), activeChar.getZ(), null, false);
        if (droppedTicket == null)
            return;
        castle.addDroppedTicket(droppedTicket);
        activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.PLACE_S1_IN_CURRENT_LOCATION_AND_DIRECTION).addItemName(itemId));
    }
}
