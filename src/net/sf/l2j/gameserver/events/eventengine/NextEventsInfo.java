package net.sf.l2j.gameserver.events.eventengine;

import net.sf.l2j.Config;
import net.sf.l2j.commons.logging.CLogger;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class NextEventsInfo {
    private static final CLogger LOGGER = new CLogger(NextEventsInfo.class.getName());

    private static String mensaje;

    public static NextEventsInfo getInstance() {
        return SingletonHolder.instance;
    }

    public String NextCtfEvent() {
        try {
            mensaje = "Evento desactivado.";
            Calendar currentTime = Calendar.getInstance();
            Calendar nextStartTime = null;
            Calendar testStartTime = null;
            for (String timeOfDay : Config.CTF_EVENT_INTERVAL) {
                testStartTime = Calendar.getInstance();
                testStartTime.setLenient(true);
                String[] splitTimeOfDay = timeOfDay.split(":");
                testStartTime.set(11, Integer.parseInt(splitTimeOfDay[0]));
                testStartTime.set(12, Integer.parseInt(splitTimeOfDay[1]));
                testStartTime.set(13, 0);
                testStartTime.set(14, 0);
                if (testStartTime.getTimeInMillis() <= currentTime.getTimeInMillis())
                    testStartTime.add(5, 1);
                if (nextStartTime == null || testStartTime.getTimeInMillis() < nextStartTime.getTimeInMillis())
                    nextStartTime = testStartTime;
            }
            if (nextStartTime != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                String nextEventTime = sdf.format(nextStartTime.getTime());
                mensaje = nextEventTime;
            }
        } catch (Exception e) {
            LOGGER.warn("CTF EventEngine: Error programming next event", e);
        }
        return mensaje;
    }

    public String NextTvtEvent() {
        try {
            mensaje = "Evento desactivado.";
            Calendar currentTime = Calendar.getInstance();
            Calendar nextStartTime = null;
            Calendar testStartTime = null;
            for (String timeOfDay : Config.TVT_EVENT_INTERVAL) {
                testStartTime = Calendar.getInstance();
                testStartTime.setLenient(true);
                String[] splitTimeOfDay = timeOfDay.split(":");
                testStartTime.set(11, Integer.parseInt(splitTimeOfDay[0]));
                testStartTime.set(12, Integer.parseInt(splitTimeOfDay[1]));
                testStartTime.set(13, 0);
                testStartTime.set(14, 0);
                if (testStartTime.getTimeInMillis() <= currentTime.getTimeInMillis())
                    testStartTime.add(5, 1);
                if (nextStartTime == null || testStartTime.getTimeInMillis() < nextStartTime.getTimeInMillis())
                    nextStartTime = testStartTime;
            }
            if (nextStartTime != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                String nextEventTime = sdf.format(nextStartTime.getTime());
                mensaje = nextEventTime;
            }
        } catch (Exception e) {
            LOGGER.warn("TvT EventEngine: Error programming next event", e);
        }
        return mensaje;
    }

    public String NextDMEvent() {
        try {
            mensaje = "Evento desactivado.";
            Calendar currentTime = Calendar.getInstance();
            Calendar nextStartTime = null;
            Calendar testStartTime = null;
            for (String timeOfDay : Config.DM_EVENT_INTERVAL) {
                testStartTime = Calendar.getInstance();
                testStartTime.setLenient(true);
                String[] splitTimeOfDay = timeOfDay.split(":");
                testStartTime.set(11, Integer.parseInt(splitTimeOfDay[0]));
                testStartTime.set(12, Integer.parseInt(splitTimeOfDay[1]));
                testStartTime.set(13, 0);
                testStartTime.set(14, 0);
                if (testStartTime.getTimeInMillis() <= currentTime.getTimeInMillis())
                    testStartTime.add(5, 1);
                if (nextStartTime == null || testStartTime.getTimeInMillis() < nextStartTime.getTimeInMillis())
                    nextStartTime = testStartTime;
            }
            if (nextStartTime != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                String nextEventTime = sdf.format(nextStartTime.getTime());
                mensaje = nextEventTime;
            }
        } catch (Exception e) {
            LOGGER.warn("DM EventEngine: Error programming next event", e);
        }
        return mensaje;
    }

    private static class SingletonHolder {
        protected static final NextEventsInfo instance = new NextEventsInfo();
    }
}
