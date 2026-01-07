package net.sf.l2j.gameserver.events.eventengine.manager;

import net.sf.l2j.Config;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.events.eventengine.AbstractEvent;
import net.sf.l2j.gameserver.events.eventengine.Announce;
import net.sf.l2j.gameserver.events.eventengine.EventState;
import net.sf.l2j.gameserver.events.eventengine.PlayerData;
import net.sf.l2j.gameserver.events.eventengine.event.CTF;
import net.sf.l2j.gameserver.model.actor.Player;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CtfEventManager {
    protected static final CLogger LOGGER = new CLogger(CtfEventManager.class.getName());
    protected List<PlayerData> playersData;

    protected AbstractEvent activeEvent = null;
    private CTF ctf;

    public static CtfEventManager getInstance() {
        return SingletonHolder.instance;
    }

    public void load() {
        if (!Config.ENABLE_EVENT_ENGINE) {
            System.out.println("Event Manager: Event Engine is disabled");
            return;
        }
        this.playersData = new ArrayList<>();
        if (Config.ALLOW_CTF_EVENT && Config.CTF_EVENT_INTERVAL != null) {
            LOGGER.info("Capture the flag Engine: is Started.");
            this.ctf = new CTF();
            scheduleNextEvent();
        } else {
            LOGGER.info("Capture the flag Engine: is disabled.");
        }
    }

    public void scheduleNextEvent() {
        try {
            Calendar currentTime = Calendar.getInstance();
            Calendar nextStartTime = null;
            Calendar testStartTime = null;
            for (String timeOfDay : Config.CTF_EVENT_INTERVAL) {
                testStartTime = Calendar.getInstance();
                testStartTime.setLenient(true);
                String[] splitTimeOfDay = timeOfDay.split(":");
                testStartTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splitTimeOfDay[0]));
                testStartTime.set(Calendar.MINUTE, Integer.parseInt(splitTimeOfDay[1]));
                testStartTime.set(Calendar.SECOND, 0);
                testStartTime.set(Calendar.MILLISECOND, 0);
                if (testStartTime.getTimeInMillis() <= currentTime.getTimeInMillis())
                    testStartTime.add(Calendar.DATE, 1);
                if (nextStartTime == null || testStartTime.getTimeInMillis() < nextStartTime.getTimeInMillis())
                    nextStartTime = testStartTime;
            }
            if (nextStartTime != null) {
                long delay = nextStartTime.getTimeInMillis() - currentTime.getTimeInMillis();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                String nextEventTime = sdf.format(nextStartTime.getTime());
                Announce.announce("System", "Next CTF event starts at: " + nextEventTime);
                ThreadPool.schedule(this.ctf, delay);
            }
        } catch (Exception e) {
            LOGGER.warn("CTF EventEngine: Error programming next event", e);
        }
    }

    public int getTotalParticipants() {
        return this.activeEvent.getPlayers().size();
    }

    public void removePlayer(Player player) {
        if (this.activeEvent == null || this.activeEvent.getState() != EventState.REGISTERING) {
            player.sendMessage("You cannot unregister now.");
            return;
        }
        if (!this.activeEvent.isInEvent(player)) {
            player.sendMessage("You are not registered to the event.");
            return;
        }
        this.activeEvent.removePlayer(player);
        player.sendMessage("You have successfully unregistered from the event.");
    }

    public void registerPlayer(Player player) {
        if (this.activeEvent == null || this.activeEvent.getState() != EventState.REGISTERING) {
            player.sendMessage("You cannot register now.");
            return;
        }
        if (this.activeEvent.isInEvent(player)) {
            player.sendMessage("You are already registered to the event.");
            return;
        }
        this.activeEvent.registerPlayer(player);
        player.sendMessage("You have successfully registered to the event.");
    }

    public void storePlayersData(List<Player> players) {
        for (Player player : players)
            this.playersData.add(new PlayerData(player));
    }

    public void restorePlayer(Player player) {
        PlayerData playerData = null;
        for (PlayerData pd : this.playersData) {
            if (pd.getPlayerId() == player.getObjectId()) {
                playerData = pd;
                pd.restore(player);
                break;
            }
        }
        if (playerData != null)
            this.playersData.remove(playerData);
    }

    public AbstractEvent getActiveEvent() {
        return this.activeEvent;
    }

    public void setActiveEvent(AbstractEvent event) {
        this.activeEvent = event;
    }

    public void onEventEnd(AbstractEvent event) {
        this.activeEvent = null;
        scheduleNextEvent();
    }

    private static class SingletonHolder {
        protected static final CtfEventManager instance = new CtfEventManager();
    }
}
