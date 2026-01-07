package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.pledge.SubPledge;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.*;

public final class RequestAnswerJoinPledge extends L2GameClientPacket {
    private int _answer;

    protected void readImpl() {
        this._answer = readD();
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        Player requestor = activeChar.getRequest().getPartner();
        if (requestor == null)
            return;
        if (this._answer == 0) {
            activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_DID_NOT_RESPOND_TO_S1_CLAN_INVITATION).addCharName(requestor));
            requestor.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DID_NOT_RESPOND_TO_CLAN_INVITATION).addCharName(activeChar));
        } else {
            if (!(requestor.getRequest().getRequestPacket() instanceof RequestJoinPledge requestPacket))
                return;
            Clan clan = requestor.getClan();
            if (clan.checkClanJoinCondition(requestor, activeChar, requestPacket.getPledgeType())) {
                activeChar.sendPacket(new JoinPledge(requestor.getClanId()));
                activeChar.setPledgeType(requestPacket.getPledgeType());
                switch (requestPacket.getPledgeType()) {
                    case -1:
                        activeChar.setPowerGrade(9);
                        activeChar.setLvlJoinedAcademy(activeChar.getLevel());
                        break;
                    case 100:
                    case 200:
                        activeChar.setPowerGrade(7);
                        break;
                    case 1001:
                    case 1002:
                    case 2001:
                    case 2002:
                        activeChar.setPowerGrade(8);
                        break;
                    default:
                        activeChar.setPowerGrade(6);
                        break;
                }
                clan.addClanMember(activeChar);
                activeChar.setClanPrivileges(clan.getPriviledgesByRank(activeChar.getPowerGrade()));
                activeChar.sendPacket(SystemMessageId.ENTERED_THE_CLAN);
                clan.broadcastToOtherOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_JOINED_CLAN).addCharName(activeChar), activeChar);
                clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListAdd(activeChar), activeChar);
                clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
                activeChar.sendPacket(new PledgeShowMemberListAll(clan, 0));
                for (SubPledge sp : activeChar.getClan().getAllSubPledges())
                    activeChar.sendPacket(new PledgeShowMemberListAll(clan, sp.getId()));
                activeChar.setClanJoinExpiryTime(0L);
                activeChar.broadcastUserInfo();
            }
        }
        activeChar.getRequest().onRequestResponse();
    }
}
