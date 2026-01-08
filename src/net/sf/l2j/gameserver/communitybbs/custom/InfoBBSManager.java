package net.sf.l2j.gameserver.communitybbs.custom;

import net.sf.l2j.gameserver.communitybbs.manager.BaseBBSManager;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.model.actor.Player;

public class InfoBBSManager extends BaseBBSManager {
    public static InfoBBSManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void parseCmd(String command, Player activeChar) {
        if (command.startsWith("_bbsInfo") || command.startsWith("_bbsloc")) {
            String html = HtmCache.getInstance().getHtm("data/html/CommunityBoard/" + getFolder() + "info.htm");
            separateAndSend(html, activeChar);
        } else {
            super.parseCmd(command, activeChar);
        }
    }

    protected String getFolder() {
        return "top/";
    }

    private static class SingletonHolder {
        protected static final InfoBBSManager INSTANCE = new InfoBBSManager();
    }
}
