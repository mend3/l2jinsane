/**/
package net.sf.l2j.gameserver.events.bossevent;

import net.sf.l2j.Config;
import net.sf.l2j.commons.pool.ThreadPool;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

public class NextBossEvent {
    protected static final Logger _log = Logger.getLogger(NextBossEvent.class.getName());
    private static NextBossEvent _instance = null;
    private static String mensaje;
    private final SimpleDateFormat format = new SimpleDateFormat("HH:mm");
    public ScheduledFuture<?> task = null;
    private Calendar nextEvent;

    public static NextBossEvent getInstance() {
        if (_instance == null) {
            _instance = new NextBossEvent();
        }

        return _instance;
    }

    public String getNextTime() {
        return this.nextEvent.getTime() != null ? this.format.format(this.nextEvent.getTime()) : "Erro";
    }

    public void startCalculationOfNextEventTime() {
        try {
            Calendar currentTime = Calendar.getInstance();
            Calendar testStartTime = null;
            long flush2 = 0L;
            long timeL = 0L;
            int count = 0;
            String[] var8 = Config.BOSS_EVENT_BY_TIME_OF_DAY;
            int var9 = var8.length;

            for (String timeOfDay : var8) {
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
                    this.nextEvent = testStartTime;
                }

                if (timeL < flush2) {
                    flush2 = timeL;
                    this.nextEvent = testStartTime;
                }

                ++count;
            }

            _log.info("[Boss Event]: Next Event Time -> " + this.nextEvent.getTime());
            ThreadPool.schedule(new StartEventTask(this), flush2);
        } catch (Exception var13) {
            System.out.println("[Boss Event]: " + var13);
        }

    }

    public String NextKTBEvent() {
        try {
            mensaje = "Evento desactivado.";
            Calendar currentTime = Calendar.getInstance();
            Calendar nextStartTime = null;
            Calendar testStartTime = null;
            String[] var4 = Config.BOSS_EVENT_BY_TIME_OF_DAY;
            int var5 = var4.length;

            for (String timeOfDay : var4) {
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
        StartEventTask(final NextBossEvent param1) {
        }

        public void run() {
            NextBossEvent._log.info("----------------------------------------------------------------------------");
            NextBossEvent._log.info(" Boss Event: Event Started.");
            NextBossEvent._log.info("----------------------------------------------------------------------------");
            BossEvent.getInstance().startRegistration();
        }
    }
}