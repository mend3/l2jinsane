package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.pledge.Clan;

import java.util.List;

public class SiegeAttackerList extends L2GameServerPacket {
    private final Castle _castle;

    public SiegeAttackerList(Castle castle) {
        this._castle = castle;
    }

    protected final void writeImpl() {
        writeC(202);
        writeD(this._castle.getCastleId());
        writeD(0);
        writeD(1);
        writeD(0);
        List<Clan> attackers = this._castle.getSiege().getAttackerClans();
        int size = attackers.size();
        if (size > 0) {
            writeD(size);
            writeD(size);
            for (Clan clan : attackers) {
                writeD(clan.getClanId());
                writeS(clan.getName());
                writeS(clan.getLeaderName());
                writeD(clan.getCrestId());
                writeD(0);
                writeD(clan.getAllyId());
                writeS(clan.getAllyName());
                writeS("");
                writeD(clan.getAllyCrestId());
            }
        } else {
            writeD(0);
            writeD(0);
        }
    }
}
