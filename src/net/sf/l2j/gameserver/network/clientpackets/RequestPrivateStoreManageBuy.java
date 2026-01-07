package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;

public final class RequestPrivateStoreManageBuy extends L2GameClientPacket {
    protected void readImpl() {
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        player.tryOpenPrivateBuyStore();
    }
}
