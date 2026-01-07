package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.pledge.ClanMember;

public class PledgeReceivePowerInfo extends L2GameServerPacket {
    private final ClanMember _member;

    public PledgeReceivePowerInfo(ClanMember member) {
        this._member = member;
    }

    protected void writeImpl() {
        writeC(254);
        writeH(60);
        writeD(this._member.getPowerGrade());
        writeS(this._member.getName());
        writeD(this._member.getClan().getPriviledgesByRank(this._member.getPowerGrade()));
    }
}
