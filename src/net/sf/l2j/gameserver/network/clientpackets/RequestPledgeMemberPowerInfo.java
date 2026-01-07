package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.pledge.ClanMember;
import net.sf.l2j.gameserver.network.serverpackets.PledgeReceivePowerInfo;

public final class RequestPledgeMemberPowerInfo extends L2GameClientPacket {
    private int _pledgeType;

    private String _player;

    protected void readImpl() {
        this._pledgeType = readD();
        this._player = readS();
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        Clan clan = activeChar.getClan();
        if (clan == null)
            return;
        ClanMember member = clan.getClanMember(this._player);
        if (member == null)
            return;
        activeChar.sendPacket(new PledgeReceivePowerInfo(member));
    }
}
