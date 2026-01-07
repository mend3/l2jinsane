package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.SocialAction;

public class NoblesItem implements IItemHandler {
    public void useItem(Playable playable, ItemInstance item, boolean forceUse) {
        if (!(playable instanceof Player activeChar))
            return;
        if (activeChar.isNoble()) {
            activeChar.sendMessage("SYS: You Are Already A Noblesse!.");
        } else {
            activeChar.broadcastPacket(new SocialAction(activeChar, 16));
            activeChar.setNoble(true, true);
            activeChar.sendMessage("You Are Now a Noble,You Are Granted With Noblesse Status , And Noblesse Skills.");
            activeChar.broadcastUserInfo();
            playable.destroyItem("Consume", item.getObjectId(), 1, null, false);
        }
    }
}
