/**/
package net.sf.l2j.gameserver.events.tournament;

import net.sf.l2j.Config;
import net.sf.l2j.commons.pool.ThreadPool;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Logger;

public class ArenaEvent {
    protected static final Logger _log = Logger.getLogger(ArenaEvent.class.getName());
    private static ArenaEvent _instance = null;
    private static String mensaje;
    private final SimpleDateFormat format = new SimpleDateFormat("HH:mm");
    private Calendar NextEvent;

    public static ArenaEvent getInstance() {
        if (_instance == null) {
            _instance = new ArenaEvent();
        }

        return _instance;
    }

    public String getNextTime() {
        return this.NextEvent.getTime() != null ? this.format.format(this.NextEvent.getTime()) : "Erro";
    }

    public void StartCalculationOfNextEventTime() {
        try {
            Calendar currentTime = Calendar.getInstance();
            Calendar testStartTime = null;
            long flush2 = 0L;
            long timeL = 0L;
            int count = 0;
            String[] var8 = Config.TOURNAMENT_EVENT_INTERVAL_BY_TIME_OF_DAY;
            int var9 = var8.length;

            for (int var10 = 0; var10 < var9; ++var10) {
                String timeOfDay = var8[var10];
                testStartTime = Calendar.getInstance();
                testStartTime.setLenient(true);
                String[] splitTimeOfDay = timeOfDay.split(":");
                testStartTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(splitTimeOfDay[0]));
                testStartTime.set(Calendar.MINUTE, Integer.parseInt(splitTimeOfDay[1]));
                testStartTime.set(Calendar.SECOND, 0);
                if (testStartTime.getTimeInMillis() < currentTime.getTimeInMillis()) {
                    testStartTime.add(Calendar.DATE, 1);
                }

                timeL = testStartTime.getTimeInMillis() - currentTime.getTimeInMillis();
                if (count == 0) {
                    flush2 = timeL;
                    this.NextEvent = testStartTime;
                }

                if (timeL < flush2) {
                    flush2 = timeL;
                    this.NextEvent = testStartTime;
                }

                ++count;
            }

            _log.info("Tournament: Next Event: " + this.NextEvent.getTime());
            ThreadPool.schedule(new StartEventTask(this), flush2);
        } catch (Exception var13) {
            System.out.println("[Tournament]: " + var13);
        }

    }

    public String NextArenaEvent() {
        try {
            mensaje = "Evento desactivado.";
            Calendar currentTime = Calendar.getInstance();
            Calendar nextStartTime = null;
            Calendar testStartTime = null;
            String[] var4 = Config.TOURNAMENT_EVENT_INTERVAL_BY_TIME_OF_DAY;
            int var5 = var4.length;

            for (int var6 = 0; var6 < var5; ++var6) {
                String timeOfDay = var4[var6];
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
                String nextEventTime = sdf.format(nextStartTime.getTime());
                mensaje = nextEventTime;
            }
        } catch (Exception var9) {
            _log.warning("CTF EventEngine: Error programming next event " + var9);
        }

        return mensaje;
    }

    static class StartEventTask implements Runnable {
        StartEventTask(final ArenaEvent param1) {
        }

        public void run() {
            ArenaEvent._log.info("----------------------------------------------------------------------------");
            ArenaEvent._log.info("[Tournament]: Event Started.");
            ArenaEvent._log.info("----------------------------------------------------------------------------");
            ArenaTask.SpawnEvent();
        }
    }
}