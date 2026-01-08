/**/
package net.sf.l2j.gameserver.events.soloboss;

import net.sf.l2j.Config;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.data.sql.SpawnTable;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.spawn.L2Spawn;
import net.sf.l2j.gameserver.network.serverpackets.Earthquake;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public final class SoloBossManager {
    private static final CLogger LOGGER = new CLogger(SoloBossManager.class.getName());
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");
    private static final int EVENT_CHECK_INTERVAL = 60000;
    private static final int REWARD_RADIUS = 2500;
    private static final int EARTHQUAKE_INTENSITY = 65;
    private static final int CORPSE_TIME = 300;
    private static final int BOSS_SPAWN_DELAY = 5000;
    private static final int REWARD_ITEM_ID;
    private static volatile boolean eventActive;
    private static volatile Npc _npc;

    static {
        REWARD_ITEM_ID = Config.SOLOBOSS_REWARD_ID;
        eventActive = false;
    }

    private ScheduledFuture<?> _task;
    private List<SoloBossCreatureHolder> bosses;
    private String _eventMessage;
    private int currentBossIndex;

    public static boolean isActive() {
        return eventActive;
    }

    private static void anunciarEventoStart(String msgEvent) {
        World.announceToOnlinePlayers(msgEvent, true);
        World.getInstance().getPlayers().stream().filter(Player::isOnline).forEach((player) -> player.sendPacket(new Earthquake(player.getX(), player.getY(), player.getZ(), 65, 10)));
    }

    private static boolean bossAlreadyExists() {
        return _npc != null;
    }

    private static void spawnBoss(SoloBossCreatureHolder boss) {
        try {
            NpcTemplate template = NpcData.getInstance().getTemplate(boss.npcId());
            if (template == null) {
                LOGGER.log(new LogRecord(Level.WARNING, "Boss template not found for ID: " + boss.npcId()));
                return;
            }

            L2Spawn spawn = new L2Spawn(template);
            spawn.setLoc(boss.x(), boss.y(), boss.z(), 0);
            spawn.setRespawnState(false);
            SpawnTable.getInstance().addSpawn(spawn, false);
            _npc = spawn.doSpawn(false);
            if (_npc != null) {
                _npc.spawnMe();
                World.announceToOnlinePlayers("SoloBoss Event: Spawned boss: " + _npc.getName(), true);
            }
        } catch (Exception var3) {
            LOGGER.log(new LogRecord(Level.WARNING, "SoloBoss: spawnBoss() exception: " + var3.getMessage()));
        }

    }

    private static boolean isCurrentBoss(Creature npc) {
        return npc != null && _npc != null && npc.getObjectId() == _npc.getObjectId();
    }

    private static void rewardPlayersInZone(List<Player> players) {
        players.forEach((player) -> player.addItem("solo boss", REWARD_ITEM_ID, 1, player, true));
    }

    private static void unSpawnNpc() {
        if (_npc != null) {
            _npc.decayMe();
            _npc = null;
        }

    }

    private static void notifyNearbyPlayers() {
        List<Player> nearbyPlayers = _npc.getKnownTypeInRadius(Player.class, 2500);
        nearbyPlayers.forEach((player) -> {
            player.sendMessage("The Solo Boss Event has ended. Thank you for participating!");
            player.sendPacket(new ExShowScreenMessage("Solo Boss Event has ended.", 5, 2, true));
            player.sendPacket(new Earthquake(player.getX(), player.getY(), player.getZ(), 65, 5));
        });
    }

    public static SoloBossManager getInstance() {
        return SoloBossManager.SingletonHolder._instance;
    }

    public void scheduleEvents() {
        if (this._task == null) {
            this._task = ThreadPool.scheduleAtFixedRate(new SoloBossManager.EventChecker(), 0L, 60000L);
        }

    }

    public String getEventMessage() {
        return this._eventMessage;
    }

    public void inicarContagemStart(String hours) {
        World.announceToOnlinePlayers("Solo Boss Event will begin !! ", false);
        this.startEvent(hours);
    }

    public void startEvent(String hour) {
        this.bosses = SoloBossData.getInstance().getBossesForHour(hour);
        this._eventMessage = SoloBossData.getInstance().getMessageEvent(hour);
        this.currentBossIndex = 0;
        if (bossAlreadyExists()) {
            LOGGER.log(new LogRecord(Level.WARNING, "A boss is already active. Event canceled."));
            eventActive = false;
        } else {
            World.announceToOnlinePlayers("SoloBoss Event: Starting.", true);
            anunciarEventoStart(this._eventMessage);
            LOGGER.log(new LogRecord(Level.INFO, "Starting the Solo Boss Event: " + hour));
            if (!this.bosses.isEmpty()) {
                spawnBoss(this.bosses.get(this.currentBossIndex));
            }

        }
    }

    public void onKill(Creature npc) {
        if (isCurrentBoss(npc)) {
            World.announceToOnlinePlayers("SoloBoss Event: Raidboss " + _npc.getName() + " has been killed", true);
            List<Player> nearbyPlayers = npc.getKnownTypeInRadius(Player.class, 2500);
            rewardPlayersInZone(nearbyPlayers);
            ++this.currentBossIndex;
            if (this.hasMoreBosses()) {
                this.scheduleNextBoss();
            } else {
                this.finalizeBossSequence();
            }

        }
    }

    private boolean hasMoreBosses() {
        return this.bosses != null && this.currentBossIndex < this.bosses.size();
    }

    private void scheduleNextBoss() {
        ThreadPool.schedule(() -> spawnBoss(this.bosses.get(this.currentBossIndex)), 5000L);
    }

    private void finalizeBossSequence() {
        if (_npc != null && _npc.getSpawn() != null) {
            _npc.getSpawn().getTemplate().setCorpseTime(300);
        }

        this.inicarContagem(0);
    }

    public void inicarContagem(int duration) {
        ThreadPool.schedule(new SoloBossManager.Countdown(duration), 0L);
    }

    private void endEvent() {
        World.announceToOnlinePlayers("SoloBoss Event: All bosses defeated. Event complete.", true);
        eventActive = false;
        if (_npc != null) {
            notifyNearbyPlayers();
        }

        this.cleanup();
    }

    private void cleanup() {
        unSpawnNpc();
        this.bosses = null;
        this.currentBossIndex = 0;
    }

    private static final class SingletonHolder {
        private static final SoloBossManager _instance = new SoloBossManager();
    }

    private final class EventChecker implements Runnable {
        public void run() {
            String currentTime = LocalTime.now().format(SoloBossManager.TIME_FORMATTER);
            Set<String> eventHours = SoloBossData.getInstance().getEventHours();
            eventHours.stream().filter((hour) -> currentTime.equals(hour) && !SoloBossManager.eventActive).findFirst().ifPresent(this::activateEvent);
        }

        private void activateEvent(String hour) {
            if (!SoloBossManager.isActive()) {
                SoloBossManager.eventActive = true;
                SoloBossManager.this.inicarContagemStart(hour);
            }
        }
    }

    protected final class Countdown implements Runnable {
        private final int _time;

        public Countdown(int time) {
            this._time = time;
        }

        public void run() {
            if (this._time > 0) {
                String countdownMsg = this._time + " second(s) to Start the Chaotic Pvp!";
                this.sendCountdownMessage(countdownMsg);
                ThreadPool.schedule(SoloBossManager.this.new Countdown(this._time - 1), 1000L);
            } else {
                SoloBossManager.this.endEvent();
            }

        }

        private void sendCountdownMessage(String message) {
            World.getInstance().getPlayers().stream().filter(Player::isOnline).forEach((player) -> player.sendPacket(new ExShowScreenMessage(message, 1000)));
            World.announceToOnlinePlayers(message, true);
        }
    }
}