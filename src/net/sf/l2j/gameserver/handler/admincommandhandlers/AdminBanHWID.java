package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.hwid.hwidmanager.HwidBan;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.StringTokenizer;
import java.util.logging.Logger;

public class AdminBanHWID implements IAdminCommandHandler {
    protected static final Logger _log = Logger.getLogger(AdminBanHWID.class.getName());
    static String INSERT_DATA = "REPLACE INTO banned_hwid (char_name, hwid) VALUES (?,?)";
    private static final String[] _adminCommands = new String[]{"admin_ban_ip"};

    public static void updateDatabase(Player player) {
        if (player == null)
            return;
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement stmt = con.prepareStatement(INSERT_DATA);
                try {
                    stmt.setString(1, player.getName());
                    stmt.setString(2, player.getHWID());
                    stmt.execute();
                    if (stmt != null)
                        stmt.close();
                } catch (Throwable throwable) {
                    if (stmt != null)
                        try {
                            stmt.close();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean useAdminCommand(String command, Player activeChar) {
        StringTokenizer st = new StringTokenizer(command);
        st.nextToken();
        String player = "";
        Player targetPlayer = null;
        if (st.hasMoreTokens()) {
            player = st.nextToken();
            targetPlayer = World.getInstance().getPlayer(player);
        } else if (activeChar.getTarget() != null && activeChar.getTarget() instanceof Player) {
            targetPlayer = (Player) activeChar.getTarget();
        }
        if (targetPlayer != null && targetPlayer.equals(activeChar)) {
            activeChar.sendPacket(SystemMessageId.CANNOT_USE_ON_YOURSELF);
            return false;
        }
        if (command.startsWith("admin_ban_ip") && targetPlayer != null) {
            String hwid = targetPlayer.getHWID();
            if (hwid != null) {
                updateDatabase(targetPlayer);
                HwidBan.addHWIDBan(targetPlayer.getClient());
                for (Player p : World.getInstance().getPlayers()) {
                    String hwidz = p.getHWID();
                    if (p.isOnline())
                        if (hwidz.equals(targetPlayer.getHWID()))
                            p.logout(true);
                }
                activeChar.sendMessage("HWID : " + hwid + " Banned");
            }
        }
        return false;
    }

    public String[] getAdminCommandList() {
        return _adminCommands;
    }
}
