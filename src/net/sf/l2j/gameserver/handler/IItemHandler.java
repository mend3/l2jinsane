package net.sf.l2j.gameserver.handler;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;

public interface IItemHandler {
    CLogger LOGGER = new CLogger(IItemHandler.class.getName());

    void useItem(Playable paramPlayable, ItemInstance paramItemInstance, boolean paramBoolean);
}
