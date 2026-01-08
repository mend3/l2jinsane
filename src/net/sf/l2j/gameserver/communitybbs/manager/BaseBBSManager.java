package net.sf.l2j.gameserver.communitybbs.manager;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.serverpackets.ShowBoard;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseBBSManager {
    protected static final CLogger LOGGER = new CLogger(BaseBBSManager.class.getName());

    protected static final String CB_PATH = "data/html/CommunityBoard/";

    public static void separateAndSend(String html, Player player) {
        if (html == null || player == null)
            return;
        if (html.length() < 4090) {
            player.sendPacket(new ShowBoard(html, "101"));
            player.sendPacket(ShowBoard.STATIC_SHOWBOARD_102);
            player.sendPacket(ShowBoard.STATIC_SHOWBOARD_103);
        } else if (html.length() < 8180) {
            player.sendPacket(new ShowBoard(html.substring(0, 4090), "101"));
            player.sendPacket(new ShowBoard(html.substring(4090), "102"));
            player.sendPacket(ShowBoard.STATIC_SHOWBOARD_103);
        } else if (html.length() < 12270) {
            player.sendPacket(new ShowBoard(html.substring(0, 4090), "101"));
            player.sendPacket(new ShowBoard(html.substring(4090, 8180), "102"));
            player.sendPacket(new ShowBoard(html.substring(8180), "103"));
        }
    }

    protected static void send1001(String html, Player player) {
        if (html.length() < 8180)
            player.sendPacket(new ShowBoard(html, "1001"));
    }

    protected static void send1002(Player player) {
        send1002(player, " ", " ", "0");
    }

    protected static void send1002(Player player, String string, String string2, String string3) {
        List<String> params = new ArrayList<>();
        params.add("0");
        params.add("0");
        params.add("0");
        params.add("0");
        params.add("0");
        params.add("0");
        params.add(player.getName());
        params.add(Integer.toString(player.getObjectId()));
        params.add(player.getAccountName());
        params.add("9");
        params.add(string2);
        params.add(string2);
        params.add(string);
        params.add(string3);
        params.add(string3);
        params.add("0");
        params.add("0");
        player.sendPacket(new ShowBoard(params));
    }

    public void parseCmd(String command, Player player) {
        separateAndSend("<html><body><br><br><center>The command: " + command + " isn't implemented.</center></body></html>", player);
    }

    public void parseWrite(String ar1, String ar2, String ar3, String ar4, String ar5, Player player) {
        separateAndSend("<html><body><br><br><center>The command: " + ar1 + " isn't implemented.</center></body></html>", player);
    }

    protected void loadStaticHtm(String file, Player player) {
        separateAndSend(HtmCache.getInstance().getHtm("data/html/CommunityBoard/" + getFolder() + file), player);
    }

    protected String getFolder() {
        return "";
    }
}
