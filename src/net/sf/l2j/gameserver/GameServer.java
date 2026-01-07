package net.sf.l2j.gameserver;

import enginemods.main.EngineModsManager;
import mods.achievement.AchievementsManager;
import mods.combineItem.CombineItem;
import mods.dressme.DressMeData;
import mods.dungeon.DungeonManager;
import mods.instance.InstanceManager;
import mods.pvpZone.RandomZoneManager;
import mods.teleportInterface.TeleportLocationDataGK;
import net.sf.l2j.Config;
import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.mmocore.SelectorConfig;
import net.sf.l2j.commons.mmocore.SelectorThread;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.util.SysUtil;
import net.sf.l2j.gameserver.communitybbs.Manager.ForumsBBSManager;
import net.sf.l2j.gameserver.data.*;
import net.sf.l2j.gameserver.data.cache.CrestCache;
import net.sf.l2j.gameserver.data.cache.HtmCache;
import net.sf.l2j.gameserver.data.manager.*;
import net.sf.l2j.gameserver.data.sql.*;
import net.sf.l2j.gameserver.data.xml.*;
import net.sf.l2j.gameserver.events.bossevent.BossEvent;
import net.sf.l2j.gameserver.events.eventengine.manager.CtfEventManager;
import net.sf.l2j.gameserver.events.eventengine.manager.DmEventManager;
import net.sf.l2j.gameserver.events.eventengine.manager.TvTEventManager;
import net.sf.l2j.gameserver.events.partyfarm.InitialPartyFarm;
import net.sf.l2j.gameserver.events.partyfarm.PartyFarm;
import net.sf.l2j.gameserver.events.pvpevent.PvPEventManager;
import net.sf.l2j.gameserver.events.soloboss.SoloBossData;
import net.sf.l2j.gameserver.events.soloboss.SoloBossManager;
import net.sf.l2j.gameserver.events.tournament.ArenaEvent;
import net.sf.l2j.gameserver.events.tournament.ArenaTask;
import net.sf.l2j.gameserver.events.tournament.arenas.Arena2x2;
import net.sf.l2j.gameserver.events.tournament.arenas.Arena4x4;
import net.sf.l2j.gameserver.events.tournament.arenas.Arena9x9;
import net.sf.l2j.gameserver.geoengine.GeoEngine;
import net.sf.l2j.gameserver.handler.*;
import net.sf.l2j.gameserver.hwid.Hwid;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.boat.*;
import net.sf.l2j.gameserver.model.olympiad.Olympiad;
import net.sf.l2j.gameserver.model.olympiad.OlympiadGameManager;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoomList;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchWaitingList;
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

    public GameServer() throws Exception {
        (new File("./log")).mkdir();
        (new File("./log/chat")).mkdir();
        (new File("./log/console")).mkdir();
        (new File("./log/error")).mkdir();
        (new File("./log/gmaudit")).mkdir();
        (new File("./log/item")).mkdir();
        (new File("./data/crests")).mkdirs();
        FileInputStream is = new FileInputStream(new File("config/logging.properties"));

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
        StringUtil.printSection("Enchants");
        EnchantTable.getInstance().load();
        StringUtil.printSection("Admins");
        AdminData.getInstance().load();
        BookmarkTable.getInstance().load();
        MovieMakerManager.getInstance();
        PetitionManager.getInstance();
        StringUtil.printSection("Fake Pc's");
        FakePcsTable.getInstance().load();
        StringUtil.printSection("Agathions");
        AgathionData.getInstance().load();
        StringUtil.printSection("Characters");
        PlayerData.getInstance().load();
        PlayerInfoTable.getInstance().load();
        NewbieBuffData.getInstance().load();
        TeleportLocationData.getInstance().load();
        HtmCache.getInstance();
        PartyMatchWaitingList.getInstance();
        PartyMatchRoomList.getInstance();
        RaidPointManager.getInstance().load();
        CouponsManager.getInstance().load();
        StringUtil.printSection("Community server");
        if (Config.ENABLE_COMMUNITY_BOARD) {
            ForumsBBSManager.getInstance().initRoot();
        } else {
            LOGGER.info("Community server is disabled.");
        }

        StringUtil.printSection("Auto Pvp Zones");
        if (Config.ENABLE_AUTO_PVP_ZONE) {
            LOGGER.info("Auto Pvp Zones - Random Zone is active.");
            RandomZoneManager.getInstance().load();
        } else {
            LOGGER.info("Auto Pvp Zones - Random Zone is disabled.");
        }

        StringUtil.printSection("Clans");
        CrestCache.getInstance().load();
        ClanTable.getInstance().load();
        StringUtil.printSection("Castles & Clan Halls");
        CastleManager.getInstance().load();
        ClanHallManager.getInstance().load();
        DoorData.getInstance().spawn();

        StringUtil.printSection("Gk Interface");
        TeleportLocationDataGK.getInstance().load();
        StringUtil.printSection("EngineMods");
        EngineModsManager.init();
        StringUtil.printSection("Icons");
        IconsTable.getInstance().load();
        if (Config.ALLOW_DRESS_ME_SYSTEM) {
            StringUtil.printSection("Dress Me / Skins");
            DressMeData.getInstance().load();
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
        StringUtil.printSection("Daily Rewards");
        DailyLoginRewardManager.getInstance().load();
        DailyRewardManager.getInstance().load();
        StringUtil.printSection("PcBang Event");
        CouponsManager.getInstance().load();
        if (Config.PCB_ENABLE) {
            LOGGER.info("PcBang Enabled");
            ThreadPool.scheduleAtFixedRate(PcBang.getInstance(), (long) (Config.PCB_INTERVAL * 1000), (long) (Config.PCB_INTERVAL * 1000));
        } else {
            LOGGER.info("PcBang is disabled.");
        }

        StringUtil.printSection("Event Engine TvT - CTF - DM");
        CtfEventManager.getInstance().load();
        TvTEventManager.getInstance().load();
        DmEventManager.getInstance().load();
        StringUtil.printSection("Solo Boss Event");
        SoloBossData.getInstance().load();
        if (Config.SOLOBOSS_EVENT_ENABLE) {
            SoloBossManager.getInstance().scheduleEvents();
        }

        StringUtil.printSection("PvP Event");
        if (Config.PVP_EVENT_ENABLED) {
            LOGGER.info("PvP Event: is Started.");
            PvPEventManager.getInstance().load();
        } else {
            LOGGER.info("PvP Event: is disabled.");
        }

        StringUtil.printSection("Kill The Boss Event");
        BossEvent.getInstance().load();
        StringUtil.printSection("Tournament 2x2 4x4 9x9");
        if (Config.TOURNAMENT_EVENT_TIME) {
            LOGGER.info("Tournament Event is enabled.");
            ThreadPool.schedule(Arena2x2.getInstance(), 5000L);
            ThreadPool.schedule(Arena9x9.getInstance(), 5000L);
            ThreadPool.schedule(Arena4x4.getInstance(), 5000L);
            ArenaEvent.getInstance().StartCalculationOfNextEventTime();
        } else if (Config.TOURNAMENT_EVENT_START) {
            LOGGER.info("Tournament Event is enabled.");
            ArenaTask.spawnNpc1();
        } else {
            LOGGER.info("Tournament Event is disabled");
        }

        StringUtil.printSection("Party Farm Event");
        LOGGER.info("Evento Party Farm");
        if (Config.PARTY_FARM_BY_TIME_OF_DAY && !Config.START_PARTY) {
            InitialPartyFarm.getInstance().load();
            LOGGER.info("[Party Farm Time]: Enabled");
        } else if (Config.START_PARTY && !Config.PARTY_FARM_BY_TIME_OF_DAY) {
            LOGGER.info("[Start Spawn Party Farm]: Enabled");
            ThreadPool.schedule(new GameServer.SpawnMonsters(this), Config.NPC_SERVER_DELAY * 1000L);
        }

        StringUtil.printSection("Achievements");
        AchievementsManager.getInstance().load();
        InstanceManager.getInstance();
        StringUtil.printSection("Dungeon Manager");
        DungeonManager.getInstance().load();
        if (Config.ALLOW_WEDDING) {
            CoupleManager.getInstance().load();
        }

        if (Config.ALT_FISH_CHAMPIONSHIP_ENABLED) {
            FishingChampionshipManager.getInstance().load();
        }

        StringUtil.printSection("Hwid Manager");
        Hwid.Init();
        StringUtil.printSection("Combine Item Data");
        CombineDataXML.getInstance().load();
        CombineItem.getInstance().load();
        StringUtil.printSection("Handlers");
        LOGGER.info("Loaded {} admin command handlers.", new Object[]{AdminCommandHandler.getInstance().size()});
        LOGGER.info("Loaded {} bypass command handlers.", new Object[]{BypassHandler.getInstance().size()});
        LOGGER.info("Loaded {} chat handlers.", new Object[]{ChatHandler.getInstance().size()});
        LOGGER.info("Loaded {} item handlers.", new Object[]{ItemHandler.getInstance().size()});
        LOGGER.info("Loaded {} skill handlers.", new Object[]{SkillHandler.getInstance().size()});
        LOGGER.info("Loaded {} user command handlers.", new Object[]{UserCommandHandler.getInstance().size()});
        LOGGER.info("Loaded {} voiced command handlers.", new Object[]{VoicedCommandHandler.getInstance().size()});
        StringUtil.printSection("Balancer");
        ClassBalanceManager.getInstance().load();
        SkillBalanceManager.getInstance().load();
        StringUtil.printSection("Dolls Data");
        DollsData.getInstance().load();
        StringUtil.printSection("System");
        Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());
        ForumsBBSManager.getInstance().load();
        if (Config.DEADLOCK_DETECTOR) {
            LOGGER.info("Deadlock detector is enabled. Timer: {}s.", new Object[]{Config.DEADLOCK_CHECK_INTERVAL});
            DeadLockDetector deadDetectThread = new DeadLockDetector();
            deadDetectThread.setDaemon(true);
            deadDetectThread.start();
        } else {
            LOGGER.info("Deadlock detector is disabled.");
        }

        System.gc();
        LOGGER.info("Gameserver has started, used memory: {} / {} Mo.", new Object[]{SysUtil.getUsedMemory(), SysUtil.getMaxMemory()});
        LOGGER.info("Maximum allowed players: {}.", new Object[]{Config.MAXIMUM_ONLINE_USERS});
        StringUtil.printSection("Login");
        LoginServerThread.getInstance().start();
        SelectorConfig sc = new SelectorConfig();
        sc.MAX_READ_PER_PASS = Config.MMO_MAX_READ_PER_PASS;
        sc.MAX_SEND_PER_PASS = Config.MMO_MAX_SEND_PER_PASS;
        sc.SLEEP_TIME = Config.MMO_SELECTOR_SLEEP_TIME;
        sc.HELPER_BUFFER_COUNT = Config.MMO_HELPER_BUFFER_COUNT;
        L2GamePacketHandler handler = new L2GamePacketHandler();
        this._selectorThread = new SelectorThread(sc, handler, handler, handler, new IPv4Filter());
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

    public class SpawnMonsters implements Runnable {
        public SpawnMonsters(final GameServer param1) {
        }

        public void run() {
            PartyFarm._aborted = false;
            PartyFarm._started = true;
            PartyFarm.spawnMonsters();
        }
    }
}