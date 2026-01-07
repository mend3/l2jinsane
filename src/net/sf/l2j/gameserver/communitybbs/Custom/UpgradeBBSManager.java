package net.sf.l2j.gameserver.communitybbs.Custom;

import net.sf.l2j.gameserver.communitybbs.Manager.BaseBBSManager;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.model.actor.Player;

public class UpgradeBBSManager extends BaseBBSManager {
    public static UpgradeBBSManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void parseCmd(String command, Player activeChar) {
        if (command.startsWith("_bbsclan") || command.startsWith("_bbsupgrade")) {
            String html = HtmCache.getInstance().getHtm("data/html/CommunityBoard/" + getFolder() + "upgrade.htm");
            separateAndSend(html, activeChar);
        } else {
            super.parseCmd(command, activeChar);
        }
    }

    protected String getFolder() {
        return "top/";
    }

    private static class SingletonHolder {
        protected static final UpgradeBBSManager INSTANCE = new UpgradeBBSManager();
    }
}
