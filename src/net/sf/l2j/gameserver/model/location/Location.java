package net.sf.l2j.gameserver.model.location;

import net.sf.l2j.commons.util.StatSet;

public class Location {
    public static final Location DUMMY_LOC = new Location(0, 0, 0);
    protected volatile int _x;
    protected volatile int _y;
    protected volatile int _z;

    public Location(int x, int y, int z) {
        this._x = x;
        this._y = y;
        this._z = z;
    }

    public Location(Location loc) {
        this._x = loc.getX();
        this._y = loc.getY();
        this._z = loc.getZ();
    }

    public Location(StatSet loc) {
        this._x = loc.getInteger("x");
        this._y = loc.getInteger("y");
        this._z = loc.getInteger("z");
    }

    public String toString() {
        return this._x + ", " + this._y + ", " + this._z;
    }

    public int hashCode() {
        return this._x ^ this._y ^ this._z;
    }

    public boolean equals(Object o) {
        if (!(o instanceof Location loc)) {
            return false;
        } else {
            return loc.getX() == this._x && loc.getY() == this._y && loc.getZ() == this._z;
        }
    }

    public int getX() {
        return this._x;
    }

    public int getY() {
        return this._y;
    }

    public int getZ() {
        return this._z;
    }

    public void set(int x, int y, int z) {
        this._x = x;
        this._y = y;
        this._z = z;
    }

    public void set(Location loc) {
        this._x = loc.getX();
        this._y = loc.getY();
        this._z = loc.getZ();
    }

    public void clean() {
        this._x = 0;
        this._y = 0;
        this._z = 0;
    }
}
