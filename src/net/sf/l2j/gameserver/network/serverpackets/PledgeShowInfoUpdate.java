package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.pledge.Clan;

public class PledgeShowInfoUpdate extends L2GameServerPacket {
    private final Clan _clan;

    public PledgeShowInfoUpdate(Clan clan) {
        this._clan = clan;
    }

    protected final void writeImpl() {
        writeC(136);
        writeD(this._clan.getClanId());
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
    }
}
