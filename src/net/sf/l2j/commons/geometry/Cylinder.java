package net.sf.l2j.commons.geometry;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.location.Location;

public class Cylinder extends Circle {
    private final int _minZ;

    private final int _maxZ;

    public Cylinder(int x, int y, int r, int minZ, int maxZ) {
        super(x, y, r);
        this._minZ = minZ;
        this._maxZ = maxZ;
    }

    public final double getArea() {
        return 6.283185307179586D * this._r * (this._r + this._maxZ - this._minZ);
    }

    public final double getVolume() {
        return Math.PI * this._r * this._r * (this._maxZ - this._minZ);
    }

    public final boolean isInside(int x, int y, int z) {
        if (z < this._minZ || z > this._maxZ)
            return false;
        int dx = x - this._x;
        int dy = y - this._y;
        return (dx * dx + dy * dy <= this._r * this._r);
    }

    public final Location getRandomLocation() {
        double distance = Math.sqrt(Rnd.nextDouble()) * this._r;
        double angle = Rnd.nextDouble() * Math.PI * 2.0D;
        return new Location((int) (distance * Math.cos(angle)), (int) (distance * Math.sin(angle)), Rnd.get(this._minZ, this._maxZ));
    }
}
