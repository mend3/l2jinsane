package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;

public final class RequestChangeMoveType extends L2GameClientPacket {
    private boolean _typeRun;

    protected void readImpl() {
        this._typeRun = (readD() == 1);
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        if (player.isMounted())
            return;
        if (this._typeRun) {
            player.setRunning();
        } else {
            player.setWalking();
        }
    }
}
