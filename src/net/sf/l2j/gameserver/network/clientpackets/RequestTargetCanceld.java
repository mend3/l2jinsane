package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;

public final class RequestTargetCanceld extends L2GameClientPacket {
    private int _unselect;

    protected void readImpl() {
        this._unselect = readH();
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        if (this._unselect == 0) {
            if (activeChar.isCastingNow() && activeChar.canAbortCast()) {
                activeChar.abortCast();
            } else {
                activeChar.setTarget(null);
            }
        } else {
            activeChar.setTarget(null);
        }
    }
}
