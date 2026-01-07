package net.sf.l2j.gameserver.model.zone.form;

import net.sf.l2j.gameserver.model.zone.ZoneForm;

public class ZoneCylinder extends ZoneForm {
    private final int _x;

    private final int _y;

    private final int _z1;

    private final int _z2;

    private final int _rad;

    private final int _radS;

    public ZoneCylinder(int x, int y, int z1, int z2, int rad) {
        this._x = x;
        this._y = y;
        this._z1 = z1;
        this._z2 = z2;
        this._rad = rad;
        this._radS = rad * rad;
    }

    public boolean isInsideZone(int x, int y, int z) {
        return !(Math.pow((this._x - x), 2.0D) + Math.pow((this._y - y), 2.0D) > this._radS) && z >= this._z1 && z <= this._z2;
    }

    public boolean intersectsRectangle(int ax1, int ax2, int ay1, int ay2) {
        if (this._x > ax1 && this._x < ax2 && this._y > ay1 && this._y < ay2)
            return true;
        if (Math.pow((ax1 - this._x), 2.0D) + Math.pow((ay1 - this._y), 2.0D) < this._radS)
            return true;
        if (Math.pow((ax1 - this._x), 2.0D) + Math.pow((ay2 - this._y), 2.0D) < this._radS)
            return true;
        if (Math.pow((ax2 - this._x), 2.0D) + Math.pow((ay1 - this._y), 2.0D) < this._radS)
            return true;
        if (Math.pow((ax2 - this._x), 2.0D) + Math.pow((ay2 - this._y), 2.0D) < this._radS)
            return true;
        if (this._x > ax1 && this._x < ax2) {
            if (Math.abs(this._y - ay2) < this._rad)
                return true;
            if (Math.abs(this._y - ay1) < this._rad)
                return true;
        }
        if (this._y > ay1 && this._y < ay2) {
            if (Math.abs(this._x - ax2) < this._rad)
                return true;
            return Math.abs(this._x - ax1) < this._rad;
        }
        return false;
    }

    public double getDistanceToZone(int x, int y) {
        return Math.sqrt(Math.pow((this._x - x), 2.0D) + Math.pow((this._y - y), 2.0D)) - this._rad;
    }

    public int getLowZ() {
        return this._z1;
    }

    public int getHighZ() {
        return this._z2;
    }

    public void visualizeZone(int id, int z) {
        int count = (int) (6.283185307179586D * this._rad / 50.0D);
        double angle = 6.283185307179586D / count;
        for (int i = 0; i < count; i++) {
            int x = (int) (Math.cos(angle * i) * this._rad);
            int y = (int) (Math.sin(angle * i) * this._rad);
            dropDebugItem(id, this._x + x, this._y + y, z);
        }
    }
}
