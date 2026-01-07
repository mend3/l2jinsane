/**/
package net.sf.l2j.gameserver.model.olympiad;

import net.sf.l2j.Config;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.model.zone.type.OlympiadStadiumZone;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

public final class OlympiadGameTask implements Runnable {
    public static final int[] TELEPORT_TO_ARENA;
    public static final int[] BATTLE_START_TIME;
    public static final int[] TELEPORT_TO_TOWN;
    private static final CLogger LOGGER = new CLogger(OlympiadGameTask.class.getName());
    private static final long BATTLE_PERIOD;

    static {
        BATTLE_PERIOD = Config.ALT_OLY_BATTLE;
        TELEPORT_TO_ARENA = new int[]{120, 60, 30, 15, 10, 5, 4, 3, 2, 1, 0};
        BATTLE_START_TIME = new int[]{60, 50, 40, 30, 20, 10, 5, 4, 3, 2, 1, 0};
        TELEPORT_TO_TOWN = new int[]{40, 30, 20, 10, 5, 4, 3, 2, 1, 0};
    }

    private final OlympiadStadiumZone _zone;
    private AbstractOlympiadGame _game;
    private OlympiadGameTask.GameState _state;
    private boolean _needAnnounce;
    private int _countDown;

    public OlympiadGameTask(OlympiadStadiumZone zone) {
        this._state = OlympiadGameTask.GameState.IDLE;
        this._needAnnounce = false;
        this._countDown = 0;
        this._zone = zone;
        zone.registerTask(this);
    }

    public boolean isRunning() {
        return this._state != OlympiadGameTask.GameState.IDLE;
    }

    public boolean isGameStarted() {
        return this._state.ordinal() >= OlympiadGameTask.GameState.GAME_STARTED.ordinal() && this._state.ordinal() <= OlympiadGameTask.GameState.CLEANUP.ordinal();
    }

    public boolean isInTimerTime() {
        return this._state == OlympiadGameTask.GameState.BATTLE_COUNTDOWN;
    }

    public boolean isBattleStarted() {
        return this._state == OlympiadGameTask.GameState.BATTLE_IN_PROGRESS;
    }

    public boolean isBattleFinished() {
        return this._state == OlympiadGameTask.GameState.TELE_TO_TOWN;
    }

    public boolean needAnnounce() {
        if (this._needAnnounce) {
            this._needAnnounce = false;
            return true;
        } else {
            return false;
        }
    }

    public OlympiadStadiumZone getZone() {
        return this._zone;
    }

    public AbstractOlympiadGame getGame() {
        return this._game;
    }

    public void attachGame(AbstractOlympiadGame game) {
        if (game == null || this._state == OlympiadGameTask.GameState.IDLE) {
            this._game = game;
            this._state = OlympiadGameTask.GameState.BEGIN;
            this._needAnnounce = false;
            ThreadPool.execute(this);
        }
    }

