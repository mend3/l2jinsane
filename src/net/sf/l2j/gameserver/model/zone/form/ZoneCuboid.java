package net.sf.l2j.gameserver.model.zone.form;

import net.sf.l2j.gameserver.model.zone.ZoneForm;

public class ZoneCuboid extends ZoneForm {
    private int _x1;
    private int _x2;
    private int _y1;
    private int _y2;
    private int _z1;
    private int _z2;

    public ZoneCuboid(int x1, int x2, int y1, int y2, int z1, int z2) {
        this._x1 = x1;
        this._x2 = x2;
        if (this._x1 > this._x2) {
            this._x1 = x2;
            this._x2 = x1;
        }

        this._y1 = y1;
        this._y2 = y2;
        if (this._y1 > this._y2) {
            this._y1 = y2;
            this._y2 = y1;
        }

        this._z1 = z1;
        this._z2 = z2;
        if (this._z1 > this._z2) {
            this._z1 = z2;
            this._z2 = z1;
        }

    }

    public boolean isInsideZone(int x, int y, int z) {
        return x >= this._x1 && x <= this._x2 && y >= this._y1 && y <= this._y2 && z >= this._z1 && z <= this._z2;
    }

    public boolean intersectsRectangle(int ax1, int ax2, int ay1, int ay2) {
        if (this.isInsideZone(ax1, ay1, this._z2 - 1)) {
            return true;
        } else if (this.isInsideZone(ax1, ay2, this._z2 - 1)) {
            return true;
        } else if (this.isInsideZone(ax2, ay1, this._z2 - 1)) {
            return true;
        } else if (this.isInsideZone(ax2, ay2, this._z2 - 1)) {
            return true;
        } else if (this._x1 > ax1 && this._x1 < ax2 && this._y1 > ay1 && this._y1 < ay2) {
            return true;
        } else if (this._x1 > ax1 && this._x1 < ax2 && this._y2 > ay1 && this._y2 < ay2) {
            return true;
        } else if (this._x2 > ax1 && this._x2 < ax2 && this._y1 > ay1 && this._y1 < ay2) {
            return true;
        } else if (this._x2 > ax1 && this._x2 < ax2 && this._y2 > ay1 && this._y2 < ay2) {
            return true;
        } else if (this.lineSegmentsIntersect(this._x1, this._y1, this._x2, this._y1, ax1, ay1, ax1, ay2)) {
            return true;
        } else if (this.lineSegmentsIntersect(this._x1, this._y1, this._x2, this._y1, ax2, ay1, ax2, ay2)) {
            return true;
        } else if (this.lineSegmentsIntersect(this._x1, this._y2, this._x2, this._y2, ax1, ay1, ax1, ay2)) {
            return true;
        } else if (this.lineSegmentsIntersect(this._x1, this._y2, this._x2, this._y2, ax2, ay1, ax2, ay2)) {
            return true;
        } else if (this.lineSegmentsIntersect(this._x1, this._y1, this._x1, this._y2, ax1, ay1, ax2, ay1)) {
            return true;
        } else if (this.lineSegmentsIntersect(this._x1, this._y1, this._x1, this._y2, ax1, ay2, ax2, ay2)) {
            return true;
        } else if (this.lineSegmentsIntersect(this._x2, this._y1, this._x2, this._y2, ax1, ay1, ax2, ay1)) {
            return true;
        } else {
            return this.lineSegmentsIntersect(this._x2, this._y1, this._x2, this._y2, ax1, ay2, ax2, ay2);
        }
    }

    public double getDistanceToZone(int x, int y) {
        double shortestDist = Math.pow(this._x1 - x, 2.0F) + Math.pow(this._y1 - y, 2.0F);
        double test = Math.pow(this._x1 - x, 2.0F) + Math.pow(this._y2 - y, 2.0F);
        if (test < shortestDist) {
            shortestDist = test;
        }

        test = Math.pow(this._x2 - x, 2.0F) + Math.pow(this._y1 - y, 2.0F);
        if (test < shortestDist) {
            shortestDist = test;
        }

        test = Math.pow(this._x2 - x, 2.0F) + Math.pow(this._y2 - y, 2.0F);
        if (test < shortestDist) {
            shortestDist = test;
        }

        return Math.sqrt(shortestDist);
    }

    public int getLowZ() {
        return this._z1;
    }

    public int getHighZ() {
        return this._z2;
    }

    public void visualizeZone(int id, int z) {
        for (int x = this._x1; x < this._x2; x += 50) {
            dropDebugItem(id, x, this._y1, z);
            dropDebugItem(id, x, this._y2, z);
        }

        for (int y = this._y1; y < this._y2; y += 50) {
            dropDebugItem(id, this._x1, y, z);
            dropDebugItem(id, this._x2, y, z);
        }

    }
}
