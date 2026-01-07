package net.sf.l2j.gameserver.model;

import mods.instance.Instance;
import mods.instance.InstanceManager;
import net.sf.l2j.Config;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.math.MathUtil;
import net.sf.l2j.gameserver.data.ItemTable;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.enums.PolyType;
import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.enums.items.ShotType;
import net.sf.l2j.gameserver.idfactory.IdFactory;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.template.NpcTemplate;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.location.SpawnLocation;
import net.sf.l2j.gameserver.model.zone.ZoneType;
import net.sf.l2j.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2j.gameserver.taskmanager.DebugMovementTaskManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class WorldObject {
    public static final CLogger LOGGER = new CLogger(WorldObject.class.getName());
    private final SpawnLocation _position = new SpawnLocation(0, 0, 0, 0);
    private String _name;
    private int _objectId;
    private NpcTemplate _polyTemplate;
    private PolyType _polyType = PolyType.DEFAULT;
    private int _polyId;
    private WorldRegion _region;

    private boolean _isVisible;

    private Instance _instance = InstanceManager.getInstance().getInstance(0);

    public WorldObject(int objectId) {
        this._objectId = objectId;
    }

    public String toString() {
        return getClass().getSimpleName() + ":" + getClass().getSimpleName() + "[" + getName() + "]";
    }

    public void onAction(Player player) {
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }

    public void onActionShift(Player player) {
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }

    public void onForcedAttack(Player player) {
        player.sendPacket(ActionFailed.STATIC_PACKET);
    }

    public void onSpawn() {
    }

    public void decayMe() {
        setRegion(null);
        World.getInstance().removeObject(this);
    }

    public void refreshID() {
        World.getInstance().removeObject(this);
        IdFactory.getInstance().releaseId(getObjectId());
        this._objectId = IdFactory.getInstance().getNextId();
    }

    public final void spawnMe() {
        this._isVisible = true;
        setRegion(World.getInstance().getRegion(this._position));
        World.getInstance().addObject(this);
        onSpawn();
    }

    public final void spawnMe(Location loc) {
        spawnMe(loc.getX(), loc.getY(), loc.getZ());
    }

    public final void spawnMe(Location loc, int heading) {
        spawnMe(loc.getX(), loc.getY(), loc.getZ(), heading);
    }

    public final void spawnMe(SpawnLocation loc) {
        spawnMe(loc.getX(), loc.getY(), loc.getZ(), loc.getHeading());
    }

    public final void spawnMe(int x, int y, int z) {
        this._position.set(MathUtil.limit(x, -130972, 229276), MathUtil.limit(y, -262044, 262044), z);
        spawnMe();
    }

    public final void spawnMe(int x, int y, int z, int heading) {
        this._position.set(MathUtil.limit(x, -130972, 229276), MathUtil.limit(y, -262044, 262044), z, heading);
        spawnMe();
    }

    public boolean isAttackable() {
        return false;
    }

    public final boolean isVisible() {
        return (this._region != null && this._isVisible);
    }

    public final void setIsVisible(boolean value) {
        this._isVisible = value;
        if (!this._isVisible)
            setRegion(null);
    }

    public final String getName() {
        return this._name;
    }

    public void setName(String value) {
        this._name = value;
    }

    public final int getObjectId() {
        return this._objectId;
    }

    public final NpcTemplate getPolyTemplate() {
        return this._polyTemplate;
    }

    public final PolyType getPolyType() {
        return this._polyType;
    }

    public final int getPolyId() {
        return this._polyId;
    }

    public boolean polymorph(PolyType type, int id) {
        if (!(this instanceof net.sf.l2j.gameserver.model.actor.Npc) && !(this instanceof Player))
            return false;
        if (type == PolyType.NPC) {
            NpcTemplate template = NpcData.getInstance().getTemplate(id);
            if (template == null)
                return false;
            this._polyTemplate = template;
        } else if (type == PolyType.ITEM) {
            if (ItemTable.getInstance().getTemplate(id) == null)
                return false;
        } else if (type == PolyType.DEFAULT) {
            return false;
        }
        this._polyType = type;
        this._polyId = id;
        decayMe();
        spawnMe();
        return true;
    }

    public void unpolymorph() {
        this._polyTemplate = null;
        this._polyType = PolyType.DEFAULT;
        this._polyId = 0;
        decayMe();
        spawnMe();
    }

    public Player getActingPlayer() {
        return null;
    }

    public void sendInfo(Player player) {
    }

    public boolean isChargedShot(ShotType type) {
        return false;
    }

    public void setChargedShot(ShotType type, boolean charged) {
    }

    public void rechargeShots(boolean physical, boolean magical) {
    }

    public boolean isInsideZone(ZoneId zone) {
        return false;
    }

    public final void setXYZ(int x, int y, int z) {
        this._position.set(x, y, z);
        if (Config.DEBUG_MOVEMENT > 0)
            DebugMovementTaskManager.getInstance().addItem(this, x, y, z);
        if (!isVisible())
            return;
        WorldRegion region = World.getInstance().getRegion(this._position);
        if (region != this._region)
            setRegion(region);
    }

    public final void setXYZ(SpawnLocation loc) {
        this._position.set(loc);
        if (Config.DEBUG_MOVEMENT > 0)
            DebugMovementTaskManager.getInstance().addItem(this, loc.getX(), loc.getY(), loc.getZ());
        if (!isVisible())
            return;
        WorldRegion region = World.getInstance().getRegion(this._position);
        if (region != this._region)
            setRegion(region);
    }

    public final void setXYZInvisible(int x, int y, int z) {
        this._position.set(MathUtil.limit(x, -130972, 229276), MathUtil.limit(y, -262044, 262044), z);
        setIsVisible(false);
    }

    public final void setXYZInvisible(Location loc) {
        setXYZInvisible(loc.getX(), loc.getY(), loc.getZ());
    }

    public final int getX() {
        return this._position.getX();
    }

    public final int getY() {
        return this._position.getY();
    }

    public final int getZ() {
        return this._position.getZ();
    }

    public final int getHeading() {
        return this._position.getHeading();
    }

    public final SpawnLocation getPosition() {
        return this._position;
    }

    public final WorldRegion getRegion() {
        return this._region;
    }

    public void setRegion(WorldRegion newRegion) {
        List<WorldRegion> oldAreas = Collections.emptyList();
        if (this._region != null) {
            this._region.removeVisibleObject(this);
            oldAreas = this._region.getSurroundingRegions();
        }
        List<WorldRegion> newAreas = Collections.emptyList();
        if (newRegion != null) {
            newRegion.addVisibleObject(this);
            newAreas = newRegion.getSurroundingRegions();
        }
        for (WorldRegion region : oldAreas) {
            if (!newAreas.contains(region)) {
                for (ZoneType zone : region.getZones())
                    zone.removeKnownObject(this);
                for (WorldObject obj : region.getObjects()) {
                    if (obj == this)
                        continue;
                    obj.removeKnownObject(this);
                    removeKnownObject(obj);
                }
                if (this instanceof Player && region.isEmptyNeighborhood())
                    region.setActive(false);
            }
        }
        for (WorldRegion region : newAreas) {
            if (!oldAreas.contains(region)) {
                for (ZoneType zone : region.getZones())
                    zone.addKnownObject(this);
                for (WorldObject obj : region.getObjects()) {
                    if (obj == this)
                        continue;
                    if (getInstance().getId() != obj.getInstance().getId())
                        continue;
                    obj.addKnownObject(this);
                    addKnownObject(obj);
                }
                if (this instanceof Player)
                    region.setActive(true);
            }
        }
        this._region = newRegion;
        refreshKnownlist();
    }

    public void addKnownObject(WorldObject object) {
    }

    public void removeKnownObject(WorldObject object) {
    }

    public final <A> List<A> getKnownType(Class<A> type) {
        WorldRegion region = this._region;
        if (region == null)
            return Collections.emptyList();
        List<A> result = new ArrayList<>();
        for (WorldRegion reg : region.getSurroundingRegions()) {
            for (WorldObject obj : reg.getObjects()) {
                if (obj == this || !type.isAssignableFrom(obj.getClass()))
                    continue;
                if (getInstance().getId() != obj.getInstance().getId())
                    continue;
                result.add((A) obj);
            }
        }
        return result;
    }

    public final <A> List<A> getKnownTypeInRadius(Class<A> type, int radius) {
        WorldRegion region = this._region;
        if (region == null)
            return Collections.emptyList();
        List<A> result = new ArrayList<>();
        for (WorldRegion reg : region.getSurroundingRegions()) {
            for (WorldObject obj : reg.getObjects()) {
                if (obj == this || !type.isAssignableFrom(obj.getClass()) || !MathUtil.checkIfInRange(radius, this, obj, true))
                    continue;
                result.add((A) obj);
            }
        }
        return result;
    }

    public final void refreshKnownlist() {
        WorldRegion region = this._region;
        if (region == null)
            return;
        for (WorldRegion reg : region.getSurroundingRegions()) {
            for (WorldObject obj : reg.getObjects()) {
                if (obj == this || obj instanceof net.sf.l2j.gameserver.model.actor.instance.Door || obj instanceof net.sf.l2j.gameserver.model.actor.instance.Fence)
                    continue;
                if (getInstance().getId() != obj.getInstance().getId()) {
                    obj.removeKnownObject(this);
                    removeKnownObject(obj);
                }
            }
        }
    }

    public void setInstance(Instance instance, boolean silent) {
        this._instance = instance;
        if (!silent) {
            decayMe();
            spawnMe();
        }
    }

    public Instance getInstance() {
        return this._instance;
    }

    public boolean isSameInstance(WorldObject other) {
        return (getInstance().getId() == other.getInstance().getId());
    }

    public abstract boolean isAutoAttackable(Creature paramCreature);
}
