package net.sf.l2j.gameserver.handler.usercommandhandlers;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.entity.Castle;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

import java.text.SimpleDateFormat;

public class ClanPenalty implements IUserCommandHandler {
    private static final String NO_PENALTY = "<tr><td width=170>No penalty is imposed.</td><td width=100 align=center></td></tr>";

    private static final int[] COMMAND_IDS = new int[]{100};

    public void useUserCommand(int id, Player activeChar) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        StringBuilder sb = new StringBuilder();
        long currentTime = System.currentTimeMillis();
        if (activeChar.getClanJoinExpiryTime() > currentTime)
            StringUtil.append(sb, "<tr><td width=170>Unable to join a clan.</td><td width=100 align=center>", sdf.format(activeChar.getClanJoinExpiryTime()), "</td></tr>");
        if (activeChar.getClanCreateExpiryTime() > currentTime)
            StringUtil.append(sb, "<tr><td width=170>Unable to create a clan.</td><td width=100 align=center>", sdf.format(activeChar.getClanCreateExpiryTime()), "</td></tr>");
        Clan clan = activeChar.getClan();
        if (clan != null) {
            if (clan.getCharPenaltyExpiryTime() > currentTime)
                StringUtil.append(sb, "<tr><td width=170>Unable to invite a clan member.</td><td width=100 align=center>", sdf.format(clan.getCharPenaltyExpiryTime()), "</td></tr>");
            int penaltyType = clan.getAllyPenaltyType();
            if (penaltyType != 0) {
                long expiryTime = clan.getAllyPenaltyExpiryTime();
                if (expiryTime > currentTime)
                    if (penaltyType == 1 || penaltyType == 2) {
                        StringUtil.append(sb, "<tr><td width=170>Unable to join an alliance.</td><td width=100 align=center>", sdf.format(expiryTime), "</td></tr>");
                    } else if (penaltyType == 3) {
                        StringUtil.append(sb, "<tr><td width=170>Unable to invite a new alliance member.</td><td width=100 align=center>", sdf.format(expiryTime), "</td></tr>");
                    } else if (penaltyType == 4) {
                        StringUtil.append(sb, "<tr><td width=170>Unable to create an alliance.</td><td width=100 align=center>", sdf.format(expiryTime), "</td></tr>");
                    }
            }
            if (clan.getDissolvingExpiryTime() > currentTime)
                StringUtil.append(sb, "<tr><td width=170>The request to dissolve the clan is currently being processed.  (Restrictions are now going to be imposed on the use of clan functions.)</td><td width=100 align=center>", sdf.format(clan.getDissolvingExpiryTime()), "</td></tr>");
            boolean registeredOnAnySiege = false;
            for (Castle castle : CastleManager.getInstance().getCastles()) {
                if (castle.getSiege().checkSides(clan)) {
                    registeredOnAnySiege = true;
                    break;
                }
            }
            if (clan.getAllyId() != 0 || clan.isAtWar() || clan.hasCastle() || clan.hasClanHall() || registeredOnAnySiege)
                StringUtil.append(sb, "<tr><td width=170>Unable to dissolve a clan.</td><td></td></tr>");
        }
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setFile("data/html/clan_penalty.htm");
        html.replace("%content%", (sb.isEmpty()) ? "<tr><td width=170>No penalty is imposed.</td><td width=100 align=center></td></tr>" : sb.toString());
        activeChar.sendPacket(html);
    }

    public int[] getUserCommandList() {
        return COMMAND_IDS;
    }
}
