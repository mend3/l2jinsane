package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.AdminForgePacket;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.util.StringTokenizer;

public class AdminPForge implements IAdminCommandHandler {
    private static final String[] ADMIN_COMMANDS = new String[]{"admin_forge", "admin_forge2", "admin_forge3", "admin_msg"};

    private static void showMainPage(Player activeChar) {
        AdminHelpPage.showHelpPage(activeChar, "pforge1.htm");
    }

    private static void showPage2(Player activeChar, String format) {
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setFile("data/html/admin/pforge2.htm");
        html.replace("%format%", format);
        StringBuilder sb = new StringBuilder();
        int i;
        for (i = 0; i < format.length(); i++) {
            StringUtil.append(sb, Character.valueOf(format.charAt(i)), " : <edit var=\"v", Integer.valueOf(i), "\" width=100><br1>");
        }
        html.replace("%valueditors%", sb.toString());
        sb.setLength(0);
        for (i = 0; i < format.length(); i++) {
            StringUtil.append(sb, " \\$v", Integer.valueOf(i));
        }
        html.basicReplace("%send%", sb.toString());
        activeChar.sendPacket(html);
    }

    private static void showPage3(Player activeChar, String format, String command) {
        NpcHtmlMessage html = new NpcHtmlMessage(0);
        html.setFile("data/html/admin/pforge3.htm");
        html.replace("%format%", format);
        html.replace("%command%", command);
        activeChar.sendPacket(html);
    }

    public boolean useAdminCommand(String command, Player activeChar) {
        if (command.equals("admin_forge")) {
            showMainPage(activeChar);
        } else if (command.startsWith("admin_forge2")) {
            try {
                StringTokenizer st = new StringTokenizer(command);
                st.nextToken();
                String format = st.nextToken();
                showPage2(activeChar, format);
            } catch (Exception ex) {
                activeChar.sendMessage("Usage: //forge2 format");
            }
        } else if (command.startsWith("admin_forge3")) {
            try {
                StringTokenizer st = new StringTokenizer(command);
                st.nextToken();
                String format = st.nextToken();
                boolean broadcast = false;
                if (format.equalsIgnoreCase("broadcast")) {
                    format = st.nextToken();
                    broadcast = true;
                }
                AdminForgePacket sp = new AdminForgePacket();
                for (int i = 0; i < format.length(); i++) {
                    String val = st.nextToken();
                    if (val.equalsIgnoreCase("$objid")) {
                        val = String.valueOf(activeChar.getObjectId());
                    } else if (val.equalsIgnoreCase("$tobjid")) {
                        val = String.valueOf(activeChar.getTarget().getObjectId());
                    } else if (val.equalsIgnoreCase("$bobjid")) {
                        if (activeChar.getBoat() != null)
                            val = String.valueOf(activeChar.getBoat().getObjectId());
                    } else if (val.equalsIgnoreCase("$clanid")) {
                        val = String.valueOf(activeChar.getObjectId());
                    } else if (val.equalsIgnoreCase("$allyid")) {
                        val = String.valueOf(activeChar.getAllyId());
                    } else if (val.equalsIgnoreCase("$tclanid")) {
                        val = String.valueOf(activeChar.getTarget().getObjectId());
                    } else if (val.equalsIgnoreCase("$tallyid")) {
                        val = String.valueOf(((Player) activeChar.getTarget()).getAllyId());
                    } else if (val.equalsIgnoreCase("$x")) {
                        val = String.valueOf(activeChar.getX());
                    } else if (val.equalsIgnoreCase("$y")) {
                        val = String.valueOf(activeChar.getY());
                    } else if (val.equalsIgnoreCase("$z")) {
                        val = String.valueOf(activeChar.getZ());
                    } else if (val.equalsIgnoreCase("$heading")) {
                        val = String.valueOf(activeChar.getHeading());
                    } else if (val.equalsIgnoreCase("$tx")) {
                        val = String.valueOf(activeChar.getTarget().getX());
                    } else if (val.equalsIgnoreCase("$ty")) {
                        val = String.valueOf(activeChar.getTarget().getY());
                    } else if (val.equalsIgnoreCase("$tz")) {
                        val = String.valueOf(activeChar.getTarget().getZ());
                    } else if (val.equalsIgnoreCase("$theading")) {
                        val = String.valueOf(activeChar.getTarget().getHeading());
                    }
                    sp.addPart(format.getBytes()[i], val);
                }
                if (broadcast) {
                    activeChar.broadcastPacket(sp);
                } else {
                    activeChar.sendPacket(sp);
                }
                showPage3(activeChar, format, command);
            } catch (Exception ex) {
                activeChar.sendMessage("Usage: //forge or //forge2 format");
            }
        } else if (command.startsWith("admin_msg")) {
            try {
                activeChar.sendPacket(SystemMessage.getSystemMessage(Integer.parseInt(command.substring(10).trim())));
            } catch (Exception e) {
                activeChar.sendMessage("Command format: //msg <SYSTEM_MSG_ID>");
                return false;
            }
        }
        return true;
    }

    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
}
