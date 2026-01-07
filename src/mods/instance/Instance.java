package mods.instance;

import net.sf.l2j.gameserver.model.actor.instance.Door;

import java.util.ArrayList;
import java.util.List;

public class Instance {
    private final int id;

    private final List<Door> doors;

    public Instance(int id) {
        this.id = id;
        this.doors = new ArrayList<>();
    }

    public void openDoors() {
        for (Door door : this.doors)
            door.openMe();
    }

    public void closeDoors() {
        for (Door door : this.doors)
            door.closeMe();
    }

    public void addDoor(Door door) {
        this.doors.add(door);
    }

    public List<Door> getDoors() {
        return this.doors;
    }

    public int getId() {
        return this.id;
    }
}
