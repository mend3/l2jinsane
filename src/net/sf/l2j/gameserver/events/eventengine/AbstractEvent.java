package net.sf.l2j.gameserver.events.eventengine;

import net.sf.l2j.Config;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.sql.SpawnTable;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.enums.skills.AbnormalEffect;
import net.sf.l2j.gameserver.events.eventengine.event.CTF;
import net.sf.l2j.gameserver.events.eventengine.event.DM;
import net.sf.l2j.gameserver.events.eventengine.event.TvT;
import net.sf.l2j.gameserver.events.eventengine.manager.CtfEventManager;
import net.sf.l2j.gameserver.events.eventengine.manager.DmEventManager;
import net.sf.l2j.gameserver.events.eventengine.manager.TvTEventManager;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.spawn.L2Spawn;
import net.sf.l2j.gameserver.network.serverpackets.AbstractNpcInfo;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;

import java.util.*;
import java.util.concurrent.ScheduledFuture;

public abstract class AbstractEvent implements Runnable {
    private final String name;
    private final int id;
    private final Map<Player, Integer> playerScores;
    private final List<Location> teleportLocations;
    private final List<Npc> spawnedNpcs;
    private final int runningMinutes;
    protected final List<Player> players;
    protected final List<EventTeam> teams;
    protected EventResTask eventRes = null;
    protected EventInformation eventInfo = null;
    private EventState state;
    private ScheduledFuture<?> endTask = null;
    private ScheduledFuture<?> resTask = null;
    private ScheduledFuture<?> infoTask = null;
    private ScheduledFuture<?> checkSayTask = null;

    protected AbstractEvent(String name, int id, int runningMinutes) {
        this.name = name;
        this.id = id;
        this.state = EventState.INACTIVE;
        this.players = new ArrayList<>();
        this.teleportLocations = new ArrayList<>();
        this.teams = new ArrayList<>();
        this.playerScores = new HashMap<>();
        this.spawnedNpcs = new ArrayList<>();
        this.runningMinutes = runningMinutes;
    }

    protected int getTopScore() {
        int topScore = 0;
        for (Player player : this.playerScores.keySet()) {
            if (this.playerScores.get(player) > topScore)
                topScore = this.playerScores.get(player);
        }
        return topScore;
    }

    protected void rewardTopInDraw(Map<Integer, Integer> rewards) {
        int topScore = 0;
        for (EventTeam et : this.teams) {
            if (et.getScore() > topScore)
                topScore = et.getScore();
        }
        List<EventTeam> topTeams = new ArrayList<>();
        for (EventTeam et : this.teams) {
            if (et.getScore() == topScore)
                topTeams.add(et);
        }
        for (EventTeam et : topTeams) {
            for (int id : rewards.keySet()) {
                et.reward(id, rewards.get(id));
            }
        }
    }

    protected void rewardTopInDraw(int id, int count) {
        Map<Integer, Integer> rewards = new HashMap<>();
        rewards.put(id, count);
        rewardTopInDraw(rewards);
    }

    protected boolean draw() {
        int topScore = 0;
        EventTeam topTeam = null;
        for (EventTeam et : this.teams) {
            if (et.getScore() > topScore) {
                topScore = et.getScore();
                topTeam = et;
            }
        }
        if (topScore == 0 || topTeam == null)
            return false;
        for (EventTeam et : this.teams) {
            if (et.getScore() == topScore && et != topTeam)
                return true;
        }
        return false;
    }

    protected Npc spawnNpc(int npcId, Location location, String title) {
        Npc ret = null;
        NpcTemplate template = NpcData.getInstance().getTemplate(npcId);
        try {
            L2Spawn spawn = new L2Spawn(template);
            spawn.setLoc(location.getX(), location.getY(), location.getZ(), 0);
            spawn.setRespawnDelay(10);
            SpawnTable.getInstance().addSpawn(spawn, false);
            spawn.doSpawn(false);
            spawn.setRespawnState(false);
            spawn.getNpc().setTitle(title);
            spawn.getNpc().broadcastPacket(new AbstractNpcInfo.NpcInfo(spawn.getNpc(), null));
            ret = spawn.getNpc();
        } catch (Exception e) {
            System.out.println("Event Manager: Unable to spawn npc with id " + npcId + ".");
            e.printStackTrace();
        }
        if (ret != null)
            this.spawnedNpcs.add(ret);
        return ret;
    }

    protected void addTeam(String name, int color, Location location) {
        this.teams.add(new EventTeam(name, color, location));
    }

