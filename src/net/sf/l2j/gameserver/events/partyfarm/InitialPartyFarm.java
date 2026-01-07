/**/
package net.sf.l2j.gameserver.events.partyfarm;

import net.sf.l2j.Config;
import net.sf.l2j.commons.pool.ThreadPool;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Logger;

public class InitialPartyFarm {
    protected static final Logger _log = Logger.getLogger(InitialPartyFarm.class.getName());
    private static InitialPartyFarm _instance = null;
    private final SimpleDateFormat format = new SimpleDateFormat("HH:mm");
    private Calendar NextEvent;

    public static InitialPartyFarm getInstance() {
        if (_instance == null) {
            _instance = new InitialPartyFarm();
        }

        return _instance;
    }

    public String getRestartNextTime() {
        return this.NextEvent.getTime() != null ? this.format.format(this.NextEvent.getTime()) : "Erro";
    }

    public void load() {
        try {
            Calendar currentTime = Calendar.getInstance();
            Calendar testStartTime = null;
            long flush2 = 0L;
            long timeL = 0L;
            int count = 0;
            String[] var8 = Config.EVENT_BEST_FARM_INTERVAL_BY_TIME_OF_DAY;
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

            _log.info("[Party Farm]: Next event: " + this.NextEvent.getTime());
            ThreadPool.schedule(new StartEventTask(this), flush2);
        } catch (Exception var13) {
            System.out.println("[Party Farm]: Some error in the configs was found!");
        }

    }

    static class StartEventTask implements Runnable {
        StartEventTask(final InitialPartyFarm param1) {
        }

        public void run() {
            InitialPartyFarm._log.info("[Party Farm]: Event Started.");
            PartyFarm.bossSpawnMonster();
        }
    }
}