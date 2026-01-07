package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.pledge.ClanMember;
import net.sf.l2j.gameserver.network.serverpackets.PledgeReceiveMemberInfo;

public final class RequestPledgeReorganizeMember extends L2GameClientPacket {
    private int _isMemberSelected;

    private String _memberName;

    private int _newPledgeType;

    private String _selectedMember;

    protected void readImpl() {
        this._isMemberSelected = readD();
        this._memberName = readS();
        this._newPledgeType = readD();
        this._selectedMember = readS();
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        Clan clan = activeChar.getClan();
        if (clan == null)
            return;
        if ((activeChar.getClanPrivileges() & 0x10) != 16)
            return;
        ClanMember member1 = clan.getClanMember(this._memberName);
        if (this._isMemberSelected == 0) {
            if (member1 != null)
                activeChar.sendPacket(new PledgeReceiveMemberInfo(member1));
            return;
        }
        ClanMember member2 = clan.getClanMember(this._selectedMember);
        if (member1 == null || member1.getObjectId() == clan.getLeaderId() || member2 == null || member2.getObjectId() == clan.getLeaderId())
            return;
        if (clan.isSubPledgeLeader(member1.getObjectId())) {
            activeChar.sendPacket(new PledgeReceiveMemberInfo(member1));
            return;
        }
        int oldPledgeType = member1.getPledgeType();
        if (oldPledgeType == this._newPledgeType)
            return;
        member1.setPledgeType(this._newPledgeType);
        member2.setPledgeType(oldPledgeType);
        clan.broadcastClanStatus();
    }
}
