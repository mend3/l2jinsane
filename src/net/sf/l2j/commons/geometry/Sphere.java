package net.sf.l2j.commons.geometry;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.location.Location;

public class Sphere extends Circle {
    private final int _z;

    public Sphere(int x, int y, int z, int r) {
        super(x, y, r);
        this._z = z;
    }

    public final double getArea() {
        return 12.566370614359172D * this._r * this._r;
    }

    public final double getVolume() {
        return 12.566370614359172D * this._r * this._r * this._r / 3.0D;
    }

    public final boolean isInside(int x, int y, int z) {
        int dx = x - this._x;
        int dy = y - this._y;
        int dz = z - this._z;
        return (dx * dx + dy * dy + dz * dz <= this._r * this._r);
    }

    public final Location getRandomLocation() {
        double r = Math.cbrt(Rnd.nextDouble()) * this._r;
        double phi = Rnd.nextDouble() * 2.0D * Math.PI;
        double theta = Math.acos(2.0D * Rnd.nextDouble() - 1.0D);
        int x = (int) (this._x + r * Math.cos(phi) * Math.sin(theta));
        int y = (int) (this._y + r * Math.sin(phi) * Math.sin(theta));
        int z = (int) (this._z + r * Math.cos(theta));
        return new Location(x, y, z);
    }
}
