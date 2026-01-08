package mods.dungeon;

import mods.instance.Instance;
import mods.instance.InstanceManager;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.data.sql.SpawnTable;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.DungeonMob;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.memo.DungeonMemo;
import net.sf.l2j.gameserver.model.spawn.L2Spawn;
import net.sf.l2j.gameserver.network.serverpackets.ExShowScreenMessage;
import net.sf.l2j.gameserver.network.serverpackets.MagicSkillUse;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

public class Dungeon {
    private final DungeonTemplate template;

    private final List<Player> players;
    private final Instance instance;
    private final List<DungeonMob> mobs = new CopyOnWriteArrayList<>();
    private ScheduledFuture<?> dungeonCancelTask = null;
    private ScheduledFuture<?> nextTask = null;
    private ScheduledFuture<?> timerTask = null;
    private DungeonStage currentStage = null;
    private long stageBeginTime = 0L;

    public Dungeon(DungeonTemplate template, List<Player> players) {
        this.template = template;
        this.players = players;
        this.instance = InstanceManager.getInstance().createInstance();
        beginTeleport();
    }

    public void onPlayerDeath(Player player) {
        if (!this.players.contains(player))
            return;
        if (this.players.size() == 1) {
            ThreadPool.schedule(this::cancelDungeon, 5000L);
        } else {
            player.sendMessage("You will be ressurected if your team completes this stage.");
        }
    }

    public synchronized void onMobKill(DungeonMob mob) {
        if (!this.mobs.contains(mob))
            return;
        deleteMob(mob);
        if (this.mobs.isEmpty()) {
            if (this.dungeonCancelTask != null)
                this.dungeonCancelTask.cancel(false);
            if (this.timerTask != null)
                this.timerTask.cancel(true);
            if (this.nextTask != null)
                this.nextTask.cancel(true);
            for (Player player : this.players) {
                if (player.isDead())
                    player.doRevive();
            }
            getNextStage();
            if (this.currentStage == null) {
                rewardPlayers();
                if (this.template.rewardHtm().equals("NULL")) {
                    broadcastScreenMessage("You have completed the dungeon!", 5);
                    teleToTown();
                } else {
                    broadcastScreenMessage("You have completed the dungeon! Select your reward", 5);
                }
                InstanceManager.getInstance().deleteInstance(this.instance.getId());
                DungeonManager.getInstance().removeDungeon(this);
            } else {
                broadcastScreenMessage("You have completed stage " + (this.currentStage.order() - 1) + "! Next stage begins in 10 seconds.", 5);
                ThreadPool.schedule(this::teleToStage, 5000L);
                this.nextTask = ThreadPool.schedule(this::beginStage, 10000L);
            }
        }
    }

    private void rewardPlayers() {
        for (Player player : this.players) {
            if (player != null) {
                DungeonMemo.setVar(player, "dungeon_atleast1time", "true", -1L);
                for (Map.Entry<Integer, Integer> itemId : this.template.rewards().entrySet())
                    player.addItem("dungeon reward", itemId.getKey(), itemId.getValue(), player, true);
            }
        }
        if (!this.template.rewardHtm().equals("NULL")) {
            NpcHtmlMessage htm = new NpcHtmlMessage(0);
            htm.setFile(this.template.rewardHtm());
            for (Player player : this.players)
                player.sendPacket(htm);
        } else {
            for (Player player : this.players) {
                player.setInstance(InstanceManager.getInstance().getInstance(0), true);
                player.teleportTo(82635, 148798, -3464, 25);
            }
        }
    }

    private void teleToStage() {
        if (!this.currentStage.teleport())
            return;
        for (Player player : this.players)
            player.teleportTo(this.currentStage.location(), 25);
    }

    private void teleToTown() {
        for (Player player : this.players) {
            if (!player.isOnline() || player.getClient().isDetached())
                continue;
            DungeonManager.getInstance().getDungeonParticipants().remove(Integer.valueOf(player.getObjectId()));
            player.setDungeon(null);
            player.setInstance(InstanceManager.getInstance().getInstance(0), true);
            player.teleportTo(82635, 148798, -3464, 25);
        }
    }

