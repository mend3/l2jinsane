package net.sf.l2j.gameserver;

import enginemods.main.EngineModsManager;
import enginemods.main.engine.community.ClanCommunityBoard;
import enginemods.main.engine.community.FavoriteCommunityBoard;
import enginemods.main.engine.community.MemoCommunityBoard;
import enginemods.main.engine.community.RegionComunityBoard;
import net.sf.l2j.Config;
import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.mmocore.SelectorConfig;
import net.sf.l2j.commons.mmocore.SelectorThread;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.util.SysUtil;
import net.sf.l2j.gameserver.communitybbs.Manager.ForumsBBSManager;
import net.sf.l2j.gameserver.data.EnchantTable;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.data.SkillTable;
import net.sf.l2j.gameserver.data.cache.CrestCache;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.data.manager.*;
import net.sf.l2j.gameserver.data.sql.*;
import net.sf.l2j.gameserver.data.xml.*;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.handler.*;
import net.sf.l2j.gameserver.hwid.Hwid;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.boat.*;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.model.olympiad.OlympiadGameManager;
import net.sf.l2j.gameserver.network.GameClient;
import net.sf.l2j.gameserver.network.L2GamePacketHandler;
import net.sf.l2j.gameserver.taskmanager.*;
import net.sf.l2j.util.DeadLockDetector;
import net.sf.l2j.util.IPv4Filter;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.util.logging.LogManager;

public class GameServer {
    private static final CLogger LOGGER = new CLogger(GameServer.class.getName());
    private static GameServer _gameServer;
    private final SelectorThread<GameClient> _selectorThread;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public GameServer() throws Exception {
        (new File("./log")).mkdir();
        (new File("./log/chat")).mkdir();
        (new File("./log/console")).mkdir();
        (new File("./log/error")).mkdir();
        (new File("./log/gmaudit")).mkdir();
        (new File("./log/item")).mkdir();
        (new File("./data/crests")).mkdirs();
        FileInputStream is = new FileInputStream("config/logging.properties");

        try {
            LogManager.getLogManager().readConfiguration(is);
        } catch (Throwable var8) {
            try {
                is.close();
            } catch (Throwable var5) {
                var8.addSuppressed(var5);
            }

            throw var8;
        }

        is.close();
        StringUtil.printSection("aCis");
        Config.loadGameServer();
        StringUtil.printSection("Poolers");
        ConnectionPool.init();
        ThreadPool.init();
        StringUtil.printSection("IdFactory");
        IdFactory.getInstance();

        StringUtil.printSection("Hwid Manager");
        Hwid.Init();
        StringUtil.printSection("Admin");
        AdminData.getInstance().load();
        MovieMakerManager.getInstance();
        PetitionManager.getInstance();
        HtmCache.getInstance();

        StringUtil.printSection("World");
        World.getInstance().init();
        MapRegionData.getInstance().load();
        AnnouncementData.getInstance().load();
        ServerMemoTable.getInstance().load();
        ZoneManager.getInstance().load();
        GeoEngine.getInstance();

        StringUtil.printSection("Skills");
        SkillTable.getInstance().load();
        SkillTreeData.getInstance().load();
        StringUtil.printSection("Items");
        ItemTable.getInstance().load();
        SummonItemData.getInstance().load();
        HennaData.getInstance().load();
        BuyListManager.getInstance().load();
        MultisellData.getInstance().load();
        RecipeData.getInstance().load();
        ArmorSetData.getInstance().load();
        FishData.getInstance().load();
        SpellbookData.getInstance().load();
        SoulCrystalData.getInstance().load();
        AugmentationData.getInstance().load();
        CursedWeaponManager.getInstance().load();
        EnchantTable.getInstance().load();
        StringUtil.printSection("Characters");
        PlayerClassData.getInstance().load();
        PlayerInfoTable.getInstance().load();
        NewbieBuffData.getInstance().load();
        TeleportLocationData.getInstance().load();
        BookmarkTable.getInstance().load();
        RaidPointManager.getInstance().load();
        StringUtil.printSection("Community server");
        if (Config.ENABLE_COMMUNITY_BOARD) {
            ForumsBBSManager.getInstance().initRoot();
            RegionComunityBoard.getInstance();
            FavoriteCommunityBoard.getInstance();
            ClanCommunityBoard.getInstance();
            MemoCommunityBoard.getInstance();
        } else {
            LOGGER.info("Community server is disabled.");
        }

        StringUtil.printSection("Clans");
        CrestCache.getInstance().load();
        ClanTable.getInstance().load();
        StringUtil.printSection("Castles & Clan Halls");
        CastleManager.getInstance().load();
        ClanHallManager.getInstance().load();
        DoorData.getInstance().spawn();

        StringUtil.printSection("EngineMods");
        EngineModsManager.init();
        if (Config.ALLOW_WEDDING) {
            CoupleManager.getInstance().load();
        }
        StringUtil.printSection("Task Managers");
        AttackStanceTaskManager.getInstance();
        DecayTaskManager.getInstance();
        GameTimeTaskManager.getInstance();
        ItemsOnGroundTaskManager.getInstance();
        MovementTaskManager.getInstance();
        PvpFlagTaskManager.getInstance();
        RandomAnimationTaskManager.getInstance();
        ShadowItemTaskManager.getInstance();
        StatusRealTimeTaskManager.getInstance();
        WaterTaskManager.getInstance();

        StringUtil.printSection("Auto Spawns");
        AutoSpawnTable.getInstance().load();
        StringUtil.printSection("Seven Signs");
        SevenSignsManager.getInstance().spawnSevenSignsNPC();
        FestivalOfDarknessManager.getInstance().init();
        StringUtil.printSection("Manor Manager");
        CastleManorManager.getInstance().load();

        StringUtil.printSection("NPCs");
        RaidBossInfoManager.getInstance().load();
        BufferManager.getInstance().load();
        HerbDropData.getInstance().load();
        NpcData.getInstance().load();
        WalkerRouteData.getInstance().load();
        StaticObjectData.getInstance().load();
        SpawnTable.getInstance().load();
        RaidBossManager.getInstance().load();
        GrandBossManager.getInstance().load();
        DayNightManager.getInstance().notifyChangeMode();
        DimensionalRiftManager.getInstance().load();
        StringUtil.printSection("Olympiads & Heroes");
        OlympiadGameManager.getInstance().load();
        Olympiad.getInstance().load();
        HeroManager.getInstance().load();
        StringUtil.printSection("Four Sepulchers");
        FourSepulchersManager.getInstance().load();
        StringUtil.printSection("Quests & Scripts");
        ScriptData.getInstance().load();
        if (Config.ALLOW_BOAT) {
            BoatManager.getInstance();
            BoatGiranTalking.load();
            BoatGludinRune.load();
            BoatInnadrilTour.load();
            BoatRunePrimeval.load();
            BoatTalkingGludin.load();
        }

        StringUtil.printSection("Events");
        DerbyTrackManager.getInstance().load();
        LotteryManager.getInstance().load();

        if (Config.ALT_FISH_CHAMPIONSHIP_ENABLED) {
            FishingChampionshipManager.getInstance().load();
        }
        StringUtil.printSection("Handlers");
        LOGGER.info("Loaded {} admin command handlers.", AdminCommandHandler.getInstance().size());
        LOGGER.info("Loaded {} bypass command handlers.", BypassHandler.getInstance().size());
        LOGGER.info("Loaded {} chat handlers.", ChatHandler.getInstance().size());
        LOGGER.info("Loaded {} item handlers.", ItemHandler.getInstance().size());
        LOGGER.info("Loaded {} skill handlers.", SkillHandler.getInstance().size());
        LOGGER.info("Loaded {} user command handlers.", UserCommandHandler.getInstance().size());
        LOGGER.info("Loaded {} voiced command handlers.", VoicedCommandHandler.getInstance().size());
        StringUtil.printSection("Balancer");
        ClassBalanceManager.getInstance().load();
        SkillBalanceManager.getInstance().load();
        StringUtil.printSection("System");
        Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());
        ForumsBBSManager.getInstance().load();
        if (Config.DEADLOCK_DETECTOR) {
            LOGGER.info("Deadlock detector is enabled. Timer: {}s.", Config.DEADLOCK_CHECK_INTERVAL);
            DeadLockDetector deadDetectThread = new DeadLockDetector();
            deadDetectThread.setDaemon(true);
            deadDetectThread.start();
        } else {
            LOGGER.info("Deadlock detector is disabled.");
        }

