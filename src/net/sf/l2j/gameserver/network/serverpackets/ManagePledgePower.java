package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.pledge.Clan;

public class ManagePledgePower extends L2GameServerPacket {
    private final int _action;

    private final Clan _clan;

    private final int _rank;

    public ManagePledgePower(Clan clan, int action, int rank) {
        this._clan = clan;
        this._action = action;
        this._rank = rank;
    }

    protected final void writeImpl() {
        writeC(48);
        writeD(this._rank);
        writeD(this._action);
        writeD(this._clan.getPriviledgesByRank(this._rank));
    }
}
