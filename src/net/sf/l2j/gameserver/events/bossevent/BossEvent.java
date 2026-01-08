/**/
package net.sf.l2j.gameserver.events.bossevent;

import enginemods.main.data.ConfigData;
import net.sf.l2j.Config;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.sql.SpawnTable;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.spawn.L2Spawn;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage.SMPOS;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

public class BossEvent {
    protected static final Logger _log = Logger.getLogger(BossEvent.class.getName());
    public L2Spawn bossSpawn;
    public List<Location> locList = new ArrayList<>();
    public Location loc;
    public List<Integer> bossList = new ArrayList<>();
    public int bossId;
    public int objectId;
    public final List<Player> eventPlayers = new ArrayList<>();
    public boolean started;
    public boolean aborted;
    public ScheduledFuture<?> despawnBoss;
    public ScheduledFuture<?> countDownTask;
    public boolean bossKilled;
    public L2Spawn eventNpc;
    public long startTime;
    private BossEvent.EventState state;
    private Player lastAttacker;
    private Map<Integer, Integer> generalRewards;
    private Map<Integer, Integer> lastAttackerRewards;
    private Map<Integer, Integer> mainDamageDealerRewards;
    private String bossName;

    public static BossEvent getInstance() {
        return BossEvent.SingleTonHolder._instance;
    }

    public void load() {
        this.state = BossEvent.EventState.INACTIVE;
        this.started = false;
        this.aborted = false;
        this.lastAttacker = null;
        this.generalRewards = new HashMap<>();
        this.lastAttackerRewards = new HashMap<>();
        this.mainDamageDealerRewards = new HashMap<>();
        this.despawnBoss = null;
        this.countDownTask = null;
        this.bossName = "";
        this.bossKilled = false;
        this.eventNpc = null;
        _log.info("Boss Event loaded.");
    }

    public boolean addPlayer(Player player) {
        return this.eventPlayers.add(player);
    }

    public boolean removePlayer(Player player) {
        return this.eventPlayers.remove(player);
    }

    public boolean isRegistered(Player player) {
        return this.eventPlayers.contains(player);
    }

    public void teleToTown() {

        for (Player p : this.eventPlayers) {
            p.teleportTo(new Location(83374, 148081, -3407), 300);
        }

        this.setState(BossEvent.EventState.INACTIVE);
    }

    public void delay(int delay) {
        try {
            Thread.sleep((long) delay);
        } catch (InterruptedException var3) {
            var3.printStackTrace();
        }

    }

    public void reward(Player p, Map<Integer, Integer> rewardType) {

        for (Entry<Integer, Integer> entry : rewardType.entrySet()) {
            if (p.isVip()) {
                p.addItem("BossEventReward", entry.getKey(), entry.getValue() * ConfigData.VIP_DROP_EVENTS_MULTIPLIER, null, true);
            } else {
                p.addItem("BossEventReward", entry.getKey(), entry.getValue(), null, true);
            }
        }

    }

    public void rewardPlayers() {

        for (Player p : this.eventPlayers) {
            if (p.getBossEventDamage() > Config.BOSS_EVENT_MIN_DAMAGE_TO_OBTAIN_REWARD) {
                this.reward(p, this.generalRewards);
            } else {
                p.sendPacket(new ExShowScreenMessage("You didn't caused min damage to receive rewards!", 5000));
                int var10001 = Config.BOSS_EVENT_MIN_DAMAGE_TO_OBTAIN_REWARD;
                p.sendMessage("You didn't caused min damage to receive rewards! Min. Damage: " + var10001 + ". Your Damage: " + p.getBossEventDamage());
            }
        }

        if (Config.BOSS_EVENT_REWARD_MAIN_DAMAGE_DEALER && this.getMainDamageDealer() != null) {
            this.reward(this.getMainDamageDealer(), this.mainDamageDealerRewards);
            this.getMainDamageDealer().sendPacket(new CreatureSay(0, 18, "[Boss Event]", "Congratulations, you was the damage dealer! So you will receive wonderful rewards."));
        }

    }

    public void finishEvent() {
        this.started = false;
        NextBossEvent.getInstance().startCalculationOfNextEventTime();
        this.rewardPlayers();
        if (this.bossKilled) {
            this.announce(this.bossName + " has been defeated!", false);
        }

        if (Config.BOSS_EVENT_REWARD_LAST_ATTACKER && this.lastAttacker != null) {
            this.announce("LastAttacker: " + this.lastAttacker.getName(), false);
        }

        if (Config.BOSS_EVENT_REWARD_MAIN_DAMAGE_DEALER && this.getMainDamageDealer() != null) {
            this.announce("Main Damage Dealer: " + this.getMainDamageDealer().getName() + ". Total Damage = " + this.getMainDamageDealer().getBossEventDamage(), false);
        }

        ThreadPool.schedule(() -> {
            BossEvent.this.teleToTown();
            BossEvent.this.eventPlayers.clear();
        }, Config.BOSS_EVENT_TIME_TO_TELEPORT_PLAYERS * 1000L);
        this.setState(BossEvent.EventState.FINISHING);
        this.startCountDown(Config.BOSS_EVENT_TIME_TO_TELEPORT_PLAYERS, true);
        if (this.despawnBoss != null) {
            this.despawnBoss.cancel(true);
            this.despawnBoss = null;
        }

        this.objectId = 0;
    }

