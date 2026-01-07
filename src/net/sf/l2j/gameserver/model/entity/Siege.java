/**/
package net.sf.l2j.gameserver.model.entity;

import net.sf.l2j.Config;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.commons.util.ArraysUtil;
import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.data.manager.HeroManager;
import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.enums.SiegeSide;
import net.sf.l2j.gameserver.enums.SiegeStatus;
import net.sf.l2j.gameserver.model.World;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.ControlTower;
import net.sf.l2j.gameserver.model.actor.instance.FlameTower;
import net.sf.l2j.gameserver.model.location.TowerSpawnLocation;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.pledge.ClanMember;
import net.sf.l2j.gameserver.model.spawn.L2Spawn;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2j.gameserver.network.serverpackets.UserInfo;
import net.sf.l2j.gameserver.scripting.Quest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

public class Siege implements Siegable {
    protected static final CLogger LOGGER = new CLogger(Siege.class.getName());
    private static final String LOAD_SIEGE_CLAN = "SELECT clan_id,type FROM siege_clans WHERE castle_id=?";
    private static final String CLEAR_SIEGE_CLANS = "DELETE FROM siege_clans WHERE castle_id=?";
    private static final String CLEAR_PENDING_CLANS = "DELETE FROM siege_clans WHERE castle_id=? AND type='PENDING'";
    private static final String CLEAR_SIEGE_CLAN = "DELETE FROM siege_clans WHERE castle_id=? AND clan_id=?";
    private static final String UPDATE_SIEGE_INFOS = "UPDATE castle SET siegeDate=?, regTimeOver=? WHERE id=?";
    private static final String ADD_OR_UPDATE_SIEGE_CLAN = "INSERT INTO siege_clans (clan_id,castle_id,type) VALUES (?,?,?) ON DUPLICATE KEY UPDATE type=VALUES(type)";
    private final Map<Clan, SiegeSide> _registeredClans = new ConcurrentHashMap();
    private final Castle _castle;
    private final List<ControlTower> _controlTowers = new ArrayList();
    private final List<FlameTower> _flameTowers = new ArrayList();
    private final List<Npc> _destroyedTowers = new ArrayList();
    protected Calendar _siegeEndDate;
    protected ScheduledFuture<?> _siegeTask;
    private Clan _formerOwner;
    private SiegeStatus _siegeStatus;
    private List<Quest> _questEvents;

