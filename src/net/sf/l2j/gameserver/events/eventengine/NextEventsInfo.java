package net.sf.l2j.gameserver.events.eventengine;

import net.sf.l2j.Config;
import net.sf.l2j.commons.logging.CLogger;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class NextEventsInfo {
    private static final CLogger LOGGER = new CLogger(NextEventsInfo.class.getName());
    private static String _message;

    public static NextEventsInfo getInstance() {
        return NextEventsInfo.SingletonHolder.instance;
    }

    public String NextCtfEvent() {
        try {
            _message = "Event is disabled.";
            Calendar currentTime = Calendar.getInstance();
            Calendar nextStartTime = null;
            Calendar testStartTime;

            for (String timeOfDay : Config.CTF_EVENT_INTERVAL) {
                testStartTime = Calendar.getInstance();
                testStartTime.setLenient(true);
                String[] splitTimeOfDay = timeOfDay.split(":");
                testStartTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splitTimeOfDay[0]));
                testStartTime.set(Calendar.MINUTE, Integer.parseInt(splitTimeOfDay[1]));
                testStartTime.set(Calendar.SECOND, 0);
                testStartTime.set(Calendar.MILLISECOND, 0);
                if (testStartTime.getTimeInMillis() <= currentTime.getTimeInMillis()) {
                    testStartTime.add(Calendar.DATE, 1);
                }

                if (nextStartTime == null || testStartTime.getTimeInMillis() < nextStartTime.getTimeInMillis()) {
                    nextStartTime = testStartTime;
                }
            }

            if (nextStartTime != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                _message = sdf.format(nextStartTime.getTime());
            }
        } catch (Exception e) {
            LOGGER.warn("CTF EventEngine: Error programming next event", e);
        }

        return _message;
    }

    public String NextTvtEvent() {
        try {
            _message = "Event is disabled.";
            Calendar currentTime = Calendar.getInstance();
            Calendar nextStartTime = null;
            Calendar testStartTime;

            for (String timeOfDay : Config.TVT_EVENT_INTERVAL) {
                testStartTime = Calendar.getInstance();
                testStartTime.setLenient(true);
                String[] splitTimeOfDay = timeOfDay.split(":");
                testStartTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splitTimeOfDay[0]));
                testStartTime.set(Calendar.MINUTE, Integer.parseInt(splitTimeOfDay[1]));
                testStartTime.set(Calendar.SECOND, 0);
                testStartTime.set(Calendar.MILLISECOND, 0);
                if (testStartTime.getTimeInMillis() <= currentTime.getTimeInMillis()) {
                    testStartTime.add(Calendar.DATE, 1);
                }

                if (nextStartTime == null || testStartTime.getTimeInMillis() < nextStartTime.getTimeInMillis()) {
                    nextStartTime = testStartTime;
                }
            }

            if (nextStartTime != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                _message = sdf.format(nextStartTime.getTime());
            }
        } catch (Exception e) {
            LOGGER.warn("TvT EventEngine: Error programming next event", e);
        }

        return _message;
    }

    public String NextDMEvent() {
        try {
            _message = "Event is disabled.";
            Calendar currentTime = Calendar.getInstance();
            Calendar nextStartTime = null;
            Calendar testStartTime;

            for (String timeOfDay : Config.DM_EVENT_INTERVAL) {
                testStartTime = Calendar.getInstance();
                testStartTime.setLenient(true);
                String[] splitTimeOfDay = timeOfDay.split(":");
                testStartTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splitTimeOfDay[0]));
                testStartTime.set(Calendar.MINUTE, Integer.parseInt(splitTimeOfDay[1]));
                testStartTime.set(Calendar.SECOND, 0);
                testStartTime.set(Calendar.MILLISECOND, 0);
                if (testStartTime.getTimeInMillis() <= currentTime.getTimeInMillis()) {
                    testStartTime.add(Calendar.DATE, 1);
                }

                if (nextStartTime == null || testStartTime.getTimeInMillis() < nextStartTime.getTimeInMillis()) {
                    nextStartTime = testStartTime;
                }
            }

            if (nextStartTime != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                _message = sdf.format(nextStartTime.getTime());
            }
        } catch (Exception e) {
            LOGGER.warn("DM EventEngine: Error programming next event", e);
        }

        return _message;
    }

    private static class SingletonHolder {
        protected static final NextEventsInfo instance = new NextEventsInfo();
    }
}