    public Location getRandomLocation() {
        return (this.teleportLocations.size() > 1) ? this.teleportLocations.get(Rnd.get(this.teleportLocations.size())) : this.teleportLocations.getFirst();
    }

    protected void increaseScore(Player player, int count) {
        if (this.playerScores.containsKey(player)) {
            this.playerScores.compute(player, (k, old) -> old == null ? 0 : old + count);
        } else {
            this.playerScores.put(player, count);
        }
        if (!this.teams.isEmpty())
            getTeam(player).increaseScore(1);
    }

    protected void abort() {
        announce("The event was canceled due to lack of participation.", true);
        cleanUp();
        switch (this) {
            case TvT tvT -> TvTEventManager.getInstance().onEventEnd(this);
            case CTF ctf -> CtfEventManager.getInstance().onEventEnd(this);
            case DM dm -> DmEventManager.getInstance().onEventEnd(this);
            default -> {
            }
        }
    }

    protected boolean enoughRegistered(int count) {
        return (this.players.size() >= count);
    }

    protected void rewardTopTeams(int top, Map<Integer, Integer> rewards) {
        List<EventTeam> temp = new ArrayList<>(this.teams);
        for (int i = 1; i <= top; i++) {
            EventTeam topTeam = null;
            int score = 0;
            for (EventTeam team : temp) {
                if (team.getScore() > score) {
                    topTeam = team;
                    score = team.getScore();
                }
            }
            if (topTeam == null)
                break;
            temp.remove(topTeam);
            for (int id : rewards.keySet()) {
                topTeam.reward(id, rewards.get(id));
            }
            topTeam = null;
            score = 0;
        }
    }

    protected void rewardTopTeams(int top, int id, int count) {
        Map<Integer, Integer> rewards = new HashMap<>();
        rewards.put(id, count);
        rewardTopTeams(top, rewards);
    }

    protected void announceTopTeams(int top) {
        List<EventTeam> temp = new ArrayList<>(this.teams);
        announce("The top team(s) of the event:", true);
        for (int i = 1; i <= top; i++) {
            EventTeam topTeam = null;
            int score = 0;
            for (EventTeam team : temp) {
                if (team.getScore() > score) {
                    topTeam = team;
                    score = team.getScore();
                }
            }
            if (topTeam == null)
                break;
            temp.remove(topTeam);
            announce(i + ". Team: " + i + " Score: " + topTeam.getName(), true);
            topTeam = null;
            score = 0;
        }
    }

    protected void rewardTop(int top, Map<Integer, Integer> rewards) {
        List<Player> temp = new ArrayList<>(this.players);
        for (int i = 1; i <= top; i++) {
            Player topPlayer = null;
            int score = 0;
            for (Player player : temp) {
                if (!this.playerScores.containsKey(player))
                    continue;
                if (this.playerScores.get(player) > score) {
                    topPlayer = player;
                    score = this.playerScores.get(player);
                }
            }
            if (topPlayer == null)
                break;
            temp.remove(topPlayer);
            for (int id : rewards.keySet()) {
                topPlayer.addItem("Event reward.", id, rewards.get(id), null, true);
            }
            topPlayer = null;
            score = 0;
        }
    }

    protected void rewardTop(int top, int id, int count) {
        Map<Integer, Integer> rewards = new HashMap<>();
        rewards.put(id, count);
        rewardTop(top, rewards);
    }

    protected void announceTop(int top) {
        List<Player> temp = new ArrayList<>(this.players);
        announce("The top player(s) of the event:", true);
        for (int i = 1; i <= top; i++) {
            Player topPlayer = null;
            int score = 0;
            for (Player player : temp) {
                if (!this.playerScores.containsKey(player))
                    continue;
                if (this.playerScores.get(player) > score) {
                    topPlayer = player;
                    score = this.playerScores.get(player);
                }
            }
            if (topPlayer == null)
                break;
            temp.remove(topPlayer);
            announce(i + ". Player: " + i + " Score: " + topPlayer.getName(), true);
            topPlayer = null;
            score = 0;
        }
    }

