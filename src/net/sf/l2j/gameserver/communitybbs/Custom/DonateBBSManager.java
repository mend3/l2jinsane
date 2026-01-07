package net.sf.l2j.gameserver.communitybbs.Custom;

import net.sf.l2j.gameserver.communitybbs.Manager.BaseBBSManager;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.model.actor.Player;

public class DonateBBSManager extends BaseBBSManager {
    public static DonateBBSManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void parseCmd(String command, Player activeChar) {
        if (command.startsWith("_bbsmail") || command.equals("_maillist_0_1_0_") || command.startsWith("_bbsdonate")) {
            String html = HtmCache.getInstance().getHtm("data/html/CommunityBoard/" + getFolder() + "donate.htm");
            separateAndSend(html, activeChar);
        } else {
            super.parseCmd(command, activeChar);
        }
    }

    protected String getFolder() {
        return "top/";
    }

    private static class SingletonHolder {
        protected static final DonateBBSManager INSTANCE = new DonateBBSManager();
    }
}
