package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.network.SystemMessageId;

public final class RequestStopPledgeWar extends L2GameClientPacket {
    private String _pledgeName;

    protected void readImpl() {
        this._pledgeName = readS();
    }

    protected void runImpl() {
        Player player = getClient().getPlayer();
        if (player == null)
            return;
        Clan playerClan = player.getClan();
        if (playerClan == null)
            return;
        Clan clan = ClanTable.getInstance().getClanByName(this._pledgeName);
        if (clan == null)
            return;
        if ((player.getClanPrivileges() & 0x20) != 32) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
            return;
        }
        if (!playerClan.isAtWarWith(clan.getClanId())) {
            player.sendPacket(SystemMessageId.NOT_INVOLVED_IN_WAR);
            return;
        }
        for (Player member : playerClan.getOnlineMembers()) {
            if (member.isInCombat()) {
                player.sendPacket(SystemMessageId.CANT_STOP_CLAN_WAR_WHILE_IN_COMBAT);
                return;
            }
        }
        ClanTable.getInstance().deleteClansWars(playerClan.getClanId(), clan.getClanId());
        for (Player member : clan.getOnlineMembers())
            member.broadcastUserInfo();
        for (Player member : playerClan.getOnlineMembers())
            member.broadcastUserInfo();
    }
}
