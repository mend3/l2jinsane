package mods.instance;

import net.sf.l2j.gameserver.model.actor.instance.Door;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InstanceManager {
    private final Map<Integer, Instance> instances;

    protected InstanceManager() {
        this.instances = new ConcurrentHashMap<>();
        this.instances.put(Integer.valueOf(0), new Instance(0));
    }

    public static InstanceManager getInstance() {
        return SingletonHolder.instance;
    }

    public void addDoor(int id, Door door) {
        if (!this.instances.containsKey(Integer.valueOf(id)) || id == 0)
            return;
        this.instances.get(Integer.valueOf(id)).addDoor(door);
    }

    public void deleteInstance(int id) {
        if (id == 0) {
            System.out.println("Attempt to delete instance with id 0.");
        }
    }

    public synchronized Instance createInstance() {
        Instance instance = new Instance(InstanceIdFactory.getNextAvailable());
        this.instances.put(Integer.valueOf(instance.getId()), instance);
        return instance;
    }

    public Instance getInstance(int id) {
        return this.instances.get(Integer.valueOf(id));
    }

    private static final class SingletonHolder {
        private static final InstanceManager instance = new InstanceManager();
    }
}
