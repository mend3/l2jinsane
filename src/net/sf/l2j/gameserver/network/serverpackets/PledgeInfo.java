package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.pledge.Clan;

public class PledgeInfo extends L2GameServerPacket {
    private final Clan _clan;

    public PledgeInfo(Clan clan) {
        this._clan = clan;
    }

    protected final void writeImpl() {
        writeC(131);
        writeD(this._clan.getClanId());
        writeS(this._clan.getName());
        writeS(this._clan.getAllyName());
    }
}
