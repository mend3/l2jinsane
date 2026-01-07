package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.network.serverpackets.RecipeShopSellList;

public final class RequestRecipeShopManagePrev extends L2GameClientPacket {
    protected void readImpl() {
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        if (player.isAlikeDead()) {
            sendPacket(ActionFailed.STATIC_PACKET);
            return;
        }
        if (!(player.getTarget() instanceof Player))
            return;
        player.sendPacket(new RecipeShopSellList(player, (Player) player.getTarget()));
    }
}
