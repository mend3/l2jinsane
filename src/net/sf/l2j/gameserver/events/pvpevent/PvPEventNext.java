package net.sf.l2j.gameserver.events.pvpevent;

import net.sf.l2j.Config;
import net.sf.l2j.commons.logging.CLogger;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Properties;

public class PvPEventNext {
    private static final CLogger LOGGER = new CLogger(PvPEventNext.class.getName());

    public static ArrayList<String> PVP_EVENT_INTERVAL;

    private static PvPEventNext instance = null;

    private static String mensaje;
    private final SimpleDateFormat format;
    private Calendar NextEvent;

    private PvPEventNext() {
        this.format = new SimpleDateFormat("HH:mm");
        loadPvPConfig();
    }

    public static PvPEventNext getInstance() {
        if (instance == null)
            instance = new PvPEventNext();
        return instance;
    }

    public static void loadPvPConfig() {
        try {
            InputStream is = new FileInputStream(new File("./config/events/pvpEvent.properties"));
            try {
                Properties eventSettings = new Properties();
                eventSettings.load(is);
                PVP_EVENT_INTERVAL = new ArrayList<>();
                String[] propertySplit = eventSettings.getProperty("PvPZEventInterval", "").split(",");
                Collections.addAll(PVP_EVENT_INTERVAL, propertySplit);
                is.close();
            } catch (Throwable throwable) {
                try {
                    is.close();
                } catch (Throwable throwable1) {
                    throwable.addSuppressed(throwable1);
                }
                throw throwable;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getNextTime() {
        if (this.NextEvent.getTime() != null)
            return this.format.format(this.NextEvent.getTime());
        return "Error";
    }

    public void StartCalculationOfNextEventTime() {
        try {
            Calendar currentTime = Calendar.getInstance();
            Calendar testStartTime = null;
            long flush2 = 0L, timeL = 0L;
            int count = 0;
            for (String timeOfDay : PVP_EVENT_INTERVAL) {
                testStartTime = Calendar.getInstance();
                testStartTime.setLenient(true);
                String[] splitTimeOfDay = timeOfDay.split(":");
                testStartTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splitTimeOfDay[0]));
                testStartTime.set(Calendar.MINUTE, Integer.parseInt(splitTimeOfDay[1]));
                testStartTime.set(Calendar.SECOND, 0);
                if (testStartTime.getTimeInMillis() < currentTime.getTimeInMillis())
                    testStartTime.add(Calendar.DATE, 1);
                timeL = testStartTime.getTimeInMillis() - currentTime.getTimeInMillis();
                if (count == 0) {
                    flush2 = timeL;
                    this.NextEvent = testStartTime;
                }
                if (timeL < flush2) {
                    flush2 = timeL;
                    this.NextEvent = testStartTime;
                }
                count++;
            }
            LOGGER.info("PvP Event Proximo Evento: " + this.NextEvent.getTime());
        } catch (Exception e) {
            System.out.println(" PvP Next Event Info: " + e);
        }
    }

    public String NextPvPEvent() {
        try {
            mensaje = "Evento desactivado.";
            Calendar currentTime = Calendar.getInstance();
            Calendar nextStartTime = null;
            Calendar testStartTime = null;
            for (String timeOfDay : Config.PVP_EVENT_INTERVAL) {
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
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                String nextEventTime = sdf.format(nextStartTime.getTime());
                mensaje = nextEventTime;
            }
        } catch (Exception e) {
            LOGGER.warn("CTF EventEngine: Error programming next event " + e);
        }
        return mensaje;
    }
}
