/**/
package net.sf.l2j.gameserver.model.zone.form;

import net.sf.l2j.gameserver.model.zone.ZoneForm;

public class ZoneNPoly extends ZoneForm {
    private final int[] _x;
    private final int[] _y;
    private final int _z1;
    private final int _z2;

    public ZoneNPoly(int[] x, int[] y, int z1, int z2) {
        this._x = x;
        this._y = y;
        this._z1 = z1;
        this._z2 = z2;
    }

    public boolean isInsideZone(int x, int y, int z) {
        if (z >= this._z1 && z <= this._z2) {
            boolean inside = false;
            int i = 0;

            for (int j = this._x.length - 1; i < this._x.length; j = i++) {
                if ((this._y[i] <= y && y < this._y[j] || this._y[j] <= y && y < this._y[i]) && x < (this._x[j] - this._x[i]) * (y - this._y[i]) / (this._y[j] - this._y[i]) + this._x[i]) {
                    inside = !inside;
                }
            }

            return inside;
        } else {
            return false;
        }
    }

    public boolean intersectsRectangle(int ax1, int ax2, int ay1, int ay2) {
        if (this._x[0] > ax1 && this._x[0] < ax2 && this._y[0] > ay1 && this._y[0] < ay2) {
            return true;
        } else if (this.isInsideZone(ax1, ay1, this._z2 - 1)) {
            return true;
        } else {
            for (int i = 0; i < this._y.length; ++i) {
                int tX = this._x[i];
                int tY = this._y[i];
                int uX = this._x[(i + 1) % this._x.length];
                int uY = this._y[(i + 1) % this._x.length];
                if (this.lineSegmentsIntersect(tX, tY, uX, uY, ax1, ay1, ax1, ay2)) {
                    return true;
                }

                if (this.lineSegmentsIntersect(tX, tY, uX, uY, ax1, ay1, ax2, ay1)) {
                    return true;
                }

                if (this.lineSegmentsIntersect(tX, tY, uX, uY, ax2, ay2, ax1, ay2)) {
                    return true;
                }

                if (this.lineSegmentsIntersect(tX, tY, uX, uY, ax2, ay2, ax2, ay1)) {
                    return true;
                }
            }

            return false;
        }
    }

    public double getDistanceToZone(int x, int y) {
        double shortestDist = Math.pow(this._x[0] - x, 2.0F) + Math.pow(this._y[0] - y, 2.0F);

        for (int i = 1; i < this._y.length; ++i) {
            double test = Math.pow(this._x[i] - x, 2.0F) + Math.pow(this._y[i] - y, 2.0F);
            if (test < shortestDist) {
                shortestDist = test;
            }
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
        for (int i = 0; i < this._x.length; ++i) {
            int nextIndex = i + 1;
            if (nextIndex == this._x.length) {
                nextIndex = 0;
            }

            int vx = this._x[nextIndex] - this._x[i];
            int vy = this._y[nextIndex] - this._y[i];
            float lenght = (float) Math.sqrt(vx * vx + vy * vy);
            lenght /= 50.0F;

            for (int o = 1; (float) o <= lenght; ++o) {
                float k = (float) o / lenght;
                dropDebugItem(id, (int) ((float) this._x[i] + k * (float) vx), (int) ((float) this._y[i] + k * (float) vy), z);
            }
        }

    }
}