    public void despawnNpc(L2Spawn spawn) {
        if (spawn != null) {
            spawn.getNpc().deleteMe();
            spawn.setRespawnState(false);
            SpawnTable.getInstance().deleteSpawn(spawn, true);
        }

    }

    public void startRegistration() {
        try {
            this.resetPlayersDamage();
            this.bossKilled = false;
            this.bossList = Config.BOSS_EVENT_ID;
            this.bossId = this.bossList.get(Rnd.get(this.bossList.size()));
            this.locList = Config.BOSS_EVENT_LOCATION;
            this.loc = this.locList.get(Rnd.get(this.locList.size()));
            if (NpcData.getInstance().getTemplate(this.bossId) == null) {
                _log.warning(getClass().getName() + ": cannot be started. Invalid BossId: " + this.bossList);
                return;
            }

            this.startTime = System.currentTimeMillis() + (Config.BOSS_EVENT_REGISTRATION_TIME * 1000L);
            this.eventNpc = this.spawnEventNpc(Config.BOSS_EVENT_NPC_REGISTER_LOC.getX(), Config.BOSS_EVENT_NPC_REGISTER_LOC.getY(), Config.BOSS_EVENT_NPC_REGISTER_LOC.getZ());
            this.generalRewards = Config.BOSS_EVENT_GENERAL_REWARDS;
            this.setLastAttackerRewards(Config.BOSS_EVENT_LAST_ATTACKER_REWARDS);
            this.mainDamageDealerRewards = Config.BOSS_EVENT_MAIN_DAMAGE_DEALER_REWARDS;
            this.started = true;
            this.aborted = false;
            this.bossName = NpcData.getInstance().getTemplate(this.bossId).getName();
            this.setState(BossEvent.EventState.REGISTRATION);
            this.announce("Registration started!", false);
            this.announce("Joinable in giran or use command \".bossevent\" to register to event", false);
            this.startCountDown(Config.BOSS_EVENT_REGISTRATION_TIME, false);
            ThreadPool.schedule(new BossEvent.Teleporting(this.eventPlayers, this.loc), Config.BOSS_EVENT_REGISTRATION_TIME * 1000L);
        } catch (Exception var2) {
            _log.warning("[Boss Event]: Couldn't be started");
            var2.printStackTrace();
        }

    }

    public int timeInMillisToStart() {
        return (int) (this.startTime - System.currentTimeMillis()) / 1000;
    }

    public void startCountDownEnterWorld(Player player) {
        if (this.getState() == BossEvent.EventState.REGISTRATION) {
            ThreadPool.schedule(new BossEvent.Countdown(player, this.timeInMillisToStart(), this.getState()), 0L);
        }

    }

    public boolean spawnNpc(int npcId, int x, int y, int z) {
        NpcTemplate tmpl = NpcData.getInstance().getTemplate(npcId);

        try {
            this.bossSpawn = new L2Spawn(tmpl);
            this.bossSpawn.setLoc(x, y, z, Rnd.get(65535));
            this.bossSpawn.setRespawnDelay(1);
            SpawnTable.getInstance().addSpawn(this.bossSpawn, false);
            this.bossSpawn.setRespawnState(false);
            this.bossSpawn.doSpawn(false);
            this.bossSpawn.getNpc().isAggressive();
            this.bossSpawn.getNpc().decayMe();
            this.bossSpawn.getNpc().spawnMe(this.bossSpawn.getNpc().getX(), this.bossSpawn.getNpc().getY(), this.bossSpawn.getNpc().getZ());
            this.bossSpawn.getNpc().broadcastPacket(new MagicSkillUse(this.bossSpawn.getNpc(), this.bossSpawn.getNpc(), 1034, 1, 1, 1));
            return true;
        } catch (Exception var7) {
            var7.printStackTrace();
            return false;
        }
    }

    public void resetPlayersDamage() {

        for (Player p : World.getInstance().getPlayers()) {
            p.setBossEventDamage(0);
        }

    }

