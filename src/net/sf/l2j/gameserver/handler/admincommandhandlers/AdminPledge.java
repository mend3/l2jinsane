package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.GMViewPledgeInfo;

import java.util.StringTokenizer;

public class AdminPledge implements IAdminCommandHandler {
    private static final String[] ADMIN_COMMANDS = new String[]{"admin_pledge"};

    private static void showMainPage(Player activeChar) {
        AdminHelpPage.showHelpPage(activeChar, "game_menu.htm");
    }

    public void useAdminCommand(String command, Player activeChar) {
        WorldObject target = activeChar.getTarget();
        if (!(target instanceof Player player)) {
            activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
            showMainPage(activeChar);
            return;
        }
        if (command.startsWith("admin_pledge")) {
            StringTokenizer st = new StringTokenizer(command, " ");
            try {
                st.nextToken();
                String action = st.nextToken();
                if (action.equals("create")) {
                    try {
                        String parameter = st.nextToken();
                        long cet = player.getClanCreateExpiryTime();
                        player.setClanCreateExpiryTime(0L);
                        Clan clan = ClanTable.getInstance().createClan(player, parameter);
                        if (clan != null) {
                            activeChar.sendMessage("Clan " + parameter + " have been created. Clan leader is " + player.getName() + ".");
                        } else {
                            player.setClanCreateExpiryTime(cet);
                            activeChar.sendMessage("There was a problem while creating the clan.");
                        }
                    } catch (Exception e) {
                        activeChar.sendMessage("Invalid string parameter for //pledge create.");
                    }
                } else {
                    if (player.getClan() == null) {
                        activeChar.sendPacket(SystemMessageId.TARGET_MUST_BE_IN_CLAN);
                        showMainPage(activeChar);
                        return;
                    }
                    if (action.equals("dismiss")) {
                        ClanTable.getInstance().destroyClan(player.getClan());
                        if (player.getClan() == null) {
                            activeChar.sendMessage("The clan is now disbanded.");
                        } else {
                            activeChar.sendMessage("There was a problem while destroying the clan.");
                        }
                    } else if (action.equals("info")) {
                        activeChar.sendPacket(new GMViewPledgeInfo(player.getClan(), player));
                    } else if (action.equals("setlevel")) {
                        try {
                            int level = Integer.parseInt(st.nextToken());
                            if (level >= 0 && level < 9) {
                                player.getClan().changeLevel(level);
                                activeChar.sendMessage("You have set clan " + player.getClan().getName() + " to level " + level);
                            } else {
                                activeChar.sendMessage("This clan level is incorrect. Put a number between 0 and 8.");
                            }
                        } catch (Exception e) {
                            activeChar.sendMessage("Invalid number parameter for //pledge setlevel.");
                        }
                    } else if (action.startsWith("rep")) {
                        try {
                            int points = Integer.parseInt(st.nextToken());
                            Clan clan = player.getClan();
                            if (clan.getLevel() < 5) {
                                activeChar.sendMessage("Only clans of level 5 or above may receive reputation points.");
                                showMainPage(activeChar);
                                return;
                            }
                            clan.addReputationScore(points);
                            activeChar.sendMessage("You " + ((points > 0) ? "added " : "removed ") + Math.abs(points) + " points " + ((points > 0) ? "to " : "from ") + clan.getName() + "'s reputation. Their current score is: " + clan.getReputationScore());
                        } catch (Exception e) {
                            activeChar.sendMessage("Invalid number parameter for //pledge rep.");
                        }
                    }
                }
            } catch (Exception e) {
                activeChar.sendMessage("Invalid action or parameter.");
            }
        }
        showMainPage(activeChar);
    }

    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
}
