package net.sf.l2j.commons.geometry;

import net.sf.l2j.commons.random.Rnd;
import net.sf.l2j.gameserver.model.location.Location;

public class Triangle extends AShape {
    protected final int _Ax;

    protected final int _Ay;

    protected final int _BAx;

    protected final int _BAy;

    protected final int _CAx;

    protected final int _CAy;

    protected final int _size;

    public Triangle(int[] A, int[] B, int[] C) {
        this._Ax = A[0];
        this._Ay = A[1];
        this._BAx = B[0] - A[0];
        this._BAy = B[1] - A[1];
        this._CAx = C[0] - A[0];
        this._CAy = C[1] - A[1];
        this._size = Math.abs(this._BAx * this._CAy - this._CAx * this._BAy) / 2;
    }

    public final int getSize() {
        return this._size;
    }

    public double getArea() {
        return this._size;
    }

    public double getVolume() {
        return 0.0D;
    }

    public final boolean isInside(int x, int y) {
        long dx = (x - this._Ax);
        long dy = (y - this._Ay);
        boolean a = ((-dx) * (this._BAy) - (this._BAx) * (-dy) >= 0L);
        boolean b = ((this._BAx - dx) * (this._CAy - this._BAy) - (this._CAx - this._BAx) * (this._BAy - dy) >= 0L);
        boolean c = ((this._CAx - dx) * (-this._CAy) - (-this._CAx) * (this._CAy - dy) >= 0L);
        return (a == b && b == c);
    }

    public boolean isInside(int x, int y, int z) {
        long dx = (x - this._Ax);
        long dy = (y - this._Ay);
        boolean a = ((-dx) * (this._BAy) - (this._BAx) * (-dy) >= 0L);
        boolean b = ((this._BAx - dx) * (this._CAy - this._BAy) - (this._CAx - this._BAx) * (this._BAy - dy) >= 0L);
        boolean c = ((this._CAx - dx) * (-this._CAy) - (-this._CAx) * (this._CAy - dy) >= 0L);
        return (a == b && b == c);
    }

    public Location getRandomLocation() {
        double ba = Rnd.nextDouble();
        double ca = Rnd.nextDouble();
        if (ba + ca > 1.0D) {
            ba = 1.0D - ba;
            ca = 1.0D - ca;
        }
        int x = this._Ax + (int) (ba * this._BAx + ca * this._CAx);
        int y = this._Ay + (int) (ba * this._BAy + ca * this._CAy);
        return new Location(x, y, 0);
    }
}