    public void run() {
        try {
            int delay = 1;
            switch (this._state.ordinal()) {
                case 0:
                    this._state = OlympiadGameTask.GameState.TELE_TO_ARENA;
                    this._countDown = Config.ALT_OLY_WAIT_TIME;
                    break;
                case 1:
                    this._game.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_WILL_ENTER_THE_OLYMPIAD_STADIUM_IN_S1_SECOND_S).addNumber(this._countDown));
                    delay = this.getDelay(TELEPORT_TO_ARENA);
                    if (this._countDown <= 0) {
                        this._state = OlympiadGameTask.GameState.GAME_STARTED;
                    }
                    break;
                case 2:
                    if (!this.startGame()) {
                        this._state = OlympiadGameTask.GameState.GAME_STOPPED;
                    } else {
                        this._state = OlympiadGameTask.GameState.BATTLE_COUNTDOWN;
                        this._countDown = Config.ALT_OLY_WAIT_BATTLE;
                        delay = this.getDelay(BATTLE_START_TIME);
                    }
                    break;
                case 3:
                    this._zone.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_WILL_START_IN_S1_SECOND_S).addNumber(this._countDown));
                    if (this._countDown == 20) {
                        this._game.buffPlayers();
                        this._game.healPlayers();
                    }

                    delay = this.getDelay(BATTLE_START_TIME);
                    if (this._countDown <= 0) {
                        this._state = OlympiadGameTask.GameState.BATTLE_STARTED;
                    }
                    break;
                case 4:
                    this._countDown = 0;
                    this._game.healPlayers();
                    this._game.resetDamage();
                    this._state = OlympiadGameTask.GameState.BATTLE_IN_PROGRESS;
                    if (!this.startBattle()) {
                        this._state = OlympiadGameTask.GameState.GAME_STOPPED;
                    }
                    break;
                case 5:
                    this._countDown += 1000;
                    if (this.checkBattle() || (long) this._countDown > Config.ALT_OLY_BATTLE) {
                        this._state = OlympiadGameTask.GameState.GAME_STOPPED;
                    }
                    break;
                case 6:
                    this._state = OlympiadGameTask.GameState.TELE_TO_TOWN;
                    this._countDown = Config.ALT_OLY_WAIT_END;
                    this.stopGame();
                    delay = this.getDelay(TELEPORT_TO_TOWN);
                    break;
                case 7:
                    this._game.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_WILL_BE_MOVED_TO_TOWN_IN_S1_SECONDS).addNumber(this._countDown));
                    delay = this.getDelay(TELEPORT_TO_TOWN);
                    if (this._countDown <= 0) {
                        this._state = OlympiadGameTask.GameState.CLEANUP;
                    }
                    break;
                case 8:
                    this.cleanupGame();
                    this._state = OlympiadGameTask.GameState.IDLE;
                    this._game = null;
                    return;
            }

            ThreadPool.schedule(this, delay * 1000L);
        } catch (Exception var2) {
            switch (this._state.ordinal()) {
                case 6:
                case 7:
                case 8:
                case 9:
                    LOGGER.error("Couldn't return players back in town.", var2);
                    this._state = OlympiadGameTask.GameState.IDLE;
                    this._game = null;
                    return;
                default:
                    LOGGER.error("Couldn't return players back in town.", var2);
                    this._state = OlympiadGameTask.GameState.GAME_STOPPED;
                    ThreadPool.schedule(this, 1000L);
            }
        }

    }

    private int getDelay(int[] times) {
        for (int i = 0; i < times.length - 1; ++i) {
            int time = times[i];
            if (time < this._countDown) {
                int delay = this._countDown - time;
                this._countDown = time;
                return delay;
            }
        }

        this._countDown = -1;
        return 1;
    }

    private boolean startGame() {
        if (this._game.checkDefection()) {
            return false;
        } else if (!this._game.portPlayersToArena(this._zone.getLocs())) {
            return false;
        } else {
            this._game.removals();
            this._needAnnounce = true;
            OlympiadGameManager.getInstance().startBattle();
            return true;
        }
    }

    private boolean startBattle() {
        if (this._game.checkBattleStatus() && this._game.makeCompetitionStart()) {
            this._game.broadcastOlympiadInfo(this._zone);
            this._zone.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.STARTS_THE_GAME));
            this._zone.updateZoneStatusForCharactersInside();
            return true;
        } else {
            return false;
        }
    }

    private boolean checkBattle() {
        return this._game.haveWinner();
    }

    private void stopGame() {
        this._game.validateWinner(this._zone);
        this._zone.updateZoneStatusForCharactersInside();
        this._game.cleanEffects();
    }

    private void cleanupGame() {
        this._game.playersStatusBack();
        this._game.portPlayersBack();
        this._game.clearPlayers();
    }

    private enum GameState {
        BEGIN,
        TELE_TO_ARENA,
        GAME_STARTED,
        BATTLE_COUNTDOWN,
        BATTLE_STARTED,
        BATTLE_IN_PROGRESS,
        GAME_STOPPED,
        TELE_TO_TOWN,
        CLEANUP,
        IDLE;

        // $FF: synthetic method
        private static OlympiadGameTask.GameState[] $values() {
            return new OlympiadGameTask.GameState[]{BEGIN, TELE_TO_ARENA, GAME_STARTED, BATTLE_COUNTDOWN, BATTLE_STARTED, BATTLE_IN_PROGRESS, GAME_STOPPED, TELE_TO_TOWN, CLEANUP, IDLE};
        }
    }
}