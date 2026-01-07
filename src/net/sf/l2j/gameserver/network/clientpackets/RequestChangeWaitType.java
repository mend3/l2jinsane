package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;

public final class RequestChangeWaitType extends L2GameClientPacket {
    private boolean _typeStand;

    protected void readImpl() {
        this._typeStand = (readD() == 1);
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        player.tryToSitOrStand(player.getTarget(), this._typeStand);
    }
}
