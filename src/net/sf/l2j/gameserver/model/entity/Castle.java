package net.sf.l2j.gameserver.model.entity;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.pool.ConnectionPool;
import net.sf.l2j.gameserver.data.manager.CastleManager;
import net.sf.l2j.gameserver.data.manager.CastleManorManager;
import net.sf.l2j.gameserver.data.manager.SevenSignsManager;
import net.sf.l2j.gameserver.data.manager.ZoneManager;
import net.sf.l2j.gameserver.data.sql.ClanTable;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.enums.SealType;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Door;
import net.sf.l2j.gameserver.model.actor.instance.HolyThing;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.item.MercenaryTicket;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.location.TowerSpawnLocation;
import net.sf.l2j.gameserver.model.pledge.Clan;
import net.sf.l2j.gameserver.model.pledge.ClanMember;
import net.sf.l2j.gameserver.model.spawn.L2Spawn;
import net.sf.l2j.gameserver.model.zone.type.CastleTeleportZone;
import net.sf.l2j.gameserver.model.zone.type.CastleZone;
import net.sf.l2j.gameserver.model.zone.type.SiegeZone;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.PlaySound;
import net.sf.l2j.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

public class Castle {
    protected static final CLogger LOGGER = new CLogger(Castle.class.getName());
    private static final String UPDATE_TREASURY = "UPDATE castle SET treasury = ? WHERE id = ?";
    private static final String UPDATE_CERTIFICATES = "UPDATE castle SET certificates=? WHERE id=?";
    private static final String UPDATE_TAX = "UPDATE castle SET taxPercent = ? WHERE id = ?";
    private static final String UPDATE_DOORS = "REPLACE INTO castle_doorupgrade (doorId, hp, castleId) VALUES (?,?,?)";
    private static final String LOAD_DOORS = "SELECT * FROM castle_doorupgrade WHERE castleId=?";
    private static final String DELETE_DOOR = "DELETE FROM castle_doorupgrade WHERE castleId=?";
    private static final String DELETE_OWNER = "UPDATE clan_data SET hasCastle=0 WHERE hasCastle=?";
    private static final String UPDATE_OWNER = "UPDATE clan_data SET hasCastle=? WHERE clan_id=?";
    private static final String LOAD_TRAPS = "SELECT * FROM castle_trapupgrade WHERE castleId=?";
    private static final String UPDATE_TRAP = "REPLACE INTO castle_trapupgrade (castleId, towerIndex, level) values (?,?,?)";
    private static final String DELETE_TRAP = "DELETE FROM castle_trapupgrade WHERE castleId=?";
    private static final String UPDATE_ITEMS_LOC = "UPDATE items SET loc='INVENTORY' WHERE item_id IN (?, 6841) AND owner_id=? AND loc='PAPERDOLL'";
    private final int _castleId;
    private final String _name;
    private final List<Door> _doors = new ArrayList<>();
    private final List<MercenaryTicket> _tickets = new ArrayList<>(60);
    private final List<Integer> _artifacts = new ArrayList<>(1);
    private final List<Integer> _relatedNpcIds = new ArrayList<>();
    private final Set<ItemInstance> _droppedTickets = new ConcurrentSkipListSet<>();
    private final List<Npc> _siegeGuards = new ArrayList<>();
    private final List<TowerSpawnLocation> _controlTowers = new ArrayList<>();
    private final List<TowerSpawnLocation> _flameTowers = new ArrayList<>();
    private int _circletId;
    private int _ownerId;
    private Siege _siege;
    private Calendar _siegeDate;
    private boolean _isTimeRegistrationOver = true;
    private int _taxPercent;
    private double _taxRate;
    private long _treasury;
    private SiegeZone _siegeZone;
    private CastleZone _castleZone;
    private CastleTeleportZone _teleZone;
    private int _leftCertificates;

    public Castle(int id, String name) {
        this._castleId = id;
        this._name = name;

        for (SiegeZone zone : ZoneManager.getInstance().getAllZones(SiegeZone.class)) {
            if (zone.getSiegeObjectId() == this._castleId) {
                this._siegeZone = zone;
                break;
            }
        }

        for (CastleZone zone : ZoneManager.getInstance().getAllZones(CastleZone.class)) {
            if (zone.getCastleId() == this._castleId) {
                this._castleZone = zone;
                break;
            }
        }

        for (CastleTeleportZone zone : ZoneManager.getInstance().getAllZones(CastleTeleportZone.class)) {
            if (zone.getCastleId() == this._castleId) {
                this._teleZone = zone;
                break;
            }
        }

    }

