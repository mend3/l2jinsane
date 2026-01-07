package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;

public final class RequestOlympiadObserverEnd extends L2GameClientPacket {
    protected void readImpl() {
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        if (activeChar.isInObserverMode())
            activeChar.leaveOlympiadObserverMode();
    }
}
