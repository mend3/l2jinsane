package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.network.serverpackets.SiegeDefenderList;

public final class RequestSiegeDefenderList extends L2GameClientPacket {
    private int _castleId;

    protected void readImpl() {
        this._castleId = readD();
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        Castle castle = CastleManager.getInstance().getCastleById(this._castleId);
        if (castle == null)
            return;
        sendPacket(new SiegeDefenderList(castle));
    }
}