    public synchronized void engrave(Clan clan, WorldObject target) {
        if (this.isGoodArtifact(target)) {
            this.setOwner(clan);
            this.getSiege().announceToPlayers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_S1_ENGRAVED_RULER).addString(clan.getName()), true);
        }
    }

    public void addToTreasury(int amount) {
        if (this._ownerId > 0) {
            if (this._name.equalsIgnoreCase("Schuttgart") || this._name.equalsIgnoreCase("Goddard")) {
                Castle rune = CastleManager.getInstance().getCastleByName("rune");
                if (rune != null) {
                    int runeTax = (int) ((double) amount * rune._taxRate);
                    if (rune._ownerId > 0) {
                        rune.addToTreasury(runeTax);
                    }

                    amount -= runeTax;
                }
            }

            if (!this._name.equalsIgnoreCase("aden") && !this._name.equalsIgnoreCase("Rune") && !this._name.equalsIgnoreCase("Schuttgart") && !this._name.equalsIgnoreCase("Goddard")) {
                Castle aden = CastleManager.getInstance().getCastleByName("aden");
                if (aden != null) {
                    int adenTax = (int) ((double) amount * aden._taxRate);
                    if (aden._ownerId > 0) {
                        aden.addToTreasury(adenTax);
                    }

                    amount -= adenTax;
                }
            }

            this.addToTreasuryNoTax(amount);
        }
    }

    public boolean addToTreasuryNoTax(long amount) {
        if (this._ownerId <= 0) {
            return false;
        } else {
            if (amount < 0L) {
                amount *= -1L;
                if (this._treasury < amount) {
                    return false;
                }

                this._treasury -= amount;
            } else if (this._treasury + amount > 2147483647L) {
                this._treasury = 2147483647L;
            } else {
                this._treasury += amount;
            }

            try (
                    Connection con = ConnectionPool.getConnection();
                    PreparedStatement ps = con.prepareStatement("UPDATE castle SET treasury = ? WHERE id = ?");
            ) {
                ps.setLong(1, this._treasury);
                ps.setInt(2, this._castleId);
                ps.executeUpdate();
            } catch (Exception e) {
                LOGGER.error("Couldn't update treasury.", e);
            }

            return true;
        }
    }

    public void banishForeigners() {
        this.getCastleZone().banishForeigners(this._ownerId);
    }

    public boolean checkIfInZone(int x, int y, int z) {
        return this.getSiegeZone().isInsideZone(x, y, z);
    }

    public SiegeZone getSiegeZone() {
        return this._siegeZone;
    }

    public CastleZone getCastleZone() {
        return this._castleZone;
    }

    public CastleTeleportZone getTeleZone() {
        return this._teleZone;
    }

    public void oustAllPlayers() {
        this.getTeleZone().oustAllPlayers();
    }

    public int getLeftCertificates() {
        return this._leftCertificates;
    }

    public void setLeftCertificates(int leftCertificates, boolean save) {
        this._leftCertificates = leftCertificates;
        if (save) {
            try (
                    Connection con = ConnectionPool.getConnection();
                    PreparedStatement ps = con.prepareStatement("UPDATE castle SET certificates=? WHERE id=?");
            ) {
                ps.setInt(1, leftCertificates);
                ps.setInt(2, this._castleId);
                ps.executeUpdate();
            } catch (Exception e) {
                LOGGER.error("Couldn't update certificates amount.", e);
            }
        }

    }

    public double getDistance(WorldObject obj) {
        return this.getSiegeZone().getDistanceToZone(obj);
    }

    public void closeDoor(Player activeChar, int doorId) {
        this.openCloseDoor(activeChar, doorId, false);
    }

    public void openDoor(Player activeChar, int doorId) {
        this.openCloseDoor(activeChar, doorId, true);
    }

    public void openCloseDoor(Player activeChar, int doorId, boolean open) {
        if (activeChar.getClanId() == this._ownerId) {
            Door door = this.getDoor(doorId);
            if (door != null) {
                if (open) {
                    door.openMe();
                } else {
                    door.closeMe();
                }
            }

        }
    }

    public void setOwner(Clan clan) {
        if (this._ownerId > 0 && (clan == null || clan.getClanId() != this._ownerId)) {
            Clan oldOwner = ClanTable.getInstance().getClan(this._ownerId);
            if (oldOwner != null) {
                Player oldLeader = oldOwner.getLeader().getPlayerInstance();
                if (oldLeader != null && oldLeader.getMountType() == 2) {
                    oldLeader.dismount();
                }

                oldOwner.setCastle(0);
            }
        }

        this.updateOwnerInDB(clan);
        if (this.getSiege().isInProgress()) {
            this.getSiege().midVictory();
            this.getSiege().announceToPlayers(SystemMessage.getSystemMessage(SystemMessageId.NEW_CASTLE_LORD), true);
        }

    }

    public void removeOwner() {
        if (this._ownerId > 0) {
            Clan clan = ClanTable.getInstance().getClan(this._ownerId);
            if (clan != null) {
                clan.setCastle(0);
                clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
                this.getSiege().getRegisteredClans().remove(clan);
                this.updateOwnerInDB(null);
                if (this.getSiege().isInProgress()) {
                    this.getSiege().midVictory();
                } else {
                    this.checkItemsForClan(clan);
                }

            }
        }
    }

    public void setTaxPercent(Player activeChar, int taxPercent) {
        int maxTax;
        switch (SevenSignsManager.getInstance().getSealOwner(SealType.STRIFE)) {
            case DAWN -> maxTax = 25;
            case DUSK -> maxTax = 5;
            default -> maxTax = 15;
        }

        if (taxPercent >= 0 && taxPercent <= maxTax) {
            this.setTaxPercent(taxPercent, true);
            activeChar.sendMessage(this._name + " castle tax changed to " + taxPercent + "%.");
        } else {
            activeChar.sendMessage("Tax value must be between 0 and " + maxTax + ".");
        }
    }

    public void setTaxPercent(int taxPercent, boolean save) {
        this._taxPercent = taxPercent;
        this._taxRate = (double) this._taxPercent / (double) 100.0F;
        if (save) {
            try (
                    Connection con = ConnectionPool.getConnection();
                    PreparedStatement ps = con.prepareStatement("UPDATE castle SET taxPercent = ? WHERE id = ?");
            ) {
                ps.setInt(1, taxPercent);
                ps.setInt(2, this._castleId);
                ps.executeUpdate();
            } catch (Exception e) {
                LOGGER.error("Couldn't update tax amount.", e);
            }
        }

    }

    public void spawnDoors(boolean isDoorWeak) {
        for (Door door : this._doors) {
            if (door.isDead()) {
                door.doRevive();
            }

            door.closeMe();
            door.setCurrentHp(isDoorWeak ? (double) (door.getMaxHp() / 2) : (double) door.getMaxHp());
            door.broadcastStatusUpdate();
        }

    }

    public void closeDoors() {
        for (Door door : this._doors) {
            door.closeMe();
        }

    }

    public void upgradeDoor(int doorId, int hp, boolean db) {
        Door door = this.getDoor(doorId);
        if (door != null) {
            door.getStat().setUpgradeHpRatio(hp);
            door.setCurrentHp(door.getMaxHp());
            if (db) {
                try (
                        Connection con = ConnectionPool.getConnection();
                        PreparedStatement ps = con.prepareStatement("REPLACE INTO castle_doorupgrade (doorId, hp, castleId) VALUES (?,?,?)");
                ) {
                    ps.setInt(1, doorId);
                    ps.setInt(2, hp);
                    ps.setInt(3, this._castleId);
                    ps.execute();
                } catch (Exception e) {
                    LOGGER.error("Couldn't upgrade castle doors.", e);
                }
            }

        }
    }

    public void loadDoorUpgrade() {
        try (
                Connection con = ConnectionPool.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT * FROM castle_doorupgrade WHERE castleId=?");
        ) {
            ps.setInt(1, this._castleId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    this.upgradeDoor(rs.getInt("doorId"), rs.getInt("hp"), false);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Couldn't load door upgrades.", e);
        }

    }

    public void removeDoorUpgrade() {
        for (Door door : this._doors) {
            door.getStat().setUpgradeHpRatio(1);
        }

        try (
                Connection con = ConnectionPool.getConnection();
                PreparedStatement ps = con.prepareStatement("DELETE FROM castle_doorupgrade WHERE castleId=?");
        ) {
            ps.setInt(1, this._castleId);
            ps.executeUpdate();
        } catch (Exception e) {
            LOGGER.error("Couldn't delete door upgrade.", e);
        }

    }

    private void updateOwnerInDB(Clan clan) {
        if (clan != null) {
            this._ownerId = clan.getClanId();
        } else {
            this._ownerId = 0;
            CastleManorManager.getInstance().resetManorData(this._castleId);
        }

        if (clan != null) {
            clan.setCastle(this._castleId);
            clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan), new PlaySound(1, "Siege_Victory"));
        }

        try (
                Connection con = ConnectionPool.getConnection();
                PreparedStatement ps = con.prepareStatement("UPDATE clan_data SET hasCastle=0 WHERE hasCastle=?");
                PreparedStatement ps2 = con.prepareStatement("UPDATE clan_data SET hasCastle=? WHERE clan_id=?");
        ) {
            ps.setInt(1, this._castleId);
            ps.executeUpdate();
            ps2.setInt(1, this._castleId);
            ps2.setInt(2, this._ownerId);
            ps2.executeUpdate();
        } catch (Exception e) {
            LOGGER.error("Couldn't update castle owner.", e);
        }

    }

    public int getCastleId() {
        return this._castleId;
    }

    public Door getDoor(int doorId) {
        for (Door door : this._doors) {
            if (door.getDoorId() == doorId) {
                return door;
            }
        }

        return null;
    }

    public List<Door> getDoors() {
        return this._doors;
    }

    public List<MercenaryTicket> getTickets() {
        return this._tickets;
    }

    public MercenaryTicket getTicket(int itemId) {
        return this._tickets.stream().filter((t) -> t.getItemId() == itemId).findFirst().orElse(null);
    }

    public Set<ItemInstance> getDroppedTickets() {
        return this._droppedTickets;
    }

    public void addDroppedTicket(ItemInstance item) {
        this._droppedTickets.add(item);
    }

    public void removeDroppedTicket(ItemInstance item) {
        this._droppedTickets.remove(item);
    }

    public int getDroppedTicketsCount(int itemId) {
        return (int) this._droppedTickets.stream().filter((t) -> t.getItemId() == itemId).count();
    }

    public boolean isTooCloseFromDroppedTicket(int x, int y, int z) {
        for (ItemInstance item : this._droppedTickets) {
            double dx = x - item.getX();
            double dy = y - item.getY();
            double dz = z - item.getZ();
            if (dx * dx + dy * dy + dz * dz < (double) 625.0F) {
                return true;
            }
        }

        return false;
    }

    public void spawnSiegeGuardsOrMercenaries() {
        if (this._ownerId > 0) {
            for (ItemInstance item : this._droppedTickets) {
                MercenaryTicket ticket = this.getTicket(item.getItemId());
                if (ticket != null) {
                    NpcTemplate template = NpcData.getInstance().getTemplate(ticket.getNpcId());
                    if (template != null) {
                        try {
                            L2Spawn spawn = new L2Spawn(template);
                            spawn.setLoc(item.getPosition());
                            spawn.setRespawnState(false);
                            this._siegeGuards.add(spawn.doSpawn(false));
                        } catch (Exception e) {
                            LOGGER.error("Couldn't spawn npc ticket {}. ", e, new Object[]{ticket.getNpcId()});
                        }

                        item.decayMe();
                    }
                }
            }

            this._droppedTickets.clear();
        }

    }

    public void despawnSiegeGuardsOrMercenaries() {
        if (this._ownerId > 0) {
            for (Npc npc : this._siegeGuards) {
                npc.doDie(npc);
            }

            this._siegeGuards.clear();
        }

    }

    public List<TowerSpawnLocation> getControlTowers() {
        return this._controlTowers;
    }

    public List<TowerSpawnLocation> getFlameTowers() {
        return this._flameTowers;
    }

    public void loadTrapUpgrade() {
        try (
                Connection con = ConnectionPool.getConnection();
                PreparedStatement ps = con.prepareStatement("SELECT * FROM castle_trapupgrade WHERE castleId=?");
        ) {
            ps.setInt(1, this._castleId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    this._flameTowers.get(rs.getInt("towerIndex")).setUpgradeLevel(rs.getInt("level"));
                }
            }
        } catch (Exception e) {
            LOGGER.error("Couldn't load traps.", e);
        }

    }

    public List<Integer> getRelatedNpcIds() {
        return this._relatedNpcIds;
    }

    public void setRelatedNpcIds(String idsToSplit) {
        for (String splittedId : idsToSplit.split(";")) {
            this._relatedNpcIds.add(Integer.parseInt(splittedId));
        }

    }

    public String getName() {
        return this._name;
    }

    public int getCircletId() {
        return this._circletId;
    }

    public void setCircletId(int circletId) {
        this._circletId = circletId;
    }

    public int getOwnerId() {
        return this._ownerId;
    }

    public void setOwnerId(int ownerId) {
        this._ownerId = ownerId;
    }

    public Siege getSiege() {
        return this._siege;
    }

    public void setSiege(Siege siege) {
        this._siege = siege;
    }

    public Calendar getSiegeDate() {
        return this._siegeDate;
    }

    public void setSiegeDate(Calendar siegeDate) {
        this._siegeDate = siegeDate;
    }

    public boolean isTimeRegistrationOver() {
        return this._isTimeRegistrationOver;
    }

    public void setTimeRegistrationOver(boolean val) {
        this._isTimeRegistrationOver = val;
    }

    public int getTaxPercent() {
        return this._taxPercent;
    }

    public double getTaxRate() {
        return this._taxRate;
    }

    public long getTreasury() {
        return this._treasury;
    }

    public void setTreasury(long treasury) {
        this._treasury = treasury;
    }

    public List<Integer> getArtifacts() {
        return this._artifacts;
    }

    public void setArtifacts(String idsToSplit) {
        for (String idToSplit : idsToSplit.split(";")) {
            this._artifacts.add(Integer.parseInt(idToSplit));
        }

    }

    public boolean isGoodArtifact(WorldObject object) {
        return object instanceof HolyThing && this._artifacts.contains(((HolyThing) object).getNpcId());
    }

    public int getTrapUpgradeLevel(int towerIndex) {
        TowerSpawnLocation spawn = this._flameTowers.get(towerIndex);
        return spawn != null ? spawn.getUpgradeLevel() : 0;
    }

    public void setTrapUpgrade(int towerIndex, int level, boolean save) {
        if (save) {
            try (
                    Connection con = ConnectionPool.getConnection();
                    PreparedStatement ps = con.prepareStatement("REPLACE INTO castle_trapupgrade (castleId, towerIndex, level) values (?,?,?)");
            ) {
                ps.setInt(1, this._castleId);
                ps.setInt(2, towerIndex);
                ps.setInt(3, level);
                ps.execute();
            } catch (Exception e) {
                LOGGER.error("Couldn't replace trap upgrade.", e);
            }
        }

        TowerSpawnLocation spawn = this._flameTowers.get(towerIndex);
        if (spawn != null) {
            spawn.setUpgradeLevel(level);
        }

    }

    public void removeTrapUpgrade() {
        for (TowerSpawnLocation ts : this._flameTowers) {
            ts.setUpgradeLevel(0);
        }

        try (
                Connection con = ConnectionPool.getConnection();
                PreparedStatement ps = con.prepareStatement("DELETE FROM castle_trapupgrade WHERE castleId=?");
        ) {
            ps.setInt(1, this._castleId);
            ps.executeUpdate();
        } catch (Exception e) {
            LOGGER.error("Couldn't delete trap upgrade.", e);
        }

    }

    public void checkItemsForMember(ClanMember member) {
        Player player = member.getPlayerInstance();
        if (player != null) {
            player.checkItemRestriction();
        } else {
            try (
                    Connection con = ConnectionPool.getConnection();
                    PreparedStatement ps = con.prepareStatement("UPDATE items SET loc='INVENTORY' WHERE item_id IN (?, 6841) AND owner_id=? AND loc='PAPERDOLL'");
            ) {
                ps.setInt(1, this._circletId);
                ps.setInt(2, member.getObjectId());
                ps.executeUpdate();
            } catch (Exception e) {
                LOGGER.error("Couldn't update items for member.", e);
            }
        }

    }

    public void checkItemsForClan(Clan clan) {
        try (
                Connection con = ConnectionPool.getConnection();
                PreparedStatement ps = con.prepareStatement("UPDATE items SET loc='INVENTORY' WHERE item_id IN (?, 6841) AND owner_id=? AND loc='PAPERDOLL'");
        ) {
            ps.setInt(1, this._circletId);

            for (ClanMember member : clan.getMembers()) {
                Player player = member.getPlayerInstance();
                if (player != null) {
                    player.checkItemRestriction();
                } else {
                    ps.setInt(2, member.getObjectId());
                    ps.addBatch();
                }
            }

            ps.executeBatch();
        } catch (Exception e) {
            LOGGER.error("Couldn't update items for clan.", e);
        }

    }
}
