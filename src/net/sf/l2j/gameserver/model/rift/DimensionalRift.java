package net.sf.l2j.gameserver.model.rift;

import net.sf.l2j.Config;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.data.manager.DimensionalRiftManager;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.group.Party;
import net.sf.l2j.gameserver.network.serverpackets.Earthquake;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class DimensionalRift {
    protected final Set<Byte> _completedRooms = ConcurrentHashMap.newKeySet();

    protected final Set<Player> _revivedInWaitingRoom = ConcurrentHashMap.newKeySet();

    protected Party _party;

    protected DimensionalRiftRoom _room;
    protected byte _currentJumps = 0;
    private Future<?> _teleporterTimerTask;
    private Future<?> _spawnTimerTask;
    private Future<?> _earthQuakeTask;
    private boolean _hasJumped = false;

    public DimensionalRift(Party party, DimensionalRiftRoom room) {
        this._party = party;
        this._room = room;
        room.setPartyInside(true);
        party.setDimensionalRift(this);
        for (Player member : party.getMembers())
            member.teleToLocation(room.getTeleportLoc());
        prepareNextRoom();
    }

    public DimensionalRiftRoom getCurrentRoom() {
        return this._room;
    }

    public boolean isInCurrentRoomZone(WorldObject object) {
        return (this._room != null && this._room.checkIfInZone(object.getX(), object.getY(), object.getZ()));
    }

    protected List<Player> getAvailablePlayers(Party party) {
        if (party == null)
            return Collections.emptyList();
        return party.getMembers().stream().filter(p -> !this._revivedInWaitingRoom.contains(p)).collect(Collectors.toList());
    }

    protected void prepareNextRoom() {
        if (this._spawnTimerTask != null) {
            this._spawnTimerTask.cancel(false);
            this._spawnTimerTask = null;
        }
        if (this._teleporterTimerTask != null) {
            this._teleporterTimerTask.cancel(false);
            this._teleporterTimerTask = null;
        }
        if (this._earthQuakeTask != null) {
            this._earthQuakeTask.cancel(false);
            this._earthQuakeTask = null;
        }
        this._spawnTimerTask = ThreadPool.schedule(() -> this._room.spawn(), Config.RIFT_SPAWN_DELAY);
        long jumpTime = (Rnd.get(Config.RIFT_AUTO_JUMPS_TIME_MIN, Config.RIFT_AUTO_JUMPS_TIME_MAX) * 1000L);
        if (this._room.isBossRoom())
            jumpTime = (long) (jumpTime * Config.RIFT_BOSS_ROOM_TIME_MUTIPLY);
        this._earthQuakeTask = ThreadPool.schedule(() -> {
            for (Player member : getAvailablePlayers(this._party))
                member.sendPacket(new Earthquake(member.getX(), member.getY(), member.getZ(), 65, 9));
        }, jumpTime - 7000L);
        this._teleporterTimerTask = ThreadPool.schedule(() -> {
            this._room.unspawn();
            if (this._currentJumps < Config.RIFT_MAX_JUMPS && !this._party.wipedOut()) {
                this._currentJumps = (byte) (this._currentJumps + 1);
                chooseRoomAndTeleportPlayers(this._room.getType(), getAvailablePlayers(this._party), true);
                prepareNextRoom();
            } else {
                killRift();
            }
        }, jumpTime);
    }

    public void manualTeleport(Player player, Npc npc) {
        Party party = player.getParty();
        if (party == null || !party.isInDimensionalRift())
            return;
        if (!party.isLeader(player)) {
            DimensionalRiftManager.getInstance().showHtmlFile(player, "data/html/seven_signs/rift/NotPartyLeader.htm", npc);
            return;
        }
        if (this._currentJumps == Config.RIFT_MAX_JUMPS) {
            DimensionalRiftManager.getInstance().showHtmlFile(player, "data/html/seven_signs/rift/UsedAllJumps.htm", npc);
            return;
        }
        if (this._hasJumped) {
            DimensionalRiftManager.getInstance().showHtmlFile(player, "data/html/seven_signs/rift/AlreadyTeleported.htm", npc);
            return;
        }
        this._hasJumped = true;
        this._room.unspawn();
        chooseRoomAndTeleportPlayers(this._room.getType(), this._party.getMembers(), false);
        prepareNextRoom();
    }

    public void manualExitRift(Player player, Npc npc) {
        Party party = player.getParty();
        if (party == null || !party.isInDimensionalRift())
            return;
        if (!party.isLeader(player)) {
            DimensionalRiftManager.getInstance().showHtmlFile(player, "data/html/seven_signs/rift/NotPartyLeader.htm", npc);
            return;
        }
        killRift();
    }

    protected void chooseRoomAndTeleportPlayers(byte type, List<Player> players, boolean canUseBossRoom) {
        this._completedRooms.add(Byte.valueOf(this._room.getId()));
        List<DimensionalRiftRoom> list = DimensionalRiftManager.getInstance().getFreeRooms(type, canUseBossRoom).stream().filter(r -> !this._completedRooms.contains(Byte.valueOf(r.getId()))).collect(Collectors.toList());
        if (list.isEmpty()) {
            killRift();
            return;
        }
        this._room = Rnd.get(list);
        this._room.setPartyInside(true);
        for (Player member : players)
            member.teleToLocation(this._room.getTeleportLoc());
    }

    public void killRift() {
        if (this._party != null) {
            for (Player member : getAvailablePlayers(this._party))
                DimensionalRiftManager.getInstance().teleportToWaitingRoom(member);
            this._party.setDimensionalRift(null);
            this._party = null;
        }
        this._completedRooms.clear();
        this._revivedInWaitingRoom.clear();
        if (this._earthQuakeTask != null) {
            this._earthQuakeTask.cancel(false);
            this._earthQuakeTask = null;
        }
        if (this._teleporterTimerTask != null) {
            this._teleporterTimerTask.cancel(false);
            this._teleporterTimerTask = null;
        }
        if (this._spawnTimerTask != null) {
            this._spawnTimerTask.cancel(false);
            this._spawnTimerTask = null;
        }
        this._room.unspawn();
        this._room = null;
    }

    public void usedTeleport(Player player) {
        this._revivedInWaitingRoom.add(player);
        if (this._party.getMembersCount() - this._revivedInWaitingRoom.size() < Config.RIFT_MIN_PARTY_SIZE)
            killRift();
    }
}
