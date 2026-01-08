package enginemods.main;

import enginemods.main.data.*;
import enginemods.main.engine.AbstractMods;
import enginemods.main.engine.events.BonusWeekend;
import enginemods.main.engine.events.Champions;
import enginemods.main.engine.events.CityElpys;
import enginemods.main.engine.events.RandomBossSpawn;
import enginemods.main.engine.mods.*;
import enginemods.main.engine.npc.NpcBufferScheme;
import enginemods.main.engine.npc.NpcClassMaster;
import enginemods.main.engine.npc.NpcRanking;
import enginemods.main.engine.npc.NpcTeleporter;
import enginemods.main.instances.NpcDropsInstance;
import enginemods.main.instances.NpcExpInstance;
import mods.achievement.AchievementsManager;
import mods.combineItem.CombineItem;
import mods.dressme.DressMeData;
import mods.dungeon.DungeonManager;
import mods.instance.InstanceManager;
import mods.pvpZone.RandomZoneManager;
import mods.teleportInterface.TeleportLocationDataGK;
import net.sf.l2j.Config;
import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.PcBang;
import net.sf.l2j.gameserver.data.*;
import net.sf.l2j.gameserver.data.manager.DailyLoginRewardManager;
import net.sf.l2j.gameserver.data.manager.DailyRewardManager;
import net.sf.l2j.gameserver.data.xml.FakePcsTable;
import net.sf.l2j.gameserver.data.xml.IconsTable;
import net.sf.l2j.gameserver.enums.skills.Stats;
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
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchRoomList;
import net.sf.l2j.gameserver.model.partymatching.PartyMatchWaitingList;
import net.sf.l2j.gameserver.model.zone.ZoneType;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

public class EngineModsManager {
    private static final Logger LOG = Logger.getLogger(EngineModsManager.class.getName());
    private static final Map<Integer, AbstractMods> ENGINES_MODS = new LinkedHashMap<>();

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void init() {
        ConfigData.load();
        IconsTable.load();
        ModsData.load();
        PlayerData.load();
        SkillData.load();
        DressMeData.getInstance().load();
        CombineDataXML.getInstance().load();
        CombineItem.getInstance().load();
        DollsData.getInstance().load();
        AgathionData.getInstance().load();
        AchievementsManager.getInstance().load();

        FakePlayerData.load();
        FakePlayer.getInstance();
        FakePcsTable.getInstance().load();

        SchemeBuffData.load();
        NpcBufferScheme.getInstance();

        NpcRanking.getInstance();
        NpcClassMaster.getInstance();

        NpcTeleporter.getInstance();
        TeleportLocationDataGK.getInstance().load();

        DailyRewardData.getInstance().load();
        DailyLoginRewardManager.getInstance().load();
        DailyRewardManager.getInstance();

        ColorAccordingAmountPvPorPk.getInstance();
        SubClassAcumulatives.getInstance();
        NewCharacterCreated.getInstance();
        EnchantAbnormalEffectArmor.getInstance();
        SpreeKills.getInstance();
        PvpReward.getInstance();
        SellBuffs.getInstance();
        AntiBot.getInstance();
        SystemAio.getInstance();
        SystemVip.getInstance();
        OfflineShop.getInstance();
        BonusWeekend.getInstance();
        Champions.getInstance();
        RandomBossSpawn.getInstance();
        CityElpys.getInstance();
        PartyMatchWaitingList.getInstance();
        PartyMatchRoomList.getInstance();
        if (Config.PCB_ENABLE) {
            CouponsManager.getInstance().load();
            ThreadPool.scheduleAtFixedRate(PcBang.getInstance(), Config.PCB_INTERVAL * 1000L, Config.PCB_INTERVAL * 1000L);
            LOG.info("PcBang Enabled");
        } else {
            LOG.info("PcBang is disabled.");
        }

        StringUtil.printSection("Dungeon Manager");
        InstanceManager.getInstance();
        DungeonManager.getInstance().load();

        StringUtil.printSection("Event Engine TvT - CTF - DM");
        CtfEventManager.getInstance();
        TvTEventManager.getInstance();
        DmEventManager.getInstance();
        StringUtil.printSection("Solo Boss Event");
        if (Config.SOLOBOSS_EVENT_ENABLE) {
            SoloBossData.getInstance().load();
            SoloBossManager.getInstance().scheduleEvents();
        } else {
            LOG.info("Solo Boss Event: is disabled.");
        }

        StringUtil.printSection("Kill The Boss Event");
        AnnounceKillBoss.getInstance();
        BossEvent.getInstance().load();
        if (Config.TOURNAMENT_EVENT_TIME) {
            StringUtil.printSection("Tournament 2x2 4x4 9x9");
            ThreadPool.schedule(Arena2x2.getInstance(), 5000L);
            ThreadPool.schedule(Arena9x9.getInstance(), 5000L);
            ThreadPool.schedule(Arena4x4.getInstance(), 5000L);
            ArenaEvent.getInstance().StartCalculationOfNextEventTime();
            LOG.info("Tournament Event is enabled.");
        } else if (Config.TOURNAMENT_EVENT_START) {
            ArenaTask.spawnNpc1();
            LOG.info("Tournament Event is enabled.");
        } else {
            LOG.info("Tournament Event is disabled");
        }

        StringUtil.printSection("Party Farm Event");
        if (Config.PARTY_FARM_BY_TIME_OF_DAY && !Config.START_PARTY) {
            InitialPartyFarm.getInstance();
            LOG.info("[Party Farm Time]: Enabled");
        } else if (Config.START_PARTY && !Config.PARTY_FARM_BY_TIME_OF_DAY) {
            ThreadPool.schedule(new PartyFarmSpawn(), Config.NPC_SERVER_DELAY * 1000L);
            LOG.info("[Party Farm Spawn]: Enabled");
        }

        StringUtil.printSection("PvP Event");
        if (Config.PVP_EVENT_ENABLED) {
            PvPEventManager.getInstance().load();
            LOG.info("PvP Event: is Started.");
        } else {
            LOG.info("PvP Event: is disabled.");
        }

        StringUtil.printSection("Auto Pvp Zones");
        if (Config.ENABLE_AUTO_PVP_ZONE) {
            RandomZoneManager.getInstance().load();
            LOG.info("Auto Pvp Zones - Random Zone is active.");
        } else {
            LOG.info("Auto Pvp Zones - Random Zone is disabled.");
        }

        LOG.info(String.format("Loaded %s Mod engines.", getAllMods().size()));
    }

