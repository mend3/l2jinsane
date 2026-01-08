package net.sf.l2j.gameserver.handler.usercommandhandlers;

import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.handler.IUserCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ClanWarsList implements IUserCommandHandler {
    private static final String SELECT_ATTACK_LIST = "SELECT clan_name,clan_id,ally_id,ally_name FROM clan_data,clan_wars WHERE clan1=? AND clan_id=clan2 AND clan2 NOT IN (SELECT clan1 FROM clan_wars WHERE clan2=?)";

    private static final String SELECT_UNDER_ATTACK_LIST = "SELECT clan_name,clan_id,ally_id,ally_name FROM clan_data,clan_wars WHERE clan2=? AND clan_id=clan1 AND clan1 NOT IN (SELECT clan2 FROM clan_wars WHERE clan1=?)";

    private static final String SELECT_WAR_LIST = "SELECT clan_name,clan_id,ally_id,ally_name FROM clan_data,clan_wars WHERE clan1=? AND clan_id=clan2 AND clan2 IN (SELECT clan1 FROM clan_wars WHERE clan2=?)";

    private static final int[] COMMAND_IDS = new int[]{88, 89, 90};

    public void useUserCommand(int id, Player player) {
        Clan clan = player.getClan();
        if (clan == null) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
            return;
        }
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement((id == 88) ? "SELECT clan_name,clan_id,ally_id,ally_name FROM clan_data,clan_wars WHERE clan1=? AND clan_id=clan2 AND clan2 NOT IN (SELECT clan1 FROM clan_wars WHERE clan2=?)" : ((id == 89) ? "SELECT clan_name,clan_id,ally_id,ally_name FROM clan_data,clan_wars WHERE clan2=? AND clan_id=clan1 AND clan1 NOT IN (SELECT clan2 FROM clan_wars WHERE clan1=?)" : "SELECT clan_name,clan_id,ally_id,ally_name FROM clan_data,clan_wars WHERE clan1=? AND clan_id=clan2 AND clan2 IN (SELECT clan1 FROM clan_wars WHERE clan2=?)"));
                try {
                    ps.setInt(1, clan.getClanId());
                    ps.setInt(2, clan.getClanId());
                    ResultSet rs = ps.executeQuery();
                    try {
                        if (rs.first()) {
                            if (id == 88) {
                                player.sendPacket(SystemMessageId.CLANS_YOU_DECLARED_WAR_ON);
                            } else if (id == 89) {
                                player.sendPacket(SystemMessageId.CLANS_THAT_HAVE_DECLARED_WAR_ON_YOU);
                            } else {
                                player.sendPacket(SystemMessageId.WAR_LIST);
                            }
                            while (rs.next()) {
                                SystemMessage sm;
                                String clanName = rs.getString("clan_name");
                                if (rs.getInt("ally_id") > 0) {
                                    sm = SystemMessage.getSystemMessage(SystemMessageId.S1_S2_ALLIANCE).addString(clanName).addString(rs.getString("ally_name"));
                                } else {
                                    sm = SystemMessage.getSystemMessage(SystemMessageId.S1_NO_ALLI_EXISTS).addString(clanName);
                                }
                                player.sendPacket(sm);
                            }
                            player.sendPacket(SystemMessageId.FRIEND_LIST_FOOTER);
                        } else if (id == 88) {
                            player.sendPacket(SystemMessageId.YOU_ARENT_IN_CLAN_WARS);
                        } else if (id == 89) {
                            player.sendPacket(SystemMessageId.NO_CLAN_WARS_VS_YOU);
                        } else if (id == 90) {
                            player.sendPacket(SystemMessageId.NOT_INVOLVED_IN_WAR);
                        }
                        if (rs != null)
                            rs.close();
                    } catch (Throwable throwable) {
                        if (rs != null)
                            try {
                                rs.close();
                            } catch (Throwable throwable1) {
                                throwable.addSuppressed(throwable1);
                            }
                        throw throwable;
                    }
                    if (ps != null)
                        ps.close();
                } catch (Throwable throwable) {
                    if (ps != null)
                        try {
                            ps.close();
                        } catch (Throwable throwable1) {
                            throwable.addSuppressed(throwable1);
                        }
                    throw throwable;
                }
                if (con != null)
                    con.close();
            } catch (Throwable throwable) {
                if (con != null)
                    try {
                        con.close();
                    } catch (Throwable throwable1) {
                        throwable.addSuppressed(throwable1);
                    }
                throw throwable;
            }
        } catch (Exception ignored) {
        }
    }

    public int[] getUserCommandList() {
        return COMMAND_IDS;
    }
}
