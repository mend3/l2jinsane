package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.pledge.SubPledge;

public class PledgeReceiveSubPledgeCreated extends L2GameServerPacket {
    private final SubPledge _subPledge;

    private final Clan _clan;

    public PledgeReceiveSubPledgeCreated(SubPledge subPledge, Clan clan) {
        this._subPledge = subPledge;
        this._clan = clan;
    }

    protected void writeImpl() {
        writeC(254);
        writeH(63);
        writeD(1);
        writeD(this._subPledge.getId());
        writeS(this._subPledge.getName());
        writeS(this._clan.getSubPledgeLeaderName(this._subPledge.getId()));
    }
}
