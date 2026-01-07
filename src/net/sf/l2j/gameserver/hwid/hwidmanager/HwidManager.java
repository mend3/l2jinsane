package net.sf.l2j.gameserver.hwid.hwidmanager;

import net.sf.l2j.gameserver.hwid.HwidConfig;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.GameClient;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HwidManager {
    private static final Logger _log = Logger.getLogger(HwidManager.class.getName());

    private static boolean multiboxKickTask(Player activeChar, Integer numberBox, Collection<Player> world) {
        Map<String, List<Player>> hwidMap = new HashMap<>();
        for (Player player : world) {
            if (player.getClient() != null) {
                if (player.getClient().isDetached())
                    continue;
                String hwid = activeChar.getHWID();
                String playerHwid = player.getHWID();
                if (!hwid.equals(playerHwid))
                    continue;
                hwidMap.computeIfAbsent(hwid, k -> new ArrayList<>());
                hwidMap.get(hwid).add(player);
                if (hwidMap.get(hwid).size() >= numberBox)
                    return true;
            }
        }
        return false;
    }

    private static String ExcedLimit(Player player) {
        String tb = "<html><body><center>" +
                "<img src=\"L2UI_CH3.herotower_deco\" width=256 height=32>HWID<font color=LEVEL> Dual Box </font>'- Manager" +
                "<br><table><tr><td height=7><img src=\"L2UI.SquareGray\" width=220 height=1></td></tr></table>" +
                "<img src=\"L2UI.SquareGray\" width=295 height=1><table width=295 border=0 bgcolor=000000><tr><td align=center>" +
                "<br>You have exceeded the PC connection limit.<br1>Server have limit to <font color=LEVEL>" + HwidConfig.PROTECT_WINDOWS_COUNT + "</font> per PC.<br><br>You will be disconnected in '<font color=LEVEL>30 seconds</font>'.<br1>" + player.getName() + ", Thanks for following the server rules.<br1>Thanks.<br>" +
                "<br><img src=\"l2ui.squarewhite\" width=\"150\" height=\"1\"><br>" +
                "<br></td></tr></table><img src=\"L2UI.SquareGray\" width=295 height=1>" +
                "<table><tr><td height=7><img src=\"L2UI.SquareGray\" width=220 height=1></td></tr></table><br>" +
                "<br><br><font color=333333>Respect the rules</font>";
        return tb;
    }

    public static void waitSecs(int i) {
        try {
            Thread.sleep((i * 1000L));
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
    }

    public static HwidManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public boolean validBox(Player activeChar, Integer numberBox, Collection<Player> world, Boolean forcedLogOut) {
        if (multiboxKickTask(activeChar, numberBox, world)) {
            if (forcedLogOut) {
                GameClient client = activeChar.getClient();
                _log.log(Level.WARNING, "Dualbox Protection: " + client.getHWID() + " was trying to use over " + numberBox + " clients!");
                if (numberBox < 0 || numberBox == 0) {
                    activeChar.sendMessage("SYS: You have exceeded the PC connection limit = unlimited box per PC.");
                } else {
                    activeChar.sendMessage("SYS: You have exceeded the PC connection limit = " + HwidConfig.PROTECT_WINDOWS_COUNT + " box per PC.");
                }
                activeChar.sendMessage("SYS: You will be disconnected in 30 seconds.");
                activeChar.setIsImmobilized(true);
                activeChar.setIsInvul(true);
                activeChar.disableAllSkills();
                showChatWindow(activeChar, 0);
                waitSecs(30);
                client.closeNow();
            }
            return true;
        }
        return false;
    }

    public void showChatWindow(Player player, int val) {
        NpcHtmlMessage msg = new NpcHtmlMessage(5);
        msg.setHtml(ExcedLimit(player));
        player.sendPacket(msg);
    }

    private static class SingletonHolder {
        protected static final HwidManager INSTANCE = new HwidManager();
    }
}
