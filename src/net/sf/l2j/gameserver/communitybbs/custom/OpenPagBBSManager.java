package net.sf.l2j.gameserver.communitybbs.custom;

import mods.pvpZone.RandomZoneManager;
import net.sf.l2j.gameserver.communitybbs.manager.BaseBBSManager;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.events.bossevent.NextBossEvent;
import net.sf.l2j.gameserver.events.eventengine.NextEventsInfo;
import net.sf.l2j.gameserver.events.partyfarm.InitialPartyFarm;
import net.sf.l2j.gameserver.events.pvpevent.PvPEventNext;
import net.sf.l2j.gameserver.events.tournament.ArenaEvent;
import net.sf.l2j.gameserver.model.actor.Player;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

public class OpenPagBBSManager extends BaseBBSManager {
    public static OpenPagBBSManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void parseCmd(String command, Player player) {
        if (command.equals("_bbspag")) {
            loadStaticHtm("index.htm", player);
        } else if (command.startsWith("_bbspag;")) {
            StringTokenizer st = new StringTokenizer(command, ";");
            st.nextToken();
            String html = HtmCache.getInstance().getHtm("data/html/CommunityBoard/" + getFolder() + st.nextToken() + ".htm");
            html = html.replaceAll("%dat%", (new SimpleDateFormat("dd/MM/yyyy")).format(new Date(System.currentTimeMillis())));
            html = html.replaceAll("%time%", (new SimpleDateFormat("HH:mm:ss")).format(new Date(System.currentTimeMillis())));
            separateAndSend(html, player);
        } else if (command.startsWith("_bbspagevents")) {
            String html = HtmCache.getInstance().getHtm("data/html/CommunityBoard/" + getFolder() + "events.htm");
            html = html.replaceAll("%tvtTime%", NextEventsInfo.getInstance().NextTvtEvent());
            html = html.replaceAll("%tourTime%", ArenaEvent.getInstance().NextArenaEvent());
            html = html.replaceAll("%pvpTime%", PvPEventNext.getInstance().NextPvPEvent());
            html = html.replaceAll("%PartyZoneTime%", InitialPartyFarm.getInstance().getRestartNextTime());
            html = html.replaceAll("%KtbEventTime%", NextBossEvent.getInstance().NextKTBEvent());
            html = html.replaceAll("%randomzone%", RandomZoneManager.getInstance().getCurrentZone().getName());
            separateAndSend(html, player);
        } else {
            super.parseCmd(command, player);
        }
    }

    protected String getFolder() {
        return "top/";
    }

    private static class SingletonHolder {
        protected static final OpenPagBBSManager INSTANCE = new OpenPagBBSManager();
    }
}
