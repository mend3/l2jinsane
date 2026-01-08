package net.sf.l2j.gameserver.model;

public class RadarMarker {
    public int _type;
    public int _x;
    public int _y;
    public int _z;

    public RadarMarker(int type, int x, int y, int z) {
        this._type = type;
        this._x = x;
        this._y = y;
        this._z = z;
    }

    public RadarMarker(int x, int y, int z) {
        this._type = 1;
        this._x = x;
        this._y = y;
        this._z = z;
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + this._type;
        result = 31 * result + this._x;
        result = 31 * result + this._y;
        result = 31 * result + this._z;
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof RadarMarker other)) {
            return false;
        } else {
            if (this._type != other._type) {
                return false;
            } else if (this._x != other._x) {
                return false;
            } else if (this._y != other._y) {
                return false;
            } else {
                return this._z == other._z;
            }
        }
    }
}
