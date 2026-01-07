package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.pledge.ClanMember;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestPledgeSetMemberPowerGrade extends L2GameClientPacket {
    private int _powerGrade;

    private String _member;

    protected void readImpl() {
        this._member = readS();
        this._powerGrade = readD();
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        Clan clan = activeChar.getClan();
        if (clan == null)
            return;
        ClanMember member = clan.getClanMember(this._member);
        if (member == null)
            return;
        if (member.getPledgeType() == -1)
            return;
        member.setPowerGrade(this._powerGrade);
        clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(member), SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_PRIVILEGE_CHANGED_TO_S2).addString(member.getName()).addNumber(this._powerGrade));
    }
}
