package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.data.xml.DoorData;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Door;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;

public class PaganKeys implements IItemHandler {
    public void useItem(Playable playable, ItemInstance item, boolean forceUse) {
        if (!(playable instanceof Player activeChar))
            return;
        WorldObject target = activeChar.getTarget();
        if (!(target instanceof Door door)) {
            activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
            activeChar.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (!activeChar.isInsideRadius(door, 150, false, false)) {
            activeChar.sendPacket(SystemMessageId.DIST_TOO_FAR_CASTING_STOPPED);
            activeChar.sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (!playable.destroyItem("Consume", item.getObjectId(), 1, null, true))
            return;
        int doorId = door.getDoorId();
        switch (item.getItemId()) {
            case 8056:
                if (doorId == 23150004 || doorId == 23150003) {
                    DoorData.getInstance().getDoor(23150003).openMe();
                    DoorData.getInstance().getDoor(23150004).openMe();
                    break;
                }
                activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
                break;
            case 8273:
                switch (doorId) {
                    case 19160002:
                    case 19160003:
                    case 19160004:
                    case 19160005:
                    case 19160006:
                    case 19160007:
                    case 19160008:
                    case 19160009:
                        DoorData.getInstance().getDoor(doorId).openMe();
                        break;
                }
                activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
                break;
            case 8275:
                switch (doorId) {
                    case 19160012:
                    case 19160013:
                        DoorData.getInstance().getDoor(doorId).openMe();
                        break;
                }
                activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
                break;
        }
    }
}