    private void cancelDungeon() {
        for (Player player : this.players) {
            if (player.isDead())
                player.doRevive();
            player.abortAttack();
            player.abortCast();
        }
        for (DungeonMob mob : this.mobs)
            deleteMob(mob);
        broadcastScreenMessage("You have failed to complete the dungeon. You will be teleported back in 5 seconds.", 5);
        ThreadPool.schedule(this::teleToTown, 5000L);
        InstanceManager.getInstance().deleteInstance(this.instance.getId());
        DungeonManager.getInstance().removeDungeon(this);
        if (this.nextTask != null)
            this.nextTask.cancel(true);
        if (this.timerTask != null)
            this.timerTask.cancel(true);
        if (this.dungeonCancelTask != null)
            this.dungeonCancelTask.cancel(true);
    }

    private void deleteMob(DungeonMob mob) {
        if (!this.mobs.contains(mob))
            return;
        this.mobs.remove(mob);
        if (mob.getSpawn() != null)
            SpawnTable.getInstance().deleteSpawn(mob.getSpawn(), false);
        mob.deleteMe();
    }

    private void beginStage() {
        for (Iterator<Integer> iterator = this.currentStage.mobs().keySet().iterator(); iterator.hasNext(); ) {
            int mobId = iterator.next();
            spawnMob(mobId, this.currentStage.mobs().get(mobId));
        }
        this.stageBeginTime = System.currentTimeMillis();
        this.timerTask = ThreadPool.scheduleAtFixedRate(this::broadcastTimer, 5000L, 1000L);
        this.nextTask = null;
        this.dungeonCancelTask = ThreadPool.schedule(this::cancelDungeon, (60000L * this.currentStage.minutes()));
        broadcastScreenMessage("You have " + this.currentStage.minutes() + " minutes to finish stage " + this.currentStage.order() + "!", 5);
    }

    private void spawnMob(int mobId, List<Location> locations) {
        NpcTemplate template = NpcData.getInstance().getTemplate(mobId);
        try {
            for (Location loc : locations) {
                L2Spawn spawn = new L2Spawn(template);
                spawn.setLoc(loc.getX(), loc.getY(), loc.getZ(), 0);
                spawn.setRespawnDelay(1);
                spawn.setRespawnState(false);
                spawn.doSpawn(false);
                ((DungeonMob) spawn.getNpc()).setDungeon(this);
                spawn.getNpc().setInstance(this.instance, false);
                spawn.getNpc().broadcastStatusUpdate();
                this.mobs.add((DungeonMob) spawn.getNpc());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void teleportPlayers() {
        for (Player player : this.players) {
            player.setInstance(this.instance, true);
            player.setDungeon(this);
        }
        teleToStage();
        broadcastScreenMessage("Stage " + this.currentStage.order() + " begins in 10 seconds!", 5);
        this.nextTask = ThreadPool.schedule(this::beginStage, 10000L);
    }

    private void beginTeleport() {
        getNextStage();
        for (Player player : this.players) {
            player.broadcastPacket(new MagicSkillUse(player, 1050, 1, 10000, 10000));
            broadcastScreenMessage("You will be teleported in 10 seconds!", 3);
        }
        this.nextTask = ThreadPool.schedule(this::teleportPlayers, 10000L);
    }

    private void getNextStage() {
        this.currentStage = (this.currentStage == null) ? this.template.stages().get(1) : this.template.stages().get(this.currentStage.order() + 1);
    }

    private void broadcastTimer() {
        int secondsLeft = (int) ((this.stageBeginTime + (60000 * this.currentStage.minutes()) - System.currentTimeMillis()) / 1000L);
        int minutes = secondsLeft / 60;
        int seconds = secondsLeft % 60;
        ExShowScreenMessage packet = new ExShowScreenMessage(String.format("%02d:%02d", minutes, seconds), 1010, ExShowScreenMessage.SMPOS.BOTTOM_RIGHT, false);
        for (Player player : this.players)
            player.sendPacket(packet);
    }

    private void broadcastScreenMessage(String msg, int seconds) {
        ExShowScreenMessage packet = new ExShowScreenMessage(msg, seconds * 1000, ExShowScreenMessage.SMPOS.TOP_CENTER, false);
        for (Player player : this.players)
            player.sendPacket(packet);
    }

    public List<Player> getPlayers() {
        return this.players;
    }
}
