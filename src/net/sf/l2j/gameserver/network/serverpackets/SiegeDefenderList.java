package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.enums.SiegeSide;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.pledge.Clan;

import java.util.List;

public class SiegeDefenderList extends L2GameServerPacket {
    private final Castle _castle;

    public SiegeDefenderList(Castle castle) {
        this._castle = castle;
    }

    protected final void writeImpl() {
        writeC(203);
        writeD(this._castle.getCastleId());
        writeD(0);
        writeD(1);
        writeD(0);
        List<Clan> defenders = this._castle.getSiege().getDefenderClans();
        List<Clan> pendingDefenders = this._castle.getSiege().getPendingClans();
        int size = defenders.size() + pendingDefenders.size();
        if (size > 0) {
            writeD(size);
            writeD(size);
            for (Clan clan : defenders) {
                writeD(clan.getClanId());
                writeS(clan.getName());
                writeS(clan.getLeaderName());
                writeD(clan.getCrestId());
                writeD(0);
                SiegeSide side = this._castle.getSiege().getSide(clan);
                if (side == SiegeSide.OWNER) {
                    writeD(1);
                } else if (side == SiegeSide.PENDING) {
                    writeD(2);
                } else if (side == SiegeSide.DEFENDER) {
                    writeD(3);
                } else {
                    writeD(0);
                }
                writeD(clan.getAllyId());
                writeS(clan.getAllyName());
                writeS("");
                writeD(clan.getAllyCrestId());
            }
            for (Clan clan : pendingDefenders) {
                writeD(clan.getClanId());
                writeS(clan.getName());
                writeS(clan.getLeaderName());
                writeD(clan.getCrestId());
                writeD(0);
                writeD(2);
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