    public static void registerMod(AbstractMods type) {
        ENGINES_MODS.put(type.hashCode(), type);
    }

    public static Collection<AbstractMods> getAllMods() {
        return ENGINES_MODS.values();
    }

    public static AbstractMods getMod(int type) {
        return ENGINES_MODS.get(type);
    }

    public static synchronized boolean onCommunityBoard(Player player, String command) {

        for (AbstractMods mod : ENGINES_MODS.values()) {
            if (mod.isStarting() && mod.onCommunityBoard(player, command)) {
                return true;
            }
        }

        return false;
    }

    public static synchronized void onShutDown() {
        ENGINES_MODS.values().stream().filter(AbstractMods::isStarting).forEach((mod) -> {
            mod.onShutDown();
            mod.endMod();
            mod.cancelScheduledState();
        });
    }

    public static synchronized boolean onExitWorld(Player player) {
        boolean exitPlayer = false;

        for (AbstractMods mod : ENGINES_MODS.values()) {
            if (mod.isStarting() && mod.onExitWorld(player)) {
                exitPlayer = true;
            }
        }

        return exitPlayer;
    }

    public static synchronized boolean onNpcExpSp(Monster npc, Creature character) {
        if (character == null) {
            return false;
        } else {
            Player killer = character.getActingPlayer();
            if (killer == null) {
                return false;
            } else {
                NpcExpInstance instance = new NpcExpInstance();
                for (AbstractMods mod : ENGINES_MODS.values()) {
                    if (mod.isStarting()) {
                        mod.onNpcExpSp(killer, npc, instance);
                    }
                }

                if (instance.hasSettings()) {
                    instance.init(npc, character);
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    public static synchronized boolean onNpcDrop(Monster npc, Creature character) {
        if (character == null) {
            return false;
        } else {
            Player killer = character.getActingPlayer();
            if (killer == null) {
                return false;
            } else {
                NpcDropsInstance instance = new NpcDropsInstance();
                for (AbstractMods mod : ENGINES_MODS.values()) {
                    if (mod.isStarting()) {
                        mod.onNpcDrop(killer, npc, instance);
                    }
                }

                if (instance.hasSettings()) {
                    instance.init(npc, character);
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    public static synchronized void onEnterZone(Creature player, ZoneType zone) {
        ENGINES_MODS.values().stream().filter(AbstractMods::isStarting).forEach((mod) -> mod.onEnterZone(player, zone));
    }

    public static synchronized void onExitZone(Creature player, ZoneType zone) {
        ENGINES_MODS.values().stream().filter(AbstractMods::isStarting).forEach((mod) -> mod.onExitZone(player, zone));
    }

    public static synchronized void onCreateCharacter(Player player) {
        ENGINES_MODS.values().stream().filter(AbstractMods::isStarting).forEach((mod) -> mod.onCreateCharacter(player));
    }

    public static synchronized boolean onVoiced(Player player, String chat) {
        for (AbstractMods mod : ENGINES_MODS.values()) {
            if (mod.isStarting()) {
                if (chat.startsWith("admin_")) {
                    if (player.getAccessLevel().getLevel() < 1) {
                        return false;
                    }

                    if (mod.onAdminCommand(player, chat.replace("admin_", ""))) {
                        return true;
                    }
                } else if (chat.startsWith(".")) {
                    if (mod.onVoicedCommand(player, chat.replace(".", ""))) {
                        return true;
                    }
                } else if (mod.onChat(player, chat)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static synchronized boolean onInteract(Player player, Creature character) {
        for (AbstractMods mod : ENGINES_MODS.values()) {
            if (mod.isStarting()) {
                if (mod.onInteract(player, character)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static synchronized void onEvent(Player player, String command) {
        ENGINES_MODS.values().stream().filter((mod) -> command.startsWith(mod.getClass().getSimpleName()) && mod.isStarting()).forEach((mod) -> {
            WorldObject obj = player.getTarget();
            if (obj instanceof Creature) {
                if (player.isInsideRadius(obj, 150, false, false)) {
                    mod.onEvent(player, (Creature) obj, command.replace(mod.getClass().getSimpleName() + " ", ""));
                }
            }
        });
    }

    public static synchronized String onSeeNpcTitle(int objectId) {
        String title = null;
        for (AbstractMods mod : ENGINES_MODS.values()) {
            if (mod.isStarting()) {
                String aux = mod.onSeeNpcTitle(objectId);
                if (aux != null) {
                    title = aux;
                }
            }
        }

        return title;
    }

    public static synchronized void onSpawn(Npc obj) {
        ENGINES_MODS.values().stream().filter(AbstractMods::isStarting).forEach((mod) -> mod.onSpawn(obj));
    }

    public static synchronized void onEnterWorld(Player player) {
        ENGINES_MODS.values().stream().filter(AbstractMods::isStarting).forEach((mod) -> mod.onEnterWorld(player));
    }

    public static synchronized void onKill(Creature killer, Creature victim, boolean isPet) {
        ENGINES_MODS.values().stream().filter(AbstractMods::isStarting).forEach((mod) -> mod.onKill(killer, victim, isPet));
    }

    public static synchronized void onDeath(Creature player) {
        ENGINES_MODS.values().stream().filter(AbstractMods::isStarting).forEach((mod) -> mod.onDeath(player));
    }

    public static synchronized void onEnchant(Creature player) {
        ENGINES_MODS.values().stream().filter(AbstractMods::isStarting).forEach((mod) -> mod.onEnchant(player));
    }

    public static synchronized void onEquip(Creature player) {
        ENGINES_MODS.values().stream().filter(AbstractMods::isStarting).forEach((mod) -> mod.onEquip(player));
    }

    public static synchronized void onUnequip(Creature player) {
        ENGINES_MODS.values().stream().filter(AbstractMods::isStarting).forEach((mod) -> mod.onUnequip(player));
    }

    public static synchronized void onRestoreSkills(Player player) {
        ENGINES_MODS.values().stream().filter(AbstractMods::isStarting).forEach((mod) -> mod.onRestoreSkills(player));
    }

    public static synchronized double onStats(Stats stat, Creature character, double value) {

        for (AbstractMods mod : ENGINES_MODS.values()) {
            if (mod.isStarting() && character != null) {
                value += mod.onStats(stat, character, value) - value;
            }
        }

        return value;
    }

    public static class PartyFarmSpawn implements Runnable {
        public PartyFarmSpawn() {
        }

        public void run() {
            PartyFarm._aborted = false;
            PartyFarm._started = true;
            PartyFarm.spawnMonsters();
        }
    }
}