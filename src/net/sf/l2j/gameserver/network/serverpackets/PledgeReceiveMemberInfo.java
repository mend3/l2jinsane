package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.pledge.ClanMember;

public class PledgeReceiveMemberInfo extends L2GameServerPacket {
    private final ClanMember _member;

    public PledgeReceiveMemberInfo(ClanMember member) {
        this._member = member;
    }

    protected void writeImpl() {
        writeC(254);
        writeH(61);
        writeD(this._member.getPledgeType());
        writeS(this._member.getName());
        writeS(this._member.getTitle());
        writeD(this._member.getPowerGrade());
        if (this._member.getPledgeType() != 0) {
            writeS(this._member.getClan().getSubPledge(this._member.getPledgeType()).getName());
        } else {
            writeS(this._member.getClan().getName());
        }
        writeS(this._member.getApprenticeOrSponsorName());
    }
}
