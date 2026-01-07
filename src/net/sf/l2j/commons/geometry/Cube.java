package net.sf.l2j.commons.geometry;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.location.Location;

public class Cube extends Square {
    private final int _z;

    public Cube(int x, int y, int z, int a) {
        super(x, y, a);
        this._z = z;
    }

    public double getArea() {
        return (6 * this._a * this._a);
    }

    public double getVolume() {
        return (this._a * this._a * this._a);
    }

    public boolean isInside(int x, int y, int z) {
        int d = z - this._z;
        if (d < 0 || d > this._a)
            return false;
        d = x - this._x;
        if (d < 0 || d > this._a)
            return false;
        d = y - this._y;
        return d >= 0 && d <= this._a;
    }

    public Location getRandomLocation() {
        return new Location(this._x + Rnd.get(this._a), this._y + Rnd.get(this._a), this._z + Rnd.get(this._a));
    }
}