    public L2Spawn spawnEventNpc(int x, int y, int z) {
        L2Spawn spawn = null;
        NpcTemplate tmpl = NpcData.getInstance().getTemplate(Config.BOSS_EVENT_REGISTRATION_NPC_ID);

        try {
            spawn = new L2Spawn(tmpl);
            spawn.setLoc(x, y, z, Rnd.get(65535));
            spawn.setRespawnDelay(1);
            SpawnTable.getInstance().addSpawn(spawn, false);
            spawn.setRespawnState(false);
            spawn.doSpawn(false);
            spawn.getNpc().isAggressive();
            spawn.getNpc().decayMe();
            spawn.getNpc().spawnMe(spawn.getNpc().getX(), spawn.getNpc().getY(), spawn.getNpc().getZ());
            spawn.getNpc().broadcastPacket(new MagicSkillUse(spawn.getNpc(), spawn.getNpc(), 1034, 1, 1, 1));
            return spawn;
        } catch (Exception var7) {
            var7.printStackTrace();
            return spawn;
        }
    }

    public final Player getMainDamageDealer() {
        int dmg = 0;
        Player mainDamageDealer = null;

        for (Player p : this.eventPlayers) {
            if (p.getBossEventDamage() > dmg) {
                dmg = p.getBossEventDamage();
                mainDamageDealer = p;
            }
        }

        return mainDamageDealer;
    }

    public void startCountDown(int time, boolean eventOnly) {
        Collection<Player> players = eventOnly ? this.eventPlayers : World.getInstance().getPlayers();

        for (Player player : players) {
            ThreadPool.schedule(new Countdown(player, time, this.getState()), 0L);
        }

    }

    public void announce(String text, boolean eventOnly) {
        Collection<Player> players = eventOnly ? this.eventPlayers : World.getInstance().getPlayers();

        for (Player player : players) {
            player.sendPacket(new CreatureSay(0, 18, "[Boss Event]", text));
        }

    }

    public void announceScreen(String text, boolean eventOnly) {
        Collection<Player> players = eventOnly ? this.eventPlayers : World.getInstance().getPlayers();

        for (Player player : players) {
            player.sendPacket(new ExShowScreenMessage(text, 4000));
        }

    }

    public BossEvent.EventState getState() {
        return this.state;
    }

    public void setState(BossEvent.EventState state) {
        this.state = state;
    }

    public Player getLastAttacker() {
        return this.lastAttacker;
    }

    public void setLastAttacker(Player lastAttacker) {
        this.lastAttacker = lastAttacker;
    }

    public Map<Integer, Integer> getLastAttackerRewards() {
        return this.lastAttackerRewards;
    }

    public void setLastAttackerRewards(Map<Integer, Integer> lastAttackerRewards) {
        this.lastAttackerRewards = lastAttackerRewards;
    }

    public enum EventState {
        REGISTRATION,
        TELEPORTING,
        WAITING,
        FIGHTING,
        FINISHING,
        INACTIVE
    }

    private static class SingleTonHolder {
        protected static final BossEvent _instance = new BossEvent();
    }

    class Teleporting implements Runnable {
        final Location teleTo;
        final List<Player> toTeleport;

        public Teleporting(List<Player> toTeleport, Location teleTo) {
            this.teleTo = teleTo;
            this.toTeleport = toTeleport;
        }

        public void run() {
            if (BossEvent.this.eventPlayers.size() >= Config.BOSS_EVENT_MIN_PLAYERS) {
                BossEvent.this.despawnNpc(BossEvent.this.eventNpc);
                BossEvent.this.setState(BossEvent.EventState.TELEPORTING);
                BossEvent.this.announce("Event Started!", false);
                BossEvent.this.startCountDown(Config.BOSS_EVENT_TIME_TO_TELEPORT_PLAYERS, true);

                for (Player p : this.toTeleport) {
                    ThreadPool.schedule(() -> p.teleportTo(Teleporting.this.teleTo, 300), Config.BOSS_EVENT_TIME_TO_TELEPORT_PLAYERS * 1000L);
                }

                BossEvent.this.delay(Config.BOSS_EVENT_TIME_TO_TELEPORT_PLAYERS * 1000);
                BossEvent.this.setState(BossEvent.EventState.WAITING);
                BossEvent.this.startCountDown(Config.BOSS_EVENT_TIME_TO_WAIT, true);
                ThreadPool.schedule(BossEvent.this.new Fighting(BossEvent.this.bossId, this.teleTo), Config.BOSS_EVENT_TIME_TO_WAIT * 1000L);
            } else {
                BossEvent.this.announce("Event was cancelled due to lack of participation!", false);
                BossEvent.this.setState(BossEvent.EventState.INACTIVE);
                BossEvent.this.despawnNpc(BossEvent.this.eventNpc);
                BossEvent.this.eventPlayers.clear();
                BossEvent.this.objectId = 0;
            }

        }
    }

