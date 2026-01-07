package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.network.serverpackets.PledgeCrest;

public final class RequestPledgeCrest extends L2GameClientPacket {
    private int _crestId;

    protected void readImpl() {
        this._crestId = readD();
    }

    protected void runImpl() {
        sendPacket(new PledgeCrest(this._crestId));
    }

    protected boolean triggersOnActionRequest() {
        return false;
    }
}
