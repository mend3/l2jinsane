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
        return !(Math.pow(this._x - x, 2.0F) + Math.pow(this._y - y, 2.0F) > (double) this._radS) && z >= this._z1 && z <= this._z2;
    }

    public boolean intersectsRectangle(int ax1, int ax2, int ay1, int ay2) {
        if (this._x > ax1 && this._x < ax2 && this._y > ay1 && this._y < ay2) {
            return true;
        } else if (Math.pow(ax1 - this._x, 2.0F) + Math.pow(ay1 - this._y, 2.0F) < (double) this._radS) {
            return true;
        } else if (Math.pow(ax1 - this._x, 2.0F) + Math.pow(ay2 - this._y, 2.0F) < (double) this._radS) {
            return true;
        } else if (Math.pow(ax2 - this._x, 2.0F) + Math.pow(ay1 - this._y, 2.0F) < (double) this._radS) {
            return true;
        } else if (Math.pow(ax2 - this._x, 2.0F) + Math.pow(ay2 - this._y, 2.0F) < (double) this._radS) {
            return true;
        } else {
            if (this._x > ax1 && this._x < ax2) {
                if (Math.abs(this._y - ay2) < this._rad) {
                    return true;
                }

                if (Math.abs(this._y - ay1) < this._rad) {
                    return true;
                }
            }

            if (this._y > ay1 && this._y < ay2) {
                if (Math.abs(this._x - ax2) < this._rad) {
                    return true;
                }

                if (Math.abs(this._x - ax1) < this._rad) {
                    return true;
                }
            }

            return false;
        }
    }

    public double getDistanceToZone(int x, int y) {
        return Math.sqrt(Math.pow(this._x - x, 2.0F) + Math.pow(this._y - y, 2.0F)) - (double) this._rad;
    }

    public int getLowZ() {
        return this._z1;
    }

    public int getHighZ() {
        return this._z2;
    }

    public void visualizeZone(int id, int z) {
        int count = (int) ((Math.PI * 2D) * (double) this._rad / (double) 50.0F);
        double angle = (Math.PI * 2D) / (double) count;

        for (int i = 0; i < count; ++i) {
            int x = (int) (Math.cos(angle * (double) i) * (double) this._rad);
            int y = (int) (Math.sin(angle * (double) i) * (double) this._rad);
            dropDebugItem(id, this._x + x, this._y + y, z);
        }

    }
}
