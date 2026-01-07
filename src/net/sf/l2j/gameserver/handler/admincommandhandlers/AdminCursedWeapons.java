package net.sf.l2j.gameserver.handler.admincommandhandlers;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.gameserver.data.manager.CursedWeaponManager;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.entity.CursedWeapon;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

import java.util.StringTokenizer;

public class AdminCursedWeapons implements IAdminCommandHandler {
    private static final String[] ADMIN_COMMANDS = new String[]{"admin_cw_info", "admin_cw_remove", "admin_cw_goto", "admin_cw_add", "admin_cw_info_menu"};

    public boolean useAdminCommand(String command, Player activeChar) {
        StringTokenizer st = new StringTokenizer(command);
        st.nextToken();
        if (command.startsWith("admin_cw_info")) {
            if (!command.contains("menu")) {
                activeChar.sendMessage("====== Cursed Weapons: ======");
                for (CursedWeapon cw : CursedWeaponManager.getInstance().getCursedWeapons()) {
                    activeChar.sendMessage(cw.getName() + " (" + cw.getName() + ")");
                    if (cw.isActive()) {
                        long milliToStart = cw.getTimeLeft();
                        double numSecs = (milliToStart / 1000L % 60L);
                        double countDown = ((milliToStart / 1000L) - numSecs) / 60.0D;
                        int numMins = (int) Math.floor(countDown % 60.0D);
                        countDown = (countDown - numMins) / 60.0D;
                        int numHours = (int) Math.floor(countDown % 24.0D);
                        int numDays = (int) Math.floor((countDown - numHours) / 24.0D);
                        if (cw.isActivated()) {
                            Player pl = cw.getPlayer();
                            activeChar.sendMessage("  Owner: " + ((pl == null) ? "null" : pl.getName()));
                            activeChar.sendMessage("  Stored values: karma=" + cw.getPlayerKarma() + " PKs=" + cw.getPlayerPkKills());
                            activeChar.sendMessage("  Current stage:" + cw.getCurrentStage());
                            activeChar.sendMessage("  Overall time: " + numDays + " days " + numHours + " hours " + numMins + " min.");
                            activeChar.sendMessage("  Hungry time: " + cw.getHungryTime() + " min.");
                            activeChar.sendMessage("  Current kills : " + cw.getNbKills() + " / " + cw.getNumberBeforeNextStage());
                        } else if (cw.isDropped()) {
                            activeChar.sendMessage("  Lying on the ground.");
                            activeChar.sendMessage("  Time remaining: " + numDays + " days " + numHours + " hours " + numMins + " min.");
                        }
                    } else {
                        activeChar.sendMessage("  Doesn't exist in the world.");
                    }
                    activeChar.sendPacket(SystemMessageId.FRIEND_LIST_FOOTER);
                }
            } else {
                StringBuilder sb = new StringBuilder(2000);
                for (CursedWeapon cw : CursedWeaponManager.getInstance().getCursedWeapons()) {
                    StringUtil.append(sb, "<table width=280><tr><td>Name:</td><td>", cw.getName(), "</td></tr>");
                    if (cw.isActive()) {
                        long milliToStart = cw.getTimeLeft();
                        double numSecs = (milliToStart / 1000L % 60L);
                        double countDown = ((milliToStart / 1000L) - numSecs) / 60.0D;
                        int numMins = (int) Math.floor(countDown % 60.0D);
                        countDown = (countDown - numMins) / 60.0D;
                        int numHours = (int) Math.floor(countDown % 24.0D);
                        int numDays = (int) Math.floor((countDown - numHours) / 24.0D);
                        if (cw.isActivated()) {
                            Player pl = cw.getPlayer();
                            StringUtil.append(sb, "<tr><td>Owner:</td><td>", (pl == null) ? "null" : pl.getName(), "</td></tr><tr><td>Stored values:</td><td>Karma=", Integer.valueOf(cw.getPlayerKarma()), " PKs=", Integer.valueOf(cw.getPlayerPkKills()), "</td></tr><tr><td>Current stage:</td><td>", Integer.valueOf(cw.getCurrentStage()), "</td></tr><tr><td>Overall time:</td><td>", Integer.valueOf(numDays),
                                    "d. ", Integer.valueOf(numHours), "h. ", Integer.valueOf(numMins), "m.</td></tr><tr><td>Hungry time:</td><td>", Integer.valueOf(cw.getHungryTime()), "m.</td></tr><tr><td>Current kills:</td><td>", Integer.valueOf(cw.getNbKills()), " / ", Integer.valueOf(cw.getNumberBeforeNextStage()),
                                    "</td></tr><tr><td><button value=\"Remove\" action=\"bypass -h admin_cw_remove ", Integer.valueOf(cw.getItemId()), "\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></td><td><button value=\"Go\" action=\"bypass -h admin_cw_goto ", Integer.valueOf(cw.getItemId()), "\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></td></tr>");
                        } else if (cw.isDropped()) {
                            StringUtil.append(sb, "<tr><td>Position:</td><td>Lying on the ground</td></tr><tr><td>Overall time:</td><td>", Integer.valueOf(numDays), "d. ", Integer.valueOf(numHours), "h. ", Integer.valueOf(numMins), "m.</td></tr><tr><td><button value=\"Remove\" action=\"bypass -h admin_cw_remove ", Integer.valueOf(cw.getItemId()), "\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></td><td><button value=\"Go\" action=\"bypass -h admin_cw_goto ", Integer.valueOf(cw.getItemId()),
                                    "\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></td></tr>");
                        }
                    } else {
                        StringUtil.append(sb, "<tr><td>Position:</td><td>Doesn't exist.</td></tr><tr><td><button value=\"Give to Target\" action=\"bypass -h admin_cw_add ", Integer.valueOf(cw.getItemId()), "\" width=75 height=21 back=\"L2UI_ch3.Btn1_normalOn\" fore=\"L2UI_ch3.Btn1_normal\"></td><td></td></tr>");
                    }
                    sb.append("</table><br>");
                }
                NpcHtmlMessage html = new NpcHtmlMessage(0);
                html.setFile("data/html/admin/cwinfo.htm");
                html.replace("%cwinfo%", sb.toString());
                activeChar.sendPacket(html);
            }
        } else {
            try {
                int id = 0;
                String parameter = st.nextToken();
                if (parameter.matches("[0-9]*")) {
                    id = Integer.parseInt(parameter);
                } else {
                    parameter = parameter.replace('_', ' ');
                    for (CursedWeapon cwp : CursedWeaponManager.getInstance().getCursedWeapons()) {
                        if (cwp.getName().toLowerCase().contains(parameter.toLowerCase())) {
                            id = cwp.getItemId();
                            break;
                        }
                    }
                }
                CursedWeapon cw = CursedWeaponManager.getInstance().getCursedWeapon(id);
                if (cw == null) {
                    activeChar.sendMessage("Unknown cursed weapon ID.");
                    return false;
                }
                if (command.startsWith("admin_cw_remove ")) {
                    cw.endOfLife();
                } else if (command.startsWith("admin_cw_goto ")) {
                    cw.goTo(activeChar);
                } else if (command.startsWith("admin_cw_add")) {
                    if (cw.isActive()) {
                        activeChar.sendMessage("This cursed weapon is already active.");
                    } else {
                        WorldObject target = activeChar.getTarget();
                        if (target instanceof Player) {
                            ((Player) target).addItem("AdminCursedWeaponAdd", id, 1, target, true);
                        } else {
                            activeChar.addItem("AdminCursedWeaponAdd", id, 1, activeChar, true);
                        }
                        cw.reActivate(true);
                    }
                } else {
                    activeChar.sendMessage("Unknown command.");
                }
            } catch (Exception e) {
                activeChar.sendMessage("Usage: //cw_remove|//cw_goto|//cw_add <itemid|name>");
            }
        }
        return true;
    }

    public String[] getAdminCommandList() {
        return ADMIN_COMMANDS;
    }
}
