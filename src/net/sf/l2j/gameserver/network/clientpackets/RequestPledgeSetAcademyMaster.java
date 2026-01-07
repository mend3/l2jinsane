package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.pledge.ClanMember;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestPledgeSetAcademyMaster extends L2GameClientPacket {
    private String _currPlayerName;

    private int _set;

    private String _targetPlayerName;

    protected void readImpl() {
        this._set = readD();
        this._currPlayerName = readS();
        this._targetPlayerName = readS();
    }

    protected void runImpl() {
        ClanMember apprenticeMember, sponsorMember;
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        Clan clan = activeChar.getClan();
        if (clan == null)
            return;
        if ((activeChar.getClanPrivileges() & 0x100) != 256) {
            activeChar.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_THE_RIGHT_TO_DISMISS_AN_APPRENTICE);
            return;
        }
        ClanMember currentMember = clan.getClanMember(this._currPlayerName);
        ClanMember targetMember = clan.getClanMember(this._targetPlayerName);
        if (currentMember == null || targetMember == null)
            return;
        if (currentMember.getPledgeType() == -1) {
            apprenticeMember = currentMember;
            sponsorMember = targetMember;
        } else {
            apprenticeMember = targetMember;
            sponsorMember = currentMember;
        }
        Player apprentice = apprenticeMember.getPlayerInstance();
        Player sponsor = sponsorMember.getPlayerInstance();
        SystemMessage sm = null;
        if (this._set == 0) {
            if (apprentice != null) {
                apprentice.setSponsor(0);
            } else {
                apprenticeMember.setApprenticeAndSponsor(0, 0);
            }
            if (sponsor != null) {
                sponsor.setApprentice(0);
            } else {
                sponsorMember.setApprenticeAndSponsor(0, 0);
            }
            apprenticeMember.saveApprenticeAndSponsor(0, 0);
            sponsorMember.saveApprenticeAndSponsor(0, 0);
            sm = SystemMessage.getSystemMessage(SystemMessageId.S2_CLAN_MEMBER_S1_APPRENTICE_HAS_BEEN_REMOVED);
        } else {
            if (apprenticeMember.getSponsor() != 0 || sponsorMember.getApprentice() != 0 || apprenticeMember.getApprentice() != 0 || sponsorMember.getSponsor() != 0) {
                activeChar.sendMessage("Remove previous connections first.");
                return;
            }
            if (apprentice != null) {
                apprentice.setSponsor(sponsorMember.getObjectId());
            } else {
                apprenticeMember.setApprenticeAndSponsor(0, sponsorMember.getObjectId());
            }
            if (sponsor != null) {
                sponsor.setApprentice(apprenticeMember.getObjectId());
            } else {
                sponsorMember.setApprenticeAndSponsor(apprenticeMember.getObjectId(), 0);
            }
            apprenticeMember.saveApprenticeAndSponsor(0, sponsorMember.getObjectId());
            sponsorMember.saveApprenticeAndSponsor(apprenticeMember.getObjectId(), 0);
            sm = SystemMessage.getSystemMessage(SystemMessageId.S2_HAS_BEEN_DESIGNATED_AS_APPRENTICE_OF_CLAN_MEMBER_S1);
        }
        sm.addString(sponsorMember.getName());
        sm.addString(apprenticeMember.getName());
        if (sponsor != activeChar && sponsor != apprentice)
            activeChar.sendPacket(sm);
        if (sponsor != null)
            sponsor.sendPacket(sm);
        if (apprentice != null)
            apprentice.sendPacket(sm);
        clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(sponsorMember), new PledgeShowMemberListUpdate(apprenticeMember));
    }
}
