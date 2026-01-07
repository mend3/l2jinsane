package net.sf.l2j.commons.geometry;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.location.Location;

public class Square extends AShape {
    protected final int _x;

    protected final int _y;

    protected final int _a;

    public Square(int x, int y, int a) {
        this._x = x;
        this._y = y;
        this._a = a;
    }

    public final int getSize() {
        return this._a * this._a;
    }

    public double getArea() {
        return (this._a * this._a);
    }

    public double getVolume() {
        return 0.0D;
    }

    public boolean isInside(int x, int y) {
        int d = x - this._x;
        if (d < 0 || d > this._a)
            return false;
        d = y - this._y;
        return d >= 0 && d <= this._a;
    }

    public boolean isInside(int x, int y, int z) {
        int d = x - this._x;
        if (d < 0 || d > this._a)
            return false;
        d = y - this._y;
        return d >= 0 && d <= this._a;
    }

    public Location getRandomLocation() {
        return new Location(this._x + Rnd.get(this._a), this._y + Rnd.get(this._a), 0);
    }
}
