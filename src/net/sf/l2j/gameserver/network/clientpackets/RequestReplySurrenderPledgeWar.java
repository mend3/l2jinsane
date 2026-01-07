package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.model.actor.Player;

public final class RequestReplySurrenderPledgeWar extends L2GameClientPacket {
    private int _answer;

    protected void readImpl() {
        this._answer = readD();
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        Player requestor = activeChar.getActiveRequester();
        if (requestor == null)
            return;
        if (this._answer == 1) {
            requestor.deathPenalty(false, false, false);
            ClanTable.getInstance().deleteClansWars(requestor.getClanId(), activeChar.getClanId());
        }
        activeChar.onTransactionRequest(requestor);
    }
}