    protected class Countdown implements Runnable {
        private final Player _player;
        private final int _time;
        final BossEvent.EventState evtState;
        private String text = "";

        public Countdown(Player player, int time, BossEvent.EventState evtState) {
            this._time = time;
            this._player = player;
            switch (evtState.ordinal()) {
                case 0:
                    this.text = "Boss Event registration ends in: ";
                    break;
                case 1:
                    this.text = "You will be teleported to Boss Event in: ";
                    break;
                case 2:
                    this.text = "Boss will spawn in: ";
                    break;
                case 4:
                    this.text = "You will be teleported to City in: ";
                    break;
                case 3:
                default:
                    break;
            }

            this.evtState = evtState;
        }

        public void run() {
            if (BossEvent.this.getState() != BossEvent.EventState.INACTIVE) {
                if (this._player.isOnline()) {
                    switch (this.evtState.ordinal()) {
                        case 0:
                        case 1:
                        case 2:
                        case 4:
                            switch (this._time) {
                                case 1:
                                case 2:
                                case 3:
                                case 4:
                                case 5:
                                case 10:
                                case 15:
                                case 30:
                                case 45:
                                    this._player.sendPacket(new CreatureSay(0, 18, "[Boss Event]", this.text + this._time + " second(s)"));
                                    break;
                                case 60:
                                case 120:
                                case 180:
                                case 240:
                                case 300:
                                    this._player.sendPacket(new CreatureSay(0, 18, "[Boss Event]", this.text + this._time / 60 + " minute(s)"));
                            }

                            if (this._time > 1) {
                                ThreadPool.schedule(BossEvent.this.new Countdown(this._player, this._time - 1, this.evtState), 1000L);
                            }
                            break;
                        case 3:
                            int minutes = this._time / 60;
                            int second = this._time % 60;
                            String var10000 = String.valueOf(minutes < 10 ? "0" + minutes : minutes);
                            String timing = var10000 + ":" + (second < 10 ? "0" + second : second);
                            this._player.sendPacket(new ExShowScreenMessage("Time Left: " + timing, 1100, SMPOS.BOTTOM_RIGHT, true));
                            if (this._time > 1) {
                                ThreadPool.schedule(BossEvent.this.new Countdown(this._player, this._time - 1, this.evtState), 1000L);
                            }
                    }
                }

            }
        }
    }

    class DespawnBossTask implements Runnable {
        final L2Spawn spawn;

        public DespawnBossTask(L2Spawn spawn) {
            this.spawn = spawn;
        }

        public void run() {
            if (this.spawn != null) {
                BossEvent.this.announceScreen("Your time is over " + this.spawn.getNpc().getName() + " returned to his home!", true);
                BossEvent.this.announce("Your time is over " + this.spawn.getNpc().getName() + " returned to his home!", true);
                BossEvent.this.announce("You will be teleported to town.", true);
                BossEvent.this.despawnNpc(this.spawn);
                ThreadPool.schedule(() -> {
                    BossEvent.this.teleToTown();
                    BossEvent.this.eventPlayers.clear();
                    BossEvent.this.setState(EventState.INACTIVE);
                    BossEvent.this.objectId = 0;
                }, 10000L);
            }

        }
    }

    class Fighting implements Runnable {
        final int bossId;
        final Location spawnLoc;

        public Fighting(int bossId, Location spawnLoc) {
            this.bossId = bossId;
            this.spawnLoc = spawnLoc;
        }

        public void run() {
            if (BossEvent.this.spawnNpc(this.bossId, BossEvent.this.loc.getX(), BossEvent.this.loc.getY(), BossEvent.this.loc.getZ())) {
                BossEvent.this.setState(BossEvent.EventState.FIGHTING);
                if (Config.BOSS_EVENT_TIME_ON_SCREEN) {
                    BossEvent.this.startCountDown(Config.BOSS_EVENT_TIME_TO_DESPAWN_BOSS, true);
                }

                BossEvent.this.despawnBoss = ThreadPool.schedule(BossEvent.this.new DespawnBossTask(BossEvent.this.bossSpawn), Config.BOSS_EVENT_TIME_TO_DESPAWN_BOSS * 1000L);
                BossEvent.this.objectId = BossEvent.this.bossSpawn.getNpc().getObjectId();

                for (Player p : BossEvent.this.eventPlayers) {
                    p.sendPacket(new ExShowScreenMessage("Boss " + BossEvent.this.bossSpawn.getNpc().getName() + " has been spawned. Go and Defeat him!", 5000));
                }
            }

        }
    }

    class Registration implements Runnable {
        public void run() {
            BossEvent.this.startRegistration();
        }
    }
}