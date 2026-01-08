package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.LoginServerThread;
import net.sf.l2j.gameserver.enums.PunishmentType;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdminBan implements IAdminCommandHandler {
    private static final Logger LOG = Logger.getLogger(AdminBan.class.getName());

    private static final String UPDATE_BAN = "UPDATE characters SET punish_level=?, punish_timer=? WHERE char_name=?";

    private static final String UPDATE_JAIL = "UPDATE characters SET x=-114356, y=-249645, z=-2984, punish_level=?, punish_timer=? WHERE char_name=?";

    private static final String UPDATE_UNJAIL = "UPDATE characters SET x=17836, y=170178, z=-3507, punish_level=0, punish_timer=0 WHERE char_name=?";

    private static final String UPDATE_ACCESS = "UPDATE characters SET accesslevel=? WHERE char_name=?";

    private static final String[] ADMIN_COMMANDS = new String[]{"admin_ban", "admin_ban_acc", "admin_ban_char", "admin_ban_chat", "admin_unban", "admin_unban_acc", "admin_unban_char", "admin_unban_chat", "admin_jail", "admin_unjail"};

    private static void banChatOfflinePlayer(Player activeChar, String name, int delay, boolean ban) {
        PunishmentType punishement;
        long value = 0L;
        if (ban) {
            punishement = PunishmentType.CHAT;
            value = (delay > 0) ? (delay * 60000L) : 60000L;
        } else {
            punishement = PunishmentType.NONE;
        }
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("UPDATE characters SET punish_level=?, punish_timer=? WHERE char_name=?");
                try {
                    ps.setInt(1, punishement.ordinal());
                    ps.setLong(2, value);
                    ps.setString(3, name);
                    ps.execute();
                    int count = ps.getUpdateCount();
                    if (count == 0) {
                        activeChar.sendMessage("Character isn't found.");
                    } else if (ban) {
                        activeChar.sendMessage(name + " is chat banned for " + name);
                    } else {
                        activeChar.sendMessage(name + "'s chat ban has been lifted.");
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
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "AdminBan.banChatOfflinePlayer :" + e.getMessage(), e);
        }
    }

    private static void jailOfflinePlayer(Player activeChar, String name, int delay) {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("UPDATE characters SET x=-114356, y=-249645, z=-2984, punish_level=?, punish_timer=? WHERE char_name=?");
                try {
                    ps.setInt(1, PunishmentType.JAIL.ordinal());
                    ps.setLong(2, (delay > 0) ? (delay * 60000L) : 0L);
                    ps.setString(3, name);
                    ps.execute();
                    int count = ps.getUpdateCount();
                    if (count == 0) {
                        activeChar.sendMessage("Character not found!");
                    } else {
                        activeChar.sendMessage(name + " has been jailed for " + name);
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
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "AdminBan.jailOfflinePlayer :" + e.getMessage(), e);
        }
    }

    private static void unjailOfflinePlayer(Player activeChar, String name) {
        try {
            Connection con = ConnectionPool.getConnection();
            try {
                PreparedStatement ps = con.prepareStatement("UPDATE characters SET x=17836, y=170178, z=-3507, punish_level=0, punish_timer=0 WHERE char_name=?");
                try {
                    ps.setString(1, name);
                    ps.execute();
                    int count = ps.getUpdateCount();
                    if (count == 0) {
                        activeChar.sendMessage("Character isn't found.");
                    } else {
                        activeChar.sendMessage(name + " has been unjailed.");
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
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "AdminBan.unjailOfflinePlayer :" + e.getMessage(), e);
        }
    }

    private static void changeCharAccessLevel(Player targetPlayer, String player, Player activeChar, int lvl) {
        if (targetPlayer != null) {
            targetPlayer.setAccessLevel(lvl);
            targetPlayer.logout(false);
            activeChar.sendMessage(targetPlayer.getName() + " has been banned.");
        } else {
            try {
                Connection con = ConnectionPool.getConnection();
                try {
                    PreparedStatement ps = con.prepareStatement("UPDATE characters SET accesslevel=? WHERE char_name=?");
                    try {
                        ps.setInt(1, lvl);
                        ps.setString(2, player);
                        ps.execute();
                        int count = ps.getUpdateCount();
                        if (count == 0) {
                            activeChar.sendMessage("Character not found or access level unaltered.");
                            boolean bool = false;
                            if (ps != null)
                                ps.close();
                            if (con != null)
                                con.close();
                            return;
                        }
                        activeChar.sendMessage(player + " now has an access level of " + player + ".");
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
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "AdminBan.changeCharAccessLevel :" + e.getMessage(), e);
            }
        }
    }

    public void useAdminCommand(String command, Player activeChar) {
        StringTokenizer st = new StringTokenizer(command);
        st.nextToken();
        String player = "";
        int duration = -1;
        Player targetPlayer = null;
        if (st.hasMoreTokens()) {
            player = st.nextToken();
            targetPlayer = World.getInstance().getPlayer(player);
            if (st.hasMoreTokens())
                try {
                    duration = Integer.parseInt(st.nextToken());
                } catch (NumberFormatException nfe) {
                    activeChar.sendMessage("Invalid number format used: " + nfe);
                    return;
                }
        } else if (activeChar.getTarget() != null && activeChar.getTarget() instanceof Player) {
            targetPlayer = (Player) activeChar.getTarget();
        }
        if (targetPlayer != null && targetPlayer.equals(activeChar)) {
            activeChar.sendPacket(SystemMessageId.CANNOT_USE_ON_YOURSELF);
            return;
        }
        if (command.startsWith("admin_ban ") || command.equalsIgnoreCase("admin_ban")) {
            activeChar.sendMessage("Available ban commands: //ban_acc, //ban_char, //ban_chat");
            return;
        }
        if (command.startsWith("admin_ban_acc")) {
            if (targetPlayer == null && player.isEmpty()) {
                activeChar.sendMessage("Usage: //ban_acc <account_name> (if none, target char's account gets banned).");
                return;
            }
            if (targetPlayer == null) {
                LoginServerThread.getInstance().sendAccessLevel(player, -100);
                activeChar.sendMessage("Ban request sent for account " + player + ".");
            } else {
                targetPlayer.getPunishment().setType(PunishmentType.ACC, 0);
                activeChar.sendMessage(targetPlayer.getAccountName() + " account is now banned.");
            }
        } else {
            if (command.startsWith("admin_ban_char")) {
                if (targetPlayer == null && player.isEmpty()) {
                    activeChar.sendMessage("Usage: //ban_char <char_name> (if none, target char is banned)");
                    return;
                }
                changeCharAccessLevel(targetPlayer, player, activeChar, -1);
                return;
            }
            if (command.startsWith("admin_ban_chat")) {
                if (targetPlayer == null && player.isEmpty()) {
                    activeChar.sendMessage("Usage: //ban_chat <char_name> [penalty_minutes]");
                    return;
                }
                if (targetPlayer != null) {
                    if (targetPlayer.getPunishment().getType() != PunishmentType.NONE) {
                        activeChar.sendMessage(targetPlayer.getName() + " is already jailed or banned.");
                        return;
                    }
                    String banLengthStr = "";
                    targetPlayer.getPunishment().setType(PunishmentType.CHAT, duration);
                    if (duration > 0)
                        banLengthStr = " for " + duration + " minutes";
                    activeChar.sendMessage(targetPlayer.getName() + " is now chat banned" + targetPlayer.getName() + ".");
                } else {
                    banChatOfflinePlayer(activeChar, player, duration, true);
                }
            } else {
                if (command.startsWith("admin_unban ") || command.equalsIgnoreCase("admin_unban")) {
                    activeChar.sendMessage("Available unban commands: //unban_acc, //unban_char, //unban_chat");
                    return;
                }
                if (command.startsWith("admin_unban_acc")) {
                    if (targetPlayer != null) {
                        activeChar.sendMessage(targetPlayer.getName() + " is currently online so mustn't be banned.");
                        return;
                    }
                    if (!player.isEmpty()) {
                        LoginServerThread.getInstance().sendAccessLevel(player, 0);
                        activeChar.sendMessage("Unban request sent for account " + player + ".");
                    } else {
                        activeChar.sendMessage("Usage: //unban_acc <account_name>");
                    }
                } else {
                    if (command.startsWith("admin_unban_char")) {
                        if (targetPlayer == null && player.isEmpty()) {
                            activeChar.sendMessage("Usage: //unban_char <char_name>");
                            return;
                        }
                        if (targetPlayer != null) {
                            activeChar.sendMessage(targetPlayer.getName() + " is currently online so mustn't be banned.");
                            return;
                        }
                        changeCharAccessLevel(null, player, activeChar, 0);
                        return;
                    }
                    if (command.startsWith("admin_unban_chat")) {
                        if (targetPlayer == null && player.isEmpty()) {
                            activeChar.sendMessage("Usage: //unban_chat <char_name>");
                            return;
                        }
                        if (targetPlayer != null) {
                            if (targetPlayer.isChatBanned()) {
                                targetPlayer.getPunishment().setType(PunishmentType.NONE, 0);
                                activeChar.sendMessage(targetPlayer.getName() + "'s chat ban has been lifted.");
                            } else {
                                activeChar.sendMessage(targetPlayer.getName() + " isn't currently chat banned.");
                            }
                        } else {
                            banChatOfflinePlayer(activeChar, player, 0, false);
                        }
                    } else if (command.startsWith("admin_jail")) {
                        if (targetPlayer == null && player.isEmpty()) {
                            activeChar.sendMessage("Usage: //jail <charname> [penalty_minutes] (if no name is given, selected target is jailed forever).");
                            return;
                        }
                        if (targetPlayer != null) {
                            targetPlayer.getPunishment().setType(PunishmentType.JAIL, duration);
                            activeChar.sendMessage(targetPlayer.getName() + " has been jailed for " + targetPlayer.getName());
                        } else {
                            jailOfflinePlayer(activeChar, player, duration);
                        }
                    } else if (command.startsWith("admin_unjail")) {
                        if (targetPlayer == null && player.isEmpty()) {
                            activeChar.sendMessage("Usage: //unjail <charname> (If no name is given target is used).");
                            return;
                        }
                        if (targetPlayer != null) {
                            targetPlayer.getPunishment().setType(PunishmentType.NONE, 0);
                            activeChar.sendMessage(targetPlayer.getName() + " has been unjailed.");
                        } else {
                            unjailOfflinePlayer(activeChar, player);
                        }
                    }
                }
            }
        }
    }

    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
}
