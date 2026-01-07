package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.pledge.SubPledge;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListAll;

public final class RequestPledgeMemberList extends L2GameClientPacket {
    protected void readImpl() {
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        Clan clan = activeChar.getClan();
        if (clan == null)
            return;
        activeChar.sendPacket(new PledgeShowMemberListAll(clan, 0));
        for (SubPledge sp : clan.getAllSubPledges())
            activeChar.sendPacket(new PledgeShowMemberListAll(clan, sp.getId()));
    }
}
