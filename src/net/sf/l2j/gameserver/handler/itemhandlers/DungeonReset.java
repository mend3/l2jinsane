package net.sf.l2j.gameserver.handler.itemhandlers;

import mods.dungeon.DungeonManager;
import net.sf.l2j.gameserver.handler.IItemHandler;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;

public class DungeonReset implements IItemHandler {
    public void useItem(Playable playable, ItemInstance item, boolean forceUse) {
        if (!(playable instanceof Player player))
            return;
        String ip = player.getHWID();
        DungeonManager.getInstance().getPlayerData().remove(ip);
        player.destroyItem("dungeon reset", item, 1, null, true);
        player.sendMessage("Your Dungeon status has been reset.");
        player.sendPacket(new ExShowScreenMessage("player: " + player.getName() + " Your Dungeon status has been reset.", 10000, ExShowScreenMessage.SMPOS.TOP_CENTER, false));
        player.sendPacket(new MagicSkillUse(player, player, 315, 1, 1, 1));
    }
}
