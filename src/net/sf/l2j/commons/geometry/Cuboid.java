package net.sf.l2j.commons.geometry;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.location.Location;

public class Cuboid extends Rectangle {
    private final int _minZ;

    private final int _maxZ;

    public Cuboid(int x, int y, int minZ, int maxZ, int w, int h) {
        super(x, y, w, h);
        this._minZ = minZ;
        this._maxZ = maxZ;
    }

    public final double getArea() {
        return (2 * (this._w * this._h + (this._w + this._h) * (this._maxZ - this._minZ)));
    }

    public final double getVolume() {
        return (this._w * this._h * (this._maxZ - this._minZ));
    }

    public boolean isInside(int x, int y, int z) {
        if (z < this._minZ || z > this._maxZ)
            return false;
        int d = x - this._x;
        if (d < 0 || d > this._w)
            return false;
        d = y - this._y;
        return d >= 0 && d <= this._h;
    }

    public Location getRandomLocation() {
        return new Location(this._x + Rnd.get(this._w), this._y + Rnd.get(this._h), Rnd.get(this._minZ, this._maxZ));
    }
}
