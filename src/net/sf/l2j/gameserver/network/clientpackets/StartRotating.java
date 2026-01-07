package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.StartRotation;

public final class StartRotating extends L2GameClientPacket {
    private int _degree;

    private int _side;

    protected void readImpl() {
        this._degree = readD();
        this._side = readD();
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        activeChar.broadcastPacket(new StartRotation(activeChar.getObjectId(), this._degree, this._side, 0));
    }
}