    protected void cleanUp() {
        this.state = EventState.INACTIVE;
        this.players.clear();
        this.playerScores.clear();
        for (EventTeam et : this.teams)
            et.clear();
        for (Npc spawn : this.spawnedNpcs)
            spawn.deleteMe();
        this.spawnedNpcs.clear();
        if (this.endTask != null)
            this.endTask.cancel(true);
        this.endTask = null;
        if (this.checkSayTask != null)
            this.checkSayTask.cancel(true);
        this.checkSayTask = null;
        if (this.resTask != null) {
            this.resTask.cancel(true);
            this.resTask = null;
        }
        if (this.infoTask != null) {
            this.infoTask.cancel(true);
            this.infoTask = null;
        }
        if (this.eventInfo != null) {
            Map<String, Integer> temp = new HashMap<>();
            for (String key : this.eventInfo.getReplacements().keySet())
                temp.put(key, 0);
            this.eventInfo.setReplacements(temp);
        }
        switch (this) {
            case TvT tvT -> TvTEventManager.getInstance().onEventEnd(this);
            case CTF ctf -> CtfEventManager.getInstance().onEventEnd(this);
            case DM dm -> DmEventManager.getInstance().onEventEnd(this);
            default -> {
            }
        }
    }

    protected void restorePlayer(Player player) {
        if (player.isOnline())
            if (this instanceof net.sf.l2j.gameserver.events.eventengine.event.TvT) {
                TvTEventManager.getInstance().restorePlayer(player);
            } else if (this instanceof net.sf.l2j.gameserver.events.eventengine.event.CTF) {
                CtfEventManager.getInstance().restorePlayer(player);
            } else if (this instanceof net.sf.l2j.gameserver.events.eventengine.event.DM) {
                DmEventManager.getInstance().restorePlayer(player);
            }
    }

    protected void restorePlayers() {
        for (Player player : this.players) {
            if (player.isOnline()) {
                switch (this) {
                    case TvT tvT -> {
                        TvTEventManager.getInstance().restorePlayer(player);
                    }
                    case CTF ctf -> {
                        CtfEventManager.getInstance().restorePlayer(player);
                    }
                    case DM dm -> DmEventManager.getInstance().restorePlayer(player);
                    default -> {
                    }
                }
            }
        }
        unparalizePlayers();
    }

    protected void end() {
        this.state = EventState.TELEPORTING;
        announce("The event has ended. Players will be teleported back in 10 seconds.", false);
        paralizePlayers();
        schedule(this::restorePlayers, 10);
        schedule(this::cleanUp, 11);
    }

    protected void onCheck() {
    }

    protected void start() {
        this.state = EventState.TELEPORTING;
        announce("The registrations have closed. The event has started.", true);
        announce("You will be teleported in 20 seconds. Get ready!", false);
        preparePlayers();
        schedule(this::teleportPlayers, 20);
    }

    protected void cancelEndTask() {
        this.endTask.cancel(true);
        this.endTask = null;
    }

    protected void cancelCheckTask() {
        this.checkSayTask.cancel(true);
        this.checkSayTask = null;
    }

    protected void teleportPlayers() {
        this.state = EventState.RUNNING;
        if (!this.teams.isEmpty()) {
            for (EventTeam team : this.teams)
                team.teleportTeam();
        } else {
            for (Player player : this.players)
                player.teleToLocation(getRandomLocation());
        }
        paralizePlayers();
        announce("You have been teleported to the event.", false);
        announce("The event will begin in 20 seconds!", false);
        schedule(this::begin, 20);
    }

    protected void begin() {
        if (!getStartingMsg().isEmpty()) {
            for (Player player : this.players) {
                player.sendPacket(new ExShowScreenMessage(getStartingMsg(), 3000, 2, false));
                player.sendPacket(new CreatureSay(player.getObjectId(), 1, "Simon Event", getStartingMsg()));
            }
            System.out.println(getStartingMsg());
        }
        unparalizePlayers();
        announce("The event has begun!", false);
        if (getId() == 4) {
            this.checkSayTask = ThreadPool.scheduleAtFixedRate(this::onCheck, 10000L, 30000L);
        } else {
            warnEventEnd(this.runningMinutes * 60);
            schedule(() -> warnEventEnd(this.runningMinutes * 60 / 2), this.runningMinutes * 60 / 2);
            schedule(() -> warnEventEnd(30), this.runningMinutes * 60 - 30);
            schedule(() -> warnEventEnd(5), this.runningMinutes * 60 - 5);
            schedule(() -> warnEventEnd(4), this.runningMinutes * 60 - 4);
            schedule(() -> warnEventEnd(3), this.runningMinutes * 60 - 3);
            schedule(() -> warnEventEnd(2), this.runningMinutes * 60 - 2);
            schedule(() -> warnEventEnd(1), this.runningMinutes * 60 - 1);
            this.endTask = schedule(this::end, this.runningMinutes * 60);
        }
        if (this.eventRes != null)
            this.resTask = ThreadPool.scheduleAtFixedRate(this.eventRes, 8000L, 8000L);
        if (this.eventInfo != null)
            this.infoTask = ThreadPool.scheduleAtFixedRate(this.eventInfo, 1000L, 1000L);
    }

