package net.sf.l2j.commons.geometry;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.location.Location;

public class Rectangle extends AShape {
    protected final int _x;

    protected final int _y;

    protected final int _w;

    protected final int _h;

    public Rectangle(int x, int y, int w, int h) {
        this._x = x;
        this._y = y;
        this._w = w;
        this._h = h;
    }

    public final int getSize() {
        return this._w * this._h;
    }

    public double getArea() {
        return (this._w * this._h);
    }

    public double getVolume() {
        return 0.0D;
    }

    public boolean isInside(int x, int y) {
        int d = x - this._x;
        if (d < 0 || d > this._w)
            return false;
        d = y - this._y;
        return d >= 0 && d <= this._h;
    }

    public boolean isInside(int x, int y, int z) {
        int d = x - this._x;
        if (d < 0 || d > this._w)
            return false;
        d = y - this._y;
        return d >= 0 && d <= this._h;
    }

    public Location getRandomLocation() {
        return new Location(this._x + Rnd.get(this._w), this._y + Rnd.get(this._h), 0);
    }
}
