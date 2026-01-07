package net.sf.l2j.gameserver.model.olympiad;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.gameserver.data.manager.ZoneManager;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.type.OlympiadStadiumZone;

import java.util.Collection;
import java.util.List;

public class OlympiadGameManager implements Runnable {
    private static final CLogger LOGGER = new CLogger(OlympiadGameManager.class.getName());
    private final OlympiadGameTask[] _tasks;
    Collection<OlympiadStadiumZone> _zones;
    private volatile boolean _battleStarted = false;

    private OlympiadGameManager() {
        _zones = ZoneManager.getInstance().getAllZones(OlympiadStadiumZone.class);
        if (_zones == null || _zones.isEmpty())
            throw new Error("No olympiad stadium zones defined !");
        this._tasks = new OlympiadGameTask[_zones.size()];
    }

    public static OlympiadGameManager getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void load() {
        int i = 0;
        for (OlympiadStadiumZone zone : _zones)
            this._tasks[i++] = new OlympiadGameTask(zone);
        LOGGER.info("Loaded {} stadiums.", Integer.valueOf(this._tasks.length));
    }

    public final void run() {
        if (Olympiad.getInstance().isOlympiadEnd())
            return;
        if (Olympiad.getInstance().isInCompPeriod()) {
            List<List<Integer>> readyClassed = OlympiadManager.getInstance().hasEnoughRegisteredClassed();
            boolean readyNonClassed = OlympiadManager.getInstance().hasEnoughRegisteredNonClassed();
            if (readyClassed != null || readyNonClassed)
                for (int i = 0; i < this._tasks.length; i++) {
                    OlympiadGameTask task = this._tasks[i];
                    synchronized (task) {
                        if (!task.isRunning()) {
                            if (readyClassed != null && i % 2 == 0) {
                                AbstractOlympiadGame newGame = OlympiadGameClassed.createGame(i, readyClassed);
                                if (newGame != null) {
                                    task.attachGame(newGame);
                                    continue;
                                }
                                readyClassed = null;
                            }
                            if (readyNonClassed) {
                                AbstractOlympiadGame newGame = OlympiadGameNonClassed.createGame(i, OlympiadManager.getInstance().getRegisteredNonClassBased());
                                if (newGame != null) {
                                    task.attachGame(newGame);
                                } else {
                                    readyNonClassed = false;
                                }
                                continue;
                            }
                        }
                    }
                }
        } else if (isAllTasksFinished()) {
            OlympiadManager.getInstance().clearRegistered();
            this._battleStarted = false;
            LOGGER.info("All current Olympiad games finished.");
        }
    }

    protected final boolean isBattleStarted() {
        return this._battleStarted;
    }

    protected final void startBattle() {
        this._battleStarted = true;
    }

    public final boolean isAllTasksFinished() {
        for (OlympiadGameTask task : this._tasks) {
            if (task.isRunning())
                return false;
        }
        return true;
    }

    public final OlympiadGameTask getOlympiadTask(int id) {
        if (id < 0 || id >= this._tasks.length)
            return null;
        return this._tasks[id];
    }

    public OlympiadGameTask[] getOlympiadTasks() {
        return this._tasks;
    }

    public final int getNumberOfStadiums() {
        return this._tasks.length;
    }

    public final void notifyCompetitorDamage(Player player, int damage) {
        if (player == null)
            return;
        int id = player.getOlympiadGameId();
        if (id < 0 || id >= this._tasks.length)
            return;
        AbstractOlympiadGame game = this._tasks[id].getGame();
        if (game != null)
            game.addDamage(player, damage);
    }

    private static class SingletonHolder {
        protected static final OlympiadGameManager INSTANCE = new OlympiadGameManager();
    }
}
