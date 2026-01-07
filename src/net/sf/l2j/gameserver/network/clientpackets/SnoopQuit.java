package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;

public final class SnoopQuit extends L2GameClientPacket {
    private int _snoopID;

    protected void readImpl() {
        this._snoopID = readD();
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        Player target = World.getInstance().getPlayer(this._snoopID);
        if (target == null) {
        }
    }
}
