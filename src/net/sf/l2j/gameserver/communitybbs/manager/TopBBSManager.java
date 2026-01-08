package net.sf.l2j.gameserver.communitybbs.manager;

import net.sf.l2j.gameserver.communitybbs.custom.OpenPagBBSManager;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.data.xml.MultisellData;
import net.sf.l2j.gameserver.model.actor.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

public class TopBBSManager extends BaseBBSManager {
    public static TopBBSManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void parseCmd(String command, Player player) {
        if (command.equals("_bbshome")) {
            String html = HtmCache.getInstance().getHtm("data/html/CommunityBoard/" + getFolder() + "index.htm");
            html = html.replaceAll("%dat%", (new SimpleDateFormat("dd/MM/yyyy")).format(new Date(System.currentTimeMillis())));
            html = html.replaceAll("%time%", (new SimpleDateFormat("HH:mm:ss")).format(new Date(System.currentTimeMillis())));
            separateAndSend(html, player);
        } else if (command.startsWith("_bbshome;")) {
            StringTokenizer st = new StringTokenizer(command, ";");
            st.nextToken();
            loadStaticHtm(st.nextToken(), player);
        } else if (command.startsWith("_bbshomevip;")) {
            StringTokenizer st = new StringTokenizer(command, ";");
            st.nextToken();
            OpenPagBBSManager.getInstance().parseCmd("_bbspag;" + st.nextToken(), player);
            MultisellData.getInstance().separateAndSend(st.nextToken(), player, null, false);
        } else {
            super.parseCmd(command, player);
        }
    }

    protected String getFolder() {
        return "top/";
    }

    private static class SingletonHolder {
        protected static final TopBBSManager INSTANCE = new TopBBSManager();
    }
}
