package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public class Elixir extends ItemSkills {
    public void useItem(Playable playable, ItemInstance item, boolean forceUse) {
        if (!(playable instanceof net.sf.l2j.gameserver.model.actor.Player)) {
            playable.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ITEM_NOT_FOR_PETS));
            return;
        }
        super.useItem(playable, item, forceUse);
    }
}
