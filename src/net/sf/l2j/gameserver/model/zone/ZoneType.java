package net.sf.l2j.gameserver.model.zone;

import enginemods.main.EngineModsManager;
import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.gameserver.enums.ScriptEventType;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;
import net.sf.l2j.gameserver.scripting.Quest;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ZoneType {
    protected static final CLogger LOGGER = new CLogger(ZoneType.class.getName());
    protected final Map<Integer, Creature> _characters = new ConcurrentHashMap<>();
    private final int _id;
    private Map<ScriptEventType, List<Quest>> _questEvents;

    private ZoneForm _zone;

    protected ZoneType(int id) {
        this._id = id;
    }

    protected abstract void onEnter(Creature paramCreature);

    protected abstract void onExit(Creature paramCreature);

    public abstract void onDieInside(Creature paramCreature);

    public abstract void onReviveInside(Creature paramCreature);

    public String toString() {
        return getClass().getSimpleName() + "[" + getClass().getSimpleName() + "]";
    }

    public int getId() {
        return this._id;
    }

    public ZoneForm getZone() {
        return this._zone;
    }

    public void setZone(ZoneForm zone) {
        if (this._zone != null)
            throw new IllegalStateException("Zone already set");
        this._zone = zone;
    }

    public boolean isInsideZone(int x, int y) {
        return this._zone.isInsideZone(x, y, this._zone.getHighZ());
    }

    public boolean isInsideZone(int x, int y, int z) {
        return this._zone.isInsideZone(x, y, z);
    }

    public boolean isInsideZone(WorldObject object) {
        return isInsideZone(object.getX(), object.getY(), object.getZ());
    }

    public double getDistanceToZone(int x, int y) {
        return this._zone.getDistanceToZone(x, y);
    }

    public double getDistanceToZone(WorldObject object) {
        return this._zone.getDistanceToZone(object.getX(), object.getY());
    }

    public void visualizeZone(int z) {
        this._zone.visualizeZone(this._id, z);
    }

    public void revalidateInZone(Creature character) {
        if (!isAffected(character))
            return;
        if (isInsideZone(character)) {
            if (!this._characters.containsKey(Integer.valueOf(character.getObjectId()))) {
                List<Quest> quests = getQuestByEvent(ScriptEventType.ON_ENTER_ZONE);
                if (quests != null)
                    for (Quest quest : quests)
                        quest.notifyEnterZone(character, this);
                this._characters.put(Integer.valueOf(character.getObjectId()), character);
                onEnter(character);
                EngineModsManager.onEnterZone(character, this);
            }
        } else {
            removeCharacter(character);
        }
    }

    public void removeCharacter(Creature character) {
        if (this._characters.remove(Integer.valueOf(character.getObjectId())) != null) {
            List<Quest> quests = getQuestByEvent(ScriptEventType.ON_EXIT_ZONE);
            if (quests != null)
                for (Quest quest : quests)
                    quest.notifyExitZone(character, this);
            onExit(character);
            EngineModsManager.onExitZone(character, this);
        }
    }

    public boolean isCharacterInZone(Creature character) {
        return this._characters.containsKey(Integer.valueOf(character.getObjectId()));
    }

    public Collection<Creature> getCharacters() {
        return this._characters.values();
    }

    public final <A> List<A> getKnownTypeInside(Class<A> type) {
        if (this._characters.isEmpty())
            return Collections.emptyList();
        List<A> result = new ArrayList<>();
        for (WorldObject obj : this._characters.values()) {
            if (type.isAssignableFrom(obj.getClass()))
                result.add((A) obj);
        }
        return result;
    }

    public void addQuestEvent(ScriptEventType type, Quest quest) {
        if (this._questEvents == null)
            this._questEvents = new HashMap<>();
        List<Quest> eventList = this._questEvents.get(type);
        if (eventList == null) {
            eventList = new ArrayList<>();
            eventList.add(quest);
            this._questEvents.put(type, eventList);
        } else {
            eventList.remove(quest);
            eventList.add(quest);
        }
    }

    public List<Quest> getQuestByEvent(ScriptEventType type) {
        return (this._questEvents == null) ? null : this._questEvents.get(type);
    }

    public void broadcastPacket(L2GameServerPacket packet) {
        for (Creature character : this._characters.values()) {
            if (character instanceof Player)
                character.sendPacket(packet);
        }
    }

    public void setParameter(String name, String value) {
        LOGGER.warn("Unknown name/values couple {}, {} for {}.", name, value, toString());
    }

    protected boolean isAffected(Creature character) {
        return true;
    }

    public void movePlayersTo(int x, int y, int z) {
        for (Player player : getKnownTypeInside(Player.class)) {
            if (player.isOnline())
                player.teleportTo(x, y, z, 0);
        }
    }

    public void movePlayersTo(Location loc) {
        movePlayersTo(loc.getX(), loc.getY(), loc.getZ());
    }

    public void addKnownObject(WorldObject object) {
    }

    public void removeKnownObject(WorldObject object) {
    }
}
