package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.enums.SiegeSide;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.network.serverpackets.SiegeDefenderList;

public final class RequestConfirmSiegeWaitingList extends L2GameClientPacket {
    private int _approved;

    private int _castleId;

    private int _clanId;

    protected void readImpl() {
        this._castleId = readD();
        this._clanId = readD();
        this._approved = readD();
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        if (player.getClan() == null)
            return;
        Castle castle = CastleManager.getInstance().getCastleById(this._castleId);
        if (castle == null)
            return;
        if (castle.getOwnerId() != player.getClanId() || !player.isClanLeader())
            return;
        Clan clan = ClanTable.getInstance().getClan(this._clanId);
        if (clan == null)
            return;
        if (!castle.getSiege().isRegistrationOver())
            if (this._approved == 1) {
                if (castle.getSiege().checkSide(clan, SiegeSide.PENDING))
                    castle.getSiege().registerClan(clan, SiegeSide.DEFENDER);
            } else if (castle.getSiege().checkSides(clan, SiegeSide.PENDING, SiegeSide.DEFENDER)) {
                castle.getSiege().unregisterClan(clan);
            }
        player.sendPacket(new SiegeDefenderList(castle));
    }
}
