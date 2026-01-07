package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;

public final class Appearing extends L2GameClientPacket {
    protected void readImpl() {
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        if (activeChar.isTeleporting())
            activeChar.onTeleported();
        sendPacket(new UserInfo(activeChar));
    }

    protected boolean triggersOnActionRequest() {
        return false;
    }
}
