package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.ExListPartyMatchingWaitingRoom;

public class RequestListPartyMatchingWaitingRoom extends L2GameClientPacket {
    private static int _page;

    private static int _minlvl;

    private static int _maxlvl;

    private static int _mode;

    protected void readImpl() {
        _page = readD();
        _minlvl = readD();
        _maxlvl = readD();
        _mode = readD();
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        activeChar.sendPacket(new ExListPartyMatchingWaitingRoom(activeChar, _page, _minlvl, _maxlvl, _mode));
    }
}
