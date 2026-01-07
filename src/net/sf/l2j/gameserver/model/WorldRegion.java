package net.sf.l2j.gameserver.model;

import mods.instance.Instance;
import mods.instance.InstanceManager;
import net.sf.l2j.gameserver.enums.IntentionType;
import net.sf.l2j.gameserver.model.actor.Attackable;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.zone.ZoneType;
import net.sf.l2j.gameserver.model.zone.type.TownZone;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class WorldRegion {
    private final Map<Integer, WorldObject> _objects = new ConcurrentHashMap<>();

    private final List<WorldRegion> _surroundingRegions = new ArrayList<>();

    private final List<ZoneType> _zones = new ArrayList<>();

    private final int _tileX;

    private final int _tileY;

    private boolean _active;

    private final AtomicInteger _playersCount = new AtomicInteger();

    private final Instance _instance;

    public WorldRegion(int x, int y) {
        this._instance = InstanceManager.getInstance().getInstance(0);
        this._tileX = x;
        this._tileY = y;
    }

    public String toString() {
        return "WorldRegion " + this._tileX + "_" + this._tileY + ", _active=" + this._active + ", _playersCount=" + this._playersCount.get() + "]";
    }

    public Collection<WorldObject> getObjects() {
        return this._objects.values();
    }

    public void addSurroundingRegion(WorldRegion region) {
        this._surroundingRegions.add(region);
    }

    public List<WorldRegion> getSurroundingRegions() {
        return this._surroundingRegions;
    }

    public List<ZoneType> getZones() {
        return this._zones;
    }

    public void addZone(ZoneType zone) {
        this._zones.add(zone);
    }

    public void removeZone(ZoneType zone) {
        this._zones.remove(zone);
    }

    public void revalidateZones(Creature character) {
        if (character.isTeleporting())
            return;
        this._zones.forEach(z -> z.revalidateInZone(character));
    }

    public void removeFromZones(Creature character) {
        this._zones.forEach(z -> z.removeCharacter(character));
    }

    public boolean containsZone(int zoneId) {
        for (ZoneType z : this._zones) {
            if (z.getId() == zoneId)
                return true;
        }
        return false;
    }

    public boolean checkEffectRangeInsidePeaceZone(L2Skill skill, Location loc) {
        int range = skill.getEffectRange();
        int up = loc.getY() + range;
        int down = loc.getY() - range;
        int left = loc.getX() + range;
        int right = loc.getX() - range;
        for (ZoneType e : this._zones) {
            if ((e instanceof TownZone && ((TownZone) e).isPeaceZone()) || e instanceof net.sf.l2j.gameserver.model.zone.type.DerbyTrackZone || e instanceof net.sf.l2j.gameserver.model.zone.type.PeaceZone) {
                if (e.isInsideZone(loc.getX(), up, loc.getZ()))
                    return false;
                if (e.isInsideZone(loc.getX(), down, loc.getZ()))
                    return false;
                if (e.isInsideZone(left, loc.getY(), loc.getZ()))
                    return false;
                if (e.isInsideZone(right, loc.getY(), loc.getZ()))
                    return false;
                if (e.isInsideZone(loc.getX(), loc.getY(), loc.getZ()))
                    return false;
            }
        }
        return true;
    }

    public void onDeath(Creature character) {
        this._zones.stream().filter(z -> z.isCharacterInZone(character)).forEach(z -> z.onDieInside(character));
    }

    public void onRevive(Creature character) {
        this._zones.stream().filter(z -> z.isCharacterInZone(character)).forEach(z -> z.onReviveInside(character));
    }

    public boolean isActive() {
        return this._active;
    }

    public void setActive(boolean value) {
        if (this._active == value)
            return;
        this._active = value;
        if (!value) {
            for (WorldObject o : this._objects.values()) {
                if (o instanceof Attackable mob) {
                    mob.setTarget(null);
                    mob.stopMove(null);
                    mob.stopAllEffects();
                    mob.getAggroList().clear();
                    mob.getAttackByList().clear();
                    if (mob.hasAI()) {
                        mob.getAI().setIntention(IntentionType.IDLE);
                        mob.getAI().stopAITask();
                    }
                }
            }
        } else {
            for (WorldObject o : this._objects.values()) {
                if (o instanceof Attackable) {
                    ((Attackable) o).getStatus().startHpMpRegeneration();
                    continue;
                }
                if (o instanceof Npc)
                    ((Npc) o).startRandomAnimationTimer();
            }
        }
    }

    public int getPlayersCount() {
        return this._playersCount.get();
    }

    public boolean isEmptyNeighborhood() {
        for (WorldRegion neighbor : this._surroundingRegions) {
            if (neighbor.getPlayersCount() != 0)
                return false;
        }
        return true;
    }

    public void addVisibleObject(WorldObject object) {
        if (object == null)
            return;
        this._objects.put(Integer.valueOf(object.getObjectId()), object);
        if (object instanceof net.sf.l2j.gameserver.model.actor.Player)
            this._playersCount.incrementAndGet();
    }

    public void removeVisibleObject(WorldObject object) {
        if (object == null)
            return;
        this._objects.remove(Integer.valueOf(object.getObjectId()));
        if (object instanceof net.sf.l2j.gameserver.model.actor.Player)
            this._playersCount.decrementAndGet();
    }

    public Instance getInstance() {
        return this._instance;
    }
}
