/**/
package net.sf.l2j.gameserver.events.pvpevent;

import net.sf.l2j.Config;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;

import java.util.Calendar;

public class PvPEventManager implements Runnable {
    private static final CLogger LOGGER = new CLogger(PvPEventManager.class.getName());
    private final PvPEvent _event = PvPEvent.getInstance();
    public PvPEventManager.EngineState _state;
    private int _tick;

    public static void announce(String msg) {
        CreatureSay cs = new CreatureSay(0, 17, "PvPEvent", "PvPEvent: " + msg);
        World.toAllOnlinePlayers(cs);
    }

    public static PvPEventManager getInstance() {
        return PvPEventManager.SingletonHolder.INSTANCE;
    }

    public void load() {
        if (Config.PVP_EVENT_ENABLED) {
            this._state = PvPEventManager.EngineState.AWAITING;
            ThreadPool.scheduleAtFixedRate(this, 1000L, 1000L);
            LOGGER.info("PvPEvent: Event is active.");
            PvPEventNext.getInstance().StartCalculationOfNextEventTime();
        } else {
            this._state = PvPEventManager.EngineState.INACTIVE;
            LOGGER.info("PvPEvent: Event is disabled.");
        }

    }

    public void run() {
        if (this._state == PvPEventManager.EngineState.AWAITING) {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(11);
            int minute = calendar.get(12);
            String[] var4 = Config.PVP_EVENT_INTERVAL;
            int var5 = var4.length;

            for (int var6 = 0; var6 < var5; ++var6) {
                String time = var4[var6];
                String[] splitTime = time.split(":");
                if (Integer.parseInt(splitTime[0]) == hour && Integer.parseInt(splitTime[1]) == minute) {
                    this.startEvent();
                }
            }
        } else if (this._state == PvPEventManager.EngineState.ACTIVE) {
            switch (this._tick) {
                case 1:
                    announce(this._tick + " second(s) until the event is finished!");
                    break;
                case 2:
                    announce(this._tick + " second(s) until the event is finished!");
                    break;
                case 3:
                case 4:
                case 5:
                case 10:
                    announce(this._tick + " second(s) until the event is finished!");
                    break;
                case 30:
                    announce(this._tick + " second(s) until the event is finished!");
                    announce(" check the current ranking by .pvpEvent!");
                    break;
                case 60:
                case 180:
                    announce(this._tick / 60 + " minute(s) until the event is finished!");
                    announce(" check the current ranking by .pvpEvent!");
                    break;
                case 300:
                    announce(this._tick / 60 + " minute(s) until the event is finished!");
                    announce(" check the current ranking by .pvpEvent!");
                    break;
                case 600:
                case 900:
                    announce(this._tick / 60 + " minute(s) until the event is finished!");
                    announce(" check the current ranking by .pvpEvent!");
                    break;
                case 1800:
                    announce(this._tick / 60 + " minute(s) until the event is finished!");
                    announce(" check the current ranking by .pvpEvent!");
                    break;
                case 3600:
                    announce(this._tick / 60 + " minute(s) until the event is finished!");
                    announce(" check the current ranking by .pvpEvent!");
                    break;
                case 5400:
                    announce(this._tick / 60 + " minute(s) until the event is finished!");
                    announce(" check the current ranking by .pvpEvent!");
                    break;
                case 7200:
                    announce(this._tick / 60 + " minute(s) until the event is finished!");
                    announce(" check the current ranking by .pvpEvent!");
            }

            if (this._tick == 0) {
                this.endEvent();
            }

            --this._tick;
        }

    }

    public void startEvent() {
        if (this._event.startPartyEvent()) {
            announce(" is enabled, go to PvP Zone.");
            this._state = PvPEventManager.EngineState.ACTIVE;
            this._tick = Config.PVP_EVENT_RUNNING_TIME * 60;
        }

    }

    public void endEvent() {
        if (this._event.endPartyEvent()) {
            this._event.rewardFinish();
            announce(" is finished, thank you for participating.");
            PvPEventNext.getInstance().StartCalculationOfNextEventTime();
            this._state = PvPEventManager.EngineState.AWAITING;
        }

    }

    protected enum EngineState {
        AWAITING,
        ACTIVE,
        INACTIVE;

        // $FF: synthetic method
        private static PvPEventManager.EngineState[] $values() {
            return new PvPEventManager.EngineState[]{AWAITING, ACTIVE, INACTIVE};
        }
    }

    private static class SingletonHolder {
        protected static final PvPEventManager INSTANCE = new PvPEventManager();
    }
}