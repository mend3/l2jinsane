package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.ExSendManorList;

public class RequestManorList extends L2GameClientPacket {
    protected void readImpl() {
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        player.sendPacket(ExSendManorList.STATIC_PACKET);
    }
}