    private void warnEventEnd(int seconds) {
        int mins = seconds / 60;
        int secs = seconds % 60;
        announce(((mins == 0) ? "" : (mins + " minute(s)")) + ((mins == 0) ? "" : (mins + " minute(s)")) + ((mins > 0 && secs > 0) ? " and " : "") + " remaining until the event ends.", false);
    }

    protected void unparalizePlayers() {
        for (Player player : this.players) {
            player.setIsParalyzed(false);
            player.stopAbnormalEffect(AbnormalEffect.ROOT);
        }
    }

    protected void paralizePlayers() {
        for (Player player : this.players) {
            player.setIsParalyzed(true);
            player.startAbnormalEffect(AbnormalEffect.ROOT);
        }
    }

    protected void addTeleportLocation(Location location) {
        this.teleportLocations.add(location);
    }

    protected void addTeleportLocation(int x, int y, int z) {
        this.teleportLocations.add(new Location(x, y, z));
    }

    protected void preparePlayers() {
        switch (this) {
            case TvT tvT -> TvTEventManager.getInstance().storePlayersData(this.players);
            case CTF ctf -> CtfEventManager.getInstance().storePlayersData(this.players);
            case DM dm -> DmEventManager.getInstance().storePlayersData(this.players);
            default -> {
            }
        }
        if (!this.teams.isEmpty())
            splitToTeams();
    }

    protected void splitToTeams() {
        int i = 0;
        for (Player player : this.players) {
            this.teams.get(i++).addPlayer(player);
            if (i > this.teams.size() - 1)
                i = 0;
        }
    }

    protected void openRegistrations() {
        this.state = EventState.REGISTERING;
        warnRegistrations(Config.EVENT_REGISTRATION_TIME * 60);
        schedule(() -> warnRegistrations(Config.EVENT_REGISTRATION_TIME * 60 / 2), Config.EVENT_REGISTRATION_TIME * 60 / 2);
        schedule(() -> warnRegistrations(30), Config.EVENT_REGISTRATION_TIME * 60 - 30);
        schedule(() -> warnRegistrations(5), Config.EVENT_REGISTRATION_TIME * 60 - 5);
    }

    private void warnRegistrations(int seconds) {
        int mins = seconds / 60;
        int secs = seconds % 60;
        announce("The registrations will close in " + ((mins == 0) ? "" : (mins + " minute(s)")) + ((mins > 0 && secs > 0) ? " and " : "") + ((secs == 0) ? "" : (secs + " second(s)")) + ".", true);
    }

    protected ScheduledFuture<?> schedule(Runnable task, int seconds) {
        return ThreadPool.schedule(task, (seconds * 1000L));
    }

    protected void announce(String msg, boolean global) {
        if (global) {
            Announce.announce(getName(), msg);
        } else {
            Announce.announce(getName(), msg, this.players);
        }
    }

    public boolean isAutoAttackable(Player attacker, Player target) {
        return false;
    }

    public void onKill(Player killer, Player victim) {
    }

    public boolean onSay(Player player, String text) {
        return true;
    }

    public void onInterract(Player player, Npc npc) {
    }

    public boolean canAttack(Player attacker, Player target) {
        return true;
    }

    public boolean canHeal(Player healer, Player target) {
        return true;
    }

    public boolean canUseItem(Player player, int itemId) {
        return true;
    }

    public boolean allowDiePacket(Player player) {
        return true;
    }

    public boolean isDisguisedEvent() {
        return false;
    }

    public EventTeam getTeam(Player player) {
        EventTeam ret = null;
        for (EventTeam team : this.teams) {
            if (team.inTeam(player)) {
                ret = team;
                break;
            }
        }
        return ret;
    }

    protected int getScore(Player player) {
        return this.playerScores.getOrDefault(player, 0);
    }

    public boolean isEventNpc(Npc npc) {
        return this.spawnedNpcs.contains(npc);
    }

    public boolean isInEvent(Player player) {
        return this.players.contains(player);
    }

    public List<Player> getPlayers() {
        return this.players;
    }

    public void registerPlayer(Player player) {
        this.players.add(player);
    }

    public void removePlayer(Player player) {
        this.players.remove(player);
    }

    public EventState getState() {
        return this.state;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getStartingMsg() {
        return "";
    }
}
