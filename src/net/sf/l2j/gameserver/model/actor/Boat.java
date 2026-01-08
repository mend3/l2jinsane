package net.sf.l2j.gameserver.model.actor;

import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.commons.pool.ThreadPool;
import net.sf.l2j.gameserver.data.xml.MapRegionData;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.model.actor.ai.type.BoatAI;
import net.sf.l2j.gameserver.model.actor.ai.type.CreatureAI;
import net.sf.l2j.gameserver.model.actor.stat.BoatStat;
import net.sf.l2j.gameserver.model.actor.template.CreatureTemplate;
import net.sf.l2j.gameserver.model.item.instance.ItemInstance;
import net.sf.l2j.gameserver.model.item.kind.Weapon;
import net.sf.l2j.gameserver.model.location.BoatLocation;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.location.SpawnLocation;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.*;
import net.sf.l2j.gameserver.taskmanager.MovementTaskManager;

import java.util.ArrayList;
import java.util.List;

public class Boat extends Creature {
    protected final List<Player> _passengers = new ArrayList<>();
    protected int _dockId;
    protected BoatLocation[] _currentPath;
    protected int _runState;
    private Runnable _engine;

    public Boat(int objectId, CreatureTemplate template) {
        super(objectId, template);
        this.setAI(new BoatAI(this));
    }

    public boolean isFlying() {
        return true;
    }

    public boolean canBeControlled() {
        return this._engine == null;
    }

    public void registerEngine(Runnable r) {
        this._engine = r;
    }

    public void runEngine(int delay) {
        if (this._engine != null) {
            ThreadPool.schedule(this._engine, delay);
        }

    }

    public void executePath(BoatLocation[] path) {
        this._runState = 0;
        this._currentPath = path;
        if (this._currentPath != null && this._currentPath.length > 0) {
            BoatLocation point = this._currentPath[0];
            if (point.getMoveSpeed() > 0) {
                this.getStat().setMoveSpeed(point.getMoveSpeed());
            }

            if (point.getRotationSpeed() > 0) {
                this.getStat().setRotationSpeed(point.getRotationSpeed());
            }

            this.getAI().setIntention(IntentionType.MOVE_TO, point);
        } else {
            this.getAI().setIntention(IntentionType.ACTIVE);
        }
    }

    public boolean moveToNextRoutePoint() {
        this._move = null;
        if (this._currentPath != null) {
            ++this._runState;
            if (this._runState < this._currentPath.length) {
                BoatLocation point = this._currentPath[this._runState];
                if (!this.isMovementDisabled()) {
                    if (point.getMoveSpeed() != 0) {
                        if (point.getMoveSpeed() > 0) {
                            this.getStat().setMoveSpeed(point.getMoveSpeed());
                        }

                        if (point.getRotationSpeed() > 0) {
                            this.getStat().setRotationSpeed(point.getRotationSpeed());
                        }

                        Creature.MoveData m = new Creature.MoveData();
                        m.disregardingGeodata = false;
                        m.onGeodataPathIndex = -1;
                        m._xDestination = point.getX();
                        m._yDestination = point.getY();
                        m._zDestination = point.getZ();
                        m._heading = 0;
                        double dx = point.getX() - this.getX();
                        double dy = point.getY() - this.getY();
                        double distance = Math.sqrt(dx * dx + dy * dy);
                        if (distance > (double) 1.0F) {
                            this.getPosition().setHeading(MathUtil.calculateHeadingFrom(this.getX(), this.getY(), point.getX(), point.getY()));
                        }

                        m._moveStartTime = System.currentTimeMillis();
                        this._move = m;
                        MovementTaskManager.getInstance().add(this);
                        this.broadcastPacket(new VehicleDeparture(this));
                        return true;
                    }

                    this.teleportTo(point, 0);
                    this._currentPath = null;
                }
            } else {
                this._currentPath = null;
            }
        }

        this.runEngine(10);
        return false;
    }

    public BoatStat getStat() {
        return (BoatStat) super.getStat();
    }

    public void initCharStat() {
        this.setStat(new BoatStat(this));
    }

    public boolean isInDock() {
        return this._dockId > 0;
    }

    public void setInDock(int d) {
        this._dockId = d;
    }

    public int getDockId() {
        return this._dockId;
    }

