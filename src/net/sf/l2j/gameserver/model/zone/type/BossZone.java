package net.sf.l2j.gameserver.model.zone.type;

import net.sf.l2j.Config;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.data.xml.MapRegionData;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.*;
import net.sf.l2j.gameserver.model.zone.ZoneType;
import net.sf.l2j.gameserver.network.serverpackets.CreatureSay;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BossZone extends ZoneType {
    private static final String SELECT_GRAND_BOSS_LIST = "SELECT * FROM grandboss_list WHERE zone = ?";
    private final Map<Integer, Long> _allowedPlayersEntryTime = new ConcurrentHashMap<>();
    private final Set<Integer> _allowedPlayers = ConcurrentHashMap.newKeySet();
    private final int[] _oustLoc = new int[3];
    private final Map<String, List<Player>> _zergMap = new ConcurrentHashMap<>();
    private int _invadeTime;
    private int _maxClanMembers;
    private int _maxNOClanMembers;

    public BossZone(int id) {
        super(id);

        try (
                Connection con = ConnectionPool.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT * FROM grandboss_list WHERE zone = ?");
        ) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    this.allowPlayerEntry(rs.getInt("player_id"));
                }
            }
        } catch (Exception e) {
            LOGGER.error("Couldn't load players for {}.", e, new Object[]{this.toString()});
        }

    }

    public void setParameter(String name, String value) {
        if (name.equals("InvadeTime")) {
            this._invadeTime = Integer.parseInt(value);
        } else if (name.equals("oustX")) {
            this._oustLoc[0] = Integer.parseInt(value);
        } else if (name.equals("oustY")) {
            this._oustLoc[1] = Integer.parseInt(value);
        } else if (name.equals("oustZ")) {
            this._oustLoc[2] = Integer.parseInt(value);
        } else if (name.equals("MaxClanMembers")) {
            this._maxClanMembers = Integer.parseInt(value);
        } else if (name.equals("MaxNOClanMembers")) {
            this._maxNOClanMembers = Integer.parseInt(value);
        } else {
            super.setParameter(name, value);
        }

    }

    protected void onEnter(Creature character) {
        character.setInsideZone(ZoneId.BOSS, true);
        if (character instanceof Player player) {
            player.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
            boolean _checkplayerCond = true;
            if (player.isGM() || this._invadeTime == 0 || Config.ALLOW_DIRECT_TP_TO_BOSS_ROOM) {
                _checkplayerCond = false;
            }

            if (player.getClan() == null && !player.isGM()) {
                String zergNOClan = "NoClan";
                this._zergMap.computeIfAbsent("NoClan", k -> new ArrayList<>());

                if ((this._zergMap.get("NoClan")).size() > this._maxNOClanMembers) {
                    player.sendMessage("Sorry only allowed " + this._maxNOClanMembers + " players without Clan.");
                    ThreadPool.execute(new KickPlayer(player));
                    return;
                }

                (this._zergMap.get("NoClan")).add(player);
                LOGGER.info("_zergMap size [enter]: " + (this._zergMap.get("NoClan")).size());
                player.sendPacket(new CreatureSay(0, 17, "Aviso: ", "_zergMap size [enter]: " + (this._zergMap.get("NoClan")).size()));
            }

            if (player.getClan() != null && !player.isGM()) {
                String zergClan1 = player.getClan().getName();
                this._zergMap.computeIfAbsent(zergClan1, k -> new ArrayList<>());

                if ((this._zergMap.get(zergClan1)).size() > this._maxClanMembers) {
                    player.sendMessage("Sorry only allowed " + this._maxClanMembers + " members by Clan.");
                    ThreadPool.execute(new KickPlayer(player));
                    return;
                }

                (this._zergMap.get(zergClan1)).add(player);
                LOGGER.info("_zergMap size [enter]: " + this._zergMap.get(zergClan1).size());
                player.sendPacket(new CreatureSay(0, 17, "Aviso: ", "_zergMap size [enter]: " + this._zergMap.get(zergClan1).size()));
            }

            if (_checkplayerCond) {
                int id = player.getObjectId();
                if (this._allowedPlayers.contains(id)) {
                    long entryTime = this._allowedPlayersEntryTime.remove(id);
                    if (entryTime > System.currentTimeMillis()) {
                        return;
                    }

                    this._allowedPlayers.remove(id);
                }

                if (this._oustLoc[0] != 0 && this._oustLoc[1] != 0 && this._oustLoc[2] != 0) {
                    player.teleportTo(this._oustLoc[0], this._oustLoc[1], this._oustLoc[2], 0);
                } else {
                    player.teleportTo(MapRegionData.TeleportType.TOWN);
                }
            }
        } else if (character instanceof Summon) {
            Player player = ((Summon) character).getOwner();
            if (player != null) {
                if (this._allowedPlayers.contains(player.getObjectId()) || player.isGM() || this._invadeTime == 0) {
                    return;
                }

                ((Summon) character).unSummon(player);
            }
        }

    }

    protected void onExit(Creature character) {
        character.setInsideZone(ZoneId.BOSS, false);
        if (character instanceof Playable) {
            if (character instanceof Player player) {
                player.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
                if (player.isGM() || this._invadeTime == 0) {
                    return;
                }

                if (player.getClan() != null) {
                    String zergClan1 = player.getClan().getName();
                    if (this._zergMap.get(zergClan1) != null && (this._zergMap.get(zergClan1)).contains(player)) {
                        (this._zergMap.get(zergClan1)).remove(player);
                        LOGGER.info("_zergMap size [exit]: " + this._zergMap.get(zergClan1).size());
                        player.sendPacket(new CreatureSay(0, 17, "Aviso: ", "_zergMap size [enter]: " + this._zergMap.get(zergClan1).size()));
                    }
                } else {
                    String zergNOClan = "NoClan";
                    if (this._zergMap.get("NoClan") != null && (this._zergMap.get("NoClan")).contains(player)) {
                        (this._zergMap.get("NoClan")).remove(player);
                        LOGGER.info("_zergMap size [exit]: " + (this._zergMap.get("NoClan")).size());
                        player.sendPacket(new CreatureSay(0, 17, "Aviso: ", "_zergMap size [enter]: " + (this._zergMap.get("NoClan")).size()));
                    }
                }

                int id = player.getObjectId();
                if (this._allowedPlayers.contains(id)) {
                    if (!player.isOnline()) {
                        this._allowedPlayersEntryTime.put(id, System.currentTimeMillis() + (long) this._invadeTime);
                    } else {
                        if (this._allowedPlayersEntryTime.containsKey(id)) {
                            return;
                        }

                        this._allowedPlayers.remove(id);
                    }
                }
            }

            if (!this._characters.isEmpty()) {
                if (!this.getKnownTypeInside(Playable.class).isEmpty()) {
                    return;
                }

                for (Attackable raid : this.getKnownTypeInside(Attackable.class)) {
                    if (raid.isRaidRelated()) {
                        raid.returnHome();
                    }
                }
            }
        } else if (character instanceof Attackable && character.isRaidRelated()) {
            ((Attackable) character).returnHome();
        }

    }

    public void allowPlayerEntry(Player player, int duration) {
        int playerId = player.getObjectId();
        this._allowedPlayers.add(playerId);

        this._allowedPlayersEntryTime.put(playerId, System.currentTimeMillis() + (duration * 1000L));
    }

    public void allowPlayerEntry(int playerId) {
        this._allowedPlayers.add(playerId);

        this._allowedPlayersEntryTime.put(playerId, System.currentTimeMillis() + (long) this._invadeTime);
    }

    public void removePlayer(Player player) {
        int id = player.getObjectId();
        this._allowedPlayers.remove(id);
        this._allowedPlayersEntryTime.remove(id);
    }

    public Set<Integer> getAllowedPlayers() {
        return this._allowedPlayers;
    }

    public List<Player> oustAllPlayers() {
        List<Player> players = this.getKnownTypeInside(Player.class);
        if (players.isEmpty()) {
            return players;
        } else {
            for (Player player : players) {
                if (player.isOnline()) {
                    if (this._oustLoc[0] != 0 && this._oustLoc[1] != 0 && this._oustLoc[2] != 0) {
                        player.teleportTo(this._oustLoc[0], this._oustLoc[1], this._oustLoc[2], 0);
                    } else {
                        player.teleportTo(MapRegionData.TeleportType.TOWN);
                    }
                }
            }

            this._allowedPlayersEntryTime.clear();
            this._allowedPlayers.clear();
            return players;
        }
    }

    public void onDieInside(Creature character) {
    }

    public void onReviveInside(Creature character) {
    }

    private static final class KickPlayer implements Runnable {
        private Player _player;

        public KickPlayer(Player player) {
            this._player = player;
        }

        public void run() {
            if (this._player != null) {
                Summon summon = this._player.getSummon();
                if (summon != null) {
                    summon.unSummon(this._player);
                }

                this._player.teleportTo(MapRegionData.TeleportType.TOWN);
                if (this._player.getClan() != null) {
                    this._player.setPvpFlag(0);
                    this._player.broadcastUserInfo();
                    this._player.setInsideZone(ZoneId.BOSS, false);
                    this._player.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
                    this._player.setInsideZone(ZoneId.NO_RESTART, false);
                }

                this._player = null;
            }

        }
    }
}
