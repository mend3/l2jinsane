package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.pledge.ClanMember;

public class PledgeShowMemberListAll extends L2GameServerPacket {
    private final Clan _clan;

    private final int _pledgeType;

    private final String _pledgeName;

    public PledgeShowMemberListAll(Clan clan, int pledgeType) {
        this._clan = clan;
        this._pledgeType = pledgeType;
        if (this._pledgeType == 0) {
            this._pledgeName = clan.getName();
        } else if (this._clan.getSubPledge(this._pledgeType) != null) {
            this._pledgeName = this._clan.getSubPledge(this._pledgeType).getName();
        } else {
            this._pledgeName = "";
        }
    }

    protected final void writeImpl() {
        writeC(83);
        writeD((this._pledgeType == 0) ? 0 : 1);
        writeD(this._clan.getClanId());
        writeD(this._pledgeType);
        writeS(this._pledgeName);
        writeS(this._clan.getSubPledgeLeaderName(this._pledgeType));
        writeD(this._clan.getCrestId());
        writeD(this._clan.getLevel());
        writeD(this._clan.getCastleId());
        writeD(this._clan.getClanHallId());
        writeD(this._clan.getRank());
        writeD(this._clan.getReputationScore());
        writeD(0);
        writeD(0);
        writeD(this._clan.getAllyId());
        writeS(this._clan.getAllyName());
        writeD(this._clan.getAllyCrestId());
        writeD(this._clan.isAtWar() ? 1 : 0);
        writeD(this._clan.getSubPledgeMembersCount(this._pledgeType));
        for (ClanMember m : this._clan.getMembers()) {
            if (m.getPledgeType() != this._pledgeType)
                continue;
            writeS(m.getName());
            writeD(m.getLevel());
            writeD(m.getClassId());
            Player player = m.getPlayerInstance();
            if (player != null) {
                writeD(player.getAppearance().getSex().ordinal());
                writeD(player.getRace().ordinal());
            } else {
                writeD(1);
                writeD(1);
            }
            writeD(m.isOnline() ? m.getObjectId() : 0);
            writeD((m.getSponsor() != 0 || m.getApprentice() != 0) ? 1 : 0);
        }
    }
}
