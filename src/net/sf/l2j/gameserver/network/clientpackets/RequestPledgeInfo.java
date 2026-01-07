package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.network.serverpackets.PledgeInfo;

public final class RequestPledgeInfo extends L2GameClientPacket {
    private int _clanId;

    protected void readImpl() {
        this._clanId = readD();
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        Clan clan = ClanTable.getInstance().getClan(this._clanId);
        if (clan == null)
            return;
        activeChar.sendPacket(new PledgeInfo(clan));
    }

    protected boolean triggersOnActionRequest() {
        return false;
    }
}
