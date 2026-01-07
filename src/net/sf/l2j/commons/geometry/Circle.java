package net.sf.l2j.commons.geometry;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.location.Location;

public class Circle extends AShape {
    protected final int _x;

    protected final int _y;

    protected final int _r;

    public Circle(int x, int y, int r) {
        this._x = x;
        this._y = y;
        this._r = r;
    }

    public final int getSize() {
        return 3 * this._r * this._r;
    }

    public double getArea() {
        return (3 * this._r * this._r);
    }

    public double getVolume() {
        return 0.0D;
    }

    public final boolean isInside(int x, int y) {
        int dx = x - this._x;
        int dy = y - this._y;
        return (dx * dx + dy * dy <= this._r * this._r);
    }

    public boolean isInside(int x, int y, int z) {
        int dx = x - this._x;
        int dy = y - this._y;
        return (dx * dx + dy * dy <= this._r * this._r);
    }

    public Location getRandomLocation() {
        double distance = Math.sqrt(Rnd.nextDouble()) * this._r;
        double angle = Rnd.nextDouble() * Math.PI * 2.0D;
        return new Location((int) (distance * Math.cos(angle)), (int) (distance * Math.sin(angle)), 0);
    }
}
