package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.HennaRemoveList;

public final class RequestHennaRemoveList extends L2GameClientPacket {
    private int _unknown;

    protected void readImpl() {
        this._unknown = readD();
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        activeChar.sendPacket(new HennaRemoveList(activeChar));
    }
}
