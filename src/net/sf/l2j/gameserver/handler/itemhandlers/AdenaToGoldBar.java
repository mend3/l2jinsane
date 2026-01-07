package net.sf.l2j.gameserver.handler.itemhandlers;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;

public class AdenaToGoldBar implements IItemHandler {
    public void useItem(Playable playable, ItemInstance item, boolean forceUse) {
        if (!(playable instanceof Player player))
            return;
        if (player.getInventory().getAdena() >= Config.ADENA_TO_GOLDBAR) {
            player.getInventory().addItem("AdenaToGoldBar", 3470, 1, player, player);
            player.sendPacket(new ExShowScreenMessage("Now you have 1 Gold Bar !", 1500, 2, true));
            player.sendPacket(new PlaySound("ambsound.gf_horn_02"));
            player.destroyItem("", item.getObjectId(), 100000, null, true);
        } else {
            player.sendMessage("Not enought adena.");
            player.sendPacket(ActionFailed.STATIC_PACKET);
        }
    }
}