        System.gc();
        LOGGER.info("Gameserver has started, used memory: {} / {} Mo.", SysUtil.getUsedMemory(), SysUtil.getMaxMemory());
        LOGGER.info("Maximum allowed players: {}.", Config.MAXIMUM_ONLINE_USERS);
        StringUtil.printSection("Login");
        LoginServerThread.getInstance().start();
        SelectorConfig sc = new SelectorConfig();
        sc.MAX_READ_PER_PASS = Config.MMO_MAX_READ_PER_PASS;
        sc.MAX_SEND_PER_PASS = Config.MMO_MAX_SEND_PER_PASS;
        sc.SLEEP_TIME = Config.MMO_SELECTOR_SLEEP_TIME;
        sc.HELPER_BUFFER_COUNT = Config.MMO_HELPER_BUFFER_COUNT;

        L2GamePacketHandler handler = new L2GamePacketHandler();
        this._selectorThread = new SelectorThread<>(sc, handler, handler, handler, new IPv4Filter());
        InetAddress bindAddress = null;
        if (!Config.GAMESERVER_HOSTNAME.equals("*")) {
            try {
                bindAddress = InetAddress.getByName(Config.GAMESERVER_HOSTNAME);
            } catch (Exception var7) {
                LOGGER.error("The GameServer bind address is invalid, using all available IPs.", var7);
            }
        }

        try {
            this._selectorThread.openServerSocket(bindAddress, Config.PORT_GAME);
        } catch (Exception var6) {
            LOGGER.error("Failed to open server socket.", var6);
            System.exit(1);
        }

        this._selectorThread.start();
    }

    public static void main(String[] args) throws Exception {
        _gameServer = new GameServer();
    }

    public static GameServer getInstance() {
        return _gameServer;
    }

    public SelectorThread<GameClient> getSelectorThread() {
        return this._selectorThread;
    }

}