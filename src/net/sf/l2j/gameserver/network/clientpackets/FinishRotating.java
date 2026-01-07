package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.StopRotation;

public final class FinishRotating extends L2GameClientPacket {
    private int _degree;

    private int _unknown;

    protected void readImpl() {
        this._degree = readD();
        this._unknown = readD();
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        activeChar.broadcastPacket(new StopRotation(activeChar.getObjectId(), this._degree, 0));
    }
}
