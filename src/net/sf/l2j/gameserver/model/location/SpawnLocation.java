package net.sf.l2j.gameserver.model.location;

public class SpawnLocation extends Location {
    public static final SpawnLocation DUMMY_SPAWNLOC = new SpawnLocation(0, 0, 0, 0);
    protected volatile int _heading;

    public SpawnLocation(int x, int y, int z, int heading) {
        super(x, y, z);
        this._heading = heading;
    }

    public SpawnLocation(SpawnLocation loc) {
        super(loc.getX(), loc.getY(), loc.getZ());
        this._heading = loc.getHeading();
    }

    public String toString() {
        return this._x + ", " + this._y + ", " + this._z + ", " + this._heading;
    }

    public int hashCode() {
        return this._x ^ this._y ^ this._z ^ this._heading;
    }

    public boolean equals(Object o) {
        if (!(o instanceof SpawnLocation loc)) {
            return false;
        } else {
            return loc.getX() == this._x && loc.getY() == this._y && loc.getZ() == this._z && loc.getHeading() == this._heading;
        }
    }

    public int getHeading() {
        return this._heading;
    }

    public void setHeading(int heading) {
        this._heading = heading;
    }

    public void set(int x, int y, int z, int heading) {
        super.set(x, y, z);
        this._heading = heading;
    }

    public void set(SpawnLocation loc) {
        super.set(loc.getX(), loc.getY(), loc.getZ());
        this._heading = loc.getHeading();
    }

    public void clean() {
        super.set(0, 0, 0);
        this._heading = 0;
    }
}
