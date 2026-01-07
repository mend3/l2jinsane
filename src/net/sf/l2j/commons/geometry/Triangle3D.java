package net.sf.l2j.commons.geometry;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.location.Location;

public class Triangle3D extends Triangle {
    private final int _minZ;

    private final int _maxZ;

    private final double _length;

    public Triangle3D(int[] A, int[] B, int[] C) {
        super(A, B, C);
        this._minZ = Math.min(A[2], Math.min(B[2], C[2]));
        this._maxZ = Math.max(A[2], Math.max(B[2], C[2]));
        int CBx = this._CAx - this._BAx;
        int CBy = this._CAy - this._BAy;
        this._length = Math.sqrt((this._BAx * this._BAx + this._BAy * this._BAy)) + Math.sqrt((this._CAx * this._CAx + this._CAy * this._CAy)) + Math.sqrt((CBx * CBx + CBy * CBy));
    }

    public double getArea() {
        return (this._size * 2) + this._length * (this._maxZ - this._minZ);
    }

    public double getVolume() {
        return (this._size * (this._maxZ - this._minZ));
    }

    public final boolean isInside(int x, int y, int z) {
        if (z < this._minZ || z > this._maxZ)
            return false;
        return super.isInside(x, y, z);
    }

    public final Location getRandomLocation() {
        double ba = Rnd.nextDouble();
        double ca = Rnd.nextDouble();
        if (ba + ca > 1.0D) {
            ba = 1.0D - ba;
            ca = 1.0D - ca;
        }
        int x = this._Ax + (int) (ba * this._BAx + ca * this._CAx);
        int y = this._Ay + (int) (ba * this._BAy + ca * this._CAy);
        return new Location(x, y, Rnd.get(this._minZ, this._maxZ));
    }
}
