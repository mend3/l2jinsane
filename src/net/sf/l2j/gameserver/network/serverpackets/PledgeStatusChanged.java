package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.pledge.Clan;

public class PledgeStatusChanged extends L2GameServerPacket {
    private final Clan _clan;

    public PledgeStatusChanged(Clan clan) {
        this._clan = clan;
    }

    protected final void writeImpl() {
        writeC(205);
        writeD(this._clan.getLeaderId());
        writeD(this._clan.getClanId());
        writeD(this._clan.getCrestId());
        writeD(this._clan.getAllyId());
        writeD(this._clan.getAllyCrestId());
        writeD(0);
        writeD(0);
    }
}
