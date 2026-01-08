package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.pledge.ClanMember;

import java.util.Collection;
import java.util.Set;

public class PledgePowerGradeList extends L2GameServerPacket {
    private final Set<Integer> _ranks;

    private final Collection<ClanMember> _members;

    public PledgePowerGradeList(Set<Integer> ranks, Collection<ClanMember> members) {
        this._ranks = ranks;
        this._members = members;
    }

    protected final void writeImpl() {
        writeC(254);
        writeH(59);
        writeD(this._ranks.size());
        for (int rank : this._ranks) {
            writeD(rank);
            writeD((int) this._members.stream().filter(m -> (m.getPowerGrade() == rank)).count());
        }
    }
}
