package net.sf.l2j.gameserver;

import enginemods.main.EngineModsManager;
import mods.dungeon.DungeonManager;
import net.sf.l2j.Config;
import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.network.StatusType;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.data.manager.*;
import net.sf.l2j.gameserver.data.sql.ServerMemoTable;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.network.GameClient;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ServerClose;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.taskmanager.ItemsOnGroundTaskManager;

public class Shutdown extends Thread {
    public static final int SIGTERM = 0;
    public static final int GM_SHUTDOWN = 1;
    public static final int GM_RESTART = 2;
    public static final int ABORT = 3;
    private static final CLogger LOGGER = new CLogger(Shutdown.class.getName());
    private static final String[] MODE_TEXT = new String[]{"SIGTERM", "shutting down", "restarting", "aborting"};
    private static Shutdown _counterInstance = null;
    private int _secondsShut;
    private int _shutdownMode;

    protected Shutdown() {
        this._secondsShut = -1;
        this._shutdownMode = 0;
    }

    public Shutdown(int seconds, boolean restart) {
        this._secondsShut = Math.max(0, seconds);
        this._shutdownMode = restart ? 2 : 1;
    }

    private static void sendServerQuit(int seconds) {
        World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.THE_SERVER_WILL_BE_COMING_DOWN_IN_S1_SECONDS).addNumber(seconds));
    }

    private static void disconnectAllPlayers() {
        for (Player player : World.getInstance().getPlayers()) {
            GameClient client = player.getClient();
            if (client != null && !client.isDetached()) {
                client.close(ServerClose.STATIC_PACKET);
                client.setPlayer(null);
                player.setClient(null);
            }
            player.deleteMe();
        }
    }

    public static Shutdown getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void run() {
        if (this == SingletonHolder.INSTANCE) {
            StringUtil.printSection("Under " + MODE_TEXT[this._shutdownMode] + " process");
            EngineModsManager.onShutDown();
            try {
                disconnectAllPlayers();
                LOGGER.info("All players have been disconnected.");
            } catch (Throwable throwable) {
            }
            ThreadPool.shutdown();
            try {
                LoginServerThread.getInstance().interrupt();
            } catch (Throwable throwable) {
            }
            try {
                DailyRewardManager.getInstance().saveRewardedPlayersHWID();
                DailyRewardManager.getInstance().saveRewardedPlayersObjId();
                LOGGER.info("Daily Reward Manager saved all received players.");
            } catch (Throwable t) {
                LOGGER.error("Error saving Daily Reward Manager: " + t);
            }
            try {
                DungeonManager.getInstance().updateDatabase();
                LOGGER.info("Dungeon Manager saved all information.");
            } catch (Throwable t) {
                LOGGER.error("Error saving DungeonManager: " + t);
            }
            if (!SevenSignsManager.getInstance().isSealValidationPeriod())
                FestivalOfDarknessManager.getInstance().saveFestivalData(false);
            SevenSignsManager.getInstance().saveSevenSignsData();
            SevenSignsManager.getInstance().saveSevenSignsStatus();
            LOGGER.info("Seven Signs Festival, general data && status have been saved.");
            ZoneManager.getInstance().save();
            RaidBossManager.getInstance().cleanUp(true);
            LOGGER.info("Raid Bosses data has been saved.");
            GrandBossManager.getInstance().cleanUp();
            LOGGER.info("World Bosses data has been saved.");
            Olympiad.getInstance().saveOlympiadStatus();
            LOGGER.info("Olympiad data has been saved.");
            HeroManager.getInstance().shutdown();
            LOGGER.info("Hero data has been saved.");
            CastleManorManager.getInstance().storeMe();
            LOGGER.info("Manors data has been saved.");
            FishingChampionshipManager.getInstance().shutdown();
            LOGGER.info("Fishing Championship data has been saved.");
            ClassBalanceManager.getInstance().store(null);
            SkillBalanceManager.getInstance().store(null);
            LOGGER.info("Balancer data has been saved.");
            BufferManager.getInstance().saveSchemes();
            LOGGER.info("BufferTable data has been saved.");
            if (Config.ALLOW_WEDDING) {
                CoupleManager.getInstance().save();
                LOGGER.info("CoupleManager data has been saved.");
            }
            ServerMemoTable.getInstance().storeMe();
            LOGGER.info("ServerMemo data has been saved.");
            ItemsOnGroundTaskManager.getInstance().save();
            try {
                Thread.sleep(5000L);
            } catch (InterruptedException interruptedException) {
            }
            try {
                GameServer.getInstance().getSelectorThread().shutdown();
            } catch (Throwable throwable) {
            }
            try {
                ConnectionPool.shutdown();
            } catch (Throwable throwable) {
            }
            Runtime.getRuntime().halt((SingletonHolder.INSTANCE._shutdownMode == 2) ? 2 : 0);
        } else {
            countdown();
            switch (this._shutdownMode) {
                case 1:
                    SingletonHolder.INSTANCE.setMode(1);
                    SingletonHolder.INSTANCE.run();
                    System.exit(0);
                    break;
                case 2:
                    SingletonHolder.INSTANCE.setMode(2);
                    SingletonHolder.INSTANCE.run();
                    System.exit(2);
                    break;
            }
        }
    }

    public void startShutdown(Player player, String ghostEntity, int seconds, boolean restart) {
        this._shutdownMode = restart ? 2 : 1;
        if (player != null) {
            LOGGER.info("GM: {} issued {} process in {} seconds.", player.toString(), MODE_TEXT[this._shutdownMode], seconds);
        } else if (!ghostEntity.isEmpty()) {
            LOGGER.info("Entity: {} issued {} process in {} seconds.", ghostEntity, MODE_TEXT[this._shutdownMode], seconds);
        }
        if (this._shutdownMode > 0)
            switch (seconds) {
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 10:
                case 30:
                case 60:
                case 120:
                case 180:
                case 240:
                case 300:
                case 360:
                case 420:
                case 480:
                case 540:
                    break;
                default:
                    sendServerQuit(seconds);
                    break;
            }
        if (_counterInstance != null)
            _counterInstance.setMode(3);
        _counterInstance = new Shutdown(seconds, restart);
        _counterInstance.start();
    }

    public void abort(Player player) {
        if (_counterInstance != null) {
            LOGGER.info("GM: {} aborted {} process.", player.toString(), MODE_TEXT[this._shutdownMode]);
            _counterInstance.setMode(3);
            World.announceToOnlinePlayers("Server aborted " + MODE_TEXT[this._shutdownMode] + " process and continues normal operation.");
        }
    }

    private void setMode(int mode) {
        this._shutdownMode = mode;
    }

    private void countdown() {
        try {
            while (this._secondsShut > 0) {
                if (this._shutdownMode == 3) {
                    if (LoginServerThread.getInstance().getServerType() == StatusType.DOWN)
                        LoginServerThread.getInstance().setServerType(Config.SERVER_GMONLY ? StatusType.GM_ONLY : StatusType.AUTO);
                    break;
                }
                switch (this._secondsShut) {
                    case 1:
                    case 2:
                    case 3:
                    case 4:
                    case 5:
                    case 10:
                    case 30:
                    case 60:
                    case 120:
                    case 180:
                    case 240:
                    case 300:
                    case 360:
                    case 420:
                    case 480:
                    case 540:
                        sendServerQuit(this._secondsShut);
                        break;
                }
                if (this._secondsShut <= 60 && LoginServerThread.getInstance().getServerType() != StatusType.DOWN)
                    LoginServerThread.getInstance().setServerType(StatusType.DOWN);
                this._secondsShut--;
                Thread.sleep(1000L);
            }
        } catch (InterruptedException interruptedException) {
        }
    }

    private static class SingletonHolder {
        protected static final Shutdown INSTANCE = new Shutdown();
    }
}