    public Siege(Castle castle) {
        this._siegeStatus = SiegeStatus.REGISTRATION_OPENED;
        this._questEvents = Collections.emptyList();
        this._castle = castle;
        if (this._castle.getOwnerId() > 0) {
            Clan clan = ClanTable.getInstance().getClan(this.getCastle().getOwnerId());
            if (clan != null) {
                this._registeredClans.put(clan, SiegeSide.OWNER);
            }
        }

        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("SELECT clan_id,type FROM siege_clans WHERE castle_id=?");

                try {
                    ps.setInt(1, this._castle.getCastleId());
                    ResultSet rs = ps.executeQuery();

                    try {
                        while (rs.next()) {
                            Clan clan = ClanTable.getInstance().getClan(rs.getInt("clan_id"));
                            if (clan != null) {
                                this._registeredClans.put(clan, Enum.valueOf(SiegeSide.class, rs.getString("type")));
                            }
                        }
                    } catch (Throwable var10) {
                        if (rs != null) {
                            try {
                                rs.close();
                            } catch (Throwable var9) {
                                var10.addSuppressed(var9);
                            }
                        }

                        throw var10;
                    }

                    if (rs != null) {
                        rs.close();
                    }
                } catch (Throwable var11) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var8) {
                            var11.addSuppressed(var8);
                        }
                    }

                    throw var11;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (Throwable var12) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var7) {
                        var12.addSuppressed(var7);
                    }
                }

                throw var12;
            }

            if (con != null) {
                con.close();
            }
        } catch (Exception var13) {
            LOGGER.error("Couldn't load siege registered clans.", var13);
        }

        this.startAutoTask();
    }

    public void startSiege() {
        if (!this.isInProgress()) {
            if (this.getAttackerClans().isEmpty()) {
                SystemMessage sm = SystemMessage.getSystemMessage(this.getCastle().getOwnerId() <= 0 ? SystemMessageId.SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST : SystemMessageId.S1_SIEGE_WAS_CANCELED_BECAUSE_NO_CLANS_PARTICIPATED);
                sm.addString(this.getCastle().getName());
                World.toAllOnlinePlayers(sm);
                this.saveCastleSiege(true);
            } else {
                this._formerOwner = ClanTable.getInstance().getClan(this._castle.getOwnerId());
                this.changeStatus(SiegeStatus.IN_PROGRESS);
                this.updatePlayerSiegeStateFlags(false);
                this.getCastle().getSiegeZone().banishForeigners(this.getCastle().getOwnerId());
                this.spawnControlTowers();
                this.spawnFlameTowers();
                this.getCastle().closeDoors();
                this.getCastle().spawnSiegeGuardsOrMercenaries();
                this.getCastle().getSiegeZone().setIsActive(true);
                this.getCastle().getSiegeZone().updateZoneStatusForCharactersInside();
                this._siegeEndDate = Calendar.getInstance();
                this._siegeEndDate.add(12, Config.SIEGE_LENGTH);
                ThreadPool.schedule(new Siege.EndSiegeTask(this.getCastle()), 1000L);
                World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_STARTED).addString(this.getCastle().getName()));
                World.toAllOnlinePlayers(new PlaySound("systemmsg_e.17"));
            }
        }
    }

    public void endSiege() {
        if (this.isInProgress()) {
            World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.SIEGE_OF_S1_HAS_ENDED).addString(this.getCastle().getName()));
            World.toAllOnlinePlayers(new PlaySound("systemmsg_e.18"));
            if (this.getCastle().getOwnerId() > 0) {
                Clan clan = ClanTable.getInstance().getClan(this.getCastle().getOwnerId());
                World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_S1_VICTORIOUS_OVER_S2_S_SIEGE).addString(clan.getName()).addString(this.getCastle().getName()));
                if (this._formerOwner != null && clan != this._formerOwner) {
                    this.getCastle().checkItemsForClan(this._formerOwner);
                    Iterator var2 = clan.getMembers().iterator();

                    while (var2.hasNext()) {
                        ClanMember member = (ClanMember) var2.next();
                        Player player = member.getPlayerInstance();
                        if (player != null && player.isNoble()) {
                            HeroManager.getInstance().setCastleTaken(player.getObjectId(), this.getCastle().getCastleId());
                        }
                    }
                }
            } else {
                World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.SIEGE_S1_DRAW).addString(this.getCastle().getName()));
            }

            Iterator var5 = this._registeredClans.keySet().iterator();

            while (var5.hasNext()) {
                Clan clan = (Clan) var5.next();
                clan.setSiegeKills(0);
                clan.setSiegeDeaths(0);
                clan.setFlag(null);
            }

            this.updateClansReputation();
            this.getCastle().getSiegeZone().banishForeigners(this.getCastle().getOwnerId());
            this.updatePlayerSiegeStateFlags(true);
            this.saveCastleSiege(true);
            this.clearAllClans();
            this.removeTowers();
            this.getCastle().despawnSiegeGuardsOrMercenaries();
            this.getCastle().spawnDoors(false);
            this.getCastle().getSiegeZone().setIsActive(false);
            this.getCastle().getSiegeZone().updateZoneStatusForCharactersInside();
        }
    }

    public final List<Clan> getAttackerClans() {
        return this._registeredClans.entrySet().stream().filter((e) -> {
            return e.getValue() == SiegeSide.ATTACKER;
        }).map(Entry::getKey).collect(Collectors.toList());
    }

    public final List<Clan> getDefenderClans() {
        return this._registeredClans.entrySet().stream().filter((e) -> {
            return e.getValue() == SiegeSide.DEFENDER || e.getValue() == SiegeSide.OWNER;
        }).map(Entry::getKey).collect(Collectors.toList());
    }

    public boolean checkSide(Clan clan, SiegeSide type) {
        return clan != null && this._registeredClans.get(clan) == type;
    }

    public boolean checkSides(Clan clan, SiegeSide... types) {
        return clan != null && ArraysUtil.contains(types, this._registeredClans.get(clan));
    }

    public boolean checkSides(Clan clan) {
        return clan != null && this._registeredClans.containsKey(clan);
    }

    public Npc getFlag(Clan clan) {
        return this.checkSide(clan, SiegeSide.ATTACKER) ? clan.getFlag() : null;
    }

    public final Calendar getSiegeDate() {
        return this.getCastle().getSiegeDate();
    }

    public Map<Clan, SiegeSide> getRegisteredClans() {
        return this._registeredClans;
    }

    public final List<Clan> getPendingClans() {
        return this._registeredClans.entrySet().stream().filter((e) -> {
            return e.getValue() == SiegeSide.PENDING;
        }).map(Entry::getKey).collect(Collectors.toList());
    }

    public void updateClansReputation() {
        Clan owner = ClanTable.getInstance().getClan(this._castle.getOwnerId());
        if (this._formerOwner != null) {
            if (this._formerOwner != owner) {
                this._formerOwner.takeReputationScore(1000);
                this._formerOwner.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_WAS_DEFEATED_IN_SIEGE_AND_LOST_S1_REPUTATION_POINTS).addNumber(1000));
                if (owner != null) {
                    owner.addReputationScore(1000);
                    owner.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_VICTORIOUS_IN_SIEGE_AND_GAINED_S1_REPUTATION_POINTS).addNumber(1000));
                }
            } else {
                this._formerOwner.addReputationScore(500);
                this._formerOwner.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_VICTORIOUS_IN_SIEGE_AND_GAINED_S1_REPUTATION_POINTS).addNumber(500));
            }
        } else if (owner != null) {
            owner.addReputationScore(1000);
            owner.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_VICTORIOUS_IN_SIEGE_AND_GAINED_S1_REPUTATION_POINTS).addNumber(1000));
        }

    }

    private void switchSide(Clan clan, SiegeSide newState) {
        this._registeredClans.put(clan, newState);
    }

    private void switchSides(SiegeSide newState, SiegeSide... previousStates) {
        Iterator var3 = this._registeredClans.entrySet().iterator();

        while (var3.hasNext()) {
            Entry<Clan, SiegeSide> entry = (Entry) var3.next();
            if (ArraysUtil.contains(previousStates, entry.getValue())) {
                entry.setValue(newState);
            }
        }

    }

    public SiegeSide getSide(Clan clan) {
        return this._registeredClans.get(clan);
    }

    public boolean isOnOppositeSide(Clan formerClan, Clan targetClan) {
        SiegeSide formerSide = this._registeredClans.get(formerClan);
        SiegeSide targetSide = this._registeredClans.get(targetClan);
        if (formerSide != null && targetSide != null) {
            return targetSide == SiegeSide.ATTACKER && (formerSide == SiegeSide.OWNER || formerSide == SiegeSide.DEFENDER || formerSide == SiegeSide.PENDING) || formerSide == SiegeSide.ATTACKER && (targetSide == SiegeSide.OWNER || targetSide == SiegeSide.DEFENDER || targetSide == SiegeSide.PENDING);
        } else {
            return false;
        }
    }

    public void midVictory() {
        if (this.isInProgress()) {
            this.getCastle().despawnSiegeGuardsOrMercenaries();
            if (this.getCastle().getOwnerId() > 0) {
                List<Clan> attackers = this.getAttackerClans();
                List<Clan> defenders = this.getDefenderClans();
                Clan castleOwner = ClanTable.getInstance().getClan(this.getCastle().getOwnerId());
                if (defenders.isEmpty() && attackers.size() == 1) {
                    this.switchSide(castleOwner, SiegeSide.OWNER);
                    this.endSiege();
                } else {
                    int allyId = castleOwner.getAllyId();
                    if (defenders.isEmpty() && allyId != 0) {
                        boolean allInSameAlliance = true;
                        Iterator var6 = attackers.iterator();

                        while (var6.hasNext()) {
                            Clan clan = (Clan) var6.next();
                            if (clan.getAllyId() != allyId) {
                                allInSameAlliance = false;
                                break;
                            }
                        }

                        if (allInSameAlliance) {
                            this.switchSide(castleOwner, SiegeSide.OWNER);
                            this.endSiege();
                            return;
                        }
                    }

                    this.switchSides(SiegeSide.ATTACKER, SiegeSide.DEFENDER, SiegeSide.OWNER);
                    this.switchSide(castleOwner, SiegeSide.OWNER);
                    Iterator var8;
                    Clan clan;
                    if (allyId != 0) {
                        var8 = attackers.iterator();

                        while (var8.hasNext()) {
                            clan = (Clan) var8.next();
                            if (clan.getAllyId() == allyId) {
                                this.switchSide(clan, SiegeSide.DEFENDER);
                            }
                        }
                    }

                    this.getCastle().getSiegeZone().banishForeigners(this.getCastle().getOwnerId());
                    var8 = defenders.iterator();

                    while (var8.hasNext()) {
                        clan = (Clan) var8.next();
                        clan.setFlag(null);
                    }

                    this.getCastle().removeDoorUpgrade();
                    this.getCastle().removeTrapUpgrade();
                    this.getCastle().spawnDoors(true);
                    this.removeTowers();
                    this.spawnControlTowers();
                    this.spawnFlameTowers();
                    this.updatePlayerSiegeStateFlags(false);
                }
            }
        }
    }

    public void announceToPlayers(SystemMessage message, boolean bothSides) {
        Iterator var3 = this.getDefenderClans().iterator();

        Clan clan;
        while (var3.hasNext()) {
            clan = (Clan) var3.next();
            clan.broadcastToOnlineMembers(message);
        }

        if (bothSides) {
            var3 = this.getAttackerClans().iterator();

            while (var3.hasNext()) {
                clan = (Clan) var3.next();
                clan.broadcastToOnlineMembers(message);
            }
        }

    }

    public void updatePlayerSiegeStateFlags(boolean clear) {
        Iterator var2 = this.getAttackerClans().iterator();

        Clan clan;
        Player[] var4;
        int var5;
        int var6;
        Player member;
        while (var2.hasNext()) {
            clan = (Clan) var2.next();
            var4 = clan.getOnlineMembers();
            var5 = var4.length;

            for (var6 = 0; var6 < var5; ++var6) {
                member = var4[var6];
                if (clear) {
                    member.setSiegeState((byte) 0);
                    member.setIsInSiege(false);
                } else {
                    member.setSiegeState((byte) 1);
                    if (this.checkIfInZone(member)) {
                        member.setIsInSiege(true);
                    }
                }

                member.sendPacket(new UserInfo(member));
                member.broadcastRelationsChanges();
            }
        }

        var2 = this.getDefenderClans().iterator();

        while (var2.hasNext()) {
            clan = (Clan) var2.next();
            var4 = clan.getOnlineMembers();
            var5 = var4.length;

            for (var6 = 0; var6 < var5; ++var6) {
                member = var4[var6];
                if (clear) {
                    member.setSiegeState((byte) 0);
                    member.setIsInSiege(false);
                } else {
                    member.setSiegeState((byte) 2);
                    if (this.checkIfInZone(member)) {
                        member.setIsInSiege(true);
                    }
                }

                member.sendPacket(new UserInfo(member));
                member.broadcastRelationsChanges();
            }
        }

    }

    public boolean checkIfInZone(WorldObject object) {
        return this.checkIfInZone(object.getX(), object.getY(), object.getZ());
    }

    public boolean checkIfInZone(int x, int y, int z) {
        return this.isInProgress() && this.getCastle().checkIfInZone(x, y, z);
    }

    public void clearAllClans() {
        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("DELETE FROM siege_clans WHERE castle_id=?");

                try {
                    ps.setInt(1, this.getCastle().getCastleId());
                    ps.executeUpdate();
                } catch (Throwable var7) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var6) {
                            var7.addSuppressed(var6);
                        }
                    }

                    throw var7;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (Throwable var8) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var5) {
                        var8.addSuppressed(var5);
                    }
                }

                throw var8;
            }

            if (con != null) {
                con.close();
            }
        } catch (Exception var9) {
            LOGGER.error("Couldn't clear siege registered clans.", var9);
        }

        this._registeredClans.clear();
        if (this.getCastle().getOwnerId() > 0) {
            Clan clan = ClanTable.getInstance().getClan(this.getCastle().getOwnerId());
            if (clan != null) {
                this._registeredClans.put(clan, SiegeSide.OWNER);
            }
        }

    }

    protected void clearPendingClans() {
        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("DELETE FROM siege_clans WHERE castle_id=? AND type='PENDING'");

                try {
                    ps.setInt(1, this.getCastle().getCastleId());
                    ps.executeUpdate();
                } catch (Throwable var7) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var6) {
                            var7.addSuppressed(var6);
                        }
                    }

                    throw var7;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (Throwable var8) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var5) {
                        var8.addSuppressed(var5);
                    }
                }

                throw var8;
            }

            if (con != null) {
                con.close();
            }
        } catch (Exception var9) {
            LOGGER.error("Couldn't clear siege pending clans.", var9);
        }

        this._registeredClans.entrySet().removeIf((e) -> {
            return e.getValue() == SiegeSide.PENDING;
        });
    }

    public void registerAttacker(Player player) {
        if (player.getClan() != null) {
            int allyId = 0;
            if (this.getCastle().getOwnerId() != 0) {
                allyId = ClanTable.getInstance().getClan(this.getCastle().getOwnerId()).getAllyId();
            }

            if (allyId != 0 && player.getClan().getAllyId() == allyId) {
                player.sendPacket(SystemMessageId.CANNOT_ATTACK_ALLIANCE_CASTLE);
            } else {
                if (this.allyIsRegisteredOnOppositeSide(player.getClan(), true)) {
                    player.sendPacket(SystemMessageId.CANT_ACCEPT_ALLY_ENEMY_FOR_SIEGE);
                } else if (this.checkIfCanRegister(player, SiegeSide.ATTACKER)) {
                    this.registerClan(player.getClan(), SiegeSide.ATTACKER);
                }

            }
        }
    }

    public void registerDefender(Player player) {
        if (player.getClan() != null) {
            if (this.getCastle().getOwnerId() <= 0) {
                player.sendPacket(SystemMessageId.DEFENDER_SIDE_FULL);
            } else if (this.allyIsRegisteredOnOppositeSide(player.getClan(), false)) {
                player.sendPacket(SystemMessageId.CANT_ACCEPT_ALLY_ENEMY_FOR_SIEGE);
            } else if (this.checkIfCanRegister(player, SiegeSide.PENDING)) {
                this.registerClan(player.getClan(), SiegeSide.PENDING);
            }

        }
    }

    private boolean allyIsRegisteredOnOppositeSide(Clan clan, boolean attacker) {
        int allyId = clan.getAllyId();
        if (allyId != 0) {
            Iterator var4 = ClanTable.getInstance().getClans().iterator();

            while (var4.hasNext()) {
                Clan alliedClan = (Clan) var4.next();
                if (alliedClan.getAllyId() == allyId && alliedClan.getClanId() != clan.getClanId()) {
                    if (attacker) {
                        if (this.checkSides(alliedClan, SiegeSide.DEFENDER, SiegeSide.OWNER, SiegeSide.PENDING)) {
                            return true;
                        }
                    } else if (this.checkSides(alliedClan, SiegeSide.ATTACKER)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public void unregisterClan(Clan clan) {
        if (clan != null && clan.getCastleId() != this.getCastle().getCastleId() && this._registeredClans.remove(clan) != null) {
            try {
                Connection con = ConnectionPool.getConnection();

                try {
                    PreparedStatement ps = con.prepareStatement("DELETE FROM siege_clans WHERE castle_id=? AND clan_id=?");

                    try {
                        ps.setInt(1, this.getCastle().getCastleId());
                        ps.setInt(2, clan.getClanId());
                        ps.executeUpdate();
                    } catch (Throwable var8) {
                        if (ps != null) {
                            try {
                                ps.close();
                            } catch (Throwable var7) {
                                var8.addSuppressed(var7);
                            }
                        }

                        throw var8;
                    }

                    if (ps != null) {
                        ps.close();
                    }
                } catch (Throwable var9) {
                    if (con != null) {
                        try {
                            con.close();
                        } catch (Throwable var6) {
                            var9.addSuppressed(var6);
                        }
                    }

                    throw var9;
                }

                if (con != null) {
                    con.close();
                }
            } catch (Exception var10) {
                LOGGER.error("Couldn't unregister clan on siege.", var10);
            }

        }
    }

    private void startAutoTask() {
        if (this.getCastle().getSiegeDate().getTimeInMillis() < Calendar.getInstance().getTimeInMillis()) {
            this.saveCastleSiege(false);
        } else {
            if (this._siegeTask != null) {
                this._siegeTask.cancel(false);
            }

            this._siegeTask = ThreadPool.schedule(new Siege.SiegeTask(this.getCastle()), 1000L);
        }

    }

    private boolean checkIfCanRegister(Player player, SiegeSide type) {
        SystemMessage sm;
        if (this.isRegistrationOver()) {
            sm = SystemMessage.getSystemMessage(SystemMessageId.DEADLINE_FOR_SIEGE_S1_PASSED).addString(this.getCastle().getName());
        } else if (this.isInProgress()) {
            sm = SystemMessage.getSystemMessage(SystemMessageId.NOT_SIEGE_REGISTRATION_TIME2);
        } else if (player.getClan() != null && player.getClan().getLevel() >= Config.MINIMUM_CLAN_LEVEL) {
            if (player.getClan().hasCastle()) {
                sm = player.getClan().getClanId() == this.getCastle().getOwnerId() ? SystemMessage.getSystemMessage(SystemMessageId.CLAN_THAT_OWNS_CASTLE_IS_AUTOMATICALLY_REGISTERED_DEFENDING) : SystemMessage.getSystemMessage(SystemMessageId.CLAN_THAT_OWNS_CASTLE_CANNOT_PARTICIPATE_OTHER_SIEGE);
            } else if (player.getClan().isRegisteredOnSiege()) {
                sm = SystemMessage.getSystemMessage(SystemMessageId.ALREADY_REQUESTED_SIEGE_BATTLE);
            } else if (this.checkIfAlreadyRegisteredForSameDay(player.getClan())) {
                sm = SystemMessage.getSystemMessage(SystemMessageId.APPLICATION_DENIED_BECAUSE_ALREADY_SUBMITTED_A_REQUEST_FOR_ANOTHER_SIEGE_BATTLE);
            } else if (type == SiegeSide.ATTACKER && this.getAttackerClans().size() >= Config.MAX_ATTACKERS_NUMBER) {
                sm = SystemMessage.getSystemMessage(SystemMessageId.ATTACKER_SIDE_FULL);
            } else {
                if (type != SiegeSide.DEFENDER && type != SiegeSide.PENDING && type != SiegeSide.OWNER || this.getDefenderClans().size() + this.getPendingClans().size() < Config.MAX_DEFENDERS_NUMBER) {
                    return true;
                }

                sm = SystemMessage.getSystemMessage(SystemMessageId.DEFENDER_SIDE_FULL);
            }
        } else {
            sm = SystemMessage.getSystemMessage(SystemMessageId.ONLY_CLAN_LEVEL_4_ABOVE_MAY_SIEGE);
        }

        player.sendPacket(sm);
        return false;
    }

    public boolean checkIfAlreadyRegisteredForSameDay(Clan clan) {
        Iterator var2 = CastleManager.getInstance().getCastles().iterator();

        Siege siege;
        do {
            if (!var2.hasNext()) {
                return false;
            }

            Castle castle = (Castle) var2.next();
            siege = castle.getSiege();
        } while (siege == this || siege.getSiegeDate().get(7) != this.getSiegeDate().get(7) || !siege.checkSides(clan));

        return true;
    }

    private void removeTowers() {
        Iterator var1 = this._flameTowers.iterator();

        while (var1.hasNext()) {
            FlameTower ct = (FlameTower) var1.next();
            ct.deleteMe();
        }

        var1 = this._controlTowers.iterator();

        while (var1.hasNext()) {
            ControlTower ct = (ControlTower) var1.next();
            ct.deleteMe();
        }

        var1 = this._destroyedTowers.iterator();

        while (var1.hasNext()) {
            Npc ct = (Npc) var1.next();
            ct.deleteMe();
        }

        this._flameTowers.clear();
        this._controlTowers.clear();
        this._destroyedTowers.clear();
    }

    private void saveCastleSiege(boolean launchTask) {
        this.setNextSiegeDate();
        this.getCastle().setTimeRegistrationOver(false);
        this.saveSiegeDate();
        if (launchTask) {
            this.startAutoTask();
        }

        LOGGER.info("New date for {} siege: {}.", this.getCastle().getName(), this.getCastle().getSiegeDate().getTime());
    }

    private void saveSiegeDate() {
        if (this._siegeTask != null) {
            this._siegeTask.cancel(true);
            this._siegeTask = ThreadPool.schedule(new Siege.SiegeTask(this.getCastle()), 1000L);
        }

        try {
            Connection con = ConnectionPool.getConnection();

            try {
                PreparedStatement ps = con.prepareStatement("UPDATE castle SET siegeDate=?, regTimeOver=? WHERE id=?");

                try {
                    ps.setLong(1, this.getSiegeDate().getTimeInMillis());
                    ps.setString(2, String.valueOf(this.isTimeRegistrationOver()));
                    ps.setInt(3, this.getCastle().getCastleId());
                    ps.executeUpdate();
                } catch (Throwable var7) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (Throwable var6) {
                            var7.addSuppressed(var6);
                        }
                    }

                    throw var7;
                }

                if (ps != null) {
                    ps.close();
                }
            } catch (Throwable var8) {
                if (con != null) {
                    try {
                        con.close();
                    } catch (Throwable var5) {
                        var8.addSuppressed(var5);
                    }
                }

                throw var8;
            }

            if (con != null) {
                con.close();
            }
        } catch (Exception var9) {
            LOGGER.error("Couldn't save siege date.", var9);
        }

    }

    public void registerClan(Clan clan, SiegeSide type) {
        if (!clan.hasCastle()) {
            switch (type) {
                case DEFENDER:
                case PENDING:
                case OWNER:
                    if (this.getDefenderClans().size() + this.getPendingClans().size() >= Config.MAX_DEFENDERS_NUMBER) {
                        return;
                    }
                    break;
                default:
                    if (this.getAttackerClans().size() >= Config.MAX_ATTACKERS_NUMBER) {
                        return;
                    }
            }

            try {
                Connection con = ConnectionPool.getConnection();

                try {
                    PreparedStatement ps = con.prepareStatement("INSERT INTO siege_clans (clan_id,castle_id,type) VALUES (?,?,?) ON DUPLICATE KEY UPDATE type=VALUES(type)");

                    try {
                        ps.setInt(1, clan.getClanId());
                        ps.setInt(2, this.getCastle().getCastleId());
                        ps.setString(3, type.toString());
                        ps.executeUpdate();
                    } catch (Throwable var9) {
                        if (ps != null) {
                            try {
                                ps.close();
                            } catch (Throwable var8) {
                                var9.addSuppressed(var8);
                            }
                        }

                        throw var9;
                    }

                    if (ps != null) {
                        ps.close();
                    }
                } catch (Throwable var10) {
                    if (con != null) {
                        try {
                            con.close();
                        } catch (Throwable var7) {
                            var10.addSuppressed(var7);
                        }
                    }

                    throw var10;
                }

                if (con != null) {
                    con.close();
                }
            } catch (Exception var11) {
                LOGGER.error("Couldn't register clan on siege.", var11);
            }

            this._registeredClans.put(clan, type);
        }
    }

    private void setNextSiegeDate() {
        Calendar siegeDate = this.getCastle().getSiegeDate();
        if (siegeDate.getTimeInMillis() < System.currentTimeMillis()) {
            siegeDate.setTimeInMillis(System.currentTimeMillis());
        }

        switch (this.getCastle().getCastleId()) {
            case 1:
                siegeDate.set(7, Config.SIEGE_DAY_GLUDIO);
                break;
            case 2:
                siegeDate.set(7, Config.SIEGE_DAY_DION);
                break;
            case 3:
                siegeDate.set(7, Config.SIEGE_DAY_GIRAN);
                break;
            case 4:
                siegeDate.set(7, Config.SIEGE_DAY_OREN);
                break;
            case 5:
                siegeDate.set(7, Config.SIEGE_DAY_ADEN);
                break;
            case 6:
                siegeDate.set(7, Config.SIEGE_DAY_INNADRIL);
                break;
            case 7:
                siegeDate.set(7, Config.SIEGE_DAY_GODDARD);
                break;
            case 8:
                siegeDate.set(7, Config.SIEGE_DAY_RUNE);
                break;
            case 9:
                siegeDate.set(7, Config.SIEGE_DAY_SCHUT);
        }

        siegeDate.add(3, Config.DAY_TO_SIEGE);
        switch (this.getCastle().getCastleId()) {
            case 1:
                siegeDate.set(11, Config.SIEGE_HOUR_GLUDIO);
                break;
            case 2:
                siegeDate.set(11, Config.SIEGE_HOUR_DION);
                break;
            case 3:
                siegeDate.set(11, Config.SIEGE_HOUR_GIRAN);
                break;
            case 4:
                siegeDate.set(11, Config.SIEGE_HOUR_OREN);
                break;
            case 5:
                siegeDate.set(11, Config.SIEGE_HOUR_ADEN);
                break;
            case 6:
                siegeDate.set(11, Config.SIEGE_HOUR_INNADRIL);
                break;
            case 7:
                siegeDate.set(11, Config.SIEGE_HOUR_GODDARD);
                break;
            case 8:
                siegeDate.set(11, Config.SIEGE_HOUR_RUNE);
                break;
            case 9:
                siegeDate.set(11, Config.SIEGE_HOUR_SCHUT);
        }

        siegeDate.set(12, 0);
        siegeDate.set(13, 0);
        siegeDate.set(14, 0);
        World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.S1_ANNOUNCED_SIEGE_TIME).addString(this.getCastle().getName()));
        this.changeStatus(SiegeStatus.REGISTRATION_OPENED);
    }

    private void spawnControlTowers() {
        Iterator var1 = this.getCastle().getControlTowers().iterator();

        while (var1.hasNext()) {
            TowerSpawnLocation ts = (TowerSpawnLocation) var1.next();

            try {
                L2Spawn spawn = new L2Spawn(NpcData.getInstance().getTemplate(ts.getNpc().getNpcId()));
                spawn.setLoc(ts);
                ControlTower tower = (ControlTower) spawn.doSpawn(false);
                tower.setCastle(this.getCastle());
                this._controlTowers.add(tower);
            } catch (Exception var5) {
                LOGGER.error("Couldn't spawn control tower.", var5);
            }
        }

    }

    private void spawnFlameTowers() {
        Iterator var1 = this.getCastle().getFlameTowers().iterator();

        while (var1.hasNext()) {
            TowerSpawnLocation ts = (TowerSpawnLocation) var1.next();

            try {
                L2Spawn spawn = new L2Spawn(NpcData.getInstance().getTemplate(ts.getNpc().getNpcId()));
                spawn.setLoc(ts);
                FlameTower tower = (FlameTower) spawn.doSpawn(false);
                tower.setCastle(this.getCastle());
                tower.setUpgradeLevel(ts.getUpgradeLevel());
//                tower.setZoneList(ts.getZoneList());
                this._flameTowers.add(tower);
            } catch (Exception var5) {
                LOGGER.error("Couldn't spawn flame tower.", var5);
            }
        }

    }

    public final Castle getCastle() {
        return this._castle;
    }

    public final boolean isInProgress() {
        return this._siegeStatus == SiegeStatus.IN_PROGRESS;
    }

    public final boolean isRegistrationOver() {
        return this._siegeStatus != SiegeStatus.REGISTRATION_OPENED;
    }

    public final boolean isTimeRegistrationOver() {
        return this.getCastle().isTimeRegistrationOver();
    }

    public final long getSiegeRegistrationEndDate() {
        return this.getCastle().getSiegeDate().getTimeInMillis() - 86400000L;
    }

    public void endTimeRegistration(boolean automatic) {
        this.getCastle().setTimeRegistrationOver(true);
        if (!automatic) {
            this.saveSiegeDate();
        }

    }

    public int getControlTowerCount() {
        return (int) this._controlTowers.stream().filter((lc) -> {
            return lc.isActive();
        }).count();
    }

    public List<Npc> getDestroyedTowers() {
        return this._destroyedTowers;
    }

    public void addQuestEvent(Quest quest) {
        if (this._questEvents.isEmpty()) {
            this._questEvents = new ArrayList(3);
        }

        this._questEvents.add(quest);
    }

    public SiegeStatus getStatus() {
        return this._siegeStatus;
    }

    protected void changeStatus(SiegeStatus status) {
        this._siegeStatus = status;
        Iterator var2 = this._questEvents.iterator();

        while (var2.hasNext()) {
            Quest quest = (Quest) var2.next();
            quest.onSiegeEvent();
        }

    }

    public class EndSiegeTask implements Runnable {
        private final Castle _castle;

        public EndSiegeTask(Castle castle) {
            this._castle = castle;
        }

        public void run() {
            if (Siege.this.isInProgress()) {
                long timeRemaining = Siege.this._siegeEndDate.getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
                if (timeRemaining > 3600000L) {
                    Siege.this.announceToPlayers(SystemMessage.getSystemMessage(SystemMessageId.S1_HOURS_UNTIL_SIEGE_CONCLUSION).addNumber(2), true);
                    ThreadPool.schedule(Siege.this.new EndSiegeTask(this._castle), timeRemaining - 3600000L);
                } else if (timeRemaining <= 3600000L && timeRemaining > 600000L) {
                    Siege.this.announceToPlayers(SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_SIEGE_CONCLUSION).addNumber(Math.round((float) (timeRemaining / 60000L))), true);
                    ThreadPool.schedule(Siege.this.new EndSiegeTask(this._castle), timeRemaining - 600000L);
                } else if (timeRemaining <= 600000L && timeRemaining > 300000L) {
                    Siege.this.announceToPlayers(SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_SIEGE_CONCLUSION).addNumber(Math.round((float) (timeRemaining / 60000L))), true);
                    ThreadPool.schedule(Siege.this.new EndSiegeTask(this._castle), timeRemaining - 300000L);
                } else if (timeRemaining <= 300000L && timeRemaining > 10000L) {
                    Siege.this.announceToPlayers(SystemMessage.getSystemMessage(SystemMessageId.S1_MINUTES_UNTIL_SIEGE_CONCLUSION).addNumber(Math.round((float) (timeRemaining / 60000L))), true);
                    ThreadPool.schedule(Siege.this.new EndSiegeTask(this._castle), timeRemaining - 10000L);
                } else if (timeRemaining <= 10000L && timeRemaining > 0L) {
                    Siege.this.announceToPlayers(SystemMessage.getSystemMessage(SystemMessageId.CASTLE_SIEGE_S1_SECONDS_LEFT).addNumber(Math.round((float) (timeRemaining / 1000L))), true);
                    ThreadPool.schedule(Siege.this.new EndSiegeTask(this._castle), timeRemaining);
                } else {
                    this._castle.getSiege().endSiege();
                }

            }
        }
    }

    private class SiegeTask implements Runnable {
        private final Castle _castle;

        public SiegeTask(Castle castle) {
            this._castle = castle;
        }

        public void run() {
            Siege.this._siegeTask.cancel(false);
            if (!Siege.this.isInProgress()) {
                long timeRemaining;
                if (!Siege.this.isTimeRegistrationOver()) {
                    timeRemaining = Siege.this.getSiegeRegistrationEndDate() - Calendar.getInstance().getTimeInMillis();
                    if (timeRemaining > 0L) {
                        Siege.this._siegeTask = ThreadPool.schedule(Siege.this.new SiegeTask(this._castle), timeRemaining);
                        return;
                    }

                    Siege.this.endTimeRegistration(true);
                }

                timeRemaining = Siege.this.getSiegeDate().getTimeInMillis() - Calendar.getInstance().getTimeInMillis();
                if (timeRemaining > 86400000L) {
                    Siege.this._siegeTask = ThreadPool.schedule(Siege.this.new SiegeTask(this._castle), timeRemaining - 86400000L);
                } else if (timeRemaining <= 86400000L && timeRemaining > 13600000L) {
                    World.toAllOnlinePlayers(SystemMessage.getSystemMessage(SystemMessageId.REGISTRATION_TERM_FOR_S1_ENDED).addString(Siege.this.getCastle().getName()));
                    Siege.this.changeStatus(SiegeStatus.REGISTRATION_OVER);
                    Siege.this.clearPendingClans();
                    Siege.this._siegeTask = ThreadPool.schedule(Siege.this.new SiegeTask(this._castle), timeRemaining - 13600000L);
                } else if (timeRemaining <= 13600000L && timeRemaining > 600000L) {
                    Siege.this._siegeTask = ThreadPool.schedule(Siege.this.new SiegeTask(this._castle), timeRemaining - 600000L);
                } else if (timeRemaining <= 600000L && timeRemaining > 300000L) {
                    Siege.this._siegeTask = ThreadPool.schedule(Siege.this.new SiegeTask(this._castle), timeRemaining - 300000L);
                } else if (timeRemaining <= 300000L && timeRemaining > 10000L) {
                    Siege.this._siegeTask = ThreadPool.schedule(Siege.this.new SiegeTask(this._castle), timeRemaining - 10000L);
                } else if (timeRemaining <= 10000L && timeRemaining > 0L) {
                    Siege.this._siegeTask = ThreadPool.schedule(Siege.this.new SiegeTask(this._castle), timeRemaining);
                } else {
                    this._castle.getSiege().startSiege();
                }

            }
        }
    }
}