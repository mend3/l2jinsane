package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class RequestSurrenderPledgeWar extends L2GameClientPacket {
    private String _pledgeName;

    protected void readImpl() {
        this._pledgeName = readS();
    }

    protected void runImpl() {
        Player activeChar = getClient().getPlayer();
        if (activeChar == null)
            return;
        Clan playerClan = activeChar.getClan();
        if (playerClan == null)
            return;
        if ((activeChar.getClanPrivileges() & 0x20) != 32) {
            activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
            return;
        }
        Clan clan = ClanTable.getInstance().getClanByName(this._pledgeName);
        if (clan == null)
            return;
        if (!playerClan.isAtWarWith(clan.getClanId())) {
            activeChar.sendPacket(SystemMessageId.NOT_INVOLVED_IN_WAR);
            return;
        }
        activeChar.deathPenalty(false, false, false);
        activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_SURRENDERED_TO_THE_S1_CLAN).addString(this._pledgeName));
        ClanTable.getInstance().deleteClansWars(playerClan.getClanId(), clan.getClanId());
    }
}
