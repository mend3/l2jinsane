package net.sf.l2j.gameserver.network.serverpackets;

import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.pledge.Clan;

import java.util.Calendar;

public class SiegeInfo extends L2GameServerPacket {
    private final Castle _castle;

    public SiegeInfo(Castle castle) {
        this._castle = castle;
    }

    protected final void writeImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        writeC(201);
        writeD(this._castle.getCastleId());
        writeD((this._castle.getOwnerId() == player.getClanId() && player.isClanLeader()) ? 1 : 0);
        writeD(this._castle.getOwnerId());
        Clan clan = null;
        if (this._castle.getOwnerId() > 0)
            clan = ClanTable.getInstance().getClan(this._castle.getOwnerId());
        if (clan != null) {
            writeS(clan.getName());
            writeS(clan.getLeaderName());
            writeD(clan.getAllyId());
            writeS(clan.getAllyName());
        } else {
            writeS("NPC");
            writeS("");
            writeD(0);
            writeS("");
        }
        writeD((int) (Calendar.getInstance().getTimeInMillis() / 1000L));
        writeD((int) (this._castle.getSiege().getSiegeDate().getTimeInMillis() / 1000L));
        writeD(0);
    }
}