    public void oustPlayers() {
        for (Player player : this._passengers) {
            this.oustPlayer(player, false, Location.DUMMY_LOC);
        }

        this._passengers.clear();
    }

    public void oustPlayer(Player player, boolean removeFromList, Location location) {
        player.setBoat(null);
        if (removeFromList) {
            this.removePassenger(player);
        }

        player.setInsideZone(ZoneId.PEACE, false);
        player.sendPacket(SystemMessageId.EXIT_PEACEFUL_ZONE);
        Location loc = location.equals(Location.DUMMY_LOC) ? MapRegionData.getInstance().getLocationToTeleport(this, MapRegionData.TeleportType.TOWN) : location;
        if (player.isOnline()) {
            player.teleportTo(loc.getX(), loc.getY(), loc.getZ(), 0);
        } else {
            player.setXYZInvisible(loc);
        }

    }

    public void addPassenger(Player player) {
        if (player != null && !this._passengers.contains(player)) {
            if (player.getBoat() != null && player.getBoat() != this) {
            } else {
                this._passengers.add(player);
                player.setInsideZone(ZoneId.PEACE, true);
                player.sendPacket(SystemMessageId.ENTER_PEACEFUL_ZONE);
            }
        } else {
        }
    }

    public void removePassenger(Player player) {
        this._passengers.remove(player);
    }

    public boolean isEmpty() {
        return this._passengers.isEmpty();
    }

    public List<Player> getPassengers() {
        return this._passengers;
    }

    public void broadcastToPassengers(L2GameServerPacket sm) {
        for (Player player : this._passengers) {
            if (player != null) {
                player.sendPacket(sm);
            }
        }

    }

    public void payForRide(int itemId, int count, Location loc) {
        for (Player player : this.getKnownTypeInRadius(Player.class, 1000)) {
            if (player.isInBoat() && player.getBoat() == this) {
                if (itemId > 0) {
                    if (!player.destroyItemByItemId("Boat", itemId, count, this, false)) {
                        this.oustPlayer(player, true, loc);
                        player.sendPacket(SystemMessageId.NOT_CORRECT_BOAT_TICKET);
                        continue;
                    }

                    if (count > 1) {
                        player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(itemId).addItemNumber(count));
                    } else {
                        player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(itemId));
                    }
                }

                this.addPassenger(player);
            }
        }

    }

    public boolean updatePosition() {
        boolean result = super.updatePosition();

        for (Player player : this._passengers) {
            if (player != null && player.getBoat() == this) {
                player.setXYZ(this.getX(), this.getY(), this.getZ());
                player.revalidateZone(false);
            }
        }

        return result;
    }

    public void teleportTo(int x, int y, int z, int randomOffset) {
        if (this.isMoving()) {
            this.stopMove(null);
        }

        this.setIsTeleporting(true);
        this.getAI().setIntention(IntentionType.ACTIVE);

        for (Player player : this._passengers) {
            if (player != null) {
                player.teleportTo(x, y, z, randomOffset);
            }
        }

        this.decayMe();
        this.setXYZ(x, y, z);
        this.onTeleported();
        this.revalidateZone(true);
    }

    public void stopMove(SpawnLocation loc) {
        this._move = null;
        if (loc != null) {
            this.setXYZ(loc);
            this.revalidateZone(true);
        }

        this.broadcastPacket(new VehicleStarted(this, 0));
        this.broadcastPacket(new VehicleInfo(this));
    }

    public void deleteMe() {
        this._engine = null;
        if (this.isMoving()) {
            this.stopMove(null);
        }

        this.oustPlayers();
        this.decayMe();
        super.deleteMe();
    }

    public void updateAbnormalEffect() {
    }

    public ItemInstance getActiveWeaponInstance() {
        return null;
    }

    public Weapon getActiveWeaponItem() {
        return null;
    }

    public ItemInstance getSecondaryWeaponInstance() {
        return null;
    }

    public Weapon getSecondaryWeaponItem() {
        return null;
    }

    public int getLevel() {
        return 0;
    }

    public boolean isAutoAttackable(Creature attacker) {
        return false;
    }

    public void setAI(CreatureAI newAI) {
        if (this._ai == null) {
            this._ai = newAI;
        }

    }

    public void detachAI() {
    }

    public void sendInfo(Player activeChar) {
        activeChar.sendPacket(new VehicleInfo(this));
    }
}
