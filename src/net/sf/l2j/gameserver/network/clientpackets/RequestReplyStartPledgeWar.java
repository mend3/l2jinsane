package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;

public final class RequestReplyStartPledgeWar extends L2GameClientPacket {
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
            ClanTable.getInstance().storeClansWars(requestor.getClanId(), activeChar.getClanId());
        } else {
            requestor.sendPacket(SystemMessageId.WAR_PROCLAMATION_HAS_BEEN_REFUSED);
        }
        activeChar.setActiveRequester(null);
        requestor.onTransactionResponse();
    }
}
