package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.pledge.ClanMember;

public class GMViewPledgeInfo extends L2GameServerPacket {
    private final Clan _clan;

    private final Player _activeChar;

    public GMViewPledgeInfo(Clan clan, Player activeChar) {
        this._clan = clan;
        this._activeChar = activeChar;
    }

    protected final void writeImpl() {
        writeC(144);
        writeS(this._activeChar.getName());
        writeD(this._clan.getClanId());
        writeD(0);
        writeS(this._clan.getName());
        writeS(this._clan.getLeaderName());
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
        writeD(this._clan.getMembersCount());
        for (ClanMember member : this._clan.getMembers()) {
            if (member != null) {
                writeS(member.getName());
                writeD(member.getLevel());
                writeD(member.getClassId());
                writeD(member.getSex().ordinal());
                writeD(member.getRaceOrdinal());
                writeD(member.isOnline() ? member.getObjectId() : 0);
                writeD((member.getSponsor() != 0) ? 1 : 0);
            }
        }
    }
}
